package socket;

import java.util.ArrayList;

import sysmon.app;
import sysmon.info;

public class _appmon extends _runnable {

	private _socket socket;

	public boolean stop = false;
	public String mask = "";

	protected void appmon_debug(String mess) {
		mess = log.log_debug("appmon", thread_name() + mess);
		System.out.println(mess);
	}
	
	public _appmon() {
		super(null);
	}

	public _appmon(_socket socket, _logger log) {
		super(log);
		this.socket = socket;
		name("appmon", socket);
	}

	@Override
	protected int onClose() {
		appmon_debug("- onClose -");
		return super.onClose();
	}
	
	protected void close() {
		appmon_debug("- close -");
		stop = true;
	}

	private void write(info info) {
		ArrayList<String> list = new ArrayList<>();
		info.get(list);
		if(list.size() > 0) {
			synchronized (socket) {
				byte[] b = new byte[2+4];
				b[0] = 0x1d;
				b[1] = 0x02;
				for(String t : list) {
					try {
						byte[] d = t.getBytes("MS932");
						itobuf(b, 2, d.length, 4);
						socket.write(b);
						socket.write(d);
					} catch (Exception e) {
						// NONE: handle exception
					}
				}
			}
		}
	}
	
	@Override
	public void run() {
		appmon_debug("- start -");
		onConn(null, "appmon");
		//String host = System.getenv("COMPUTERNAME");
		//bthlib.DefProfile(host);
		//String mask = bthlib.GetProfile("appmon", "mask");
		app app = new app(mask);
		while( ! stop ) {
			try { Thread.sleep(1000); } catch (Exception e) { }

			if(stop) break;
			app.get();
			write(app);
		}
		onClose();
		appmon_debug("- ended -");
	}
}
