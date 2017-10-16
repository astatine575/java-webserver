package serverhost;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class RequestHandler implements Runnable{
	
	private LinkedList<Socket> _requestQueue;
	
	private Lock _requestLock;
	
	private Wait _requestMonitor;
	
	private boolean _isRunning = true;
	
	public RequestHandler(LinkedList<Socket> requestQueue, Lock requestLock, Wait requestMonitor) {
		_requestQueue = requestQueue;
		_requestLock = requestLock;
		_requestMonitor = requestMonitor;
	}
	
	public void run(){
		
		// extract a request to be handled.
		
		Socket currentRequest; 
		
		while(_isRunning){
			_requestLock.acquire();
			if (!_requestQueue.isEmpty()){
				currentRequest = _requestQueue.pop();
				_requestLock.release();
				handleRequest(currentRequest);
			}
			else {
				_requestLock.release();
				_requestMonitor.sleep();
			}
		}
		
		System.out.println(Thread.currentThread().getName()+": Shutting Down...");
	}
	
	private void handleRequest(Socket request){
		
		OutputStream out = null;
		InputStream in = null;
		
		try{
		out = request.getOutputStream();
		in = request.getInputStream();
		} catch (IOException e){
			System.err.println(Thread.currentThread().getName()+": Failed to establish streams with client");
			return;
		}
		
		ServerProtocol.processInput(in, out);
		
		try {
			in.close();
			out.close();
			request.close();
		} catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to close socket");
		}
		return;
		
		
	}
	
	public void shutdown(){
		_isRunning = false;
	}


}
