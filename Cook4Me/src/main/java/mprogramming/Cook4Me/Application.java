package mprogramming.Cook4Me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import SocketServer.Server;

@SpringBootApplication @ComponentScan({"Controller", "WebSockets"})	
public class Application {

    public static void main(String[] args) {
    	Server server = new Server();
    	server.start();
        SpringApplication.run(Application.class, args);
    }
}