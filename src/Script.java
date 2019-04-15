package cisco_number_dialer.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;

/**
 * this class executes the logic and initiates
 * necessary objects to test n number of calls
 * 
 * @author Mamdouh Elgamal
 *
 */

public class Script {
	private final int START_EXT = 3809002;
	protected ScriptData data;
	private Scanner sc;
	private static String promptThreadCount = "Enter # of Threads(1-45): ";
	private static String promptForUsername = "Enter api Username: ";
	private static String promptForPasswd =  "Enter api Passwd: ";
	private static String promptForHost = "Enter Host: ";
	private static String promptForNumFile = "Enter file path for dailed numbers"
			+ " (/src/etc/etc/num.txt or C:\\user\\melgamal\\etc\\etc\num.txt : ";

	public Script() {
		this.data = new ScriptData();
		this.sc = new Scanner(System.in);
	}
	
	private boolean validInput(String input) {
		if (input.length() == 0)
			return false;
		return true;
	}
	
	private String readLine(String prompt) {
		System.out.print(prompt);
		return sc.nextLine();
	}
	
	private void askForUsername() {
		String username = readLine(Script.promptForUsername);
		if (!validInput(username))
			askForUsername();
		this.data.setUsername(username);
	}
	
	private void askForHost() {
		String host = readLine(Script.promptForHost);
		if (!validInput(host))
			askForHost();
		this.data.setHost(host);
	}
	
	private void askForPasswd() {
		String passwd = readLine(Script.promptForPasswd);
		//String passwd = String.valueOf(passwdList);
		if (!validInput(passwd))
			askForPasswd();
		data.setPasswd(passwd);
	}
	
	private void askForThreadCount() {
		String num = readLine(Script.promptThreadCount);
		try {
			this.data.threadCount = Integer.parseInt(num);
			if (!(this.data.threadCount >= this.data.MIN_CC 
					&& this.data.threadCount <= this.data.MAX_CC))
				throw new Exception("Error: out of range");
			this.data.setCallingNums(START_EXT);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			askForThreadCount();
		}
	}
	
	private File promptForFilename() {
		String filename = readLine(Script.promptForNumFile);
		File file = new File(filename);
		if (file.exists())
			return file;
		return promptForFilename();
	}
	
	private void collectCalledNums(List<String> numbers, File file) {
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
	
	/*
	 * ask for relevant information
	 * initiate test objects
	 * start testing
	 */
	public void start() throws InterruptedException {
		askForUsername();
		askForPasswd();
		askForHost();
		collectCalledNums(this.data.calledNumbers, promptForFilename());
		askForThreadCount();
		try {
			CallQueuer queuer = new CallQueuer(this.data);
			queuer.proccessCalls();
		} catch (InvalidArgumentException | ResourceUnavailableException 
				| MethodNotSupportedException
				| JtapiPeerUnavailableException | InvalidStateException | 
				PrivilegeViolationException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
