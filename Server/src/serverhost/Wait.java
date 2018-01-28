package serverhost;

import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class Wait {

	LinkedList<Thread> threadQueue = null;
	
	ReentrantLock lock = null;
	
	public Wait() {
		threadQueue = new LinkedList<Thread>();
		lock = new ReentrantLock();
	}

	public void sleep() {
		
		boolean awoken = false;
		Thread callingThread = Thread.currentThread();
		lock.lock();
		threadQueue.add(callingThread);
		lock.unlock();
		while (!awoken){
			try{ callingThread.sleep(1000); } catch (InterruptedException e) { awoken = true; }
		}
	}


	public synchronized void wake(){
		lock.lock();
		if (!threadQueue.isEmpty())
			threadQueue.removeFirst().interrupt();
		else
			System.out.println("All threads awake!");
		lock.unlock();
		
	}
}

