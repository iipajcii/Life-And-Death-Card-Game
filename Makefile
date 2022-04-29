build_and_run: Main.java Communication.java Game.java Message.java Player.java Card.java
	make build
	java Main

build: Main.java Communication.java Game.java Message.java Player.java Card.java
	javac Main.java Main.java Communication.java Game.java Message.java Player.java Card.java

run:
	java Main

jar: 
	make build;
	jar cvfe Life_And_Death_Client.jar Main Main.class Communication.class Game.class Message.class Player.class Card.class 