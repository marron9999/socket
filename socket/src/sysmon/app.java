package sysmon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import com.sun.tools.attach.VirtualMachine;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.Tlhelp32;

public class app extends info {

    @FieldOrder({"cb", "PageFaultCount",
    		"PeakWorkingSetSize", "WorkingSetSize",
    		"QuotaPeakPagedPoolUsage", "QuotaPagedPoolUsage",
    		"QuotaPeakNonPagedPoolUsage", "QuotaNonPagedPoolUsage",
    		"PagefileUsage", "PeakPagefileUsage"})
    public class PROCESS_MEMORY_COUNTERS extends Structure {
    	public int cb;
    	public int PageFaultCount;
    	public WinBase.INT_PTR PeakWorkingSetSize;
    	public WinBase.INT_PTR WorkingSetSize;
    	public WinBase.INT_PTR QuotaPeakPagedPoolUsage;
    	public WinBase.INT_PTR QuotaPagedPoolUsage;
    	public WinBase.INT_PTR QuotaPeakNonPagedPoolUsage;
    	public WinBase.INT_PTR QuotaNonPagedPoolUsage; 
    	public WinBase.INT_PTR PagefileUsage;
    	public WinBase.INT_PTR PeakPagefileUsage;
        public PROCESS_MEMORY_COUNTERS() {
        	cb = size();
        }
    }
	
    @SuppressWarnings("deprecation")
    protected interface PsapiX extends Library {
    	PsapiX INSTANCE = (PsapiX) Native.loadLibrary("Psapi.dll", PsapiX.class);
    	boolean GetProcessMemoryInfo(Kernel32.HANDLE proc, PROCESS_MEMORY_COUNTERS pmc, int size);
    }
	
	private ArrayList<String> nms= new ArrayList<>();
	private HashMap<String, Integer> ids= new HashMap<>();
	private HashMap<Integer, Long> kts = new HashMap<>();
	private HashMap<Integer, Long> uts = new HashMap<>();
	private HashMap<Integer, Long> wss = new HashMap<>();
	private HashMap<Integer, String> ans = new HashMap<>();
	private HashMap<Integer, String> jns = new HashMap<>();

	private static final int NAME = 0;
	private static final int JAVA = 1;
	private static final int USED = 2;
	private static final int WORK = 3;
	private static final int MAX_APP = WORK+ 1;
	private info[] app = new info[8];
	private byte[] rmv = new byte[8];

	private long lasttime = 0;

	public app(String args) {
		super('a', 0);
		lasttime = now();
		set(args);
		get();
	}

	public void set(String args) {
		nms.clear();
		for(String s : args.split(" ")) {
			if(s.length() > 0) {
				s = s.replace(".", "\\.").replace("*", ".*");
				nms.add(s.toLowerCase());
			}
		}
	}

	@Override
	public void get(ArrayList<String> list) {
		try {
			for(int i=0; i<app.length; i++) {
				if(app[i] != null) {
					byte[] b = app[i].getByte();
					if(b != null) {
						list.add(new String(b, "ms932"));
					}
				} else {
					if(rmv[i] != 0) {
						rmv[i] = 0;
						String s = "" + (char)('a'+i);
						list.add(s);
					}
				}
			}
		} catch (Exception e) {
			// NONE
		}
	}
	
	public void get() {
		long currtime = now();
		snapshot();
		Set<String> ks = ids.keySet();
		String[] keys = ks.toArray(new String[ks.size()]);
		Arrays.sort(keys);
		for(int i=0; i<Math.min(app.length, keys.length); i++) {
			Integer id = ids.get(keys[i]);
			int u = get(id, currtime - lasttime);
			Long w = wss.get(id);
			String n = ans.get(id);
			String j = jns.get(id);
			if(app[i] == null) {
				app[i] = new info((char)('a'+i), MAX_APP);
			}
			app[i].label[NAME] = id + " 名前";
			app[i].label[JAVA] = id + " 情報";
			app[i].label[USED] = id + " 利用";
			app[i].label[WORK] = id + " サイズ";
			app[i].value[NAME] = n;
			app[i].value[JAVA] = j;
			app[i].value[USED] = u + "%\f" + u;
			app[i].value[WORK] = ksize(w, 0)+ "B";
		}
		for(int i=keys.length; i<app.length; i++) {
			if(app[i] != null) {
				app[i] = null;;
				rmv[i] = 1;
			}
		}
		lasttime = currtime;
	}
	
	private long now() {
		WinBase.SYSTEMTIME st = new WinBase.SYSTEMTIME();
		Kernel32.INSTANCE.GetLocalTime(st);
		WinBase.FILETIME ft = new WinBase.FILETIME();
		Kernel32.INSTANCE.SystemTimeToFileTime(st, ft);
		return L(ft);
	}
	
	private int get(int id, long tm) {
		Kernel32.HANDLE proc = Kernel32.INSTANCE.OpenProcess(
				Kernel32.PROCESS_ALL_ACCESS, false, id);
		if(proc != null) {
			Long bkt = kts.get(id);
			Long but = uts.get(id);
			if(bkt == null) bkt = Long.valueOf(0);
			if(but == null) but = Long.valueOf(0);
			WinBase.FILETIME ct = new WinBase.FILETIME();
			WinBase.FILETIME et = new WinBase.FILETIME();
			WinBase.FILETIME kt = new WinBase.FILETIME();
			WinBase.FILETIME ut = new WinBase.FILETIME();
			Kernel32.INSTANCE.GetProcessTimes(proc, ct, et, kt, ut);
			PROCESS_MEMORY_COUNTERS pmc = new PROCESS_MEMORY_COUNTERS();
			PsapiX.INSTANCE.GetProcessMemoryInfo(proc, pmc, pmc.cb);
			long pws = pmc.WorkingSetSize.longValue();
			Kernel32.INSTANCE.CloseHandle(proc);
			long pkt = L(kt);
			long put = L(ut);
			kts.put(id,  pkt);
			uts.put(id,  put);
			wss.put(id,  pws);
			//aps.put(id,  pws);
			pkt -= bkt;
			put -= but;
			return (tm == 0)? 0 : (int)((pkt + put) / tm);
		}
		kts.remove(id);
		uts.remove(id);
		wss.remove(id);
		ans.remove(id);
		jns.remove(id);
		return 0;
	}

	private long L(WinBase.FILETIME ft) {
		long v = ((long) ft.dwLowDateTime) & 0x00ffffffffL;
		return ((long) ft.dwHighDateTime) << 32 | v;
	}

	private void snapshot() {
		Tlhelp32.PROCESSENTRY32 pe32 = new Tlhelp32.PROCESSENTRY32();
		Kernel32.HANDLE snap = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
					Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
		if (Kernel32.INSTANCE.Process32First(snap, pe32)) {
			while(true) {
				int id = pe32.th32ProcessID.intValue();
				String name = new String(pe32.szExeFile);
				name = name.replace("" + (char)0x00, "");
				String jname = " ";
				if(name.equalsIgnoreCase("java.exe")
				|| name.equalsIgnoreCase("javaw.exe")) {
					String j = jname(id);
					if(j != null)
						jname = j;
				}
				String n = name.toLowerCase();
				String j = jname.toLowerCase();
				for(String m : nms) {
					if(n.matches(m) || j.matches(m)) {
						String key = "000000000000" + id;
						key = key.substring(key.length() - 12);
						ids.put(key, id);
						ans.put(id, name);
						jns.put(id, jname);
						break;
					}
				}
				if( ! Kernel32.INSTANCE.Process32Next(snap, pe32)) break;
			}
		}
		Kernel32.INSTANCE.CloseHandle(snap);
	}
	
	private String jname(int id) {
		try {
			VirtualMachine vm = VirtualMachine.attach("" + id);
			Properties prop = vm.getSystemProperties();
			String name = prop.getProperty("sun.java.command");
			if(name == null
			|| name.length() <= 0) return null;
//			String[] ms = name.split(" ");
//			String[] arg2 = { "-cp", "-classpath", "--class-path",
//					"-p", "--module-path", "--upgrade-module-path", "--add-modules",
//					"--enable-native-access",
//					"-d", "--describe-module",
//					"--add-reads", "--add-exports", "--add-opens", "--limit-modules", "--patch-module",
//					"--source",
//					null };
//			for(int j=0; j<ms.length; j++) {
//				if(ms[j].length() == 0) continue;
//				if(ms[j].charAt(0) == '-') {
//					if(ms[j].equalsIgnoreCase("-jar")) {
//						return ms[j + 1];
//					}
//					for (int k = 0; arg2[k] != null; k++)
//					{
//						if (ms[j].equalsIgnoreCase(arg2[k]))
//						{
//							j++;
//							break;
//						}
//					}
//				} else {
//					return ms[j];
//				}
//			}
			return name;
		} catch (Exception e) {
			// NONE
			return e.getMessage();
		}
		//return null;
	}
}
