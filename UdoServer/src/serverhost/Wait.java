package serverhost;

import java.util.LinkedList;

public class Wait {

	LinkedList<Thread> threadQueue = null;

	public Wait() {
		threadQueue = new LinkedList<Thread>();
	}

	public void sleep() {
		boolean awoken = false;
		Thread callingThread = Thread.currentThread();
		threadQueue.add(callingThread);
		while (!awoken){
			try{ callingThread.sleep(1000); } catch (InterruptedException e) { awoken = true; }
		}
	}


	public synchronized void wake(){
		if (!threadQueue.isEmpty())
			threadQueue.removeFirst().interrupt();
		
	}
}

