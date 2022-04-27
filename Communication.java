import java.lang.Math;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

class Communication {
	ArrayList<Player> players = new ArrayList<Player>();
	int serverSocketNumber = 3333;
	private	Socket socket;
	private int players_needed = 2;
	private Player currentPlayer = null;

	// Peer Server Variables
	private ServerSocket peerServerSocket = null;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;

	// External Peer Server Variables
	private Socket externalPeerSocket = null;
	private	ObjectInputStream peer_ois;
	private	ObjectOutputStream peer_oos;

	Message receivedMessage = new Message();

	public InetSocketAddress getPeerServerInetSocketAddress(){
		try {
			return new InetSocketAddress(InetAddress.getLocalHost(), this.peerServerSocket.getLocalPort());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public void connectToServer() throws Exception {
		try {
			socket = new Socket(InetAddress.getLocalHost(), serverSocketNumber);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		this.getStreams();
	}

	public void connectToPeerServer(InetSocketAddress sa) throws Exception {
		try {
			// externalPeerSocket = new Socket(sa.getAddress(), sa.getPort());
			externalPeerSocket = new Socket("localhost", sa.getPort());
			System.out.println("Echoing Address: " + externalPeerSocket.getInetAddress() + ", Port: " + externalPeerSocket.getPort());
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		System.out.println("Is the Port Okay: " + externalPeerSocket);
	}

	public Message requestServerToStartGame(){
		Message m = new Message();
		m.task = "Start Game";
		this.sendMessage(m);
		Message receivedMessage = this.receiveMessage();
		if( receivedMessage.task.equals("Await Players") ){
			Message response = new Message();
			response.task = "Awaiting Players";
			response.setDataObject(this.getPeerServerInetSocketAddress());
			this.sendMessage(response);
			this.closeConnection();
			System.out.println("Waiting For Other Players To Connect..");
			while(players.size() < this.players_needed + 1){
				try {
					externalPeerSocket = peerServerSocket.accept();
					System.out.println("New Peer Connected");
					System.out.println("Getting New Peer Streams");
					this.getPeerStreams(externalPeerSocket);
					Player currentPlayer = new Player(externalPeerSocket);
					System.out.println("Peer Server Received Client Connection");
					//Server Receiving Greeting From Client
					this.receiveMessageFromObjectInputStream(peer_ois);
					
				} 
				catch (SocketException e) {
					System.out.println("Broken Pipe");
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					System.out.println("Something went wrong!");
					e.printStackTrace();
				}
			}
			System.out.println("//We Have All The Players We Need, Now DIE!");
		}
		else if(receivedMessage.task.equals("Join Players") ){
			this.closeConnection();
			InetSocketAddress peerServerSocketAddress = (InetSocketAddress) receivedMessage.getDataObject();
			System.out.println("Joining Peer Server At: " + (InetSocketAddress) receivedMessage.getDataObject());
			try {
				this.connectToPeerServer(peerServerSocketAddress);
				this.getPeerStreams(externalPeerSocket);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			currentPlayer = new Player(externalPeerSocket);
			currentPlayer.setStreams(peer_ois, peer_oos);
			players.add(currentPlayer);
			// The Players ObjectOutput and ObjectInput Streams are handled by the function below
			this.greetPeerServer(currentPlayer.getObjectOutputStream(), currentPlayer.getObjectInputStream());
		}
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

	public void greetPeerServer(ObjectOutputStream oos, ObjectInputStream ois){
		Message message = new Message();
		message.task = "Hello Peer Server";
		this.sendMessageToObjectOutputStream(oos, message);
		receiveMessageFromObjectInputStream(ois);
		Message m = this.receiveMessage();
	}

	public void greetPeerClient(ObjectOutputStream oos, ObjectInputStream ois){
		Message message = new Message();
		message.task = "Hello Peer Client";
		this.sendMessageToObjectOutputStream(oos, message);
		receiveMessageFromObjectInputStream(ois);
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

	public void getPeerStreams(Socket peerSocket){
		//This function is used by both a peer-server and peer-client
		System.out.println("This is the Peer Streams");
		try {
			peer_oos = new ObjectOutputStream(peerSocket.getOutputStream());
			peer_ois = new ObjectInputStream(peerSocket.getInputStream());
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public void getPeerServerStreams(Socket peerSocket){
		try {
			peer_oos = new ObjectOutputStream(peerSocket.getOutputStream());
			peer_ois = new ObjectInputStream(peerSocket.getInputStream());
			currentPlayer.setStreams(peer_ois, peer_oos);
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

	public void sendMessageToObjectOutputStream(ObjectOutputStream oos, Message message){
		try {
			System.out.println("Sending Peer: " + message.task);
			oos.writeObject(message);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public Message receiveMessageFromObjectInputStream(ObjectInputStream ois){
		try {
			Message message = (Message) ois.readObject();
			System.out.println("Peer Received: " + message.task);
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