package cisco_number_dialer.src;

/**
 * this class notifies threads
 * of different connection stages in a call
 * 
 * @author Mamdouh Elgamal
 *
 */

public class CallStatus extends Status{
	
	private boolean initiated = false;
	private boolean dialed = false;
	private boolean alerted = false;
	private boolean networkDelivered = false;
	private boolean offered = false;
	private boolean reached = false;
	private boolean established = false;
	private boolean disconnected = false;
	private boolean failed = false;
	private boolean dropped = false;
	private boolean unkown = false;
	private boolean timeout = false;
	
	public CallStatus() {
		super();
	}
	
	public boolean isCallSuccess() {
		return this.initiated && this.dialed && this.reached
				&& this.alerted && this.disconnected
				&& !this.failed && !this.dropped && !this.timeout;
	}
	
	public boolean isCallEstablished() {
		return this.established;
	}
	
	public boolean isCallDisconnected() {
		return this.disconnected;
	}
	
	public boolean isCallFailed() {
		return this.failed;
	}
	
	public boolean isCallAlerted() {
		return this.alerted;
	}
	
	public boolean isCallReached() {
		return this.reached;
	}
	
	public boolean isCallDelivered() {
		return this.reached && this.alerted 
				&& !this.failed && !this.disconnected && !this.unkown;
	}
	
	public boolean isCallTimedout() {
		return this.timeout;
	}
	
	public synchronized void callNetworkDelivered() {
		this.networkDelivered = true;
		this.notifyAll();
	}
	
	public synchronized void callTimedout() {
		this.timeout = true;
		this.notifyAll();
	}
	
	public synchronized void callInitiated() {
		this.initiated = true;
	}
	
	public synchronized void callDialed() {
		this.dialed = true;
	}
	
	public synchronized void callAlerted() {
		this.alerted = true;
	}
	
	public synchronized void callReached() {
		this.reached = true;
	}
	
	public synchronized void callOffered() {
		this.offered = true;
		this.notifyAll();
	}
	
	public synchronized void callEstablished() {
		this.established = true;
		this.notifyAll();
	}
	
	public synchronized void callDisconnected() {
		this.disconnected = true;
		this.notifyAll();
	}
	
	public synchronized void callFailed() {
		this.failed = true;
		this.notifyAll();
	}
	
	public synchronized void callDropped() {
		this.dropped = true;
		this.notifyAll();
	}
	
	public synchronized void callUnkown() {
		this.unkown = true;
		this.notifyAll();
	}
	
	private boolean isCallActive() {
		return !this.failed && !this.dropped && !this.unkown
				&& !this.timeout && !this.disconnected;
	}
	
	public synchronized void waitForOffered() {
		while (this.isCallActive() && !this.offered) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForEstablished() {
		while (this.isCallActive() && (!this.established || !this.networkDelivered)) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForDisconnected() {
		while (!this.disconnected) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForFailed() {
		while (!this.failed) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForDropped() {
		while (!this.dropped) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void waitForUnkown() {
		while (!this.unkown) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
