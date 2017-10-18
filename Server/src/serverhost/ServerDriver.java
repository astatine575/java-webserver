package serverhost;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.*;

public class ServerDriver {

	public static ServerThread server = null;
	
	public static RequestHandler requestHandlers[];
	
	private static ArrayList<String> validFiles;
	
	public static String homepage;
	
	public static String mode;
	
	public static String serverdata;
	
	public static void main(String[] args) {
		
		if (args.length>2)
			System.err.println("proper usage: UdoServer <portNumber> <threadNum> <mode>");

		int portNumber = 80;
		
		int threadNum = 4;
		
		mode = "http";
		
		serverdata = "/serverdata";
		
		homepage = "/index.html";
		
		String permissionsFile = "/validFiles.ini";
		
		LinkedList<Socket> requestQueue = new LinkedList<Socket>();
		
		validFiles = new ArrayList<String>();
		
		Lock requestLock = new Lock();
		
		Wait requestMonitor = new Wait();
		
		if (args.length>=1)
			portNumber = Integer.parseInt(args[0]);
		if (args.length>=2)
			threadNum = Integer.parseInt(args[1]);
		if (args.length>=3)
			mode = "" + args[2];
		
		System.out.println("Loading file access permissions...");
		
		loadFilePermissions(System.getProperty("user.dir")+permissionsFile);
		
		System.out.println("File permissions loaded from " + System.getProperty("user.dir")+permissionsFile);
		
		/*
		for (String filename : validFiles) 
			System.out.println(filename);
		*/
		
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
			else if (consolein.equals("valid files"))
				for (String filename : validFiles) 
					System.out.println(filename);
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
	
	public static boolean isValidFile(String filename){
		return validFiles.contains(filename);
	}
	
	public static void loadFilePermissions(String filename){
		RandomAccessFile permissionFile = null;
		try {
			permissionFile = new RandomAccessFile(filename, "r");
		} catch (FileNotFoundException e) {
			System.err.println("SERVER PERMISSION FILE "+filename+" NOT FOUND");
			System.exit(1);
		}
		
		String nextLine = "";
		
		try{
			while ((nextLine = permissionFile.readLine())!=null)
				validFiles.add(nextLine);
			permissionFile.close();
		}catch(IOException e){
			System.err.println("SERVER PERMISSION FILE "+filename+" FAILED TO BE READ FROM");
			System.exit(1);
		}
	}
}

