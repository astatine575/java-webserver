package serverhost;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class RequestHandler implements Runnable{
	
	private LinkedList<Socket> _requestQueue;
	
	private ReentrantLock _requestLock;
	
	private Wait _requestMonitor;
	
	private int _status = -1; // -1 = uninitialized, 0 = running and looking for request, 1 = waiting for request, 2 = handling request, 3 = connecting to client   
	
	private boolean _isRunning = true;
	
	public RequestHandler(LinkedList<Socket> requestQueue, ReentrantLock requestLock, Wait requestMonitor) {
		_requestQueue = requestQueue;
		_requestLock = requestLock;
		_requestMonitor = requestMonitor;
	}
	
	public void run(){
		
		// extract a request to be handled.
		_status = 0;
		
		Socket currentRequest; 
		
		while(_isRunning){
			//System.out.println(Thread.currentThread().getName()+": setting status to 0");
			_status = 0;
			//System.out.println(Thread.currentThread().getName()+": status set to 0, acquiring lock");
			_requestLock.lock();
			//System.out.println(Thread.currentThread().getName()+": Lock acquired, looking for request");
			if (!_requestQueue.isEmpty()){
				//System.out.println(Thread.currentThread().getName()+": request found, setting status to 1");
				_status = 1;
				//System.out.println(Thread.currentThread().getName()+": status set to 1, retrieving request");
				currentRequest = _requestQueue.pop();
				//System.out.println(Thread.currentThread().getName()+": request retrieved, releasing lock");
				_requestLock.unlock();
				//System.out.println(Thread.currentThread().getName()+": Lock released, setting status to 3");
				_status = 3;
				//System.out.println(Thread.currentThread().getName()+": status set to 1, handling request");
				handleRequest(currentRequest);
			}
			else {
				//System.out.println(Thread.currentThread().getName()+": no request found, releasing lock");
				_requestLock.unlock();
				//System.out.println(Thread.currentThread().getName()+": lock released, setting status to 2");
				_status = 2;
				//System.out.println(Thread.currentThread().getName()+": status set to 2, sleeping on request Monitor");
				_requestMonitor.sleep();
				//System.out.println(Thread.currentThread().getName()+": woken up from request monitor, repeating loop");
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
		
		_status = 4;
		
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
	
	public String getStatus(){
		switch (_status){
		case -1: return("Handler is unstarted");
		case 0: return("Handler is looking for request");
		case 1: return("Handler found request");
		case 2: return("Handler is waiting for request");
		case 3: return("Handler is handling request");
		case 4: return("Handler is connecting with client");
		default: return("Handler status is "+_status);
		}
	}
	
	public void shutdown(){
		_isRunning = false;
	}


}
