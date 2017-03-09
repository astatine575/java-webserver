package serverhost;

import java.io.*;
import java.net.*;

public class AcceptClient implements Runnable{
	
	private Socket _clientSocket;
	
	public AcceptClient(Socket clientSocket){ //just store the socket so that run can use it
		_clientSocket = clientSocket;
	}
	
	public void run(){ 
		
		
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			out = new PrintWriter(_clientSocket.getOutputStream(), true);
			in = new BufferedReader( new InputStreamReader(_clientSocket.getInputStream()) );
		} catch (IOException e) {
			System.err.println("Failed to establish IO connection");
			out.close();
			try {
				_clientSocket.close();
				ServerDriver.server._clientLock.acquire();
				ServerDriver.server._clientCount--;
				ServerDriver.server._clientLock.release();
			} catch (IOException f) {
				System.err.println("Failed to close output to socket");
			}
			return;
		}

		String inputLine;

		try{
			while ((inputLine = in.readLine()) != null) { //while client is connected
				if (ServerProtocol.processInput(inputLine,out)){
					System.out.println("Client closed connection");
					out.close();
					try {
						in.close();
						_clientSocket.close();
						ServerDriver.server._clientLock.acquire();
						ServerDriver.server._clientCount--;
						ServerDriver.server._clientLock.release();
					} catch (IOException f) {
						System.err.println("Failed to close output to socket");
					}
					return;
				}
			}
		} catch (IOException e) {
			System.err.println("Connection Error");
			out.close();
			try {
				in.close();
				_clientSocket.close();
				ServerDriver.server._clientLock.acquire();
				ServerDriver.server._clientCount--;
				ServerDriver.server._clientLock.release();
			} catch (IOException f) {
				System.err.println("Failed to close output to socket");
			}
			return;
		}
		out.close();
		try {
			in.close();
			_clientSocket.close();
			ServerDriver.server._clientLock.acquire();
			ServerDriver.server._clientCount--;
			ServerDriver.server._clientLock.release();
		} catch (IOException f) {
			System.err.println("Failed to close output to socket");
		}
		return;
		
		
		// System.out.println("I'm a little new thread, short and stout");
	}


}
