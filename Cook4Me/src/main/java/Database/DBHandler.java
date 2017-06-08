package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.PreparedStatement;
public class DBHandler{  
	
	private static DBHandler instance = null;
	private Connection connection;
	private java.sql.PreparedStatement insertUserStmnt;
	private java.sql.PreparedStatement selectUserStmnt;
	private java.sql.PreparedStatement createRankingTableStmnt;
	
	private DBHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection=DriverManager.getConnection(  
					"jdbc:mysql://127.0.0.1:3306/cookformedb","root","14OHI08slash95");  
			insertUserStmnt = connection.prepareStatement("insert into users (name, password) values (?,?)");
			selectUserStmnt = connection.prepareStatement("select * from users where name = ?");
			createRankingTableStmnt = connection.prepareStatement("create table ? (name VARCHAR(40), stars INT, comment VARCHAR(1000))");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		 
	}
	
	public boolean insertUser(User user) {
		try {
			insertUserStmnt.setString(1, user.getName());
			insertUserStmnt.setString(2, user.getPass());
			insertUserStmnt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
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
			outUser.setPass(resultPass);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void createRankingTable(String tableName) {
		try {
			createRankingTableStmnt.setString(1, tableName);
			createRankingTableStmnt.execute();
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
	
}