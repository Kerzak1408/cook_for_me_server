package SocketServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import DataStructures.CookingData;
import DataStructures.Ranking;
import Database.DBHandler;
import Serialization.GsonTon;

public class User extends Thread {
	Socket socket;
	Server server;
	PrintWriter writer;
	private String myLogin;
	
	public User(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println("RECIEVED: " + line);

				String[] lineArr = line.split("#");
				if ("cook".equals(lineArr[0])) {
					Gson  gson = GsonTon.getInstance().getGson();
					CookingData data = gson.fromJson(lineArr[1], CookingData.class);
					myLogin = data.getLogin();
					server.addOrUpdateCook(myLogin, line);
					
				} else if ("refresh".equals(lineArr[0])) {
					server.sendCooksToUser(this);
				} else if ("cancelCooking".equals(lineArr[0])) {
					server.cancelCooking(myLogin);
				} else if ("register".equals(lineArr[0])) {
					server.registerCooking(myLogin, lineArr[1], this);
				} else if ("login".equals(lineArr[0])) {
					myLogin = lineArr[1];
					server.sendCooksToUser(this);
					System.out.println("LOGGED (thread) USER: " + myLogin);
					server.addUser(this);
				} else if ("logout".equals(lineArr[0])) {
					server.logoutUser(this);
				} else if ("search".equals(lineArr[0])) {
					System.out.println("Search task with pattern: " + lineArr[1]);
			    	DBHandler handler = DBHandler.getInstance();
			    	List<String> list = handler.getFilteredNicknames(lineArr[1]);
			    	Gson gson = new Gson();
			    	String result = gson.toJson(list);
			    	System.out.println("Search result: " + result);
			    	sendMessage("searchResults#" + result);
				} else if ("getRanking".equals(lineArr[0])) {
			    	System.out.println("RankingRequestfor name = " + lineArr[1]);
			    	DBHandler handler = DBHandler.getInstance();
			    	Ranking ranking = handler.getRanking(lineArr[1]);
			    	Gson gson = new Gson();
			    	String result = gson.toJson(ranking);
			    	System.out.println("Sending ranking of " + lineArr[1] + ": " + gson);
			    	sendMessage("ranking#" + result);
				}
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(String line) {
		writer.println(line);
		writer.flush();
		System.out.println("SENT:" + line);
	}

	public String getLogin() {
		// TODO Auto-generated method stub
		return myLogin;
	}
	
}
