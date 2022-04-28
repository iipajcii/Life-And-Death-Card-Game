import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class Game {
	private int playerNumber = 0;
	private int currentPlayerNumber = 0;
	private int playerCount = 0;
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
		this.currentPlayerNumber = 1;
		this.determinePlayerNumber();
		this.mainLoop();
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

	public void determinePlayerNumber(){
		int portNumbers[] = new int[communication.getPlayers().size() + 1];
		playerCount = portNumbers.length;
		for(int counter = 0, count = communication.getPlayers().size(); counter < count; counter++){
			portNumbers[counter] = communication.getPlayers().get(counter).getPeerServerInetSocketAddress().getPort();
		}
		portNumbers[communication.getPlayers().size()] = communication.getPeerServerInetSocketAddress().getPort();
		Arrays.sort(portNumbers);

		for(int counter = 0, count = portNumbers.length; counter < count; counter++){
			if(communication.getPeerServerPort() == portNumbers[counter]){
				this.playerNumber = counter + 1;
				break;
			}
		}
		System.out.println(Arrays.toString(portNumbers));
		System.out.println("Port: " + communication.getPeerServerPort() +", Player Number: " + this.playerNumber);
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
		if(currentPlayerNumber == playerNumber){ return true; }
		return false;
	}

	public void mainLoop(){
		Scanner input;
		int choice = -1;

		while(choice != 0){
			if(this.isMyTurn()){
				System.out.println("It's My Turn");
			}
			input = new Scanner(System.in);
			choice = input.nextInt();
			if(choice != 0){this.nextPlayer();}
		}
	}
}