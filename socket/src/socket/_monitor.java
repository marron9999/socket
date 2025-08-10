package socket;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

public class _monitor extends _base {

	protected ServerSocket ssocket; 
	protected Socket socket;
	
	private static Process javaw(Class<?> cls, String[] args) {
		return exec("javaw.exe", cls, args);
	}
	//public static Process java(Class<?> cls, String[] args) {
	//	return exec("java.exe", cls, args);
	//}
	private static Process exec(String exe, Class<?> cls, String[] args) {
		try {
		    ArrayList<String> cmd = new ArrayList<>();
		    cmd.add("cmd.exe"); cmd.add("/c");
		    cmd.add(System.getProperty("java.home") + "\\bin\\" + exe);
		    cmd.add("-cp"); cmd.add(System.getProperty("java.class.path"));
		    cmd.add(cls.getTypeName());
		    for(int i=0; i<args.length; i++) cmd.add(args[i]);
		    return Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));
	    } catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void accept(int port) throws Exception {
		ssocket = new ServerSocket(port);
		socket = ssocket.accept();
	}
	private void close() {
		try { socket.close(); } catch (Exception e) { }
		try { ssocket.close(); } catch (Exception e) { }
	}
	
	public static void main(String[] args) {
		//main(_monitor.class, args);
		try {
		//	String name = cls.getTypeName();
		//	Object obj = cls.getDeclaredConstructor().newInstance();
			Thread.currentThread().setName("monitor");
		//	((_monitor)obj).run(args);
			new _monitor().run(args);
			Thread.currentThread().setName("-");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//protected static void main(Class<?> cls, String[] args) {
	//	try {
	//	    String name = cls.getTypeName();
	//	    Object obj = cls.getDeclaredConstructor().newInstance();
	//		Thread.currentThread().setName(name);
	//	    ((_monitor)obj).run(args);
	//		Thread.currentThread().setName("-");
	//	} catch (Exception e) {
	//		e.printStackTrace();
	//	}
	//}

	public static Socket open(String text) {
		try {
			Socket socket = new Socket();
		    socket.bind(null);
			int port = socket.getLocalPort();
		    socket.close();
		    javaw(_monitor.class, new String[] { text, "" + port });
		    socket = null;
			int c = 0;
			while (socket == null) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// NONE
				}
				try {
					socket = new Socket("localhost", port);
				} catch (Exception e) {
					//e.printStackTrace();
					socket = null;
					if(++c > 10) break;
				}
			}
			return socket;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void run(String[] args) throws Exception {
		//System.out.println("start");
		int port = 9999;
		try { port = Integer.parseInt(args[1]); } catch (Exception e) { }
		_console console = _console.INSTANCE;
		console.alloc(args[0], true);
		accept(port);
		try {
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "ms932");
			int len;
			char[] buf = new char[4096];
			while ((len = isr.read(buf)) >= 0) {
				if(len == 0) continue;
				console.write(buf, len);
			}
			isr.close();
		} catch (Exception e) {
			// NONE
		}
		close();
		//System.out.println("stop");
	}
}
