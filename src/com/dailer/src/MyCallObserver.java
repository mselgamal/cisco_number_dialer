package cisco_number_dialer.src;

import javax.telephony.Connection;
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
	private StringBuffer log;
	
	public MyCallObserver(StringBuffer log) {
		this.log = log;
	}
	
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
					log.append("Call Failed\n");
					this.status.callFailed();
					try {
						int state = event.getCall().getConnections()[0].getState();
						if (state != Connection.DISCONNECTED)
							event.getCall().getConnections()[0].disconnect();
					} catch (Exception e) {
						SupportObjects.failedDst.put(event.getCall().getConnections()[0].
								toString(), e.getLocalizedMessage());
					}
					break;
				case CallCtlConnUnknownEv.ID:
					log.append("Call Status Unkown\n");
					this.status.callUnkown();
					break;
				case CallCtlConnOfferedEv.ID:
					log.append("Call is offered\n");
					this.status.callOffered();
					break;
				case CallCtlTermConnDroppedEv.ID:
					log.append("Call Dropped\n");
					this.status.callDropped();
					break;
				case CallCtlConnInitiatedEv.ID:
					log.append("Call Initiated to destination\n");
					this.status.inService();
					this.status.callInitiated();
					break;
				case CallCtlConnDialingEv.ID:
					log.append("Dialing Destination\n");
					this.status.callDialed();
					break;
				case CallCtlConnAlertingEv.ID:
				case CallCtlConnNetworkAlertingEv.ID:
					log.append("Alerted Destination\n");
					this.status.callAlerted();
					break;
				case CallCtlConnNetworkReachedEv.ID:
					log.append("Reached Destination\n");
					this.status.callReached();
					break;
				case CallCtlConnEstablishedEv.ID:
					if (this.status.isCallAlerted() && this.status.isCallReached()) {
						log.append("Call Established\n");
						this.status.callEstablished();
					} else {
						log.append("Call Establishing\n");
					}
					break;
				case CallCtlConnDisconnectedEv.ID:
					log.append("Call Disconnected\n");
					this.status.callDisconnected();
					break;
			}
		} if (event instanceof CallObservationEndedEv) {
			this.status.outOfService();
		}
	}
}
