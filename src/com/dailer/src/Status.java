package cisco_number_dialer.src;

/**
 * this class is a parent for any 
 * type of status
 * 
 * @author Mamdouh Elgamal
 *
 */

public class Status {
	private boolean OOS = true;
	
	public synchronized void inService() {
		OOS = false;
		this.notifyAll();
	}
	
	public synchronized void outOfService() {
		OOS = true;
		this.notifyAll();
	}
	
	public synchronized void waitForInService() {
		while (OOS) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForOOS() {
		while (!OOS) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
