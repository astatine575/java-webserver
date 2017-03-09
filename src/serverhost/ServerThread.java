package serverhost;

import java.io.*;
import java.net.*;

public class ServerThread {

	ServerProtocol _protocol = new ServerProtocol();
	
	int _clientCount;
	
	Lock _clientLock;
	
	int _clientMax;
	
	ServerSocket _serverSocket = null;
	
	public ServerThread(int portNumber, int clientMax) {
		
		_clientLock = new Lock(); //initialize the lock for client count
		
		_clientMax = clientMax;
		
		// Open a socket on the appropriate port
		
        try {
            _serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.err.println("Could not listen on port "+portNumber+".");
            System.exit(1);
        }
        
        while(true){ // do this until shut down (INTRODUCE SHUTDOWN VALUE WHEN UI IS ADDED)
        	
        	_clientLock.acquire();
        	
        	while (ServerDriver.server._clientCount >= ServerDriver.server._clientMax){
        		System.out.println("SERVER AT FULL CAPACITY: "+ServerDriver.server._clientCount+"/"+ServerDriver.server._clientMax);
        		_clientLock.release();
        		try{
        			Thread.sleep(10000);
        		} catch (InterruptedException e){
        			System.out.println("Checking for new clients");
        		}
        		_clientLock.acquire();
        	}
        	
        	_clientCount++;
        	_clientLock.release();
        	acceptNewClient();
        }
	}

	public void acceptNewClient() {
		
		
		Socket clientSocket = null;
		try {
			clientSocket = _serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			_clientCount--;
			return;
		}
		
		(new Thread(new AcceptClient(clientSocket))).start();
	}
	
}
