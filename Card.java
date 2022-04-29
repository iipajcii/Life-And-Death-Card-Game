import java.io.*;

class Card implements Serializable {
	private String type = "";
	private int value = 0;
	private int playerNumber = 0;

	Card(){

	}

	Card(String t, int v){
		this.type = t;
		this.value = v;
	}

	public void setType(String t){
		this.type = t;
	}

	public String getType(){
		return this.type;
	}

	public void setValue(int v){
		this.value = v;
	}

	public int getValue(){
		return this.value;
	}

	public void setPlayerNumber(int pn){
		this.playerNumber = pn;
	}

	public int getPlayerNumber(){
		return this.playerNumber;
	}

	public void randomizeCardValue(){
		int min = 1;  
		int max = 2;  

		int typeValue = (int)(Math.random() * ( max - min + 1 ) + min );
		switch(typeValue){
			case 1:
				type = "Life";
				break;
			case 2:
				type = "Death";
				break;
		}

		min = 2;  
		max = 6;
		value = (int)(Math.random() * ( max - min + 1 ) + min );
	}

	public String toString(){
		return "[Card Type: " + this.type + ", Card Value: " + this.value + ", Player Number: " + this.playerNumber + "]";
	}
}