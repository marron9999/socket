package socket;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;

public class _screen {

	public static Socket open(String text) {
		try {
			Socket socket = new Socket();
		    socket.bind(null);
			int port = socket.getLocalPort();
		    socket.close();
		    String[] cmd = {
	    		"cmd.exe", "/c",
		    	System.getProperty("java.home") + "\\bin\\javaw.exe",
		    	"-cp", System.getProperty("java.class.path"), 
		    	_screen.class.getTypeName(), text, "" + port };
		    try {
				//System.out.println(String.join(" ", cmd));
			    Runtime.getRuntime().exec(cmd);
		    } catch (Exception e) {
				e.printStackTrace();
			}

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

	public static void main(String[] args) {
		try {
			new _screen().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public _screen() {
	}

	private void initConsole(String text) {
		Kernel32 K = Kernel32.INSTANCE;
		K.AllocConsole();
		WinNT.HANDLE stdout = K.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
		IntByReference mode = new IntByReference(0);
		K.GetConsoleMode(stdout, mode);
		K.SetConsoleMode(stdout, mode.getValue()
				| Kernel32.ENABLE_PROCESSED_OUTPUT
				| Kernel32.ENABLE_VIRTUAL_TERMINAL_PROCESSING);
		K.SetConsoleTitle(text);
		WinNT.HWND hwnd = K.GetConsoleWindow();
		User32.INSTANCE.ShowWindow(hwnd, User32.SW_SHOW);
	}
	
	private void writeConsole(String buf) {
		Kernel32 K = Kernel32.INSTANCE;
		WinNT.HANDLE stdout = K.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
		K.WriteConsole(stdout, buf, buf.length(), null, null);
	}

	public void run(String[] args) throws Exception {
		Thread.currentThread().setName("screen");
		int port = 9999;
		try {
			port = Integer.parseInt(args[1]);
		} catch (Exception e) {
			// NONE
		}

		initConsole(args[0]);

		//System.out.println("start");
		ServerSocket server_socket = new ServerSocket(port);
		Socket socket = server_socket.accept();
		try {
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "ms932");
			int len;
			char[] buf = new char[4096];
			StringBuffer sb = new StringBuffer();
			while ((len = isr.read(buf)) >= 0) {
				if(len == 0) continue;
				for(int i=0; i<len; i++) {
					if (buf[i] == 0x1f || buf[i] == 0x1e) continue;
					sb.append(buf[i]);
					if (buf[i] == '\n') {
						String mess = sb.toString(); 
						writeConsole(mess);
						sb.setLength(0);
					}
				}
				if(sb.length() > 0) {
					String mess = sb.toString(); 
					writeConsole(mess);
					sb.setLength(0);
				}
			}
			isr.close();
			//System.out.println("stop");
		} catch (Exception e) {
			// NONE
		}
		try {
			socket.close();
		} catch (Exception e) {
			// NONE
		}
		try {
			server_socket.close();
		} catch (Exception e) {
			// NONE
		}
		Thread.currentThread().setName("-");
	}
}
