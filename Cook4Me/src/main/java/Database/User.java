package Database;

public class User {
	private String name;
	private String pass;
	
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
}
