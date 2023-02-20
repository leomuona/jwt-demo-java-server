package maibb.server.database.model;

public class DbUser {

	public static final String TABLE = "users";

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String ACTIVE = "active";

	private String id;
	private String name;
	private String login;
	private String password;
	private boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
