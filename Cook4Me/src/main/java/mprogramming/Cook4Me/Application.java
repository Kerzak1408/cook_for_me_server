package mprogramming.Cook4Me;

import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.google.gson.Gson;

import DataStructures.CookingData;
import Serialization.GsonTon;
import SocketServer.Server;

@SpringBootApplication @ComponentScan({"Controller", "WebSockets"})	
public class Application {

    public static void main(String[] args) {
    	test();
    	Server server = new Server();
    	server.start();
        SpringApplication.run(Application.class, args);
    }
    
    // only for testing purposes, will be deleted in release
    private static void test() {
    	CookingData data = new CookingData("aaa", "meno", new ArrayList<String>(), 5, 5, 2014, 22, 22, 25, 5, 2018, 22, 42, 2, true, 4, "jj", "CZK");
    	Gson gson = GsonTon.getInstance().getGson();
    	String serializedData = gson.toJson(data);
    	System.out.println("TEST: serializedData = " + serializedData);
    }
}