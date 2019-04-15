package cisco_number_dialer.src;

import java.util.ArrayList;
import java.util.List;

/**
 * this class acts as a container for relevant 
 * script data
 * 
 * @author Mamdouh Elgamal
 *
 */

public class ScriptData {
	public int threadCount;
	public int trueThreadCount;
	public final int MAX_CC = 45;
	public final int MIN_CC = 1;
	private String username;
	private String passwd;
	private String host;
	private String callingNum;
	protected List<String> calledNumbers;
	protected List<String> callingNumbers;
	
	public ScriptData() {
		this.calledNumbers = new ArrayList<String>();
		this.callingNumbers = new ArrayList<String>();
	}
	
	public void setCallingNums(int start) {
		trueThreadCount = threadCount <= this.calledNumbers.size() 
				? threadCount : this.calledNumbers.size(); 
		for (int i = 0; i < trueThreadCount; i++) {
			this.callingNumbers.add(String.valueOf(start));
			start++;
		}
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
