package SocketServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import com.google.gson.Gson;

import DataStructures.CookingData;
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
			server.sendCooksToUser(this);
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
					server.registerCooking(myLogin, lineArr[1]);
				} else if ("login".equals(lineArr[0])) {
					myLogin = lineArr[1];
					System.out.println("LOGGED (thread) USER: " + myLogin);
					server.addUser(this);
				} else if ("logout".equals(lineArr[0])) {
					server.logoutUser(this);
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
