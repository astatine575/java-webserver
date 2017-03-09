package serverhost;

public class ServerDriver {

	public static ServerThread server;
	
	public static void main(String[] args) {
		
		if (args.length>2)
			System.err.println("proper usage: UdoServer <portNumber> <clientMax>");

		int portNumber = 4444;
		
		int clientMax = 100;
		
		if (args.length>=1)
			portNumber = Integer.parseInt(args[0]);
		if (args.length>=2)
			clientMax = Integer.parseInt(args[1]);
		
		server = new ServerThread(portNumber, clientMax); //start the server
        
        
    }

}

