package socket;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class _command extends _runnable {

	public static boolean screen = false;
	private Socket console_socket;
	private PrintStream console_stream;

	private InputStream is;
	private PrintStream ps;
	//private OutputStream socket_output;
	private _socket socket;
	private String last;
	public String pwd;
	private _cpumon cpumon;

	protected void command_debug(String mess) {
		mess = log.log_debug("command", thread_name() + mess);
		System.out.println(mess);
	}

	public _command(_socket socket, _logger log) {
		super(log);
		this.socket = socket;
		name("command", socket);

		cpumon = new _cpumon(socket, log);
		Thread t = new Thread(cpumon);
		t.start();
	
		if(screen) {
			try {
				console_socket = _screen.open(name.split(" ")[0]);
				if(console_socket != null) {
					console_stream = new PrintStream(console_socket.getOutputStream(), true, ENCODE);
					//String host = console_socket.getInetAddress().getHostName();
					//host += ":" + console_socket.getPort();
					//println("ƒRƒ“ƒ\[ƒ‹ " + host + " ‚ÉÚ‘±‚µ‚Ü‚µ‚½");
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.console_socket = null;
				this.console_stream = null;
			}
		}

		try {
			String cmd = "ver & prompt " + (char) 0x1f + "%COMPUTERNAME%$s$p$g" + (char) 0x1e;
			ProcessBuilder pb = new ProcessBuilder("cmd", "/k", cmd);
			pb.redirectErrorStream(true);
			//if (dir != null) {
			//	pb.directory(dir);
			//}
			Process process = pb.start();
			is = process.getInputStream();
			ps = new PrintStream(process.getOutputStream(), true, ENCODE);
			
		} catch (Exception e) {
			exception("command", e);
		}
	}

	@Override
	protected int onClose() {
		command_debug("- onClose -");
		return super.onClose();
	}
	
	protected void close() {
		command_debug("- close -");
		cpumon.stop = true;
		try {
			if(console_socket != null)
				console_socket.close();
			console_socket = null;
			console_stream = null;
		} catch (Exception e) {
			// NONE
		}
		try {
			if(ps != null)
				ps.close();
			ps = null;
		} catch (Exception e) {
			// NONE
		}
	}

	protected void send(String line) {
		//if (console_stream != null) {
		//	synchronized (console_stream) {
		//		try {
		//			console_stream.print(line);
		//			//console_stream.flush();
		//		} catch (Exception e) {
		//			// NONE
		//		}
		//	}
		//}
		last = line.replace("\r", "").replace("\n", "");
		command_debug(last);
		ps.print(line);
		//System.out.print(">" + line + "<");
	}


	@Override
	public void run() {
		command_debug("- start -");
		onConn(null, "");
		try {
			InputStreamReader isr = new InputStreamReader(is, ENCODE);
			StringBuffer sb = new StringBuffer();
			char[] buf = new char[512];
			int len;
			while ((len = isr.read(buf)) >= 0) {
				if (len == 0) {
					continue;
				}
				if (console_stream != null) {
					synchronized (console_stream) {
						String t = new String(buf, 0, len);
						console_stream.print(t);
						//console_stream.flush();
					}
				}
				//System.out.print("(" + new String(buf, 0, len) + ")");
				for(int i=0; i<len; i++) {
					char ch = buf[i];
					// SEE command.java: prompt 0x1f $p$g 0x1e
					if (ch != '\r' && ch != '\n' && ch != 0x1e) {
						sb.append((char) ch);
						continue;
					}
					if(ch == 0x1e) {
						int p = sb.indexOf("" + (char) 0x1f);
						pwd = sb.substring(p + 1);
						p = pwd.indexOf(" ");
						if(p > 0) pwd = pwd.substring(p).trim();
						p = pwd.indexOf(">");
						if(p > 0) pwd = pwd.substring(0, p).trim();
					}
					if(ch == 0x1e && i+1 != len) {
						int p = sb.indexOf("" + (char) 0x1f);
						sb.deleteCharAt(p);
						continue;
					}
					String t = sb.toString();
					sb.setLength(0);
					if (last != null) {
						if (last.equalsIgnoreCase(t)) {
							last = null;
							continue;
						}
					}
					last = null;
					//t += (ch != 0x1e) ? (char) ch : '\r';
					t += (char) ch;
					//if (socket_output != null) {
					synchronized (socket) {
						try {
							byte[] b = t.getBytes("MS932");
							socket.write(b);
						} catch (Exception e) {
							// NONE: handle exception
						}
					}
					//}
				}
			}
			isr.close();
			is.close();
		} catch (Exception e) {
			// NONE;
		}
		close();
		onClose();
		command_debug("- ended -");
	}
}
