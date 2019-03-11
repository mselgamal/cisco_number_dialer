package cisco_number_dialer.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import javax.telephony.InvalidArgumentException;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.Provider;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;

import com.cisco.jtapi.CallImpl;
import com.cisco.jtapi.extensions.CiscoAddress;

/**
 * this class executes the logic and initiates
 * necessary objects to test n number of calls
 * 
 * @author Mamdouh Elgamal
 *
 */

public class Script {
	protected ScriptData data;
	private Scanner sc;
	private static String promptForUsername = "Enter api Username: ";
	private static String promptForPasswd =  "Enter api Passwd: ";
	private static String promptForHost = "Enter Host: ";
	private static String promptForCalledNum = "Enter Calling Number: ";
	private static String promptForNumFile = "Enter file path for dailed numbers"
			+ " (/src/etc/etc/num.txt or C:\\user\\melgamal\\etc\\etc\num.txt : ";
	private MyAddressObserver addressObserver;
	private MyCallObserver callObserver;
	private AddrStatus addrStatus;
	private ProvStatus provStatus;
	private CallStatus callStatus;
	private CiscoAddress address;
	private Provider provider;
	private Terminal terminal;
	private CallImpl call;
	private static HashMap<String,String> failedDst = new HashMap<String,String>();
	private CallDurationMonitor callDuration;
	private List<Thread> threads;
	private int DELAY = 1500;
	
	public Script() {
		data = new ScriptData();
		sc = new Scanner(System.in);
		this.addrStatus = new AddrStatus();
		this.provStatus = new ProvStatus();
		this.threads = new ArrayList<Thread>();
	}
	
	private boolean validInput(String input) {
		if (input.length() == 0)
			return false;
		return true;
	}
	
	private void askForUsername() {
		System.out.print(Script.promptForUsername);
		String username = sc.nextLine();
		if (!validInput(username))
			askForUsername();
		this.data.setUsername(username);
	}
	
	private void askForHost() {
		System.out.print(Script.promptForHost);
		String host = sc.nextLine();
		if (!validInput(host))
			askForHost();
		this.data.setHost(host);
	}
	
	private void askForPasswd() {
		System.out.print(Script.promptForPasswd);
		String passwd = sc.nextLine();
		if (!validInput(passwd))
			askForPasswd();
		data.setPasswd(passwd);
	}
	
	private void askForCallingNum() {
		System.out.print(Script.promptForCalledNum);
		String num = sc.nextLine();
		if (!validInput(num))
			askForCallingNum();
		this.data.setCallingNum(num);
	}
	
	private File promptForFilename() {
		System.out.print(Script.promptForNumFile);
		String filename = sc.nextLine();
		File file = new File(filename);
		if (file.exists())
			return file;
		return promptForFilename();
	}
	
	private void collectCalledNums(Queue<String> numbers, File file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null)
				numbers.add(line);
		} catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
			System.exit(0);
		}	
	}
	
	private void exit() {
		sc.close();
		if (this.provider != null)
			this.provider.shutdown();
		if (this.addrStatus != null)
			this.addrStatus.waitForOOS();
		if (this.provStatus != null)
			this.provStatus.waitForOOS();
		System.out.println("## Terminating Helper Threads... ##");
		for (int i = 0; i < this.threads.size() ;i++) {
			try {
				this.threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void reset() {
		this.callStatus.waitForOOS();
		this.address.removeObserver(this.address.getObservers()[0]);
		this.address.removeCallObserver(this.address.getCallObservers()[0]);
		this.addrStatus.waitForOOS();
	}
	
	private void initTestObjects() throws JtapiPeerUnavailableException, 
	ResourceUnavailableException, MethodNotSupportedException {
		String url = data.getHost() + ";"+"login=" + data.getUsername() + ";passwd=" + data.getPasswd();
		JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(null);
		System.out.println("## JTAPI Peer Initialzed ##");
		this.provider = peer.getProvider(url);
		System.out.println("## Provider Initialzed ##");
		MyProviderObserver providerObserver = new MyProviderObserver(this.provStatus);
		this.provider.addObserver(providerObserver);
		this.provStatus.waitForInService();
	}
	
	private void initAddressObjects() throws ResourceUnavailableException, 
	MethodNotSupportedException, IOException, InvalidArgumentException {	
		this.addressObserver = new MyAddressObserver(this.addrStatus);
		this.address.addObserver(this.addressObserver);
		this.callObserver = new MyCallObserver();
		this.address.addCallObserver(this.callObserver);
		this.setTerminal();
		this.addrStatus.waitForInService();
	}
	
	private void setTerminal() {
		Terminal[] terminals = this.address.getInServiceAddrTerminals();
		for (Terminal t : terminals) {
			if (!this.address.isRestricted(t)) {
				this.terminal = t;
				break;
			}
		}
		if (this.terminal == null) {
			System.out.println("Error: The calling number "+this.address.getName()+
					" has restricted or unregistered terminals, terminating");
			this.exit();
		} else {
			System.out.println("\n## Phone Used For testing --> " + this.terminal+" ##");
		}
	}
	
	/*
	 * setup call status object
	 * setup call duration object
	 * assign call observer the call status Obj
	 * create a new thread to run() call duration
	 * create a call ()
	 * start call duration thread
	 * attempt to connect() call
	 * 		- connect() will cause threads to wait() until destination
	 * 		has been alerted.
	 * wait for call established
	 * wait for specified delay
	 * drop call
	 * wait for call to disconnect
	 * find out if call failed
	 * print call duration
	 */
	private void makeCall(String calledNum, String type) {
		double DELAY = 1.000d;
		Thread durationMonThread = null;
		try {
			System.out.println("--------------------------------");
			System.out.println("Attempting to call "+calledNum);
			
			this.callStatus = new CallStatus();
			this.callObserver.setCallStatus(this.callStatus);
			this.callDuration = new CallDurationMonitor(this.callStatus,type);
			durationMonThread = new Thread(this.callDuration);
			
			this.call = (CallImpl) this.provider.createCall();
			durationMonThread.start();
			this.call.connect(this.terminal, this.address, calledNum);
			
			this.callStatus.waitForEstablished();
			Thread.sleep((long)(DELAY*1000l));
			
			if (this.callStatus.isCallEstablished() || this.callStatus.isCallTimedout())
				this.call.drop();
			
			this.callStatus.waitForDisconnected();
			
			System.out.println("## Call Duration in secs (approx): " + String.format("%.2f", 
					(this.callDuration.duration + DELAY)) + " ##");
			
			if (this.callStatus.isCallTimedout()) {
				failedDst.put(calledNum, "Call Timedout.. duration -> " + 
			this.callDuration.duration +" secs, test manual");
			} else if (!this.callStatus.isCallSuccess()) {
				failedDst.put(calledNum, "Call Failed unkown cause, test manual");
			}
		} catch (Exception e) {
			String error = e.getMessage();
			if (this.callStatus.isCallFailed())
				error = "Call Failed to reach dst or establish a connection";
			failedDst.put(calledNum, error + " test manually");
		} finally {
			this.callStatus.waitForOOS();
			if (durationMonThread != null) {
				this.threads.add(durationMonThread);
			}
		}
	}
	
	private void printFailedCalls() {
		System.out.println("--------------------------------");
		System.out.println("## Failed Calls ##");
		Iterator<String> keys = failedDst.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			System.out.println(key+" "+failedDst.get(key));
		}
		System.out.println("--------------------------------");
	}
	
	/*
	 * get the current time
	 * while list of numbers is not empty
	 * 	retrieve a number
	 *  make a call
	 *  delay
	 * calculate and print script duration
	 */
	private void startTesting(Queue<String> numList) throws ResourceUnavailableException, 
	MethodNotSupportedException, IOException, InvalidArgumentException, InterruptedException {
		this.initAddressObjects();
		long start = System.currentTimeMillis(), end;
		double duration = 0;
		String strDuration = "", format = " secs";
		String[] line = null;
 		while (!numList.isEmpty()) {
			line = numList.poll().split(",");
			if (line.length == 2)
				makeCall(line[0],line[1]);
			else
				makeCall(line[0],"internal");
			Thread.sleep(this.DELAY);
		}
		end = System.currentTimeMillis();
		duration = (((double)end - (double)start) / 1000d);
		if (duration > 60) {
			duration /= 60d;
			format = " mins";
		}
		strDuration = String.format("%.2f",duration);
		System.out.println("--------------------------------");
		System.out.println("### Total Testing Time: " + strDuration + format + " ###");
		this.printFailedCalls();
		this.reset();
	}
	
	/*
	 * ask for relevant information
	 * initiate test objects
	 * start testing
	 */
	public void start() throws JtapiPeerUnavailableException, ResourceUnavailableException, 
	MethodNotSupportedException, IOException, InvalidArgumentException, InterruptedException {
		askForUsername();
		askForPasswd();
		askForHost();
		askForCallingNum();
		collectCalledNums(data.calledNumbers, promptForFilename());
		
		initTestObjects();
		this.address = (CiscoAddress) this.provider.getAddress(this.data.getCallingNum());
		this.startTesting(this.data.calledNumbers);
		
		this.exit();
	}
}
