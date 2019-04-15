package cisco_number_dialer.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.telephony.InvalidArgumentException;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.Provider;
import javax.telephony.ResourceUnavailableException;

import com.cisco.jtapi.extensions.CiscoJtapiPeer;

public class SupportObjects {
	protected static final long MEDIA_DELAY = 60000l;
	protected static final long EST_DELAY = 1000l;
	protected ProvStatus provStatus;
	protected MyProviderObserver providerObserver;
	protected Provider provider;
	protected ScriptData data;
	private long startTime;
	protected List<Thread> helperThreads = 
			Collections.synchronizedList(new ArrayList<Thread>());
	protected static Map<String,String> failedDst = 
			Collections.synchronizedMap(new HashMap<String,String>());
	
	protected SupportObjects(ScriptData data) throws InvalidArgumentException, 
	ResourceUnavailableException, MethodNotSupportedException, JtapiPeerUnavailableException {
		this.data = data;
		this.provStatus = new ProvStatus();
		
		String url = data.getHost() + ";"+"login=" + data.getUsername() + ";passwd=" + data.getPasswd();
		CiscoJtapiPeer peer = (CiscoJtapiPeer) JtapiPeerFactory.getJtapiPeer(null);
		System.out.println("\n## JTAPI Peer Initialzed ##");
		this.provider = peer.getProvider(url);
		System.out.println("## Provider Initialzed ##");
		this.providerObserver = new MyProviderObserver(this.provStatus);
		this.provider.addObserver(this.providerObserver);
		this.provStatus.waitForInService();
		System.out.println();
	}
	
	protected void shutDownProvider() {
		if (this.provider != null) {
			this.provider.removeObserver(this.providerObserver);
			this.provStatus.waitForOOS();
			this.provider.shutdown();
		}
		
		while (!this.helperThreads.isEmpty()) {
			try {
				this.helperThreads.remove(0).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	protected void printFailedCalls() {
		System.out.println("--------------------------------");
		System.out.println("## Failed Calls ##");
		Iterator<String> keys = failedDst.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			System.out.println(key+" "+failedDst.get(key));
		}
		System.out.println("--------------------------------");
	}
	
	protected void startTimer() {
		this.startTime = System.currentTimeMillis();
	}
	
	protected void stopTimerAndPrint() {
		long end = 0l;
		double duration = 0;
		String strDuration = "", format = " secs";
		end = System.currentTimeMillis();
		duration = (((double)end - (double)this.startTime) / 1000d);
		if (duration > 60) {
			duration /= 60d;
			format = " mins";
		}
		strDuration = String.format("%.2f",duration);
		System.out.println("--------------------------------");
		System.out.println("### Total Testing Time: " + strDuration + format + " ###");
	}
}
