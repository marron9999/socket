package socket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
//import java.net.Socket;
import java.io.PrintStream;
import java.net.Socket;

import com.sun.jna.ptr.IntByReference;

import sysmon.monitor;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

public class client extends _logger {

	protected boolean bPrompt = false;
	protected _reciver reciver;
	private BufferedReader br;
	private File download;

	private Socket sysmon_socket = null;
	private PrintStream sysmon_stream = null;
	private Socket appmon_socket = null;
	private PrintStream appmon_stream = null;

	public client() {
		this("client.log");
		download = new File(System.getenv("USERPROFILE"));
		download = new File(download, "Downloads");
	}
	public client(String logname) {
		super(logname);
	}

	protected void client_reciver_println(String mess) {
		System.out.println(mess);
		log_println(mess);
	}

	protected void client_reciver_println(long val) {
		//
	}
	
	protected void client_print(String mess) {
		System.out.print(mess);
	}

	protected void client_debug(String mess) {
		mess = log_debug("client", mess);
		System.out.println(mess);
	}
	
	private void initConsole() {
		Kernel32 K = Kernel32.INSTANCE;
		WinNT.HANDLE stdout = K.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
		IntByReference mode = new IntByReference(0);
		K.GetConsoleMode(stdout, mode);
		K.SetConsoleMode(stdout, mode.getValue()
				| Kernel32.ENABLE_PROCESSED_OUTPUT
				| Kernel32.ENABLE_VIRTUAL_TERMINAL_PROCESSING);
	}

	protected void run(String host, int port) throws Exception {
		client_debug("- connect to " + host + ":" + port + " -");
		initConsole();

		_socket socket = null;
		if(port > 0) {
			socket = vmhost.get(host, port, this);
		} else {
			bthlib.log = this;
			bthlib lib = bthlib.connect(host.toUpperCase());
			socket = new _socket(lib);
		}
		reciver = new _reciver(socket, this) {
			@Override
			protected void println(String mess) {
				client_reciver_println(mess);
			}

			@Override
			protected int onClose() {
				super.onClose();
				bPrompt = true;
				if(br != null) {
					client_debug("- ended -");
					System.exit(0);
				}
				return 0;
			}

			@Override
			protected void file_to_socket_print(String mess) {
				log_debug("upload", mess);
			}

			@Override
			protected String file_download_path(String path) {
				if(path != null) {
					try {
						File file = new File(path);
						download = file.getCanonicalFile();
					} catch (Exception e) {
						// NONE
					}
				}
				return download.getAbsolutePath(); 
			}
			@Override
			protected void file_from_socket_print(String mess) {
				client_print(mess + "\n");				
				log_debug("download", mess);
			}
			@Override
			protected void file_from_socket_print1(byte[] bf2) {
				if(sysmon_stream != null) {
					try {
						String t = new String(bf2, "ms932");
						sysmon_stream.println(t);
					} catch (Exception e) {
						// NONE
					}
				}
			}
			@Override
			protected void file_from_socket_print2(byte[] bf2) {
				if(appmon_stream != null) {
					try {
						String t = new String(bf2, "ms932");
						appmon_stream.println(t);
					} catch (Exception e) {
						// NONE
					}
				}
			}

			@Override
			protected void file_from_socket_print(long val) {
				client_reciver_println(val);
			}

			protected int reciver_println(byte[] buf, int rlen) 
			{
				rlen = file_from_socket(download, socket, buf, rlen);
				return rlen;
			}
			
			@Override
			protected void reciver_println(String mess) {
				// SEE command.java: prompt 0x1f $p$g 0x1e
				mess = mess.replace("\r", "").replace("\n", "");
				if(mess.length() > 0) {
					boolean nl = true;
					if (mess.charAt(0) == 0x1f) {
						mess = mess.substring(1);
						mess = mess.substring(0, mess.length() - 1);
						bPrompt = true;
						nl = false;
					}
					//log_println(mess);
					if(mess.startsWith(".build" + "\t"))
						client_debug(mess.replace("\t", " "));
					if(nl) mess += "\n";
					client_print(mess);
				}
			}

			@Override
			protected void reciver_sender_ended() {
				log_time();
				log_println("");
			}

			@Override
			protected boolean _send(String m0, String m1) {
				if(m0.equalsIgnoreCase("#download")) {
					String fn = file_download_path(null);
					if(m1 != null && m1.length() > 0) {
						fn = file_download_path(m1);
					}
					file_from_socket_print("ダウンロード先: " + fn);
					return true;
				}
				if(m0.equalsIgnoreCase("@sysmon")) {
					if(m1 != null && m1.length() > 0) {
						if(m1.equalsIgnoreCase("true")
						|| m1.equalsIgnoreCase("on")) {
							if(sysmon_stream == null) {
								try {
									sysmon_socket = monitor.open(host.toUpperCase(), "sysmon");
									sysmon_stream = new PrintStream(
											sysmon_socket.getOutputStream(), true, "ms932");
								} catch (Exception e) {
									// NONE
								}
							}
						}
						if(m1.equalsIgnoreCase("false")
						|| m1.equalsIgnoreCase("off")) {
							if(sysmon_stream != null) {
								try { sysmon_stream.close(); } catch (Exception e) { }
								try { sysmon_socket.close(); } catch (Exception e) { }
								sysmon_stream = null;
								sysmon_socket = null;
							}
						}
					}
					return false;
				}
				if(m0.equalsIgnoreCase("@appmon")) {
					if(m1 != null && m1.length() > 0) {
						if(m1.equalsIgnoreCase("true")
						|| m1.equalsIgnoreCase("on")) {
							if(appmon_stream == null) {
								try {
									appmon_socket = monitor.open(host.toUpperCase(), "appmon");
									appmon_stream = new PrintStream(
											appmon_socket.getOutputStream(), true, "ms932");
								} catch (Exception e) {
									// NONE
								}
							}
						}
						if(m1.equalsIgnoreCase("false")
						|| m1.equalsIgnoreCase("off")) {
							if(appmon_stream != null) {
								try { appmon_stream.close(); } catch (Exception e) { }
								try { appmon_socket.close(); } catch (Exception e) { }
								appmon_stream = null;
								appmon_socket = null;
							}
						}
					}
					return false;
				}
				return false;
			}
		};
		Thread t = new Thread(reciver);
		t.start();

		dispatch();

		try { reciver.close(); } catch (Exception e) { }
		client_debug("- ended " + host + ":" + port + " -");
	}

	protected void dispatch() throws Exception {
		try {
			reciver.send("@start\n");
			InputStreamReader isr = new InputStreamReader(System.in, ENCODE);
			br = new BufferedReader(isr);
			FileInputStream fis = null;
			InputStreamReader isr2 = null;
			BufferedReader br2 = null;
			String line;
			while (true) {
				while( ! bPrompt) {
					sleep(100);
				}
				sleep(100);
				bPrompt = false;

				if(br2 != null) {
					line = br2.readLine();
					if(line == null) {
						br2.close();
						isr2.close();
						fis.close();
						br2 = null;
						isr2 = null;
						fis = null;
						log_time();
						log_println("");
						bPrompt = true;
						continue;
					}
					line = line.trim();
				} else {
					line = br.readLine();
					if(line == null) break;
					line = line.trim();
					if(line.length() > 0 
					&& line.charAt(0) == '@') {
						File file = new File(line.substring(1).trim());
						if(file.exists()) {
							log_time();
							log_println("");
							log_debug("client", "@" + file.getCanonicalPath());
							fis = new FileInputStream(file);
							isr2 = new InputStreamReader(fis, ENCODE);
							br2 = new BufferedReader(isr2);
							bPrompt = true;
							continue;
						}
					}
				}
				{
					log_time();
					log_println(line);
					reciver.send(line + "\n");
				}
				//if(line.equalsIgnoreCase("exit"))
				//	bPrompt = true;
			}
		} catch (Exception e) {
			// NONE
		}
	}
	
	public void run(String[] args) throws Exception {
		client_debug("- Start -");
		String host = "";
		int port = 0;
		if (args.length > 1
		&& (args[0].equalsIgnoreCase("bth") || args[0].equalsIgnoreCase("ble"))) {
			host = args[1];
		}
		else {
			host = args[0];
			port = 9999;
			if (args.length > 1) {
				try { port = Integer.parseInt(args[1]); } catch (Exception e) { }
			}
		}
		run(host, port);
		
		client_debug("- ended -");
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			new client().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
