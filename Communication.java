import java.lang.Math;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

class Communication {
	ArrayList<Player> players = new ArrayList<Player>();
	int serverSocketNumber = 3333;
	private	Socket socket;
	private int players_needed = 3;
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
			while(players.size() < this.players_needed - 1){
				try {
					externalPeerSocket = peerServerSocket.accept();
					System.out.println("New Peer Connected");
					System.out.println("Getting New Peer Streams");
					Player currentPlayer = new Player(externalPeerSocket);
					this.getPeerStreams(externalPeerSocket);
					currentPlayer.setStreams(peer_ois, peer_oos);
					players.add(currentPlayer);
					System.out.println("Players: " + (players.size() + 1) +", Players Needed: " + this.players_needed);
					System.out.println("Peer Server Received Client Connection");
					//Server Receiving Greeting From Client
					this.receiveMessageFromObjectInputStream(peer_ois);
					this.greetPeerClient(peer_oos, peer_ois);
					//Server Asks Client For Server Socket
					m = new Message();
					m.setTask("Requesting Server Address");
					this.sendMessageToObjectOutputStream(peer_oos, m);
					m = this.receiveMessageFromObjectInputStream(peer_ois);
					currentPlayer.setPeerServerInetSocketAddress((InetSocketAddress) m.getDataObject());
					System.out.println("Player Server Address: " + (InetSocketAddress) m.getDataObject());
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
			System.out.println("We Have All The Players We Need!");
			//Sending each connected Player a message that the game should start
			try {
				for ( int counter = 0, count = players.size(); counter < count; counter++ ) {
					m = new Message();
					m.setTask("Begin Game");
					Player player = players.get(counter);
					System.out.println(player);
					this.sendMessageToObjectOutputStream(player.getObjectOutputStream(), m);
					// Server Receiving Request For Other Players From Client
					response = this.receiveMessageFromObjectInputStream(player.getObjectInputStream());
					if(response.task.equals("Requesting Peer Addresses")){
						ArrayList<InetSocketAddress> playerAddresses = new ArrayList<InetSocketAddress>();
						for( int counter2 = 0, count2 = players.size(); counter2 < count2; counter2++ ) {
							if(players.get(counter2) != player ){
								playerAddresses.add(players.get(counter2).getPeerServerInetSocketAddress());
							}
						}
						m = new Message();
						m.setTask("Peer Addresses");
						m.setDataObject(playerAddresses);
						System.out.println("Client Requesting Peers Function");
						this.sendMessageToObjectOutputStream(player.getObjectOutputStream(), m);
					}
				}
			}
			catch (Exception ex) {
				System.out.println("Failed to Begin Game With Peers");
				ex.printStackTrace();
			}
			this.serverCoordinateMeshingWithPeers();
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
			//-----
			receiveMessageFromObjectInputStream(peer_ois);
			receiveMessageFromObjectInputStream(peer_ois);
			m = new Message();
			m.setTask("Sending Server Address");
			m.setDataObject(this.getPeerServerInetSocketAddress());
			this.sendMessageToObjectOutputStream(peer_oos, m);
			Message begin = this.receiveMessageFromObjectInputStream(peer_ois);
			m = new Message();
			m.setTask("Requesting Peer Addresses");
			this.sendMessageToObjectOutputStream(peer_oos, m);
			//Response with peer addresses
			m = this.receiveMessageFromObjectInputStream(peer_ois);
			System.out.println("This is where the players should go");
			System.out.println( ( (ArrayList<InetSocketAddress>) m.getDataObject() ).size() );
			this.peerCoordinateMeshingWithServer();
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
	}

	public void serverCoordinateMeshingWithPeers(){
		try{
			System.out.println("Server Meshing Function Started");
			//Have a list of all connections
			//Sorting Players By Port Number
			ArrayList<Player> sortedPlayers = new ArrayList<Player>();
			int portNumbers[] = new int[players.size()];
			for(int counter = 0, count = players.size(); counter < count; counter++){
				portNumbers[counter] = players.get(counter).getPeerServerInetSocketAddress().getPort();
			}
			Arrays.sort(portNumbers);
			for(int counter = 0, count = players.size(); counter < count; counter++){
				for(int counter2 = 0, count2 = players.size(); counter2 < count2; counter2++){
					if(portNumbers[counter] == players.get(counter2).getPeerServerInetSocketAddress().getPort()){
						sortedPlayers.add(players.get(counter2));
						break;
					}
				}
			}
			//loop over all connections in order of lowest port number
			for(int counter = 0, count = sortedPlayers.size(); counter < count; counter++){
				InetSocketAddress currentSocketAddress = sortedPlayers.get(counter).getPeerServerInetSocketAddress();
				// Inform all connections to should connect to current port in iteration
				// **Inform the one that shoud be accepting connections _FIRST_ so it can start accepting connections
				for(int counter2 = 0, count2 = sortedPlayers.size(); counter2 < count2; counter2++){
					if(sortedPlayers.get(counter2).getPeerServerInetSocketAddress().getPort() != currentSocketAddress.getPort()){continue;}
					Message message = new Message();
					message.setTask("Connect To Peer");
					message.setDataObject(sortedPlayers.get(counter2).getPeerServerInetSocketAddress());
					System.out.println("Port: " + sortedPlayers.get(counter2).getPeerServerInetSocketAddress().getPort());
					this.sendMessageToObjectOutputStream(sortedPlayers.get(counter2).getObjectOutputStream(), message);
					this.receiveMessageFromObjectInputStream(sortedPlayers.get(counter2).getObjectInputStream());
				}
				//Proceed to inform other connections
				System.out.println("Other Connections");
				for(int counter2 = 0, count2 = sortedPlayers.size(); counter2 < count2; counter2++){
					//We already informed the peer that the other peers should be connecting to in the loop above
					if(sortedPlayers.get(counter2).getPeerServerInetSocketAddress().getPort() == sortedPlayers.get(counter).getPeerServerInetSocketAddress().getPort()){continue;}
					Message message = new Message();
					message.setTask("Connect To Peer");
					message.setDataObject(sortedPlayers.get(counter2).getPeerServerInetSocketAddress());
					System.out.println("Port: " + sortedPlayers.get(counter2).getPeerServerInetSocketAddress().getPort());
					this.sendMessageToObjectOutputStream(sortedPlayers.get(counter2).getObjectOutputStream(), message);
					this.receiveMessageFromObjectInputStream(sortedPlayers.get(counter2).getObjectInputStream());
				}
				
				// Server waits for all peers to say they have connected. *After which they will start listening*
				System.out.println("Next Peer Iteration");
			}
			// At the end of the loop send a finished meshing message to all peers
			for(int counter = 0, count = sortedPlayers.size(); counter < count; counter++){
				Message message = new Message();
				message.setTask("Finished Meshing");
				this.sendMessageToObjectOutputStream(sortedPlayers.get(counter).getObjectOutputStream(), message);
				//this.receiveMessageFromObjectInputStream(sortedPlayers.get(counter).getObjectInputStream());
			}
			System.out.println("Server Meshing Function Completed");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void peerCoordinateMeshingWithServer(){
		try {
			System.out.println("Peer Meshing Function Started");
			Message response = new Message();
			Message received = new Message();
			received.setTask("");
			while(!received.task.equals("Finished Meshing")){
				// Listen to what main server has to say
				received = this.receiveMessageFromObjectInputStream(peer_ois);
				System.out.println(received.getTask());
				// Connect to what main server tells you to connect to
				
				// Reply that you have connected
				response.setTask("Finished Connecting");
				// After you have finished connecting listen again
				this.sendMessageToObjectOutputStream(peer_oos, response);
				System.out.println("Is Finished Meshing? " + Boolean.toString(received.task.equals("Finished Meshing")));
			}
			// When server says finished meshing the function ends.
			System.out.println("Peer Meshing Function Completed");
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
	}

	public void greetPeerClient(ObjectOutputStream oos, ObjectInputStream ois){
		Message message = new Message();
		message.task = "Hello Peer Client";
		this.sendMessageToObjectOutputStream(oos, message);
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