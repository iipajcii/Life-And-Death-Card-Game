build_and_run: Main.java Communication.java Game.java Message.java Player.java Card.java
	make build
	java Main

build: Main.java Communication.java Game.java Message.java Player.java Card.java
	javac Main.java Main.java Communication.java Game.java Message.java Player.java Card.java

run:
	java Main
