import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

import javax.swing.JTextArea;

class ClientWorker implements Runnable {
	  private Socket client;
	  private JTextArea textArea;
	  private Long lastLoginAttempt;

	  ClientWorker(Socket client, JTextArea textArea) {
	  this.client = client;
	  this.textArea = textArea;
	  lastLoginAttempt = 0L;
	  }

	  public Socket getClient()
	  {
		  return client;
	  }

	  public boolean verifyUser()
	  {
		  String username = "!";
		  String password = "!";
		  BufferedReader in = null;
		  PrintWriter out = null;

		  while(!checkUserPassword(username, password))
		  {
			  try {
				  in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				  out = new PrintWriter(client.getOutputStream(), true);

				  String usernameAttempt = "";
				  String passwordAttempt = "";
				  out.println("USERNAME: ");
				  usernameAttempt = in.readLine();
				  out.println("PASSWORD: ");
				  passwordAttempt = in.readLine();

				  Long now = new Date().getTime();
				  if(lastLoginAttempt > now - 1000)
				  {
					  out.println("Too many login attempts!");
				  }
				  else
				  {
					  lastLoginAttempt = now;
					  username = usernameAttempt;
					  password = passwordAttempt;
				  }

			 } 
			 catch (IOException e) {
				 System.out.println("in or out failed");
				 System.exit(-1);
			 }
		  }

		  out.println("Successfully logged in!");
		  return true;
	  }

	  private boolean checkUserPassword(String user, String password)
	  {
		  for (Map.Entry<String, String> entry : AuthentificationManager.registeredUsers.entrySet())
		  {
			  if(user.equals(entry.getKey()) && password.equals(entry.getValue()))
			  {
				  return true;
			  }
		  }
		  return false;
	  }

	  public void run(){
		  String line;
		  BufferedReader in = null;
		  PrintWriter out = null;
		  try{
			  in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			  out = new PrintWriter(client.getOutputStream(), true);
		  } catch (IOException e) {
			  System.out.println("in or out failed");
			  System.exit(-1);
		  }

		  if(this.verifyUser())
		  {
			  MessageManager.registerClient(client);
			  while(true){
				  try{
					  line = in.readLine();
					  //Send data back to client
					  //out.println(line);
					  MessageManager.sendMessage(line);
					  textArea.append(line + "\n");
				  } 
				  catch (IOException e) {
					  System.out.println("Read failed");
					  System.exit(-1);
				  }
			  }
		  }
	  }
}