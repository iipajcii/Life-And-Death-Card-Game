import java.lang.Math;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

class Communication {
	ArrayList<Player> players = new ArrayList<Player>();
	int serverSocketNumber = 3333;
	private	Socket socket;

	// Peer Server Variables
	private int peerServerSocketNumber = 0;
	private ServerSocket peerServerSocket = null;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;

	Message receivedMessage = new Message();

	public void connectToServer() throws Exception {
		try {
			socket = new Socket(InetAddress.getLocalHost(), serverSocketNumber);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		this.getStreams();
	}

	public Message requestServerToStartGame(){
		Message m = new Message();
		m.task = "Start Game";
		this.sendMessage(m);
		Message receivedMessage = this.receiveMessage();
		return receivedMessage;
	}

	public void startPeerServer(){
		System.out.println("Starting Peer Server");
		Boolean serverStarted = false;
		//Attempt to start the server
		while(!serverStarted){
			try {
				int min = 2000;  
				int max = 8000;  

				//Generate random value between min and max inclusive.
				int port = (int)(Math.random() * ( max - min + 1 ) + min );
				peerServerSocket = new ServerSocket(port, 1);
				System.out.println("Peer Server Started on port: " + port);
				serverStarted = true;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		// while(true)
		// {
		// 	try {
		// 		Socket playerSocket = peerServerSocket.accept();
		// 		Player player = new Player(playerSocket);
		// 		players.add(player);
		// 		System.out.println("New Player has joined");
		// 	} 
		// 	catch (SocketException e) {
		// 		System.out.println("Broken Pipe");
		// 		e.printStackTrace();
		// 	}
		// 	catch (IOException e) {
		// 		e.printStackTrace();
		// 	}
		// 	catch (Exception e) {
		// 		System.out.println("Something went wrong!");
		// 		e.printStackTrace();
		// 	}
		// }
		/* 
			Unreachable Statements to close sockets
			din.close();  
			dout.close();  
			socket.close();  
			serverSocket.close();
		*/
	}

	public void greetServer(){
		Message message = new Message();
		message.task = "Hello Server";
		this.sendMessage(message);
		Message m = this.receiveMessage();
	}

	public void getStreams(){
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public void sendMessage(Message message){
		try {
			System.out.println("Sending Server: " + message.task);
			oos.writeObject(message);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public Message receiveMessage(){
		try {
			Message message = (Message) ois.readObject();
			System.out.println("Server Response: " + message.task);
			return message;
		}
		catch(ClassCastException ex){
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return null;
	}

	public void closeConnection(){
		try {
			oos.close();
			ois.close();
			socket.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}