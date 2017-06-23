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
	
	HashMap<String, User> users;
	// Key = login, Value = serialized CookingData (json)
	HashMap<String, String> cooks;
	HashMap<String, List<String>> registeredCustomers;
	HashMap<Date, String> finishCookingTimes;
	
	@Override
	public void run() {
		users = new HashMap<>();
		cooks = new HashMap<>();
		registeredCustomers = new HashMap<>();
		finishCookingTimes = new HashMap<>();
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
		for (String cook : cooks.values()) {
        	user.sendMessage(cook);
        }
	}
	
	public void addOrUpdateCook(String login, String serializedData) {
		String[] serializedDataArr = serializedData.split("#");
		
		registeredCustomers.put(login, new ArrayList<>());
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
		Ranking ranking = dbHandler.getRankingByLogin(login);
		data.setRanking(ranking.getRanking());
		String rankedData = "cook#" + gson.toJson(data);
		cooks.put(login, rankedData);
		broadcast(rankedData);
	}
	
	public void cancelCooking(String login) {
		System.out.println("CANCELING COOKING: " + login);
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

	
}
