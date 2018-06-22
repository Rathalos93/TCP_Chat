import java.util.HashMap;
import java.util.Map;

public class AuthentificationManager {
	public static Map<String, String> registeredUsers = new HashMap<>();
	public static void initUserbase()
	{
		registeredUsers.put("user1", "123");
		registeredUsers.put("user2", "123");
	}
}

