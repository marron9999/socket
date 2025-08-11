package socket;

import java.io.File;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;

public class _reciver extends _runnable {

	protected _socket socket;
	//private OutputStream reciver_output;
	//private InputStream reciver_input;
	//private Long reciver_stream;
	protected _command command;

	protected void reciver_println(String mess) {
		//println("[p]" + mess);
	}
	protected int reciver_println(byte[] buf, int rlen) {
		//println("[p]" + mess);
		return rlen;
	}
	protected void reciver_debug(String mess) {
		mess = log.log_debug("reciver", thread_name() + mess);
		System.out.println(mess);
	}
	protected void reciver_sender_println(String mess) {
		println(mess);
	}

	protected void reciver_sender_ended() {
	}

	protected void reciver_close() {
		close();
	}
	
	public _reciver(_socket socket, _logger log) {
		super(log);
		name("reciver", socket);
		this.socket = socket;
		//try {
		//	reciver_output = socket.getOutputStream();
		//	reciver_input  = socket.getInputStream();
		//	reciver_stream = socket.getBthStream();
		//} catch (Exception e) {
		//	e.printStackTrace();
		//	reciver_output = null;
		//	reciver_stream = null;
		//}
	}

	protected String file_download_path(String path) {
		return path;
	}
	
	protected boolean _send(String m0, String m1) {
		return false;
	}

	protected void send(String mess) {
		String m1 = mess.replace("\r","").replace("\n","").trim();
		String m0 = m1;
		int p = m0.indexOf(" ");
		if(p > 0) {
			m1 = m0.substring(p+1).trim();
			m0 = m0.substring(0, p).trim();
		} else {
			m1 = "";
		}
		if(_send(m0, m1)) {
			mess = "\n";
			m0 = "";
			m1 = "";
		}
		if(m0.equalsIgnoreCase("@upload")) {
			//server_debug(socket_name(socket) + m1);
			String fn = m1.trim();
			if(fn.startsWith("\"")) {
				fn = fn.substring(1);
				p = fn.indexOf("\"");
				if(p > 0) {
					fn = fn.substring(0, p);
					File[] files = listFiles(new File(fn));
					if(files.length > 1)
						file_to_socket_print("transfer " + files.length + " files");
					for(File f : files) {
						if( ! f.isDirectory()) {
							byte[] bf1 = file_to_zip(f);
							file_to_socket(socket, f.getName(), f.length(), f.lastModified(), bf1);
						}
					}
					try {
						byte[] buf = { 0x1c };
						socket.write(buf, 1);
					} catch (Exception e) {
						// NONE
					}
					mess = "\n";
				}
			} else {
				File[] files = listFiles(new File(fn));
				if(files.length > 1)
					file_to_socket_print("transfer " + files.length + " files");
				for(File f : files) {
					if( ! f.isDirectory()) {
						byte[] bf1 = file_to_zip(f);
						file_to_socket(socket, f.getName(), f.length(), f.lastModified(), bf1);
					}
				}
				try {
					byte[] buf = { 0x1c };
					socket.write(buf, 1);
				} catch (Exception e) {
					// NONE
				}
				mess = "\n";
			}
		}
		try {
			byte[] buf = mess.getBytes("MS932");
			socket.write(buf);
		} catch (Exception e) {
			// NONE: handle exception
		}
	}

	@Override
	protected int onClose() {
		reciver_debug("- onClose -");
		return super.onClose();
	}
	
	protected void close() {
		//reciver_output = null;
		if (command != null) {
			reciver_debug("- close command -");
			_command c = command;
			command = null;
			c.close();
		}
		if (socket != null) {
			reciver_debug("- close socket -");
			_socket s = socket;
			socket = null;
			try {
				s.close();
			} catch (Exception e) {
				// NONE
			}
		}
	}

	@Override
	public void run() {
		reciver_debug("- start -");
		onConn(null, "");
		try {
			byte[] buf = new byte[4096];
			int len = 0;
			byte[] rbuf = new byte[4096];
			int rlen = 0;
			while ((rlen = socket.read(rbuf)) >= 0) {
				if (rlen == 0)
					continue;
				for(int i=0; i<rlen; i++) {
					if(rbuf[i] == 0x1d) {
						{
							rlen -= i;
							byte[] rbf2 = new byte[rlen];
							for(int j=0; j<rlen; j++, i++) rbf2[j] = rbuf[i];
							for(int j=0; j<rlen; j++, i++) rbuf[j] = rbf2[j];
						}
						rlen = reciver_println(rbuf, rlen);
						i = -1;
						continue;
					}
					if(rbuf[i] == 0x1c) {
						send("\n");
						continue;
					}
					if ((char)rbuf[i] != '\r'
					&& (char)rbuf[i] != '\n'
					&& (char)rbuf[i] != 0x1e) {
						buf[len] = rbuf[i];
						len++;
						continue;
					}
					buf[len] = rbuf[i];
					len++;
					String t = new String(buf, 0, len, "MS932");
					len = 0;
					if(t.length() > 0)
						reciver_println(t);
				}
			}
			//{
			//	int a =0;
			//	a++;
			//}
		} catch (Exception e) {
			// NONE
			//int a = 0;
			//a++;
		}
		close();
		onClose();
		reciver_debug("- ended -");
	}
}
