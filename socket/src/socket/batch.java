package socket;

public class batch extends client {

	private String[] mess;

	public batch() {
		this(true);
	}
	public batch(boolean _con) {
		super("batch.log");
	}

	@Override
	protected void client_reciver_println(String mess) {
		log_time();
		log_println(mess);
	}

	
	public void run(String host, int port, String[] mess) throws Exception {
		log_debug("batch", "- Start -");
		this.mess = mess;
		
		run(host, port);

		log_debug("batch" ,"- ended -");
	}

	@Override
	protected void dispatch() throws Exception {
		reciver.send("@start\n");
		wait_prompt();
		for(int i=0; i<mess.length; i++) {
			send_cmd(reciver, mess[i]);
		}
		try { send_cmd(reciver, "exit"); } catch (Exception e) { }
	}

	private void send_cmd(_reciver reciver, String mess) {
		log_debug("batch", mess);
		reciver.send(mess + "\n");
		wait_prompt();
	}

	private void wait_prompt() {
		while( ! bPrompt) {
			sleep(100);
		}
		sleep(100);
		bPrompt = false;
	}
}
