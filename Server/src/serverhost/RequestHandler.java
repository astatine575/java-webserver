package serverhost;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class RequestHandler implements Runnable{
	
	private LinkedList<Socket> _requestQueue;
	
	private ReentrantLock _requestLock;
	
	private boolean _isRunning = true;
	
	public enum Status {
		UNINITIALIZED,
		LOOKING_FOR_REQUEST,
		FOUND_REQUEST,
		WAITING_FOR_REQUEST,
		HANDLING_REQUEST,
		CONNECTING_WITH_CLIENT;
	}
	
	private Status _currentStatus = Status.UNINITIALIZED; // -1 = uninitialized, 0 = running and looking for request, 1 = waiting for request, 2 = handling request, 3 = connecting to client   
	
	public RequestHandler(LinkedList<Socket> requestQueue, ReentrantLock requestLock) {
		_requestQueue = requestQueue;
		_requestLock = requestLock;
	}
	
	public void run(){
		Socket currentRequest; 
		while(_isRunning){
			// Set status to LOOKING_FOR_REQUEST
			DebugLog(Thread.currentThread().getName()+": setting status to LOOKING_FOR_REQUEST");
			_currentStatus = Status.LOOKING_FOR_REQUEST;
			
			DebugLog(Thread.currentThread().getName()+": status set to LOOKING_FOR_REQUEST, acquiring lock");
			_requestLock.lock();
			
			DebugLog(Thread.currentThread().getName()+": Lock acquired, looking for request");
			if (!_requestQueue.isEmpty()){
				// Queue isn't empty, retrieve request and release the lock
				DebugLog(Thread.currentThread().getName()+": request found, setting status to FOUND_REQUEST");
				_currentStatus = Status.FOUND_REQUEST;
				DebugLog(Thread.currentThread().getName()+": status set to FOUND_REQUEST, retrieving request");
				currentRequest = _requestQueue.pop();
				DebugLog(Thread.currentThread().getName()+": request retrieved, releasing lock");
				_requestLock.unlock();
				
				// Handle the request
				DebugLog(Thread.currentThread().getName()+": Lock released, setting status to HANDLING_REQUEST");
				_currentStatus = Status.HANDLING_REQUEST;
				DebugLog(Thread.currentThread().getName()+": status set to HANDLING_REQUEST, handling request");
				handleRequest(currentRequest);
			}
			else {
				// Queue was empty, release the lock
				DebugLog(Thread.currentThread().getName()+": no request found, releasing lock");
				_requestLock.unlock();
				
				// Wait for request to come in on request monitor
				DebugLog(Thread.currentThread().getName()+": lock released, setting status to WAITING_FOR_REQUEST");
				_currentStatus = Status.WAITING_FOR_REQUEST;
				DebugLog(Thread.currentThread().getName()+": status set to WAITING_FOR_REQUEST, waiting on request queue");
				synchronized (_requestQueue)
				{
				try { _requestQueue.wait(); } catch (InterruptedException e) { }
				}
				
				// Once we are notified, we go back and look for the request in the queue
				DebugLog(Thread.currentThread().getName()+": notified from request queue, repeating loop");
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
		
		_currentStatus = Status.CONNECTING_WITH_CLIENT;
		
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
	
	private void DebugLog(String s)
	{
		if (false)
		{
			System.out.println(s);
		}
	}
	
	public String getStatus(){
		switch (_currentStatus){
		case UNINITIALIZED: return("Handler is unstarted");
		case LOOKING_FOR_REQUEST: return("Handler is looking for request");
		case FOUND_REQUEST: return("Handler found request");
		case WAITING_FOR_REQUEST: return("Handler is waiting for request");
		case HANDLING_REQUEST: return("Handler is handling request");
		case CONNECTING_WITH_CLIENT: return("Handler is connecting with client");
		default: return("Handler is in state: " + _currentStatus.toString());
		}
	}
	
	public void shutdown(){
		_isRunning = false;
	}


}
