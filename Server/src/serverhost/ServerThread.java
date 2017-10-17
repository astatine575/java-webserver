package serverhost;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ServerThread implements Runnable{

	ServerProtocol _protocol = new ServerProtocol();
	
	Lock _requestLock;
	
	Wait _requestMonitor;
	
	int _portNumber;
	
	LinkedList<Socket> _requestQueue;
	
	boolean _isRunning;
	
	Thread _mainThread;
	
	ServerSocket _serverSocket = null;
	
	public ServerThread(int portNumber, LinkedList<Socket> requestQueue, Lock requestLock, Wait requestMonitor) {
		
		_requestLock = requestLock; //initialize the lock for client count
		
		_requestMonitor = requestMonitor;
		
		_portNumber = portNumber;
		
		_requestQueue = requestQueue;
		
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
        
        while(_isRunning){
        	
        	Socket newRequest = acceptNewRequest(); // listen for a new request
        	
        	if (newRequest != null){ // if a valid socket was connected, add it to the request list and notify any potential sleepers
        		_requestLock.acquire();
        		_requestQueue.add(newRequest);
        		System.out.println("Queued request from: "+newRequest.getInetAddress()); // client connected, print this info out for our lovely admins
        		_requestLock.release();
        		_requestMonitor.wake();
        	}
        	
        }
        
	}

	public Socket acceptNewRequest() { // listen to the socket till we find a client that wants to connect. connect him and 
		
		Socket requestSocket = null;
		try {
			requestSocket = _serverSocket.accept(); // listen to the socket
		} catch (SocketException e) { //when exiting
			return null;
		} catch (IOException e) {
			System.err.println("Accept failed."); // something went bubkus
			return null;
		} 
		
		return requestSocket;
		
		
		/*
		AcceptClient newClient = new AcceptClient(requestSocket); // make a new AcceptClient with the socket
		
		Thread newThread = new Thread(newClient); // make a new thread with AcceptClient.run() as its starting point and newClient as its memory
		
		newThread.setDaemon(true); // daemonize the new thread. After all, it's only right that a daemon spawns more daemons
		
		newThread.start(); // start the new thread
		*/
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
