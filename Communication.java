import java.net.*;
import java.io.*;
import java.util.Scanner;

class Communication {
	int socketNumber = 3333;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;
	private	Socket socket;

	public void connectToServer() throws Exception {
		try {
			socket = new Socket(InetAddress.getLocalHost(), socketNumber);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}

		this.getStreams();
		Message message = new Message();
		message.task = "Greetings";
		this.sendMessage(message);
		
		Message receivedMessage = this.receiveResponse();
		System.out.println(receivedMessage.task);
		socket.close();  
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
			oos.writeObject(message);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public Message receiveResponse(){
		try {
			Message message = (Message) ois.readObject();
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
}