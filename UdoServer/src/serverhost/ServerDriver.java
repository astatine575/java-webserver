package serverhost;

import java.util.Scanner;

public class ServerDriver {

	public static ServerThread server = null;
	
	public static void main(String[] args) {
		
		if (args.length>2)
			System.err.println("proper usage: UdoServer <portNumber> <clientMax>");

		int portNumber = 4444;
		
		int clientMax = 8000;
		
		if (args.length>=1)
			portNumber = Integer.parseInt(args[0]);
		if (args.length>=2)
			clientMax = Integer.parseInt(args[1]);
		
		server = new ServerThread(portNumber, clientMax); //initialize the server
		
		Thread serverThread = new Thread(server);
		
		serverThread.setDaemon(true);
		
		System.out.println("Starting server...");
		
		serverThread.start();
		
		while (!server._isRunning){
			try { //wait for server thread to wake you up
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
		
		System.out.println("Server started on port "+portNumber);
		
		String consolein;
		
		Scanner sc = new Scanner(System.in);
		
		System.out.print(": ");
		
		while ((consolein=sc.nextLine())!=null){
			if (consolein.equals("quit"))
				break;
			System.out.print(": ");
		}
		
		sc.close();
		
		System.out.println("Shutting down server...");
		
		server.shutDown();
		
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			System.err.println("INTERRUPT THROWN");
			e.printStackTrace();
		}
		
		//TODO: ADD NICE WAY TO SHUT DOWN THREADS BEFORE JVM EXITS
		
    }

}

