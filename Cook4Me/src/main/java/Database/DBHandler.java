package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import DataStructures.Ranking;
public class DBHandler{  
	
	private static DBHandler instance = null;
	private Connection connection;
	private java.sql.PreparedStatement insertUserStmnt;
	private java.sql.PreparedStatement selectUserStmnt;
	private java.sql.PreparedStatement createRankingTableStmnt;
	private java.sql.PreparedStatement selectIdOfUserStmnt;
	private java.sql.PreparedStatement setNickNameStmnt;
	private java.sql.PreparedStatement isNicknameAvailable;
	private java.sql.PreparedStatement selectFilteredNicknamesStmnt;
	private java.sql.PreparedStatement findIdStmnt;
	
	private DBHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection=DriverManager.getConnection(  
					"jdbc:mysql://127.0.0.1:3306/cookformedb","root","14OHI08slash95");  
			insertUserStmnt = connection.prepareStatement("insert into users (name, password) values (?,?)");
			selectUserStmnt = connection.prepareStatement("select * from users where name = ?");
			createRankingTableStmnt = connection.prepareStatement("create table ? (name VARCHAR(40), stars INT, comment VARCHAR(1000))");
			selectIdOfUserStmnt = connection.prepareStatement("select id from users where name = ?");
			setNickNameStmnt = connection.prepareStatement("update users set nickname = ? where name = ?");
			isNicknameAvailable = connection.prepareStatement("select * from users where nickname = ?");
			selectFilteredNicknamesStmnt = connection.prepareStatement("select nickname from users where nickname regexp ? limit 10");
			findIdStmnt = connection.prepareStatement("select id from users where nickname = ?");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		 
	}
	
	/**
	 * 
	 * @param user
	 * @return id of the user
	 */
	public int insertUser(User user) {
		int id = -1;
		try {
			insertUserStmnt.setString(1, user.getName());
			insertUserStmnt.setString(2, user.getPass());
			insertUserStmnt.execute();
			selectIdOfUserStmnt.setString(1, user.getName());
			ResultSet resultSet = selectIdOfUserStmnt.executeQuery();
			resultSet.next();
			id = resultSet.getInt(1);
			System.out.println("New user: NAME = " + user.getName() + " ID = " + id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}
	
	public boolean existsUser(String name, User outUser) {
		try {
			selectUserStmnt.setString(1, name);
			ResultSet result = selectUserStmnt.executeQuery();
			if (!result.next()) {
				return false;
			}
			String resultName = result.getString(1);
			String resultPass = result.getString(2);
			int id = result.getInt(3);
			String nickname = result.getString(4);
			outUser.setPass(resultPass);
			outUser.setId(id);
			outUser.setNickname(nickname);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void setUsersNickname(String name, String nickname) {
		try {
			setNickNameStmnt.setString(1, nickname);
			setNickNameStmnt.setString(2, name);
			setNickNameStmnt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isNicknameAvailable(String nickname) {
		try {
			isNicknameAvailable.setString(1, nickname);
			ResultSet resultSet = isNicknameAvailable.executeQuery();
			if (!resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void createRankingTable(String tableName) {
		try {
			java.sql.PreparedStatement stmnt = connection.prepareStatement("create table " + tableName +" (name VARCHAR(40), stars INT, comment VARCHAR(1000), primary key (name))");
			System.out.println(stmnt);
			stmnt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DBHandler getInstance() {
		if (instance == null) {
			instance = new DBHandler();
		}
		return instance;
	}

	public List<String> getFilteredNicknames(String pattern) {
		List<String> result = new ArrayList<>();
		try {
			selectFilteredNicknamesStmnt.setString(1, "^" + pattern);
			ResultSet resultSet = selectFilteredNicknamesStmnt.executeQuery();
			while (resultSet.next()) {
				result.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public Ranking getRanking(String name) {
		Ranking result = null;
		try {
			findIdStmnt.setString(1, name);
			ResultSet resultSet = findIdStmnt.executeQuery();
			resultSet.next();
			int id = resultSet.getInt(1);
			java.sql.PreparedStatement selectAllRankings = connection.prepareStatement("select * from ranking" + id);
			ResultSet allRankings = selectAllRankings.executeQuery();
			HashMap<String, String> comments = new HashMap<>();
			HashMap<String, Integer> rankings = new HashMap<>(); 
			float sum = 0;
			int counter = 0;
			while(allRankings.next()) {
				counter++;
				String whoCommented = allRankings.getString(1);
				int stars = allRankings.getInt(2);
				String comment = allRankings.getString(3);
				comments.put(whoCommented, comment);
				rankings.put(whoCommented, stars);
				sum += stars;
			}
			result = new Ranking(comments, rankings, sum/counter);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void InsertOrUpdateRanking(String table, String name, int stars, String comment) {
		String query = "INSERT INTO " + table + " VALUES ('" + name + "', " + stars + ", '" + comment + "') ON DUPLICATE KEY UPDATE stars = " + stars + ", comment = '" + comment + "'";
		System.out.println("RANKING UPDATE: " + query);
		try {
			java.sql.PreparedStatement stmnt = connection.prepareStatement(query);
			stmnt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int selectUsersId(String nickname) {
		try {
			findIdStmnt.setString(1, nickname);
			ResultSet result = findIdStmnt.executeQuery();
			result.next();
			return result.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
		
	}
	
	public String selectNicknameByMail(String mail) {
		try {
			selectUserStmnt.setString(1, mail);
			ResultSet resultSet = selectUserStmnt.executeQuery();
			resultSet.next();
			return resultSet.getString(4);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error";
	}
	
}