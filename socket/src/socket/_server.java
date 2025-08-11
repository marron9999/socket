package socket;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

//import java.lang.Thread.State;
//import java.net.ServerSocket;
//import java.net.Socket;

import sysmon.dsplib;;

public class _server extends _logger implements Runnable {

	private boolean bServerClose = false;
	private String host;
	private int port = 9999;
	private _ssocket server_socket;
	private String name;
	private _sysmon sysmon;
	private _appmon appmon;
	private String appmon_mask;
	
	public _server(int port) {
		this("server", port);
	}
	public _server(String name) {
		this(name, -1);
	}
	public _server(String name, int port) {
		super(name + ".log");
		this.name = name;
		this.port = port;
		host = System.getenv("COMPUTERNAME");
		bthlib.DefProfile(host);
		appmon_mask = bthlib.GetProfile("appmon", "mask");
	}

	protected void server_reciver_println(String mess) {
		log_debug("[srv][rcv]", mess);
	}

	protected void server_cmd_println(String mess) {
		log_debug("[srv][cmd]", mess);
	}

	protected void server_debug(String mess) {
		mess = log_debug(name, mess);
		System.out.println(mess);
	}
	
	protected void server_close() {
		_ssocket s = server_socket;
		server_socket = null;
		if (s != null) {
			server_debug("- server_close -");
			try {
			 	s.close();
			} catch (Exception e) {
				// NONE
			}
		}
	}

	private void server_command(_reciver reciver) {
		reciver.command = new _command(reciver.socket, this) {
			@Override
			protected void println(String mess) {
				server_cmd_println(mess);
				//System.out.println(mess);
			}
			@Override
			protected void close() {
				super.close();
				reciver.command = null;
				reciver.reciver_close();
			}
		};
		Thread t = new Thread(reciver.command);
		t.start();
	}

	private void server_sysmon(_reciver reciver) {
		server_debug(socket_name(reciver.socket));
		if(sysmon == null) {
			sysmon = new _sysmon(reciver.socket, this);
			Thread t = new Thread(sysmon);
			t.start();
		}
	}
	private void server_appmon(_reciver reciver) {
		server_debug(socket_name(reciver.socket));
		if(appmon == null) {
			appmon = new _appmon(reciver.socket, this);
			appmon.mask = appmon_mask;
			Thread t = new Thread(appmon);
			t.start();
		}
	}

	private String socket_name(_socket soc) {
		String host = soc.getHostName();
		return "[" + host + ":" + soc.getPort() + "]";
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName(name);
		server_debug("- Start -");
		try {
			if(port > 0) {
				server_socket = new _ssocket(port, this);
			} else {
				server_socket = new _ssocket(this);
			}
			while (server_socket != null) {
				try {
					server_debug("- ready -");
					_socket socket = server_socket.accept();
					server_debug(socket_name(socket) + "- accept -");

					_reciver reciver = new _reciver(socket, this) {
						@Override
						protected void println(String line) {
							server_reciver_println(line);
							super.println(line);
						}

						@Override
						protected void file_to_socket_print(String mess) {
							mess = log_debug("download", mess);
							super.println(mess);
						}
						
						@Override
						protected void file_from_socket_print(String mess) {
							String mes0 = log_debug("upload", mess);
							super.println(mes0);
							try {
								byte[] buf = (mess + "\n").getBytes("ms932");
								socket.write(buf);
							} catch (Exception e) {
								// NONE
							}
						}
						@Override
						protected int reciver_println(byte[] buf, int rlen) 
						{
							File file = new File(command.pwd);
							rlen = file_from_socket(file, socket, buf, rlen);
							return rlen;
						}
						//@Override
						//protected void file_from_socket_print(long val) {
						//	server_cpumon((int)val);
						//}
						
						@Override
						protected void reciver_println(String mess) {
							String m1 = mess.replace("\r","").replace("\n","").trim();
							String m0 = m1;
							int p = m0.indexOf(" ");
							if(p > 0) {
								m1 = m0.substring(p + 1).trim();
								m0 = m0.substring(0, p).trim();
							} else {
								m1 = "";
							}
							if(m0.equalsIgnoreCase("@exit")) {
								bServerClose = true;
							}
							if(m0.equalsIgnoreCase("@start")) {
								if (command == null) {
									server_command(this);
								}
								return;
							}

							if(m0.equalsIgnoreCase("@screen")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								if(m1.equalsIgnoreCase("on")) _command.screen = true;
								if(m1.equalsIgnoreCase("off")) _command.screen = false;
								if(m1.equalsIgnoreCase("true")) _command.screen = true;
								if(m1.equalsIgnoreCase("false")) _command.screen = false;
								m1 = "@SCREEN is " + _command.screen  + ".";
								try {
									byte[] buf = (m1 + "\n").getBytes("ms932");
									socket.write(buf);
								} catch (Exception e) {
									// NONE
								}
								m0 = "";
								m1 = "";
								mess = "\n";
							}

							if(m0.equalsIgnoreCase("@print")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								{
									int no = 0;
									try { no = Integer.parseInt(m1); } catch (Exception e) { }
									dsplib lib = new dsplib();
									byte[] buf = lib.capture(no);
									SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
									Date tm = new Date();
									String fn = "display" + no + "_" + sdf.format(tm) + ".png"; 
									byte[] bf1 = file_to_zip(fn, tm.getTime(), buf);
									file_to_socket(socket, fn, buf.length, tm.getTime(), bf1);
								}
								try {
									byte[] buf = { 0x1c };
									socket.write(buf, 1);
								} catch (Exception e) {
									// NONE
								}
								m0 = "";
								m1 = "";
								mess = "";
							}

							if(m0.equalsIgnoreCase("@download")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								String fn = m1;
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
										mess = "";
									}
								} else {
									File file = new File(command.pwd);
									file = new File(file, fn);
									File[] files = listFiles(file);
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
									m0 = "";
									m1 = "";
									mess = "";
								}
							}

							if(m0.equalsIgnoreCase("@sysmon")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								if(m1.equalsIgnoreCase("true")
								|| m1.equalsIgnoreCase("on")) {
									if(sysmon == null) {
										server_sysmon(this);
									}
								}
								if(m1.equalsIgnoreCase("false")
								|| m1.equalsIgnoreCase("off")) {
									if(sysmon != null) {
										_sysmon s = sysmon;
										sysmon = null;
										s.stop = true;
									}
								}
								m1 = "@SYSMON is " + (sysmon != null) + ".";
								try {
									byte[] buf = (m1 + "\n").getBytes("ms932");
									socket.write(buf);
								} catch (Exception e) {
									// NONE
								}
								m0 = "";
								m1 = "";
								mess = "\n";
							}
							if(m0.equalsIgnoreCase("@appmon")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								if(m1.equalsIgnoreCase("true")
								|| m1.equalsIgnoreCase("on")) {
									if(appmon == null) {
										server_appmon(this);
									}
								}
								if(m1.equalsIgnoreCase("false")
								|| m1.equalsIgnoreCase("off")) {
									if(appmon != null) {
										_appmon s = appmon;
										appmon = null;
										s.stop = true;
									}
								}
								m1 = "@APPMON is " + (appmon != null) + ".";
								try {
									byte[] buf = (m1 + "\n").getBytes("ms932");
									socket.write(buf);
								} catch (Exception e) {
									// NONE
								}
								m0 = "";
								m1 = "";
								mess = "\n";
							}
							if(m0.equalsIgnoreCase("#appmon")) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								String[] mx = m1.split(" ");
								for(int i=1; i<mx.length; i++) {
									if(mx[0].equalsIgnoreCase("add")) {
										appmon_mask += " " + mx[i];
									}
									else if(mx[0].equalsIgnoreCase("del")) {
										String u = " " + appmon_mask + " ";
										u = u.replace(" " + mx[i] + " " , " ");
										appmon_mask = u.trim();
									}
								}
								bthlib.DefProfile(host);
								bthlib.SetProfile("appmon", "mask", appmon_mask);
								m1 = "#APPMON is \"" + appmon_mask + "\".";
								try {
									byte[] buf = (m1 + "\n").getBytes("ms932");
									socket.write(buf);
								} catch (Exception e) {
									// NONE
								}
								m0 = "";
								m1 = "";
								mess = "\n";
							}

							if(mess.length() > 0) {
								server_debug(socket_name(socket) + m0 + " " + m1);
								command.send(mess);
							}
							//else {
							//	int a = 0;
							//	a++;								
							//}
						}
						
						@Override
						protected int onConn(Object object, String remoteName) {
							super.onConn(object, remoteName);
							//System.out.println(time() + "custom begin()");
							//System.out.println(time() + "new _command()");
							//System.out.println(time() + "end begin");
							return 0;
						}

						@Override
						protected int onClose() {
							if(sysmon != null) {
								_sysmon s = sysmon;
								sysmon = null;
								s.stop = true;
							}
							if(appmon != null) {
								_appmon s = appmon;
								appmon = null;
								s.stop = true;
							}
							super.onClose();
							if(bServerClose) {
								server_close();
							}
							return 0;
						}
					};
					Thread t = new Thread(reciver);
					t.start();
					//server_socket.join(t);

				} catch (Exception e) {
					if (server_socket != null) {
						e.printStackTrace();
						try {
							server_socket.close();
						} catch (Exception e2) {
							// NONE
						}
						server_socket = null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		server_debug("- ended -");
		Thread.currentThread().setName("--");
	}

}
