package cisco_number_dialer.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;

import com.cisco.jtapi.extensions.CiscoAddress;

public class CallQueuer {
	protected SupportObjects settings;
	private Queue<TestCall> queue;
	private List<CiscoAddress> addresses;
	private List<String> calledNumbers;
	private Thread[] testCallsThreads;
	private int counter;
	private HashMap<String,Integer> testCallPos;
	
	protected CallQueuer(ScriptData data) throws InvalidArgumentException, 
	ResourceUnavailableException, MethodNotSupportedException, JtapiPeerUnavailableException, 
	InvalidStateException, PrivilegeViolationException {
		this.settings = new SupportObjects(data);
		this.queue = new LinkedList<TestCall>();
		this.addresses = new ArrayList<CiscoAddress>();
		this.calledNumbers = this.settings.data.calledNumbers;
		this.testCallPos = new HashMap<String,Integer>();
		this.testCallsThreads = new Thread[this.settings.data.trueThreadCount];
		this.counter = this.calledNumbers.size();
		
		for (int i = 0; i < this.settings.data.callingNumbers.size() ;i++) {
			String callingNum = this.settings.data.callingNumbers.get(i);
			CiscoAddress address = (CiscoAddress) this.settings.provider.getAddress(callingNum);
			this.addresses.add(address);
		}
		
		for (int i = 0; i < this.settings.data.trueThreadCount ;i++) {
			String[] line = this.getCalledNumAndType(this.calledNumbers.remove(0));
			this.queue.add(new TestCall(this,this.addresses.get(i),line[0],line[1]));
		}
		
		for (int i = 0; i < this.testCallsThreads.length ;i++) {
			TestCall testCall = this.queue.poll();
			this.testCallsThreads[i] = new Thread(testCall);
			this.testCallPos.put(testCall.getCallingNum(), i);
		}
		
		this.settings.startTimer();
		for (Thread t: this.testCallsThreads) {
			t.start();
		}
	}
	
	private String[] getCalledNumAndType(String line) {
		String[] data = line.split(",");
		String[] result = new String[2];
		result[0] = data[0];
		String type = "internal";
		if (data.length >= 2) 
			type = data[1];
		result[1] = type;
		
		return result;
	}
	
	protected synchronized void proccessCalls() throws InterruptedException {
		while (!this.calledNumbers.isEmpty()) {
			if (!this.queue.isEmpty()) {
				TestCall testCall = this.queue.poll();
				int pos = this.testCallPos.get(testCall.getCallingNum());
				try {
					this.testCallsThreads[pos].join();
					String[] line = this.getCalledNumAndType(this.calledNumbers.remove(0));
					testCall.setCalledNum(line[0]);
					testCall.setType(line[1]);
					this.testCallsThreads[pos] = new Thread(testCall);
					this.testCallsThreads[pos].start();
				} catch (InterruptedException e) {
					SupportObjects.failedDst.put(testCall.getCalledNum(), 
							e.fillInStackTrace().toString());
				}
			} else {
				this.wait();
			}
		}
		
		while (this.counter != 0) this.wait();
		this.settings.stopTimerAndPrint();
		this.settings.printFailedCalls();
		this.settings.shutDownProvider();
		for (int i = 0; i < this.addresses.size() ;i++) {
			CiscoAddress address = this.addresses.get(i);
			address.removeCallObserver(address.getCallObservers()[0]);
			address.removeObserver(address.getObservers()[0]);
		}
	}
	
	protected synchronized void addToQueue(TestCall testCall) {
		this.queue.add(testCall);
		this.counter--;
		this.notifyAll();
	}
}
