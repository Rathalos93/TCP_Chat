import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MessageManager {
	private static ArrayList<Socket> connectedClients = new ArrayList<>();

	public synchronized static void sendMessage(String message)
	{
		for(Socket client : connectedClients)
		{
			try
			{
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.println(message);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.out.println("Error occurred, when broadcasting");
			}
		}
	}

	public synchronized static void registerClient(Socket client)
	{
		connectedClients.add(client);
	}

	public synchronized static void removeClient(Socket client)
	{
		connectedClients.remove(client);
	}
}