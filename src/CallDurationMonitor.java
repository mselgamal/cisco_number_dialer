package cisco_number_dialer.src;

/**
 * This class calculates the running
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
	
	/**
	 * Specify max call duration based on call type
	 * 
	 * @param status
	 * @param type
	 */
	public CallDurationMonitor(CallStatus status, String type) {
		this.status = status;
		if (type.equals("external"))
			maxDuration = 11d;
		else if(type.equals("media"))
			maxDuration = 120d;
	}
	
	/**
	 * while call is active, calculate call duration
	 * terminate call when duration >= max duration
	 * 
	 */
	@Override
	public void run() {
		this.startTime = System.currentTimeMillis();
		while (!this.status.isCallDisconnected()) {
			this.endTime = System.currentTimeMillis();
			this.duration = (((double)this.endTime) - ((double)this.startTime)) / 1000.000d;
			if (Double.compare(this.duration, this.maxDuration) > 0) {
				if (!this.status.isCallDelivered()) 
					this.status.callTimedout();
				else
					this.status.callNetworkDelivered();
				break;
			}	
		}
	}

}
