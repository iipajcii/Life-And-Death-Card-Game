import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.*;

class Game {
	private Boolean testing = false;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private int playerNumber = 0;
	private int currentPlayerNumber = 1;
	private Player currentPlayer = null;
	private int playerCount = 0;
	private int health = 20;
	private int maxHealth = 20;
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

		System.out.println("THE GAME HAS STARTED\n");
		this.playerNumber = this.getPlayerNumber();
		this.makeNewHandOfCards(5);
		Boolean end = false;
		int loopCounter = 0;
		System.out.println("$ You Are Player " + playerNumber);
		if(currentPlayerNumber == playerNumber){
			System.out.println("Your Turn!");
		}
		else{
			System.out.println("Player " + currentPlayerNumber + "'s Turn");
		}
		while(true){
			if(isMyTurn()){
				for(int counter = 0, count = 2; counter < count; counter++){
					try {
						ServerSocket peerServerSocket = communication.getPeerServerSocket();
						if(testing){System.out.println("Peer Server Socket: " + peerServerSocket);}
						Socket temp_socket = peerServerSocket.accept();
						if(testing){System.out.println("Temp Socket: " + temp_socket);}
						int temp_socket_port = temp_socket.getPort();
						ObjectOutputStream temp_oos = new ObjectOutputStream(temp_socket.getOutputStream());
						ObjectInputStream temp_ois = new ObjectInputStream(temp_socket.getInputStream());
						//Receive Player Number
						Message m = communication.receiveMessageFromObjectInputStream(temp_ois);
						Player player = this.getPlayerByPortNumber((Integer)m.getDataObject());
						player.setStreams(temp_ois, temp_oos);
						if(testing){System.out.println("Player Connected Successfully");}
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}
				this.playerInterface();
			}
			else {
				Message m = new Message();
				System.out.println("Listeing To Player: " + this.currentPlayerNumber + ", On Port: " + currentPlayerPortNumber());
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
				do {
					m = communication.receiveMessageFromObjectInputStream(this.getCurrentPlayer().getObjectInputStream());
					
					if(m.task.equals("Play Card")){
						Card c = (Card) m.getDataObject();
						System.out.println("Player " + this.currentPlayerNumber +" played a " + c.getType() + " card of value " + c.getValue() + " on Player " + c.getPlayerNumber());
						Player p = getPlayerByNumber(c.getPlayerNumber());
						if(p != null){
							p.applyCard(c);
						}
						else {
							this.applyCard(c);
						}
						this.displayPlayers();
					}
					else if(m.task.equals("Next Player")){
						this.nextPlayer();
					}
					else if(m.task.equals("Skip Turn")){
						System.out.println("Player " + this.currentPlayerNumber + " is skipping their turn.");
					}
				} while(!m.task.equals("Next Player"));
			}
		}
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
		if(testing){System.out.println(Arrays.toString(portNumbers));}
		if(testing){System.out.println("Port: " + communication.getPeerServerPort() +", Player Number: " + playerNumber);}
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

	public void displayPlayers(){
		// ArrayList<Player> players = communication.getPlayers();
		for (int counter = 0, count = portNumbers.length; counter < count ; counter++ ) {
			Player player = getPlayerByPortNumber(portNumbers[counter]);
			if(player != null){
				System.out.println("Player " + (counter + 1) + ": [Health " + player.getHealth() +"]");
			}
			else {
				System.out.println("Player " + (counter + 1) + ": [Health " + this.health +"]");
			}
		}
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
	
	public Player getPlayerByNumber(int num){
		if(num == playerNumber){return null;}
		//Need functionality in getPlayerNumber to alter the PortNumberArray
		getPlayerNumber();
		return getPlayerByPortNumber(portNumbers[num - 1]);
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

	public void playerInterface(){
		Scanner input;
		int choice;
		Boolean done = false;
		do {
			int cardChoice = -1;
			Message m = new Message();
			this.displayPlayers();
			System.out.println("Actions:");
			System.out.println("1. Play Card");
			System.out.println("2. Skip Turn");
			input = new Scanner(System.in);
			choice = input.nextInt();
			switch(choice){
				case 1:
					System.out.println("The cards in your hand are displayed. What Card Do You Want To Play?");
					System.out.println("0. Cancel Playing Card");
					this.displayCards();
					cardChoice = -1;
					do {
						Scanner cardChoiceinput = new Scanner(System.in);
						cardChoice = cardChoiceinput.nextInt();
					} while(cardChoice < 0 || cardChoice > cards.size());
					if(cardChoice == 0){break;}
					System.out.println("What player do you want to target?");
					System.out.println("0. Cancel Playing Card");
					for (int counter = 0, count = playerCount; counter < count ; counter++ ) {
						System.out.println((counter + 1) + ": Player " + (counter + 1));
					}
					int playerChoice = -1;
					do {
						Scanner playerChoiceInput = new Scanner(System.in);
						playerChoice = playerChoiceInput.nextInt();
					} while(playerChoice < 0 || playerChoice > playerCount);
					if(playerChoice == 0){break;}
					Card card = cards.get(cardChoice - 1);
					//Removing the Card from the players hand
					card.setPlayerNumber(playerChoice);
					cards.remove(card);

					//The player draws a new card
					Card newCard = new Card();
					newCard.randomizeCardValue();
					cards.add(newCard);
					
					System.out.println("Played Card: " + card.toString());
					Player p = getPlayerByNumber(playerChoice);
					if(p != null){
						p.applyCard(card);
					}
					else {
						this.applyCard(card);
					}
					this.displayPlayers();
					m.setTask("Play Card");
					m.setDataObject(card);
					done = true;
					
				break;
				case 2:
					m.setTask("Skip Turn");
					done = true;
				break;
			}
			if(choice < 1 || choice > 2 || cardChoice == 0){
				if(cardChoice == 0){
					System.out.println("Cancelled Choosing Card");
				}
				else {
					System.out.println("Invalid Option");
				}
			}
			else {
				//If choice is not invalid then it must be valid
				communication.broadcast(m);
			}
		} while(!done);
		Message next = new Message();
		next.setTask("Next Player");
		this.nextPlayerBroadcast(next);
		this.nextPlayer();
	}

	public void applyCard(Card c){
		if(c.getType().equals("Life")){
			this.health += c.getValue();
			if(this.health > this.maxHealth){
				this.health = this.maxHealth;
			}
		}
		else if(c.getType().equals("Death")){
			this.health -= c.getValue();
		}
	}
}