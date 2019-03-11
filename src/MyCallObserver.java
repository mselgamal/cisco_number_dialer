package cisco_number_dialer.src;

import javax.telephony.callcontrol.CallControlCallObserver;
import javax.telephony.callcontrol.events.CallCtlConnAlertingEv;
import javax.telephony.callcontrol.events.CallCtlConnDialingEv;
import javax.telephony.callcontrol.events.CallCtlConnDisconnectedEv;
import javax.telephony.callcontrol.events.CallCtlConnEstablishedEv;
import javax.telephony.callcontrol.events.CallCtlConnFailedEv;
import javax.telephony.callcontrol.events.CallCtlConnInitiatedEv;
import javax.telephony.callcontrol.events.CallCtlConnNetworkAlertingEv;
import javax.telephony.callcontrol.events.CallCtlConnNetworkReachedEv;
import javax.telephony.callcontrol.events.CallCtlConnOfferedEv;
import javax.telephony.callcontrol.events.CallCtlConnUnknownEv;
import javax.telephony.callcontrol.events.CallCtlTermConnDroppedEv;
import javax.telephony.events.CallEv;
import javax.telephony.events.CallObservationEndedEv;
import javax.telephony.events.ConnEv;

/**
 * this class attaches to an address object and
 * is used to receive call events from PBX
 * 
 * @author Mamdouh Elgamal
 *
 */

public class MyCallObserver implements CallControlCallObserver{
	
	private CallStatus status;
	
	public void setCallStatus(CallStatus status) {
		this.status = status;
	}
	
	@Override
	public void callChangedEvent(CallEv[] events) {
		for (CallEv event : events) {
			this.handleEvent(event);
		}
	}
	
	private void handleEvent(CallEv event) {
		if (event instanceof ConnEv) {
			switch(event.getID()) {
				case CallCtlConnFailedEv.ID:
					System.out.println("Call Failed");
					this.status.callFailed();
					break;
				case CallCtlConnUnknownEv.ID:
					System.out.println("Call Status Unkown");
					this.status.callUnkown();
					break;
				case CallCtlConnOfferedEv.ID:
					System.out.println("Call is offered");
					this.status.callOffered();
					break;
				case CallCtlTermConnDroppedEv.ID:
					System.out.println("Call Dropped");
					this.status.callDropped();
					break;
				case CallCtlConnInitiatedEv.ID:
					System.out.println("Call Initiated to destination");
					this.status.inService();
					this.status.callInitiated();
					break;
				case CallCtlConnDialingEv.ID:
					System.out.println("Dialing Destination");
					this.status.callDialed();
					break;
				case CallCtlConnAlertingEv.ID:
				case CallCtlConnNetworkAlertingEv.ID:
					System.out.println("Alerted Destination");
					this.status.callAlerted();
					break;
				case CallCtlConnNetworkReachedEv.ID:
					System.out.println("Reached Destination");
					this.status.callReached();
					break;
				case CallCtlConnEstablishedEv.ID:
					if (this.status.isCallAlerted() && this.status.isCallReached()) {
						System.out.println("Call Established");
						this.status.callEstablished();
					} else {
						System.out.println("Call Establishing");
					}
					break;
				case CallCtlConnDisconnectedEv.ID:
					System.out.println("Call Disconnected");
					this.status.callDisconnected();
					break;
			}
		} if (event instanceof CallObservationEndedEv) {
			this.status.outOfService();
		}
	}
}
