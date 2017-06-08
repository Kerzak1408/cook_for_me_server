package SocketServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Server extends Thread{
	
	HashMap<String, User> users;
	// Key = login, Value = serialized CookingData (json)
	HashMap<String, String> cooks;
	HashMap<String, List<String>> registeredCustomers;
	
	@Override
	public void run() {
		users = new HashMap<>();
		cooks = new HashMap<>();
		registeredCustomers = new HashMap<>();
		
		ServerSocket listener = null;
        try {
        	listener = new ServerSocket(6666);
            while (true) {
                Socket socket = listener.accept();
                System.out.println("NEW CONNECTION ON 6666");
                User user = new User(socket, this);
                user.start();
                // Send all the cooking data to the user.
                
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
        	try {
				listener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
	
	public void sendCooksToUser(User user) {
		System.out.println("Sending " + cooks.values().size() + " cooks to " + user.getName());
		for (String cook : cooks.values()) {
        	user.sendMessage(cook);
        }
	}
	
	public void addOrUpdateCook(String login, String serializedData) {
		cooks.put(login, serializedData);
		registeredCustomers.put(login, new ArrayList<>());
	}
	
	public void cancelCooking(String login) {
//		if (login == null) {
//			return;
//		}
		cooks.remove(login);
		broadcast("remove#" + login);
	}
	
	public void broadcast(String line) {
		for (User user : users.values()) {
			user.sendMessage(line);
		}
		
	}

	public void registerCooking(String myLogin, String cook) {
		List cooksCustomers= registeredCustomers.get(cook);
		cooksCustomers.add(myLogin);
		registeredCustomers.put(myLogin, cooksCustomers);
		broadcast("registered#" + cook + "#" + cooksCustomers.size());
	}

	public void addUser(User user) {
		users.put(user.getLogin(), user);
		
	}

	
}
