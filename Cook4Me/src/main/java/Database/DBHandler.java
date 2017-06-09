package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
			java.sql.PreparedStatement stmnt = connection.prepareStatement("create table " + tableName +" (name VARCHAR(40), stars INT, comment VARCHAR(1000))");
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
	
}