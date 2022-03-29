build_and_run: Main.java Communication.java Game.java
	make build
	java Main

build: Main.java Communication.java Game.java
	javac Main.java Main.java Communication.java Game.java

run:
	java Main
