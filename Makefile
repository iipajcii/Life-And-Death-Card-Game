build_and_run: Main.java Communication.java Game.java Message.java
	make build
	java Main

build: Main.java Communication.java Game.java Message.java
	javac Main.java Main.java Communication.java Game.java Message.java

run:
	java Main
