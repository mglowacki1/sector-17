package pl.sector17.sector17.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import pl.sector17.sector17.model.User;
import pl.sector17.sector17.model.VerificationToken;
import pl.sector17.sector17.repository.UserRepository;
import pl.sector17.sector17.service.UserDetailsServiceImp;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;


@Controller
public class RegisterController {
    @Value("${application.domain.name}")
    String applicationDomainName;
    @Value("${application.domain.port}")
    String applicationDomainPort;
    @Value("${application.mail.email}")
    String applicationMailEmail;
    @Value("${application.mail.password}")
    String applicationMailPassword;
    @Value("${application.mail.host}")
    String applicationMailHost;
    @Value("${application.mail.port}")
    String applicationMailPort;
    @Value("${application.mail.auth}")
    String applicationMailAuth;
    @Value("${application.mail.ttls}")
    String getApplicationMailTtls;


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
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) throws MessagingException {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("user", user);
            modelAndView.setViewName("register");
        } else {
            userDetailsService.saveUser(user);
            VerificationToken token = new VerificationToken();
            token.setUser(user);
            token.setToken(UUID.randomUUID().toString());
            userDetailsService.saveToken(token);


            Properties properties = System.getProperties();
            properties.put("mail.smtp.port", applicationMailPort);
            properties.put("mail.smtp.auth", applicationMailAuth);
            properties.put("mail.smtp.starttls.enable", getApplicationMailTtls);

            Session session = Session.getDefaultInstance(properties, null);

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setSubject("Confirm Your Sector 17 account");
            String emailBody= "<p>Do not reply to this email!<p>" +
                    "<p>To confirm your account please click the link below:<p></br>" +
                    "<a href=\"http://"+ applicationDomainName+":"+applicationDomainPort+"/confirm?token="
                    +token.getToken()+"\">Account Confirmation Link</a>\n";

            mimeMessage.setContent(emailBody,"text/html");
            Transport transport = session.getTransport("smtp");
            transport.connect(applicationMailHost,applicationMailEmail,applicationMailPassword);
            transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
            transport.close();
            modelAndView.setViewName("login");

        }
        return modelAndView;
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public ModelAndView confirm(@RequestParam String token) {

        userDetailsService.confirmUser(token);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("confirmed");
        return modelAndView;
    }
}
