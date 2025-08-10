package socket;

public class bserver {
	private static String PC = System.getenv("COMPUTERNAME").toLowerCase();
	private static _server _net;

	public static void stop() {
		_net.server_close();
	}

	public static void main(String[] args) {
		try {
			int port = 9999;
			int i = 0;
			try {
				if (args.length > 0) {
					port = Integer.parseInt(args[i]);
					i++;
				}
			} catch (Exception e) {
				// NONE
			}
			_net = new _server(PC, port);
			Thread net = new Thread(_net);
			net.start();
			net.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
