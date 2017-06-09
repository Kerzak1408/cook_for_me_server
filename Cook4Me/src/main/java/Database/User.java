package Database;

public class User {
	private String name;
	private String pass;
	private int id;
	private String nickname;
	
	public User (String name) {
		this.name = name;
	}
	
	public User(String name, String pass) {
		this.name = name;
		this.pass = pass;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
		
	}

	public void setId(int id) {
		this.id = id;
		
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
