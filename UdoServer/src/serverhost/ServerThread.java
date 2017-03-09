package serverhost;

import java.io.*;
import java.net.*;

public class ServerThread implements Runnable{

	ServerProtocol _protocol = new ServerProtocol();
	
	int _clientCount;
	
	Lock _clientLock;
	
	int _clientMax;
	
	int _portNumber;
	
	boolean _isRunning;
	
	Thread _mainThread;
	
	ServerSocket _serverSocket = null;
	
	public ServerThread(int portNumber, int clientMax) {
		
		_clientLock = new Lock(); //initialize the lock for client count
		
		_clientMax = clientMax;
		
		_portNumber = portNumber;
		
		_isRunning = false;
		
		_mainThread = Thread.currentThread();
	}
	
	public void run(){
		
		// Open a socket on the appropriate port
		
        try {
            _serverSocket = new ServerSocket(_portNumber);
        } catch (IOException e) {
            System.err.println("Could not listen on port "+_portNumber+".");
            System.exit(1);
        }
        
        _isRunning = true; //tell main thread that we're up and running
        
        _mainThread.interrupt(); 
        
        while(_isRunning){ // do this until shut down (INTRODUCE SHUTDOWN VALUE WHEN UI IS ADDED)
        	
        	_clientLock.acquire(); //acquire the lock for _clientCount
        	
        	while (_clientCount >= _clientMax){ //if we're full, wait 5 seconds then check again.
        		System.out.println("SERVER AT FULL CAPACITY: "+_clientCount+"/"+_clientMax);
        		_clientLock.release();
        		try{
        			Thread.sleep(5000);
        		} catch (InterruptedException e){
        			System.out.println("Checking for new clients");
        		}
        		_clientLock.acquire();
        	}
        	
        	_clientCount++; //increment the client count as we're about to add one
        	_clientLock.release(); // we're done with _clientCount, release the lock on it
        	acceptNewClient();
        }
        
        //TODO: Shut down client threads safely
        
	}

	public void acceptNewClient() { // listen to the socket till we find a client that wants to connect. connect him and 
		
		Socket clientSocket = null;
		try {
			clientSocket = _serverSocket.accept(); // listen to the socket
		} catch (SocketException e) { //when exiting
			_clientLock.acquire(); //acquire the lock for _clientCount
			_clientCount--;
			_clientLock.release(); // we're done with _clientCount, release the lock on it
			return;
		} catch (IOException e) {
			System.err.println("Accept failed."); // something went bubkus
			_clientLock.acquire(); //acquire the lock for _clientCount
			_clientCount--;
			_clientLock.release(); // we're done with _clientCount, release the lock on it
			return;
		} 
		
		System.out.println("Connected client: "+clientSocket.getInetAddress()); // client connected, print this info out for our lovely admins
		
		AcceptClient newClient = new AcceptClient(clientSocket); // make a new AcceptClient with the socket
		
		Thread newThread = new Thread(newClient); // make a new thread with AcceptClient.run() as its starting point and newClient as its memory
		
		newThread.setDaemon(true); // daemonize the new thread. After all, it's only right that a daemon spawns more daemons
		
		newThread.start(); // start the new thread
	}

	public void shutDown() {
		// TODO Set up shutdown methods
		_isRunning=false;
		try {
			_serverSocket.close();
		} catch (IOException e) {
			System.err.println("Couldn't close server socket, forcing shutdown");
			System.exit(1);
		}
		
	}
	
}
