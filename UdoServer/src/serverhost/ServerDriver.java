package serverhost;

import java.util.Scanner;

public class ServerDriver {

	public static ServerThread server = null;
	
	public static void main(String[] args) {
		
		if (args.length>2)
			System.err.println("proper usage: UdoServer <portNumber> <clientMax>");

		int portNumber = 4444;
		
		int clientMax = 100;
		
		if (args.length>=1)
			portNumber = Integer.parseInt(args[0]);
		if (args.length>=2)
			clientMax = Integer.parseInt(args[1]);
		
		server = new ServerThread(portNumber, clientMax); //initialize the server
		
		Thread serverThread = new Thread(server);
		
		serverThread.setDaemon(true);
		
		serverThread.start();
		
		
		String love;
		
		Scanner sc = new Scanner(System.in);
		
		while ((love=sc.nextLine())!=null){
			if (love.equals("HATE"))
				break;
			System.out.println("I love "+love+"!");
		}
		
		sc.close();
		
		//TODO: ADD NICE WAY TO SHUT DOWN THREADS BEFORE JVM EXITS
		
    }

}

