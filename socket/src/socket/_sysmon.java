package socket;

import java.util.ArrayList;

import sysmon.cpu;
import sysmon.drv;
import sysmon.dsp;
import sysmon.info;

public class _sysmon extends _runnable {

	private _socket socket;

	public boolean stop = false;

	protected void sysmon_debug(String mess) {
		mess = log.log_debug("sysmon", thread_name() + mess);
		System.out.println(mess);
	}
	
	public _sysmon(_socket socket, _logger log) {
		super(log);
		this.socket = socket;
		name("sysmon", socket);
	}

	@Override
	protected int onClose() {
		sysmon_debug("- onClose -");
		return super.onClose();
	}
	
	protected void close() {
		sysmon_debug("- close -");
		stop = true;
	}

	private void write(info info) {
		ArrayList<String> list = new ArrayList<>();
		info.get(list);
		if(list.size() > 0) {
			synchronized (socket) {
				byte[] b = new byte[2+4];
				b[0] = 0x1d;
				b[1] = 0x01;
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
		sysmon_debug("- start -");
		onConn(null, "sysmon");
		cpu cpu = new cpu();
		write(cpu);
		dsp dsp = new dsp();
		write(dsp);
		drv drv = new drv();
		write(drv);
		while( ! stop ) {
			try { Thread.sleep(1000); } catch (Exception e) { }

			if(stop) break;
			cpu.get();
			write(cpu);

			if(stop) break;
			dsp.get();
			write(dsp);

			if(stop) break;
			drv.get();
			write(drv);
		}
		onClose();
		sysmon_debug("- ended -");
	}
}
