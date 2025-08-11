package socket;

public class server {
	private static _server _net;
	private static _server _bth;

	public static void stop() {
		_net.server_close();
		_bth.server_close();
	}
	public static void main(String[] args) {
		try {
			int port = 9999;
			for(int i = 0; i<args.length; i++) {
				if(args[i].equalsIgnoreCase("/screen")
				|| args[i].equalsIgnoreCase("-screen")) {
					_command.screen = true;
					continue;
				}
				try { port = Integer.parseInt(args[i]); } catch (Exception e) { }
				//break;
			}
			_net = new _server("serv_net", port) {
				@Override
				protected void server_close() {
					super.server_close();
					if(_bth != null) {
						_server s = _bth;
						_bth = null;
						s.server_close();
					}
				}
			};
			_bth = new _server("serv_bth") {
				@Override
				protected void server_close() {
					super.server_close();
					if(_net != null) {
						_server s = _net;
						_net = null;
						s.server_close();
					}
				}
			};
			Thread net = new Thread(_net);
			Thread bth = new Thread(_bth);
			net.start();
			bth.start();
			bth.join();
			net.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
