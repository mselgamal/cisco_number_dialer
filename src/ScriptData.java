package cisco_number_dialer.src;

import java.util.LinkedList;
import java.util.Queue;

/**
 * this class acts as a container for relevant 
 * script data
 * 
 * @author Mamdouh Elgamal
 *
 */

public class ScriptData {
	private String username;
	private String passwd;
	private String host;
	private String callingNum;
	protected Queue<String> calledNumbers;
	
	public ScriptData() {
		this.calledNumbers = new LinkedList<String>();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getCallingNum() {
		return callingNum;
	}

	public void setCallingNum(String callingNum) {
		this.callingNum = callingNum;
	}
	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
}
