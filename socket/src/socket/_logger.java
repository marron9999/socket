package socket;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class _logger extends _base {

	protected String logfile;
	private StringBuffer logbuf = new StringBuffer();
	protected long logsize = 2L * 1024L * 1024L;
	private static final String BASE = "%DEV%\\logs";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

	public _logger(String filename) {
		super();
		if(logfile == null) {
			logfile = filename;
		}
	}

	private static String _time() {
		return "[" + sdf.format(new Date()) + "]";
	}
	
	protected void log_time() {
		logbuf.insert(0, _time());
	}

	private void _raf(RandomAccessFile raf, String line) throws Exception {
		raf.write(line.getBytes(ENCODE));
	}
	
	private String _escape(String line) {
		// Remove Escape Sequence String
		int p = line.indexOf((char) 0x1b + "[");
		while (p >= 0) {
			int p2 = p + 2;
			int c2 = 0;
			char c = 0;
			while (true) {
				c = line.charAt(p2);
				if (c >= '0' && c <= '9') {
					p2++;
					c2++;
					continue;
				}
				if (c == ';') {
					p2++;
					c2++;
					continue;
				}
				break;
			}
			if (c2 > 0) {
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					line = line.substring(0, p) + line.substring(p2 + 1);
					p = line.indexOf((char) 0x1b + "[", p + 1);
				}
			}
		}
		return line;
	}

	private void _write() {
		if (logbuf.length() <= 0) {
			return;
		}

		String line = _escape(logbuf.toString());
		logbuf.setLength(0);

		try {
			File file = new File(EDIT(BASE), logfile);
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			long pos = 0;

			// Get Next Write Offset
			raf.seek(0);
			byte[] buf = new byte[10];
			int len = raf.read(buf);
			if (len != 10) {
				raf.seek(0);
				_raf(raf, "00000000\r\n");
				pos = 10;
			} else {
				pos = Long.parseLong(new String(buf, 0, 8, ENCODE), 16);
			}

			// Write Log String
			raf.seek(pos);
			_raf(raf, line + "\r\n");
			pos = raf.getFilePointer();
			_raf(raf, "\r\n\r\n\r\n\r\n\r\n");

			// Set Next Write Offset
			if (pos > logsize) {
				pos = 10;
			}
			line = "00000000" + Long.toHexString(pos);
			line = line.substring(line.length() - 8) + "\r\n";
			raf.seek(0);
			_raf(raf, line);
			raf.close();
		} catch (Exception e) {
			// NONE
		}
	}

	public static String log_debug_text(String func, String line) {
		func = (func + "        ").substring(0,  8);
		line = _time() + "[" + func + "]" + line;
		return line;
	}
	public String log_debug(String func, String line) {
		line = log_debug_text(func, line);
		log_println(line);
		return line;
	}
	
	public void log_println(String line) {
		line = line.replace("\r", "").replace("\n", "");
		log_print(line + "\n");
	}
	public void log_print(String line) {
		synchronized (logbuf) {
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (c == '\r' || c == '\n') {
					_write();
					continue;
				}
				logbuf.append(c);
			}
		}
	}
}
