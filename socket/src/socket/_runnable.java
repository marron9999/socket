package socket;

//import java.net.Socket;

public class _runnable extends _base implements Runnable {

	protected _logger log;
	
	public _runnable(_logger log) {
		super();
		this.log= log;
	}

	protected void name(String base, _socket socket) {
		String host = socket.getHostName();
		int port = socket.getPort();
		name = host + ":" + port + " " + base;
	}

	protected String thread_name() {
		return "[" + name + "]";
	}
	
	protected void exception(String func, Exception e) {
		log.log_debug(func, "");
		System.out.println(e.toString());
	}
	protected void debug(String func, String line) {
		log.log_debug(func, line);
	}
	protected void println(String line) {
		line = line.replace("\r", "").replace("\n", "");
		if(line.length() > 0) {
			System.out.println(line);
		}
	}
	//protected void trace(String line) {
	//	System.out.println(time() + thread_name() + line);
	//}

	protected int onConn(Object object, String remoteName) {
		Thread.currentThread().setName(name);
		//trace("[run]start....");
		return 0;
	}

	protected int onClose() {
		//trace("[run]ended");
		Thread.currentThread().setName("_");
		return 0;
	}

	@Override
	public void run() {
		onConn(null, "");
		onClose();
	}
}
