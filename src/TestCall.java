package cisco_number_dialer.src;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;

import com.cisco.jtapi.CallImpl;
import com.cisco.jtapi.extensions.CiscoAddress;

public class TestCall implements Runnable {
	private MyCallObserver callObserver;
	private CallStatus callStatus;
	private CallDurationMonitor callDuration;
	private CallImpl call;
	private CallQueuer queuer;
	private SupportObjects settings;
	private String type;
	private String calledNum;
	private Terminal terminal;
	private CiscoAddress address;
	private AddrStatus addrStatus;
	private MyAddressObserver addrObserver;
	protected StringBuffer log;
	
	protected TestCall(CallQueuer queuer, CiscoAddress address,
			String calledNum, String type) throws 
	ResourceUnavailableException, InvalidStateException, PrivilegeViolationException, 
	MethodNotSupportedException, InvalidArgumentException {
		this.log = new StringBuffer();
		this.queuer = queuer;
		this.address = address;
		this.settings = this.queuer.settings;
		this.calledNum = calledNum;
		this.type = type;
		
		this.addrStatus = new AddrStatus();
		this.callObserver = new MyCallObserver(this.log);
		this.addrObserver = new MyAddressObserver(this.addrStatus);
		
		this.address.addObserver(this.addrObserver);
		this.address.addCallObserver(this.callObserver);
		this.addrStatus.waitForInService();
		System.out.println("## Address IN_SERVICE ##");
		
		Terminal[] terminals = address.getInServiceAddrTerminals();
		for (Terminal t : terminals) {
			if (!address.isRestricted(t)) {
				this.terminal = t;
				break;
			}
		}
		if (terminal == null) {
			throw new IllegalArgumentException("Error: The calling number "+
					this.address.getName()+" has restricted or unregistered terminals, "
							+ "terminating");
		} else {
			System.out.println("## Device Used For testing --> " + this.terminal+" ##\n");
		}
	}
	
	public void setCalledNum(String calledNum) {
		this.calledNum = calledNum;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCalledNum() {
		return this.calledNum;
	}
	
	public String getCallingNum() {
		return this.address.getName();
	}
	
	private void cleanup() {
		this.callStatus.waitForOOS();
	}
	
	private void makeCall() {
		long delay = type.equals("media") ? SupportObjects.MEDIA_DELAY : 
			SupportObjects.EST_DELAY;
		Thread durationMonThread = null;
		try {
			log.append("--------------------------------\n");
			log.append("Attempting to call "+this.calledNum+"\n");
			
			this.callStatus = new CallStatus();
			this.callObserver.setCallStatus(this.callStatus);
			this.call = (CallImpl) this.settings.provider.createCall();
			this.callDuration = new CallDurationMonitor(this.callStatus,type);
			durationMonThread = new Thread(this.callDuration);
			
			durationMonThread.start();
			this.call.connect(this.terminal, this.address, 
					this.calledNum);
			
			this.callStatus.waitForEstablished();

			if (this.callStatus.isCallEstablished()) {
				Thread.sleep(delay);
				this.call.drop();
			} else if (this.callStatus.isCallTimedout()) {
				this.call.drop();
			}
			
			this.callStatus.waitForDisconnected();
			
			log.append("## Call Duration in secs (approx): " + String.format("%.2f", 
					(this.callDuration.duration)) + " ##\n");
			
			if (this.callStatus.isCallTimedout()) {
				SupportObjects.failedDst.put(calledNum, "Call Timedout.. duration -> " + 
			this.callDuration.duration +" secs, test manual");
			} else if (!this.callStatus.isCallSuccess()) {
				SupportObjects.failedDst.put(calledNum, "Call Failed unkown cause, test manual");
			}
			this.settings.helperThreads.add(durationMonThread);
		} catch (Exception e) {
			String error = e.getMessage();
			if (this.callStatus.isCallFailed())
				error = "Call Failed to reach dst or establish a connection";
			SupportObjects.failedDst.put(calledNum, error + " test manually");
		} finally {
			this.cleanup();
			System.out.println(this.log.toString());
			log.setLength(0);
			this.queuer.addToQueue(this);
		}
	}

	@Override
	public void run() {
		this.makeCall();
	}
}
