package cisco_number_dialer.src;

public class Driver {

	public synchronized static void main(String[] args) throws InterruptedException {
		Script script = new Script();
		script.start();
	}

}
