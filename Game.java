import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.*;

class Game {
	private ArrayList<Card> cards = new ArrayList<Card>();
	private int playerNumber = 0;
	private int currentPlayerNumber = 1;
	private Player currentPlayer = null;
	private int playerCount = 0;
	private int health = 20;
	private int portNumbers[];
	private Communication communication = null;
	public void start(){
		System.out.println("Starting Game...");
		try {
			communication = new Communication();
			communication.connectToServer();
			communication.greetServer();
			communication.startPeerServer();
			communication.requestServerToStartGame();			
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to Start Game");
		}

		System.out.println("THE GAME HAS STARTED");
		this.playerNumber = this.getPlayerNumber();
		this.makeNewHandOfCards(5);
		Boolean end = false;
		int loopCounter = 0;
		System.out.println("Player Number: " + currentPlayerNumber);
		while(loopCounter < 9){
			if(isMyTurn()){
				for(int counter = 0, count = 2; counter < count; counter++){
					try {
						ServerSocket peerServerSocket = communication.getPeerServerSocket();
						System.out.println("Peer Server Socket: " + peerServerSocket);
						Socket temp_socket = peerServerSocket.accept();
						System.out.println("Temp Socket: " + temp_socket);
						int temp_socket_port = temp_socket.getPort();
						// System.out.println("Temp Socket Port: " + temp_socket_port);
						// Player player = this.getPlayerByPortNumber(temp_socket_port);
						ObjectOutputStream temp_oos = new ObjectOutputStream(temp_socket.getOutputStream());
						ObjectInputStream temp_ois = new ObjectInputStream(temp_socket.getInputStream());
						//Receive Player Number
						Message m = communication.receiveMessageFromObjectInputStream(temp_ois);
						Player player = this.getPlayerByPortNumber((Integer)m.getDataObject());
						player.setStreams(temp_ois, temp_oos);
						System.out.println("A Player Re-Streamed");
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}
				Scanner input;
				int choice;
				do {
					Message m = new Message();
					System.out.println("I should broadcast");
					input = new Scanner(System.in);
					choice = input.nextInt();
					m.setTask("Number: " + choice);
					communication.broadcast(m);
				} while(choice != 0);
				// this.nextPlayerBroadcast(m);
			}
			else {
				Message m = new Message();
				m.setTask("Listening To Player: " + this.currentPlayerNumber);
				System.out.println("I should listen to Player: " + this.currentPlayerNumber + ", At Port: " + currentPlayerPortNumber());
				try {
					Socket temp_socket = new Socket(InetAddress.getLocalHost(), currentPlayerPortNumber());
					ObjectOutputStream temp_oos = new ObjectOutputStream(temp_socket.getOutputStream());
					ObjectInputStream temp_ois = new ObjectInputStream(temp_socket.getInputStream());
					Message playerNumberMessage = new Message();
					playerNumberMessage.setTask("Player Port");
					playerNumberMessage.setDataObject(new Integer(portNumbers[playerNumber - 1]));
					communication.sendMessageToObjectOutputStream(temp_oos, playerNumberMessage);					
					Player player = getPlayerByPortNumber(currentPlayerPortNumber());
					player.setStreams(temp_ois, temp_oos);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				while(true){
					m = communication.receiveMessageFromObjectInputStream(this.getCurrentPlayer().getObjectInputStream());
				}
			}
			// this.nextPlayer();
			loopCounter++;
		}
		// while(true){
		// 	if(isMyTurn()){
		// 		System.out.println("It's My Turn");
		// 		Message message = new Message();
		// 		message.setTask("Player Number: " + currentPlayerNumber);
		// 		this.nextPlayerBroadcast(message);
		// 		this.nextPlayer();
		// 	}
		// 	else {
		// 		Message message = new Message();
		// 		currentPlayer = getCurrentPlayer();
		// 		message = communication.receiveMessageFromObjectInputStream(currentPlayer.getObjectInputStream());
		// 		this.nextPlayer();
		// 	}
		// }
		//this.mainLoop();
	}

	public void end(){
		System.out.println("The Game has Ended. Thank you for playing Life and Death");
		System.exit(0);
	}

	public void menu(){
		Scanner input;
		int choice;

		do {
			input = new Scanner(System.in);
			System.out.println("What would you like to do?");
			System.out.println("1. Start Game");
			System.out.println("2. Exit Game");
			choice = input.nextInt();

			switch(choice){
				case 1:
					this.start();
					break;
				case 2:
					this.end();
					break;
				default:
					System.out.println("Not a vaild choice. Please enter again.");
					break;
			}
		} while(choice < 1 || choice > 2);
	}

	public int getPlayerNumber(){
		int playerNumber = 0;
		portNumbers = new int[communication.getPlayers().size() + 1];
		playerCount = portNumbers.length;
		for(int counter = 0, count = communication.getPlayers().size(); counter < count; counter++){
			portNumbers[counter] = communication.getPlayers().get(counter).getPeerServerInetSocketAddress().getPort();
		}
		portNumbers[communication.getPlayers().size()] = communication.getPeerServerInetSocketAddress().getPort();
		Arrays.sort(portNumbers);

		for(int counter = 0, count = portNumbers.length; counter < count; counter++){
			if(communication.getPeerServerPort() == portNumbers[counter]){
				playerNumber = counter + 1;
				break;
			}
		}
		System.out.println(Arrays.toString(portNumbers));
		System.out.println("Port: " + communication.getPeerServerPort() +", Player Number: " + playerNumber);
		return playerNumber;
	}

	public int currentPlayerPortNumber(){
		portNumbers = new int[communication.getPlayers().size() + 1];
		playerCount = portNumbers.length;
		for(int counter = 0, count = communication.getPlayers().size(); counter < count; counter++){
			portNumbers[counter] = communication.getPlayers().get(counter).getPeerServerInetSocketAddress().getPort();
		}
		portNumbers[communication.getPlayers().size()] = communication.getPeerServerInetSocketAddress().getPort();
		Arrays.sort(portNumbers);
		return portNumbers[currentPlayerNumber - 1];
	}

	public Player getPlayerByPortNumber(int port){
		ArrayList<Player> players = communication.getPlayers();
		for (int counter = 0, count = players.size(); counter < count ; counter++ ) {
			Player player = players.get(counter);
			if(player.getPeerServerInetSocketAddress().getPort() == port){
				return player;
			}
		}
		return null;
	}

	public int nextPlayerPortNumber(){
		int portNumber = -1;
		portNumbers = new int[communication.getPlayers().size() + 1];
		playerCount = portNumbers.length;
		for(int counter = 0, count = communication.getPlayers().size(); counter < count; counter++){
			portNumbers[counter] = communication.getPlayers().get(counter).getPeerServerInetSocketAddress().getPort();
		}
		portNumbers[communication.getPlayers().size()] = communication.getPeerServerInetSocketAddress().getPort();
		Arrays.sort(portNumbers);
		if(currentPlayerNumber == playerCount){
			portNumber = portNumbers[0];
		}
		else {
			// Remeber currentPlayerNumber starts from 1 and not 0, so currentPlayerNumber is returning index+1 and currentPlayerNumber returns index
			portNumber = portNumbers[currentPlayerNumber];
		}
		return portNumber;
	}

	public Boolean nextPlayer(){
		if(currentPlayerNumber < playerCount){
			currentPlayerNumber++;
		}
		else {
			currentPlayerNumber = 1;
		}
		System.out.println("Players Turn: " + currentPlayerNumber);
		return true;
	}

	public Boolean isThisPeerServersPortNumber(int portNumber){
		if(portNumber == communication.getPeerServerPort()){return true;}
		return false;
	}

	public Boolean isMyTurn(){
		if(currentPlayerNumber == playerNumber){ 
			this.currentPlayer = null;
			return true; 
		}
		return false;
	}

	public void mainLoop(){
		Scanner input;
		int choice = -1;

		while(choice != 0){
			if(this.isMyTurn()){
				System.out.println("YOUR TURN!");
			}

			this.displayPlayers();
			System.out.println("Your Health: " + this.health);
			System.out.println("Card Count: " + this.cards.size());
			System.out.println("Cards In Hand");

			this.displayCards();
			if(this.isMyTurn()){
				System.out.println("\nActions:");
				System.out.println("1. Play Card");
				System.out.println("2. Skip Turn");
				System.out.println("3. Leave Game");
				
				input = new Scanner(System.in);
				choice = input.nextInt();

				switch(choice){
					case 1:
						this.playCard();
						break;
					case 2:
						this.skipTurn();
						break;
					case 3:
						this.leaveGame();
						break;
				}

				// if(choice != 0){this.nextPlayer();}
			}
			else {
				System.out.println("Waiting For Other Players");
				//The (this.nextPlayer();) function is Not supposed to be here
				//Just preventing a loop while developing
				this.nextPlayer();
			}
		}
	}

	public void makeNewHandOfCards(int handSize){
		cards = new ArrayList<Card>();
		for(int counter = 0, count = handSize; counter < count; counter++){
			Card card = new Card();
			card.randomizeCardValue();
			cards.add(card);
		}
	}

	public void displayCards(){
		for(int counter = 0, count = cards.size(); counter < count; counter++){
			Card card = this.cards.get(counter);
			System.out.println((counter + 1) + ": Type: " + card.getType() + ", Value: " + card.getValue());
		}
	}

	public void playCard(){
		System.out.println("What card do you want to play?");
		System.out.println("0. Cancel Playing Card");
		this.displayCards();

		int choice = -1;
		do {
			Scanner input = new Scanner(System.in);
			choice = input.nextInt();
		} while(choice < 0 || choice > cards.size());
		Card card = cards.get(choice - 1);
		cards.remove(card);
		System.out.println("Played Card: " + card.toString());

		Message m = new Message();
		m.setTask("Play Card");
		m.setDataObject(card);
	}

	public void skipTurn(){
		//
	}

	public void leaveGame(){
		//
	}

	public void displayPlayers(){
		System.out.println("We should display players here");
	}

	public Player getCurrentPlayer(){
		ArrayList<Player> players = communication.getPlayers();
		for (int counter = 0, count = players.size(); counter < count ; counter++) {
			Player player = players.get(counter);
			if(player.getPeerServerInetSocketAddress().getPort() == currentPlayerPortNumber()){
				return player;
			}
		}
		return null;
	}

	public Player getNextPlayer(){
		ArrayList<Player> players = communication.getPlayers();
		for (int counter = 0, count = players.size(); counter < count ; counter++) {
			Player player = players.get(counter);
			if(player.getPeerServerInetSocketAddress().getPort() == nextPlayerPortNumber()){
				return player;
			}
		}
		return null;
	}

	public void nextPlayerBroadcast(Message m){
		//Message next player
		ArrayList<Player> players = communication.getPlayers();
		Player nextPlayer = getNextPlayer();
		System.out.println("Sending To Port: " + nextPlayer.getPeerServerInetSocketAddress().getPort());
		communication.sendMessageToObjectOutputStream(nextPlayer.getObjectOutputStream(), m);
		// this.sleep(2000);
		//Then Message Everyone else
		for (int counter = 0, count = players.size(); counter < count ; counter++) {
			if(players.get(counter) == nextPlayer){continue;}
			Player player = players.get(counter);
			System.out.println("Sending To Port: " + player.getPeerServerInetSocketAddress().getPort());
			communication.sendMessageToObjectOutputStream(player.getObjectOutputStream(), m);
			// this.sleep(2000);
		}
	}

	public void sleep(int ms){
		try
		{
			System.out.println("Sleeping for " + ms +" seconds");
		    Thread.sleep(ms);
		}
		catch(InterruptedException ex)
		{
		    Thread.currentThread().interrupt();
		}
	}
}