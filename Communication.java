import java.net.*;
import java.io.*;
import java.util.Scanner;

class Communication {
	int socketNumber = 3333;
	public void connectToServer() throws Exception {  
		Socket socket = new Socket("localhost",socketNumber);
		Scanner input;
		int choice = 0;
		
		while(choice != 1){
			input = new Scanner(System.in);
			choice = input.nextInt();
		}

		socket.close();  
	}  
}