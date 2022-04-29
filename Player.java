import java.net.*;
import java.io.*;

class Player {
	private Socket socket = null;
	private	ObjectInputStream ois;
	private	ObjectOutputStream oos;
	private InetSocketAddress peerServerInetSocketAddress = null;
	private int maxHealth = 20;
	private int health = 20;

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
	
	public void setSocket(Socket s){
		this.socket = s;
	}

	public Socket getSocket(){
		return this.socket;
	}

	public void setHealth(int h){
		this.health = h;
	}

	public int getHealth(){
		return this.health;
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