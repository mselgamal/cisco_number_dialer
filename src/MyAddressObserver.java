package cisco_number_dialer.src;

import javax.telephony.AddressObserver;
import javax.telephony.events.AddrEv;
import javax.telephony.events.AddrObservationEndedEv;

import com.cisco.jtapi.extensions.CiscoAddrEv;
import com.cisco.jtapi.extensions.CiscoAddrInServiceEv;
import com.cisco.jtapi.extensions.CiscoAddrOutOfServiceEv;

/**
 * this class attaches to an address object and
 * is used to receive address events from PBX
 * 
 * @author Mamdouh Elgamal
 *
 */

public class MyAddressObserver implements AddressObserver{
	
	private AddrStatus status;
	public MyAddressObserver (AddrStatus status) {
		this.status = status;
	}

	@Override
	public void addressChangedEvent(AddrEv[] events) {	
		for (AddrEv event : events) {
			if (event instanceof CiscoAddrEv) {
				CiscoAddrEv ciscoAddrEv = (CiscoAddrEv) event;
				if (ciscoAddrEv instanceof CiscoAddrInServiceEv) {
					this.status.inService();
				} else if (ciscoAddrEv instanceof CiscoAddrOutOfServiceEv) {
					this.status.outOfService();
				}
			} else if (event instanceof AddrObservationEndedEv) {
				this.status.outOfService();
			} else {
				System.out.println("Warning: Unkown Address Event recieved.. "+event+" terminating");
			}
		}
	}

}
