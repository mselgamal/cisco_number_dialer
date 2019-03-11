package cisco_number_dialer.src;

import java.io.IOException;

import javax.telephony.InvalidArgumentException;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ResourceUnavailableException;

public class Driver {

	public synchronized static void main(String[] args) {
		Script script = new Script();
		try {
			script.start();
		} catch (JtapiPeerUnavailableException | ResourceUnavailableException 
				| MethodNotSupportedException
				| IOException | InvalidArgumentException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
