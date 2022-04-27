import java.net.*;
import java.io.*;

class Player {
	private Socket socket = null;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;
	private InetSocketAddress peerServerInetSocketAddress = null;

	public Player (Socket s){
		this.socket = s;
	}

	public void setStreams(ObjectInputStream pois, ObjectOutputStream poos){
		this.ois = pois;
		this.oos = poos;
	}

	public ObjectOutputStream getObjectOutputStream(){
		return this.oos;
	}

	public ObjectInputStream getObjectInputStream(){
		return this.ois;
	}

	public void setPeerServerInetSocketAddress(InetSocketAddress psisa){
		this.peerServerInetSocketAddress = psisa;
	}

	public InetSocketAddress getPeerServerInetSocketAddress(){
		return this.peerServerInetSocketAddress;
	}
}