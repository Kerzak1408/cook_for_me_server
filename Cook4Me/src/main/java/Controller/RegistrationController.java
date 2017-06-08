package Controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Database.DBHandler;
import Database.User;
import Mail.MailMail;

@RestController
public class RegistrationController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    public static final String IP = "http://192.168.179.94:8090";
    
    @RequestMapping("/register")
    public String register (
    		@RequestParam(value="name") String name, 
    		@RequestParam(value="pass") String pass) {
        DBHandler dbHandler = DBHandler.getInstance();
        User dbUser = new User(name);
        if (dbHandler.existsUser(name, dbUser)) {
        	if (pass.equals(dbUser.getPass())) {
        		return "OK";
        	}
        	return "WRONGPASS";
        }
        // else go to the registration process
        ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Mail.xml");
        
       	MailMail mm = (MailMail) context.getBean("mailMail");
           mm.sendMail("cook4me.info@gmail.com",
        		   name,
       		   "Cook4Me Registration",
       		   "Welcome to Cook4Me! \n \n To complete your registration click on the following link: \n"+
       		   IP + "/registerconfirm?name=" + name + "&pass=" + pass +
       		   "\n \n Your name: " +
       		   name + "\n Your password: " + pass +
       		   "\n \n Enjoy your meal, \n Cook4Me team.");
        return "NEW";
    }
    
    @RequestMapping("/registerconfirm")
    public String registerConfirm(
    		@RequestParam(value="name") String name, 
    		@RequestParam(value="pass") String pass) {
    	System.out.println("New user: name=" + name + " pass= " + pass );
    	DBHandler dbHandler = DBHandler.getInstance();
    	return dbHandler.insertUser(new User(name, pass)) ? "Registration completed! :)" : "Error";
    }
}