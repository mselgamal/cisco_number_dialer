package cisco_number_dialer.src;

import javax.telephony.ProviderObserver;
import javax.telephony.events.ProvEv;
import javax.telephony.events.ProvInServiceEv;
import javax.telephony.events.ProvShutdownEv;

/**
 * this class attaches to an provider object and
 * is used to receive provider events from PBX
 * 
 * @author Mamdouh Elgamal
 *
 */

public class MyProviderObserver implements ProviderObserver{
	
	private ProvStatus status;
	public MyProviderObserver(ProvStatus status) {
		this.status = status;
	}
	@Override
	public void providerChangedEvent(ProvEv[] events) {	
		for (ProvEv event : events) {
			if (event instanceof ProvInServiceEv) {
				System.out.println("## Provider IN_SERVICE ##");
				this.status.inService();
			} else if (event instanceof ProvShutdownEv) {
				System.out.println("## Provider SHUTDOWN ##");
				this.status.shutdown();
			} else {
				System.out.println("## Provider OUT_OF_SERVICE ##");
				this.status.outOfService();
			}
		}
	}

}
