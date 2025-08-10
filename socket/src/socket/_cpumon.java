package socket;

import sysmon.cpulib;

public class _cpumon extends _runnable {

	private _socket socket;

	public boolean stop = false;

	protected void monsys_debug(String mess) {
		mess = log.log_debug("cpumon", thread_name() + mess);
		System.out.println(mess);
	}
	
	public _cpumon(_socket socket, _logger log) {
		super(log);
		this.socket = socket;
		name("cpumon", socket);
	}

	@Override
	protected int onClose() {
		monsys_debug("- onClose -");
		return super.onClose();
	}
	
	protected void close() {
		monsys_debug("- close -");
		stop = true;
	}

	private void write(cpulib info) {
		int[] u = info.get();
		synchronized (socket) {
			try {
				byte[] b = new byte[2+4];
				b[0] = 0x1d;
				b[1] = 0x00;
				itobuf(b, 2, u[0], 4);
				socket.write(b);
			} catch (Exception e) {
				// NONE
			}
		}
	}
	
	@Override
	public void run() {
		monsys_debug("- start -");
		onConn(null, "cpumon");
		cpulib cpu = new cpulib();
		write(cpu);
		while( ! stop ) {
			try { Thread.sleep(1000); } catch (Exception e) { }

			if(stop) break;
			write(cpu);
		}
		onClose();
		monsys_debug("- ended -");
	}
}
