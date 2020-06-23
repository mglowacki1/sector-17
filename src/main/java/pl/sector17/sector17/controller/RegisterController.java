package pl.sector17.sector17.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import pl.sector17.sector17.model.User;
import pl.sector17.sector17.repository.UserRepository;
import pl.sector17.sector17.service.UserDetailsServiceImp;

import javax.validation.Valid;
import java.util.Date;
import java.util.Properties;


@Controller
public class RegisterController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDetailsServiceImp userDetailsService;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView register() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("user", user);
            modelAndView.setViewName("register");
        } else {
            userDetailsService.saveUser(user);
            modelAndView.setViewName("login");
        }
        return modelAndView;
    }

}
