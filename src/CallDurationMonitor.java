package cisco_number_dialer.src;

/**
 * this is a class that calculates the running
 * time of a call
 * 
 * @author Mamdouh Elgamal
 *
 */

public class CallDurationMonitor implements Runnable {
	
	private CallStatus status;
	protected double duration;
	private long startTime;
	private long endTime;
	private double maxDuration = 25d;
	
	public CallDurationMonitor(CallStatus status, String type) {
		this.status = status;
		if (type.equals("external"))
			maxDuration = 11d;
		else if(type.equals("media"))
			maxDuration = 120d;
	}
	@Override
	public void run() {
		this.startTime = System.currentTimeMillis();
		while (!this.status.isCallDisconnected()) {
			this.endTime = System.currentTimeMillis();
			this.duration = (((double)this.endTime) - ((double)this.startTime)) / 1000.000d;
			if (Double.compare(this.duration, this.maxDuration) > 0) {
				this.status.callTimedout();
				break;
			}	
		}
	}

}
