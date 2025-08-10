package win32;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class Util {

	public static final String ENCODE = "utf8";
	public static final int MAX_BUF = 0x7fff;
	public static final String CRNL = "\r\n";

	public static final String USERNAME = System.getenv("USERNAME");
	public static final String USERPROFILE = System.getenv("USERPROFILE");
	public static final String COMPUTERNAME = System.getenv("COMPUTERNAME").toUpperCase();
	public static SimpleDateFormat ymd_hms = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static String ksize(long s, int len) {
		return ksize(s, len, 1024);
	}
	public static String ksize(long s, int len, int us) {
		String x1 = "";
		String v = "" + s;
		String v2 = "";
		if(len >= 0) {
			double d = s;
			if(d >= us) {
				x1 = "K"; d /= us;  
				if(d >= us) {
					x1 = "M"; d /= us;  
					if(d >= us) {
						x1 = "G"; d /= us;  
					}
				}
				v = "" + d;
				int p = v.indexOf(".");
				if(p > 0) {
					v2 = v.substring(p).substring(0, 2);
					v = v.substring(0, p);
				}
			}
		}
		if(v.length() > 3) {
			v2 = "," + v.substring(v.length() - 3) + v2;
			v = v.substring(0, v.length() - 3);
			if(v.length() > 3) {
				v2 = "," + v.substring(v.length() - 3) + v2;
				v = v.substring(0, v.length() - 3);
				if(v.length() > 3) {
					v2 = "," + v.substring(v.length() - 3) + v2;
					v = v.substring(0, v.length() - 3);
				}
			}
		}
		v = v + v2;
		if(len <= 0) {
			return v + x1;
		}
		x1 = "                    " + v + x1;
		return x1.substring(x1.length() - len);
	}

	public static String size(long s, int len) {
		String v = "" + s;
		String v2 = "";
		if(v.length() > 3) {
			v2 = "," + v.substring(v.length() - 3) + v2;
			v = v.substring(0, v.length() - 3);
			if(v.length() > 3) {
				v2 = "," + v.substring(v.length() - 3) + v2;
				v = v.substring(0, v.length() - 3);
				if(v.length() > 3) {
					v2 = "," + v.substring(v.length() - 3) + v2;
					v = v.substring(0, v.length() - 3);
				}
			}
		}
		v = v + v2;
		if(len <= 0) return v;
		v = "                    " + v;
		return v.substring(v.length() - len);
	}

	public static File path(File dir, String a) {
		if(a.length() >= 2 && a.charAt(1) == ':') {
			dir = new File(a);
		} else if(a.length() >= 1 && a.charAt(0) == '\\') {
			dir = new File(a);
		} else if(a.length() >= 1 && a.charAt(0) == '/') {
			dir = new File(a.replace('/', '\\'));
		} else {
			dir = new File(dir, a);
		}
		return dir;
	}

	public static long _long(byte[] buf) {
		int p = 0;
		long len = (((long)buf[p++]) & 0x00ffL);  
		len     |= (((long)buf[p++]) & 0x00ffL) << 8;
		len     |= (((long)buf[p++]) & 0x00ffL) << 16;
		len     |= (((long)buf[p++]) & 0x00ffL) << 24;
		len     |= (((long)buf[p++]) & 0x00ffL) << 32;
		len     |= (((long)buf[p++]) & 0x00ffL) << 40;
		len     |= (((long)buf[p++]) & 0x00ffL) << 48;
		len     |= (((long)buf[p++]) & 0x00ffL) << 56;
		return len;
	}
	public static byte[] _long(long len) {
		byte[] buf = new byte[8]; 
		int p = 0;
		buf[p++] = (byte)((len      ) & 0x00ffL);
		buf[p++] = (byte)((len >> 8 ) & 0x00ffL);
		buf[p++] = (byte)((len >> 16) & 0x00ffL);
		buf[p++] = (byte)((len >> 24) & 0x00ffL);
		buf[p++] = (byte)((len >> 32) & 0x00ffL);
		buf[p++] = (byte)((len >> 40) & 0x00ffL);
		buf[p++] = (byte)((len >> 48) & 0x00ffL);
		buf[p++] = (byte)((len >> 56) & 0x00ffL);
		return buf;
	}

	public static int _int(byte[] buf) {
		int p = 0;
		int len = (((int)buf[p++]) & 0x00ff);  
		len    |= (((int)buf[p++]) & 0x00ff) << 8;
		len    |= (((int)buf[p++]) & 0x00ff) << 16;
		len    |= (((int)buf[p++]) & 0x00ff) << 24;
		return len;
	}
	public static byte[] _int(int len) {
		byte[] buf = new byte[4]; 
		int p = 0;
		buf[p++] = (byte)((len      ) & 0x00ffL);
		buf[p++] = (byte)((len >> 8 ) & 0x00ffL);
		buf[p++] = (byte)((len >> 16) & 0x00ffL);
		buf[p++] = (byte)((len >> 24) & 0x00ffL);
		return buf;
	}

	public static int _short(byte[] buf) {
		int p = 0;
		int len = (((int)buf[p++]) & 0x00ff);
		len    |= (((int)buf[p++]) & 0x00ff) << 8;
		return len;
	}
	public static byte[] _short(int len) {
		byte[] buf = new byte[2];
		int p = 0;
		buf[p++] = (byte)((len      ) & 0x00ff);
		buf[p++] = (byte)((len >> 8 ) & 0x00ff);
		return buf;
	}


	public static boolean wildcard(String pat, String val) {
		pat = pat.toLowerCase();
		val = val.toLowerCase();
		if(pat.equals("*")) return true;
		int p = pat.indexOf("*");
		if(p >= 0) {
			String[] ps = null;
			if(pat.endsWith("*")) {
				ps = (pat + "@").replace("*", "\t").split("\t");
				ps[ps.length - 1] = "";
			} else {
				ps = pat.replace("*", "\t").split("\t");
			}
			int q1 = 0;
			int q2 = 0;
			for(int i=0; i<ps.length; i++) {
				if(ps[i].length() > 0) {
					q2 = val.indexOf(ps[i], q1);
					if(q2 < 0) return false;
					q1 = q2 + ps[i].length();
				} else {
					if(i + 1 == ps.length) {
						q1 = val.length();
						break;
					}
				}
			}
			if(val.length() == q1) return true;
			return false;
		}
		return pat.equals(val);
	}

	public static String repenv(String line, char dlm) {
		int p = line.indexOf(dlm);
		while(p >= 0) {
			int q = line.indexOf(dlm, p+1);
			if(q < 0) break;
			String s = line.substring(p+1, q);
			String v = System.getProperty(s);
			if(v == null) v = System.getProperty(s.toLowerCase());
			if(v == null) v = System.getenv(s);
			if(v == null) v = System.getenv(s.toUpperCase());
			if(v != null) {
				line = line.substring(0, p)
						+ v + line.substring(q+1);
			} else p = q+1;
		}
		return line;
	}

	public static String format_date(Date date) {
		return format_date("yyyy/MM/dd HH:mm:ss", date);
	}
	public static String format_yymmdd(Date date) {
		return format_date("yyyy/MM/dd", date);
	}
	public static String format_hhmmss(Date date) {
		return format_date("HH:mm:ss", date);
	}
	public static String format_date(String format, Date date) {
		SimpleDateFormat ymd_hms = new SimpleDateFormat(format);
		return ymd_hms.format(date);
	}

	public static byte[] encode(String data) throws Exception {
		return data.getBytes(ENCODE);
	}
	public static byte[] encode_nl(String data) throws Exception {
		return encode(data + CRNL);
	}
	public static String decode(byte[] data) throws Exception {
		return new String(data, ENCODE);
	}

	public static String encode64(byte[] data) {
		if(data != null) {
			try {
				return Base64.getEncoder().encodeToString(data);
			} catch(Exception e) {
				// NONE
			}
		}
		return null;
	}
	public static byte[] decode64(String data) {
		if(data != null && data.length() > 0) {
			try {
				return Base64.getDecoder().decode(data);
			} catch(Exception e) {
				// NONE
			}
		}
		return null;
	}

	public static byte[] readFile(File file) {
		if(file == null) return null;
		if( ! file.exists()) return null;
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[16 * 1024];
			int length;
			while ((length = fis.read(buffer)) >= 0) {
				body.write(buffer, 0, length);
			}
			fis.close();
			buffer = body.toByteArray();
			body.close();
			return buffer;
		} catch (Exception e) {
			// NONE
		}
		return null;
	}

	public static void writeStream(OutputStream os, File file) {
		if(file == null) return;
		if( ! file.exists()) return;
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = fis.read(buffer)) >= 0) {
				os.write(buffer, 0, length);
			}
			fis.close();
		} catch (Exception e) {
			// NONE
		}
	}

	public static boolean writeFile(File file, byte[] body) {
		if(file == null) return false;
		if(file.exists()) file.delete();
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(body, 0, body.length);
			fos.close();
			return true;
		} catch (Exception e) {
			// NONE
		}
		return false;
	}

	public static byte[] readStream(InputStream is) {
		try {
			ByteArrayOutputStream body = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = is.read(buffer)) >= 0) {
				body.write(buffer, 0, length);
			}
			is.close();
			buffer = body.toByteArray();
			body.close();
			return buffer;
		} catch (Exception e) {
			// NONE
		}
		return null;
	}

	public static boolean writeFile(File file, InputStream is) {
		if(file == null) return false;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = is.read(buffer)) >= 0) {
				fos.write(buffer, 0, length);
			}
			is.close();
			fos.close();
			return true;
		} catch (Exception e) {
			// NONE
		}
		return false;
	}

	public static void sleep(int msec) {
		if(msec > 0) {
			try { Thread.sleep(msec); } catch (Exception e) { }
		}
	}

	public static void setClip(String str) {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(str);
			clipboard.setContents(selection, null);
		} catch(Exception e){
			// NONE
		}
	}
	public static String getClip() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable object = clipboard.getContents(null);
			return (String)object.getTransferData(DataFlavor.stringFlavor);
		} catch(Exception e){
			// NONE
		}
		return null;
	}
}
