 package sysmon;

import java.util.ArrayList;

public class info {
	public String[] value; 
	public String[] label; 
	public String[] prev; 
	public char id; 
	
	public info(char id, int size) {
		this.id = id;
		value = new String[size];
		label = new String[size];
		prev = new String[size];
	}
	
	public void get(ArrayList<String> list) {
	}
	
	public byte[]  getByte() {
		String t = "";
		if(value != null) {
			String tl = "";
			String tv = "";
			int vc = 0;
			int m = Math.min(value.length, label.length);
			for(int i=0; i<m; i++) {
				if(prev[i] == null)
					prev[i] = "null";
				if(value[i] == null 
				|| prev[i].equalsIgnoreCase(value[i])) {
					tl += "\t";
					tv += "\t";
				} else {
					if(label[i] != null) {
						tl += "\t";
						tl += label[i];
						tv += "\t";
						if(value[i] != null) {
							tv += value[i];
							prev[i] = value[i];
						}
						vc++;
					} else {
						tl += "\t";
						tv += "\t";
					}
				}
			}
			if(vc > 0)
				t = tl + tv;
		}
		if(t.length() == 0) {
			return null;
		}
		try {
			byte[] b = t.getBytes("ms932");
			b[0] = (byte)id;
			return b;
		} catch (Exception e) {
			// NONE
		}
		return null;
	}

	public void parse(byte[] b) {
		try {
			id = (char)b[0];
			String t = new String(b, 1, b.length - 1, "ms932");
			if(t.length() > 0) t += "\t@";
			String[] v = t.split("\t");
			int m = v.length / 2;
			value = new String[m];
			label = new String[m];
			for(int i=0; i<m; i++) {
				label[i] = v[    i];
				value[i] = v[m + i];
			}
		} catch (Exception e) {
			// NONE
		}
	}

	protected String size(long s, int len) {
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

	protected String ksize(long s, int len) {
		return ksize(s, len, 1024);
	}
	protected String ksize(long s, int len, int us) {
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
}
