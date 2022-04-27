import java.net.*;
import java.io.*;

class Player {
	private Socket socket = null;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;

	public Player (Socket s){
		this.socket = s;
	}

	public void setStreams(ObjectInputStream pois, ObjectOutputStream poos){
		this.ois = pois;
		this.oos = poos;
	}

	public ObjectOutputStream getObjectOutputStream(){
		return oos;
	}

	public ObjectInputStream getObjectInputStream(){
		return ois;
	}
}