import java.net.*;

class Player {
	private Socket socket = null;
	public Player (Socket s){
		this.socket = s;
	}
}