package socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class _base {

	protected String name;
	protected final String ENCODE = "ms932";
	//protected ExecutorService service;

	protected void sleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch (Exception e) {
			// NONE
		}
	}

	public _base() {
		name = getClass().getName();
		//service = Executors.newCachedThreadPool();
	}

	protected String EDIT(String path) {
		if(path.indexOf("%") < 0) return path;
		String[] paths = path.replace("%", "\t").split("\t");
		boolean update = false;
		for(int i=0; i<paths.length; i++) {
			if(paths[i] == null) {
				paths[i] = "";
				continue;
			}
			if(paths[i].length() <= 0) continue;
			if(paths[i].indexOf(" ") >= 0) continue;
			String v = System.getenv(paths[i]); 
			if(v == null) v = System.getenv(paths[i].toLowerCase()); 
			if(v == null) v = System.getenv(paths[i].toUpperCase()); 
			if(v == null) v = System.getProperty(paths[i]); 
			if(v == null) v = System.getProperty(paths[i].toLowerCase()); 
			if(v == null) v = System.getProperty(paths[i].toUpperCase()); 
			if(v == null) continue; 
			paths[i] = v;
			update = true;
		}
		if( !update ) return path;
		return String.join("", paths);
	}

	protected int buftoi(byte[] buf, int ix, long[] val, int len) {
		long v = 0;
		int i = 0;
		while(len > 0) {
			v |= (((long) buf[ix]) & 0x00ff) << i;
			i+= 8;
			ix++;
			len--;
		}
		val[0] = v;
		return ix;
	}
	protected int buftob(byte[] buf, int ix, byte[] val, int len) {
		int i = 0;
		while(len > 0) {
			val[i] = buf[ix];
			i++;
			ix++;
			len--;
		}
		return ix;
	}
	protected int itobuf(byte[] buf, int ix, long val, int len) {
		while(len > 0) {
			buf[ix] = (byte)(val & 0x00ff);
			val = val >> 8;
			ix++;
			len--;
		}
		return ix;
	}
	protected int btobuf(byte[] buf, int ix, byte[] val, int len) {
		int i = 0;
		while(len > 0) {
			buf[ix] = val[i];
			i++;
			ix++;
			len--;
		}
		return ix;
	}

	protected void file_from_socket_print(String mess) {
		//client_print("Å†");
	}
	protected void file_from_socket_print1(byte[] bf2) {
		//client_print("Å†");
	}
	protected void file_from_socket_print2(byte[] bf2) {
		//client_print("Å†");
	}
	protected void file_from_socket_print(long val) {
		//client_print("Å†");
	}
	protected int file_from_socket(File dir, _socket socket, byte[] rbuf, int rlen) {
		try {
			byte[] buf = new byte[rbuf.length];
			byte[] bf2 = null;
			int ix = 2;							// 1
			for(int i=0; i<rlen; i++) buf[i] = rbuf[i];
			if(rlen < 6) {
				rlen += socket.read(buf, rlen, buf.length - rlen);
			}
			byte c = buf[1];
			long[] val = new long[1];
			ix = buftoi(buf, ix, val, 4);
			rlen -= ix;
			if(c == (byte)0x00) {
				file_from_socket_print(val[0]);
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while(val[0] > 0) {
					int wlen = (int) Math.min(rlen, val[0]);
					baos.write(buf, ix, wlen);
					val[0] -= wlen;
					ix += wlen;
					rlen -= wlen;
					if(val[0] > 0) {
						rlen = socket.read(buf, 0, (int) Math.min(val[0], buf.length));
						ix = 0;
					}
				}
				bf2 = baos.toByteArray();
				baos.close();
				if(c == (byte)0x01) {
					file_from_socket_print1(bf2);
				}
				else if(c == (byte)0x02) {
					file_from_socket_print2(bf2);
				}
				else if(c == (byte)0xff) {
					ZipEntry entry = zip_to_file(dir, bf2);
					String inf2 = "   " + (bf2.length * 100 / entry.getSize());
					inf2 = inf2.substring(inf2.length() - 3);
					String info = "                  ";
					info += new DecimalFormat("#,###").format(entry.getSize());
					info = info.substring(info.length() - 17);
					info += " [" + inf2.substring(inf2.length() - 3) + "%]";
					file_from_socket_print(
							new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(entry.getTime()))
							+ " " + info + " " + entry.getName());
				}
			}
			if(rlen> 0) {
				for(int i=0; i<rlen; i++, ix++) rbuf[i] = buf[ix];
				return rlen;
			}
		} catch (Exception e) {
			// NONE
		}
		return 0;
	}

	protected void file_to_socket_print(String mess) {
		//client_print("Å†");
	}
//	protected void file_to_socket(_socket socket, File file) {
//		try {
//			byte[] buf = file_to_zip(file);
//			int leng = buf.length;
//			byte[] bf2 = new byte[5];
//			bf2[0] = 0x1d;
//			bf2[1] = (byte)0xff;
//			itobuf(bf2, 2, leng, 4);
//			socket.write(bf2, bf2.length);
//			socket.write(buf, leng);
//			String inf2 = "   " + (leng * 100 / file.length());
//			String info = "                  ";
//			info += new DecimalFormat("#,###").format(file.length());
//			info = info.substring(info.length() - 17);
//			info += " [" + inf2.substring(inf2.length() - 3) + "%]";
//			file_to_socket_print(
//					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(file.lastModified()))
//					+ " " + info + " " + file.getName());
//		} catch (Exception e) {
//			// NONE
//		}
//	}
	protected void file_to_socket(_socket socket, String fn, long leng, long tm, byte[] bf1) {
		try {
			//byte[] bf1 = file_to_zip(fn, tm, buf);
			//int leng = bf1.length;
			byte[] bf2 = new byte[6];
			bf2[0] = 0x1d;
			bf2[1] = (byte)0xff;
			itobuf(bf2, 2, bf1.length, 4);
			socket.write(bf2, bf2.length);
			socket.write(bf1, bf1.length);
			String inf2 = "   " + (bf1.length * 100 / leng);
			String info = "                  ";
			info += new DecimalFormat("#,###").format(leng);
			info = info.substring(info.length() - 17);
			info += " [" + inf2.substring(inf2.length() - 3) + "%]";
			file_to_socket_print(
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(tm))
					+ " " + info + " " + fn);
		} catch (Exception e) {
			// NONE
		}
	}

	protected File[] listFiles(File file) {
		final String fmask = file.getName().toLowerCase().replace(".", "\\.").replace("*", ".*");
		file = file.getParentFile();
		File[] files = file.listFiles(new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String name) {
	            return name.toLowerCase().matches(fmask);
	        }
	    });
        if (files == null)
        	files = new File[0];
        return files;
	}

	protected byte[] file_to_zip(File file) {
		byte[] buf = new byte[8*1024];
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			//long leng = file.length();
			ZipEntry zip1 = new ZipEntry(file.getName());
			zip1.setTime(file.lastModified());
			zos.putNextEntry(zip1);
			int rlen = 0;
			while((rlen = fis.read(buf)) > 0) {
				zos.write(buf, 0, rlen);
			}
			zos.closeEntry();
			zos.finish();
			zos.close();
			//	System.out.println(" " + (zip1.getCompressedSize() * 100 / zip1.getSize()) + "%");
			buf = baos.toByteArray();
			baos.close();
			fis.close();
		} catch (Exception e) {
			buf = null;
		}
		return buf;
	}
	protected byte[] file_to_zip(String fn, long tm, byte[] buf) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			//long leng = file.length();
			ZipEntry zip1 = new ZipEntry(fn);
			zip1.setTime(tm);
			zos.putNextEntry(zip1);
			zos.write(buf, 0, buf.length);
			zos.closeEntry();
			zos.finish();
			zos.close();
			//	System.out.println(" " + (zip1.getCompressedSize() * 100 / zip1.getSize()) + "%");
			buf = baos.toByteArray();
			baos.close();
		} catch (Exception e) {
			buf = null;
		}
		return buf;
	}

	protected ZipEntry zip_to_file(File dir, byte[] zip) {
		ZipEntry entry = null;
		try {
		  	ByteArrayInputStream bais = new ByteArrayInputStream(zip);
		  	ZipInputStream zis = new ZipInputStream(bais);
			entry = zis.getNextEntry();
			File file = new File(dir, entry.getName());
			FileOutputStream fos = new FileOutputStream(file);
	     	int rlen = 0;	
			byte[] buf = new byte[8*1024];
			while ((rlen = zis.read(buf)) > 0) {
				fos.write(buf, 0, rlen);
	        }
			fos.close();
			file.setLastModified(entry.getTime());
		} catch (Exception e) {
			entry = null;
		}
		return entry;
	}
} 