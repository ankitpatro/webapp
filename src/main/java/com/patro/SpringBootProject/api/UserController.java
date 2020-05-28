package com.patro.SpringBootProject.api;

import com.patro.SpringBootProject.model.User;
import com.patro.SpringBootProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
public class UserController extends ResponseEntityExceptionHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController() {

    }

    //User userObj;

    @RequestMapping(value = {"/signup"}, method = RequestMethod.GET)
    public ModelAndView getSignupPage() {
        ModelAndView mv = new ModelAndView();
        User user = new User();
        mv.addObject("user", user);
        mv.setViewName("signup");
        return mv;
    }

    @RequestMapping(value = {"/signup"}, method = RequestMethod.POST)
    public ModelAndView createUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("signup");
        try {

            if (isUserAlreadyRegistered(user.getUsername())) {
                mv.addObject("alreadyRegistered", "User Account Already Exists!");
                return mv;
            }

            if (!isValidEmail(user.getUsername())) {
                mv.addObject("invalidEmail", "Invalid Email Address");
                return mv;
            }

            if (!isPasswordStrong(user.getPassword())) {
                mv.addObject("weakPassword", "Password too weak!");
                return mv;
            } else {
                userService.addUser(user);

                mv.addObject(user);
                mv.addObject("msg", "User has been registered successfully!");

                System.out.println(user.getFirstName());
                return mv;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public ModelAndView getLoginPage() {
        ModelAndView model = new ModelAndView();
        User user = new User();
        model.addObject("user", user);
        model.setViewName("login");
        return model;
    }

    @RequestMapping(value = {"/login"}, method = RequestMethod.POST)
    public ModelAndView validateUser(@Valid User user, BindingResult bindingResult, HttpSession session) {
        ModelAndView model = new ModelAndView();
        System.out.println(user.getUsername() + "  " + user.getPassword());
        model.setViewName("login");
        Optional<User> backendUserOptional = userService.getUserByUsername(user.getUsername());
        if (!backendUserOptional.isPresent()) {
            model.addObject("noUser", "No user found");
            return model;
        }

        System.out.println("Login Password: " + user.getPassword() + " Encoded Password: " + backendUserOptional.get().getPassword());
        if (BCrypt.checkpw(user.getPassword(), backendUserOptional.get().getPassword())) {
            System.out.println("Password Matched");
            ModelAndView mv = new ModelAndView();
            mv.setViewName("userdetails");
            User backendUser = backendUserOptional.get();
            //userObj = backendUser;
            mv.addObject("user", backendUser);
            session.setAttribute("userSession", backendUser);
            System.out.println("User Details: " + backendUser.getUsername() + " " + backendUser.getFirstName() + " " + backendUser.getLastName());
            mv.addObject("loginsuccessful", "User Logged In Successfully!");
            return mv;
        } else {
            model.addObject("incorrectPassword", "Incorrect Password");
            return model;
        }

    }

    @RequestMapping(value = {"/userDetails"}, method = RequestMethod.GET)
    public ModelAndView getUserDetails(@Valid User user, HttpSession session)
    {
        ModelAndView mv = new ModelAndView();
        User userSessionObj = (User) session.getAttribute("userSession");
        if(userSessionObj == null)
        {
            mv.setViewName("accessdenied");
            return mv;
        }
        mv.setViewName("userdetails");
        User newUser = userService.getUserByUsername(userSessionObj.getUsername()).get();
        mv.addObject("user", newUser);
        return mv;
    }

    @RequestMapping(value = {"/update"}, method = RequestMethod.GET)
    public ModelAndView getUpdatePageByGetCall(HttpSession session) {
        User sessionObj = (User) session.getAttribute("userSession");
        ModelAndView modelAndView = new ModelAndView();
        if (sessionObj == null) {
            modelAndView.setViewName("accessdenied");
            return modelAndView;
        }

        User newUser = userService.getUserByUsername(sessionObj.getUsername()).get();
        modelAndView.addObject("user", newUser);
        modelAndView.setViewName("update");
//        modelAndView.addObject("user", user);
       // modelAndView.addObject("user", sessionObj);


        System.out.println("Inside getUpdatePage method" + sessionObj);
//        System.out.println("Inside getUpdatePage method" + userObj.getFirstName() + " " + userObj.getLastName() + " " + userObj.getPassword());
        return modelAndView;
    }

    @RequestMapping(value = {"/update"}, method = RequestMethod.POST)
    public ModelAndView getUpdatePage(HttpSession session) {
        User sessionObj = (User) session.getAttribute("userSession");
        ModelAndView modelAndView = new ModelAndView();
        if (sessionObj == null) {
            modelAndView.setViewName("accessdenied");
            return modelAndView;
        }

        modelAndView.setViewName("update");
//        modelAndView.addObject("user", user);
        modelAndView.addObject("user", sessionObj);


        System.out.println("Inside getUpdatePage method" + sessionObj);
//        System.out.println("Inside getUpdatePage method" + userObj.getFirstName() + " " + userObj.getLastName() + " " + userObj.getPassword());
        return modelAndView;
    }

    @RequestMapping(value = {"/saveupdate"}, method = RequestMethod.GET)
    public ModelAndView getUpdates(@Valid User user, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        User sessionObj = (User) session.getAttribute("userSession");
        if (sessionObj == null) {
            modelAndView.setViewName("accessdenied");
            return modelAndView;
        }
        modelAndView.addObject("user", sessionObj);
        modelAndView.setViewName("update");
        return modelAndView;
    }

    @RequestMapping(value = {"/saveupdate"}, method = RequestMethod.POST)
    public ModelAndView saveUpdates(@Valid User user, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        User sessionObj = (User) session.getAttribute("userSession");
        if (sessionObj == null) {
            modelAndView.setViewName("accessdenied");
            return modelAndView;
        }
        modelAndView.setViewName("update");
        modelAndView.addObject("user", user);
        if (!isPasswordStrong(user.getPassword())) {
            modelAndView.addObject("weakPassword", "Password too weak!");
            return modelAndView;
        }


        user.setUsername(sessionObj.getUsername());
        System.out.println("Updated Details " + user.getUsername() + " " + user.getFirstName() + " " + user.getLastName() + " " + user.getPassword());
        //  user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userService.updateUserDetails(user);
        modelAndView.addObject("succesfulupdate", "User Details Updated Successfully");
        return modelAndView;
    }

    @RequestMapping(value = {"/logout"}, method = RequestMethod.GET)
    public ModelAndView logout(HttpSession session) {
        User u = (User) session.getAttribute("userSession");
        System.out.println("SESSION OBJECT BEFORE invalidating: " + u);
        session.invalidate();
//        User u2 = (User) session.getAttribute("userSession");
//        System.out.println("SESSION OBJECT AFTER invalidating: "+ u);
        ModelAndView mv = new ModelAndView();
        User user = new User();
        mv.addObject("user", user);

        mv.setViewName("login");
        return mv;
    }

    public boolean isPasswordStrong(String password) {
        String regularExpression = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(regularExpression);
    }

    private boolean isUserAlreadyRegistered(String username) {
        Optional<User> existingUser = userService.getUserByUsername(username);
        return existingUser.isPresent();

    }

    public boolean isValidEmail(String emailStr) {
        Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}
