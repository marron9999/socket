package socket;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

@SuppressWarnings("deprecation")
public class bthlib {
	private String host;
	private /*SOCKET*/Long socket;
	public static _logger log;
	private static void bthlib_debug(String mess) {
		if(log != null) {
			mess = log.log_debug("bthlib", mess);
		} else {
			mess = _logger.log_debug_text("bthlib", mess);
		}
		System.out.println(mess);
	}

	public bthlib(String host) {
		this.socket = null;
		this.host = host;
	}
	public bthlib(/*SOCKET*/Long socket, String host) {
		this.socket = socket;
		this.host = host;
	}

	private interface notify extends Callback {
		public int invoke(String message);
	}
	private interface dll extends Library {
		long BthConnect(String RemoteName);
		long BthSocket();
		long BthAccept(/*SOCKET*/long socket);
		int BthSend(/*SOCKET*/long socket, byte[] pszDataBuffer, int length);
		int BthRecv(/*SOCKET*/long socket, byte[] pszDataBuffer, int pos, int length);
		int BthClose(/*SOCKET*/long socket);
		long BthCallback(notify notify);
		void DefProfile(String name);
		int GetProfile(String sec, String key, Pointer buf, int leng);
		int SetProfile(String sec, String key, String buf);
	}

	private static dll dll;
	static {
		URL url = bthlib.class.getResource("bthlib.class");
		File dir = new File(url.getPath()).getParentFile();
		String fn = new File(dir, "../..").getAbsolutePath();		try { fn = URLDecoder.decode(fn, "utf-8"); } catch (Exception e) { }
		dll = (dll) Native.loadLibrary(fn + "/jna/bthlib.dll", dll.class);
		dll.BthCallback(new notify() {
			@Override
			public int invoke(String mess) {
				bthlib_debug(mess);
				return 0;
			}
		});
	}

    public String getHostName() {
    	return host;
    }
    public Long getStream() {
    	return socket;
    }
	
	public static bthlib connect(String remote)
	{
		long s = dll.BthConnect(remote);
		if(s == 0) return null;
		bthlib bth = new bthlib(s, remote);
		return bth;
	}
	public static bthlib socket(String local)
	{
		long s = dll.BthSocket();
		if(s == 0) return null;
		return new bthlib(s, local);
	}
	public bthlib accept()
	{
		if(socket == null) return null;
		long s = dll.BthAccept(socket.longValue());
		if(s == 0) return null;
		return new bthlib(s, host);
	}
	public int send( byte[] pszDataBuffer, int length)
	{
		if(socket == null) return -1;
		return dll.BthSend(socket.longValue(), pszDataBuffer, length);
	}
	public int recv(byte[] pszDataBuffer, int pos, int length)
	{
		if(socket == null) return -1;
		return dll.BthRecv(socket.longValue(), pszDataBuffer, pos, length);
	}
	public int close()
	{
		if(socket == null) return -1;
		dll.BthClose(socket.longValue());
		socket = null;
		return 0;
	}

	public static void DefProfile(String name)
	{
		dll.DefProfile(name);
	}
	public static String GetProfile(String sec, String key)
	{
		Pointer buf = new Memory(256);
		dll.GetProfile(sec, key, buf, 256);
		return buf.getString(0);
	}
	public static void SetProfile(String sec, String key, String buf)
	{
		dll.SetProfile(sec, key, buf);
	}
	public static int GetProfInt(String sec, String key)
	{
		Pointer buf = new Memory(256);
		buf.clear(256);
		dll.GetProfile(sec, key, buf, 256);
		int rc = 0;
		try {
			String s = buf.getString(0);
			rc = Integer.parseInt(s);
		} catch (Exception e) {
			// NONE
		}
		return rc;
	}
	public static void SetProfInt(String sec, String key, int val)
	{
		dll.SetProfile(sec, key, "" + val);
	}
}
