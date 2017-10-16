package serverhost;

import java.util.LinkedList;
import java.util.Scanner;
import java.io.IOException;
import java.net.Socket;
import java.security.*;

public class ServerDriver {

	public static ServerThread server = null;
	
	public static RequestHandler requestHandlers[];
	
	public static void main(String[] args) {
		
		if (args.length>2)
			System.err.println("proper usage: UdoServer <portNumber> <threadNum>");

		int portNumber = 80;
		
		int threadNum = 4;
		
		LinkedList<Socket> requestQueue = new LinkedList<Socket>();
		
		Lock requestLock = new Lock();
		
		Wait requestMonitor = new Wait();
		
		if (args.length>=1)
			portNumber = Integer.parseInt(args[0]);
		if (args.length>=2)
			threadNum = Integer.parseInt(args[1]);
		
		server = new ServerThread(portNumber, requestQueue, requestLock, requestMonitor); //initialize the server
		
		Thread serverThread = new Thread(server);
		
		serverThread.setName("Server Thread");
		
		serverThread.setDaemon(true);
		
		System.out.println("Starting server...");
		
		serverThread.start();
		
		while (!server._isRunning){
			try { //wait for server thread to wake you up
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		System.out.println("Server started on port "+portNumber);
		
		System.out.println("Starting request handlers...");
		
		requestHandlers = new RequestHandler[threadNum];
		Thread requestThreads[] = new Thread[threadNum];
		
		
		for (int i = 0; i < threadNum; i++){
			requestHandlers[i] = new RequestHandler(requestQueue, requestLock, requestMonitor);
			requestThreads[i] = new Thread(requestHandlers[i]);
			requestThreads[i].setDaemon(true);
			requestThreads[i].setName("Request Thread #" + i);
			requestThreads[i].start();
		}
		
		System.out.println("Started " + threadNum + " request handlers");
		
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

		for (RequestHandler rh : requestHandlers)
			rh.shutdown();
		
		try {
			serverThread.interrupt();
			serverThread.join();
			for (Thread thread : requestThreads){
				thread.interrupt();
				thread.join();
			}
		} catch (InterruptedException e) {
			System.err.println("INTERRUPT THROWN");
			e.printStackTrace();
		}
		
		for (Socket socket : requestQueue) 
			try { socket.close(); } catch (IOException e) { }
		
		
		System.out.println("Server Shutdown");
		
		System.exit(0);
    }

}

