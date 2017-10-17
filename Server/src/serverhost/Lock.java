/* FIFO Spinlock for general use
 * Author: Ehab Mahran
 */

package serverhost;

import java.util.LinkedList;
import java.util.Queue;

public class Lock {
	


	boolean isLocked = false;
	Thread  lockedBy = null;
	int     lockedCount = 0;
	LinkedList<Thread> threadQueue = null;

	public Lock() {
		threadQueue = new LinkedList<Thread>();
	}


	public synchronized void acquire() {
		Thread callingThread = Thread.currentThread();
		if (isLocked && lockedBy != callingThread)
			threadQueue.add(callingThread);
		while(isLocked && lockedBy != callingThread){
			try{
				callingThread.sleep(1000);
			} catch (InterruptedException e){
				
			}
		}
		isLocked = true;
		lockedCount++;
		lockedBy = callingThread;
	}


	public synchronized void release(){
		if(Thread.currentThread() == this.lockedBy){
			lockedCount--;
			if(lockedCount == 0){
				isLocked = false;
				if (!threadQueue.isEmpty())
					threadQueue.removeFirst().notify();
			}
		}
	}

}
