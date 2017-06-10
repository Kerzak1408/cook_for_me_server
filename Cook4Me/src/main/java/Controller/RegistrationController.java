package Controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import DataStructures.Ranking;
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
        		if (dbUser.getNickname() != null) {
        			return "OK#" + dbUser.getNickname();
        		} else {
        			return "NICKNAME";
        		}
        		
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
    	int id = dbHandler.insertUser(new User(name, pass));
    	dbHandler.createRankingTable("ranking" + id);
    	return "Registration completed! :)";
    	
    }
    
    @RequestMapping("/setnickname")
    public String setNickname(
    		@RequestParam(value="name") String name,
    		@RequestParam(value="pass") String pass,
    		@RequestParam(value="nickname") String nickname) {
    	 DBHandler dbHandler = DBHandler.getInstance();
         User dbUser = new User(name);
         if (dbHandler.existsUser(name, dbUser)) {
         	if (pass.equals(dbUser.getPass())) {
         		if (dbUser.getNickname() == null) {
         			//check availability
         			if (dbHandler.isNicknameAvailable(nickname)) {
         				// set nickname
             			dbHandler.setUsersNickname(dbUser.getName(), nickname);
             			return "OK";
         			} else {
         				// nickname unavailable
         				return "UNIQUE";
         			}
         			
         		} else {
         			return "hasNickname#" + dbUser.getNickname();
         		}
         	} else {
         		// Hacking?
         		return "WRONGPASS";
         	}
         }
    	return "ERROR";
    }
    
    @RequestMapping("/searchusers")
    public String searchUsers(
    		@RequestParam(value="pattern") String pattern)  {
    	System.out.println("Search task with pattern: " + pattern);
    	DBHandler handler = DBHandler.getInstance();
    	List<String> list = handler.getFilteredNicknames(pattern);
    	Gson gson = new Gson();
    	String result = gson.toJson(list);
    	System.out.println("Search result: " + result);
    	return result;
    }
    
    @RequestMapping("/getuserranking")
    public String getUserRanking(
    		@RequestParam(value = "name") String name) {
    	System.out.println("RankingRequestfor name = " + name);
    	DBHandler handler = DBHandler.getInstance();
    	Ranking ranking = handler.getRanking(name);
    	Gson gson = new Gson();
    	String result = gson.toJson(ranking);
    	System.out.println("Sending ranking of " + name + ": " + gson);
    	return result;
    }
    
    @RequestMapping(value = "/updateuserranking", method = RequestMethod.POST)
    public String updateUserRanking(HttpServletRequest request,
    		@RequestParam(value = "name") String name,
    		@RequestParam(value = "pass") String pass,
    		@RequestParam(value = "cook") String cook,    		
    		@RequestParam(value = "stars") int stars) {
    	String comment = request.getParameter("comment");
    	System.out.println("BODY: " + comment);
    	DBHandler dbHandler = DBHandler.getInstance();
    	User dbUser = new User(name);
    	if (dbHandler.existsUser(name, dbUser)) {
          	if (pass.equals(dbUser.getPass())) {
          		// User authenticated, proceed to the update
          		int id = dbHandler.selectUsersId(cook);
          		String nickname = dbHandler.selectNicknameByMail(name);
          		dbHandler.InsertOrUpdateRanking("ranking" + id , nickname, stars, comment);
          		return "UPDATED";
          	}
      	}
		return "ERROR";
	}
}