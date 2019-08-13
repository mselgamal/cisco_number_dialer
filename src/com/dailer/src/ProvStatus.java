package cisco_number_dialer.src;

/**
 * this class notifies threads
 * of different provider events
 * 
 * @author Mamdouh Elgamal
 *
 */

public class ProvStatus extends Status{
	private boolean provShutdown = false;
	
	public ProvStatus() {
		super();
	}
	
	public synchronized void shutdown() {
		this.provShutdown = true;
		this.notifyAll();
	}
	
	public synchronized void waitForShutdown() {
		while (!this.provShutdown) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
