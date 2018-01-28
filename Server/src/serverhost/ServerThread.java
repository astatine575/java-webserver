package serverhost;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class ServerThread implements Runnable{

	ServerProtocol _protocol = new ServerProtocol();
	
	ReentrantLock _requestLock;
	
	Wait _requestMonitor;
	
	int _portNumber;
	
	LinkedList<Socket> _requestQueue;
	
	boolean _isRunning;
	
	private int _status = -1; // -1 not running, 0 = starting up, 1 = waiting for client, 2 = connecting client, 3 = socket error
	
	Thread _mainThread;
	
	ServerSocket _serverSocket = null;
	
	public ServerThread(int portNumber, LinkedList<Socket> requestQueue, ReentrantLock requestLock, Wait requestMonitor) {
		
		_requestLock = requestLock; //initialize the lock for client count
		
		_requestMonitor = requestMonitor;
		
		_portNumber = portNumber;
		
		_requestQueue = requestQueue;
		
		_isRunning = false;
		
		_mainThread = Thread.currentThread();
	}
	
	public void run(){
		
		// Open a socket on the appropriate port
		_status = 0;
		
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
        		_requestLock.lock();
        		_requestQueue.add(newRequest);
        		_requestLock.unlock();
        		_requestMonitor.wake();
        	}
        }
        
        System.out.println("Server Thread shutting down...");
        
	}

	public Socket acceptNewRequest() { // listen to the socket till we find a client that wants to connect. connect him and 
		
		Socket requestSocket = null;
		try {
			_status = 1;
			requestSocket = _serverSocket.accept(); // listen to the socket
		} catch (SocketException e) { //when exiting
			if (_isRunning)
				System.out.println("Server socket broke :(");
			_status = 3;
			return null;
		} catch (IOException e) {
			System.out.println("Accept failed."); // something went bubkus
			return null;
		} 
		
		_status = 2;
		
		return requestSocket;
		
		
		/*
		AcceptClient newClient = new AcceptClient(requestSocket); // make a new AcceptClient with the socket
		
		Thread newThread = new Thread(newClient); // make a new thread with AcceptClient.run() as its starting point and newClient as its memory
		
		newThread.setDaemon(true); // daemonize the new thread. After all, it's only right that a daemon spawns more daemons
		
		newThread.start(); // start the new thread
		*/
	}
	
	public String getStatus()	{
		switch (_status){
		case -1: return("Server is unstarted");
		case 0: return("Server is starting up");
		case 1: return("Server is waiting for client on " + _serverSocket.getInetAddress()+":"+_serverSocket.getLocalPort());
		case 2: return("Server is connecting client");
		default: return("Server status is "+_status);
		}
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
