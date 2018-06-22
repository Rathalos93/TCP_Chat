//package javaLanguage.basic.socket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import java.util.*;

class SocketThrdServer extends JFrame{

	   JLabel label = new JLabel("Text received over socket:");
	   JPanel panel;
	   JTextArea textArea = new JTextArea();
	   ServerSocket server = null;



	   SocketThrdServer(){ //Begin Constructor
	     panel = new JPanel();
	     panel.setLayout(new BorderLayout());
	     panel.setBackground(Color.white);
	     getContentPane().add(panel);
	     panel.add("North", label);
	     panel.add("Center", textArea);
	   } //End Constructor

	  public void listenSocket(){
	    try{
	      server = new ServerSocket(4444);
	    } catch (IOException e) {
	      System.out.println("Could not listen on port 4444");
	      System.exit(-1);
	    }
	    while(true){
	      ClientWorker w;
	      try{
	        w = new ClientWorker(server.accept(), textArea);

					AuthentificationManager.initUserbase();

		      Thread t = new Thread(w);
		      t.start();

	      } catch (IOException e) {
	        System.out.println("Accept failed: 4444");
	        System.exit(-1);
	      }
	    }
	  }

	  protected void finalize(){
	//Objects created in run method are finalized when
	//program terminates and thread exits
	     try{
	        server.close();
	    } catch (IOException e) {
	        System.out.println("Could not close socket");
	        System.exit(-1);
	    }
	  }

	  public static void main(String[] args){
	        SocketThrdServer frame = new SocketThrdServer();
		frame.setTitle("Server Program");
	        WindowListener l = new WindowAdapter() {
	                public void windowClosing(WindowEvent e) {
	                        System.exit(0);
	                }
	        };
	        frame.addWindowListener(l);
	        frame.pack();
	        frame.setVisible(true);
	        frame.listenSocket();
	  }
	}

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

			 } catch (IOException e) {
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
		       } catch (IOException e) {
		         System.out.println("Read failed");
		         System.exit(-1);
		       }
		    }
			}

	  }
	}

	class MessageManager {
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

	class AuthentificationManager {
		public static Map<String, String> registeredUsers = new HashMap<>();

		public static void initUserbase()
		{
			registeredUsers.put("user1", "123");
			registeredUsers.put("user2", "123");
		}
	}
