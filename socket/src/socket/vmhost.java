package socket;

//import java.net.Socket;
import java.util.HashMap;

public class vmhost {
	private static HashMap<String, Object> soc = new HashMap<>();

	protected static void vmhost_debug(_logger log, String mess) {
		if(log != null) {
			mess = log.log_debug("vmhost", mess);
		} else {
			mess = _logger.log_debug_text("vmhost", mess);
		}
		System.out.println(mess);
	}
	
	public static _socket get(String _host, int _port, _logger _log) {
		final _logger log = _log; 
		final String host = _host.toUpperCase();
		final int port = _port;
		_socket s = null;
		try {
			Object o = null;
			synchronized (soc) {
				o = soc.get(host + port);
			}
			if(o != null) {
				if(o instanceof _socket) {
					s = (_socket) o;
				}
			} 
			if(s != null) {
				vmhost_debug(log, "get " + host + ":" + port);
				synchronized (soc) {
					soc.remove(host + port);
				}
				add(host, port, log);
			} else {
				vmhost_debug(log, "new " + host + ":" + port);
				s = new _socket(host, port);
			}
		} catch (Exception e) {
			// NONE
		}
		return s;
	}

	public static void add(String _host, int _port, _logger _log) {
		final _logger log = _log; 
		final String host = _host.toUpperCase();
		final int port = _port;
		Object o = null;
		synchronized (soc) {
			o = soc.get(host + port);
			if(o == null) {
				soc.put(host + port, new Object());
			}
		}
		if(o == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					//vmhost_debug(log, "add " + host + ":" + port);
					try {
						_socket s = new _socket(host, port);
						synchronized (soc) {
							soc.put(host + port, s);
						}
						vmhost_debug(log, "add " + host + ":" + port);
					} catch (Exception e) {
						// NONE
					}
				}
			}).start();
		}
	}
}
