import java.util.Scanner;

class Game {
	public void start(){
		System.out.println("Starting Game...");
		try {
			Communication communication = new Communication();
			communication.connectToServer();
			communication.greetServer();
			communication.startPeerServer();
			communication.requestServerToStartGame();			
		}
		catch(Exception e){
			System.out.println("Failed to Connect to Server");
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
}