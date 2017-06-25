package SocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import DataStructures.CookingData;
import DataStructures.Ranking;
import Database.DBHandler;
import Serialization.GsonTon;

public class Server extends Thread{
	
	// Key = login, Value = User's thread
	HashMap<String, User> users;
	// Key = login, Value = serialized CookingData (json)
	HashMap<String, String> cooks;
	// Key = login, Value = List of registered customers.
	HashMap<String, List<String>> registeredCustomers;
	HashMap<Date, String> finishCookingTimes;
	HashMap<String, List<String>> usersRegistrations;
	
	@Override
	public void run() {
		users = new HashMap<>();
		cooks = new HashMap<>();
		registeredCustomers = new HashMap<>();
		finishCookingTimes = new HashMap<>();
		usersRegistrations = new HashMap<>();
		ServerStateChecker serverStateChecker = new ServerStateChecker(this);
		serverStateChecker.start();
		
		ServerSocket listener = null;
        try {
        	listener = new ServerSocket(6666);
            while (true) {
                Socket socket = listener.accept();
                System.out.println("NEW CONNECTION ON 6666");
                User user = new User(socket, this);
                user.start();
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
		if (!usersRegistrations.containsKey(user.getLogin())) {
			System.out.println("sendCooksToUser: NEW USER REG");
			usersRegistrations.put(user.getLogin(), new ArrayList<>());
		}
		for(String login : usersRegistrations.keySet()) {
			System.out.println("KEY USER REG == " + login);
		}
		System.out.println("USER LOGIN == " + user.getLogin());
		List<String> registrations = usersRegistrations.get(user.getLogin());
		System.out.println("(SEND) users' reg length: " + registrations.size());
		for (String cook : cooks.keySet()) {
			if (registrations.contains(cook)) {
				user.sendMessage(cooks.get(cook) + "#1");
			} else {
				user.sendMessage(cooks.get(cook) + "#0");
			}
        	
        }
		
	}
	
	public void addOrUpdateCook(String login, String serializedData) {
		String[] serializedDataArr = serializedData.split("#");
		if (!registeredCustomers.containsKey(login)) {
			registeredCustomers.put(login, new ArrayList<>());
		}
		User cook = users.get(login);
		Gson gson = GsonTon.getInstance().getGson();
		System.out.println("JSON to be parsed: " + serializedDataArr[1]);
		CookingData data = gson.fromJson(serializedDataArr[1], CookingData.class);
		Calendar calendar = Calendar.getInstance();
		calendar.set(data.getYearTo(), data.getMonthTo() - 1, data.getDayTo(),
				data.getHourTo(), data.getMinuteTo());
		Date dateTo = calendar.getTime();
		finishCookingTimes.put(dateTo, login);
		DBHandler dbHandler = DBHandler.getInstance();
		StringBuilder outNickname = new StringBuilder();
		Ranking ranking = dbHandler.getRankingByLogin(login, outNickname);
		data.setRanking(ranking.getRanking());
		data.setNickname(outNickname.toString());
		String rankedData = "cook#" + gson.toJson(data);
		cancelCooking(login);
		cooks.put(login, rankedData);
		broadcast(rankedData);
	}
	
	public void cancelCooking(String login) {
		System.out.println("CANCELING COOKING: " + login);
		for (List<String> registrations : usersRegistrations.values()) {
			registrations.remove(login);
		}
		cooks.remove(login);
		broadcast("remove#" + login);
	}
	
	public void broadcast(String line) {
		for (User user : users.values()) {
			user.sendMessage(line);
		}
	}

	public void registerCooking(String myLogin, String cook, User user) {
		List<String> cooksCustomers= registeredCustomers.get(cook);
		cooksCustomers.add(myLogin);
		registeredCustomers.put(myLogin, cooksCustomers);
		String cookingDataStr = cooks.get(cook).split("#")[1];
		Gson gson = GsonTon.getInstance().getGson();
		CookingData cookingData = gson.fromJson(cookingDataStr, CookingData.class);
		cookingData.decreaseAvailablePortions();
		String newCookingDataStr = gson.toJson(cookingData);
		cooks.put(cook, "cook#" + newCookingDataStr);
		broadcast("registered#" + cook + "#" + cooksCustomers.size());
		if (!usersRegistrations.containsKey(myLogin)) {
			System.out.println("registerCooking: NEW USER REG");
			usersRegistrations.put(myLogin, new ArrayList<>());
		}
		List<String> registration = usersRegistrations.get(myLogin);
		System.out.println("ADDING REG: " + cook);
		registration.add(cook);
	}

	public void addUser(User user) {
		String login = user.getLogin();
		users.put(login, user);
		if (cooks.containsKey(login)){
			int customersCount = registeredCustomers.get(login).size();
			String serializedData = cooks.get(login);
			user.sendMessage("youcook#" + customersCount + "#" + serializedData);
		} 
	}

	public void checkState() {
		Date now = new Date();
		System.out.println("NOW: " + now);
		for (Date date : finishCookingTimes.keySet()) {
			System.out.println("DATE: " + date);
			if (now.compareTo(date) > 0) {
				cancelCooking(finishCookingTimes.get(date));
				finishCookingTimes.remove(date);
				
			}
		}
		
	}

	public void logoutUser(User user) {
		users.remove(user.getLogin());
		System.out.println("User " + user.getLogin() + " logged out");
	}

	
}
