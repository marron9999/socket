package win32;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.tools.attach.VirtualMachine;
import com.sun.jna.platform.win32.Tlhelp32;

public class ProcInfo {
	private HashMap<Integer, String> names = null;
	private HashMap<Integer, Long> mems = null;
	private HashMap<Integer, Long> hdls = null;
	private HashMap<Integer, Long> thds = null;
	private HashMap<Integer, Long> map1 = null;
	private HashMap<Long, String> jnames = new HashMap<>();

	public ProcInfo() {
		map1 = CreateToolhelp32Snapshot();
		jnames.put(ProcessHandle.current().pid(), "rcmd server");
	}

	private long L(WinBase.FILETIME ft) {
		long v = ((long) ft.dwLowDateTime) & 0x00ffffffffL;
		return ((long) ft.dwHighDateTime) << 32 | v;
	}

	private HashMap<Integer, Long> CreateToolhelp32Snapshot() {
		HashMap<Integer, Long> map = new HashMap<>(); 
		Tlhelp32.PROCESSENTRY32 pe32 = new Tlhelp32.PROCESSENTRY32();
		Kernel32.HANDLE snap = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
					Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
		if (Kernel32.INSTANCE.Process32First(snap, pe32)) {
			while(true) {
				Kernel32.HANDLE proc = Kernel32.INSTANCE.OpenProcess(
							Kernel32.PROCESS_ALL_ACCESS, false,
							pe32.th32ProcessID.intValue());
				if(proc != null) {
					WinBase.FILETIME ct = new WinBase.FILETIME();
					WinBase.FILETIME et = new WinBase.FILETIME();
					WinBase.FILETIME kt = new WinBase.FILETIME();
					WinBase.FILETIME ut = new WinBase.FILETIME();
					Kernel32.INSTANCE.GetProcessTimes(proc, ct, et, kt, ut);
					//WinBase.DWORD hc = new WinBase.DWORD();
					//Kernel32.INSTANCE.GetProcessHandleCount(proc, hc);
					Kernel32.INSTANCE.CloseHandle(proc);
					long t = L(kt) + L(ut);
					map.put(pe32.th32ProcessID.intValue(), t);
					if(names != null) {
						String n = new String(pe32.szExeFile);
						int i = n.indexOf(".");
						names.put(pe32.th32ProcessID.intValue(), n.substring(0, i));
						hdls.put(pe32.th32ProcessID.intValue(), 0L);
						mems.put(pe32.th32ProcessID.intValue(), 0L);
						thds.put(pe32.th32ProcessID.intValue(), (long) pe32.cntThreads.intValue());
					}
				}
				if( ! Kernel32.INSTANCE.Process32Next(snap, pe32)) break;
			}
		}
		Kernel32.INSTANCE.CloseHandle(snap);
		return map;
	}

	private String jnfo(int id) {
		String name = jnames.get((long)id);
		if(name != null) return name;
		try {
			VirtualMachine vm = VirtualMachine.attach("" + id);
			Properties prop =vm.getSystemProperties();
			name = prop.getProperty("sun.java.command");
			if(name != null
			&& name.length() > 0) {
				int q = name.indexOf(".jar");
				if(q > 0) {
					name = name.substring(0, q+4);
					jnames.put((long)id, name);
					return name;
				}
				q = name.indexOf(" ");
				if(q > 0) {
					name = name.substring(0, q);
					jnames.put((long)id, name);
					return name;
				}
				jnames.put((long)id, name);
			}
			return name;
		} catch (Exception e) {
			// NONE
		}
		return null;
	}

//	private String jnfo(int id) {
//		String name = null;
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			Exec.run(baos, new File("."), "jinfo " + id);
//			byte[] buf = baos.toByteArray();
//			baos.close();
//			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//			InputStreamReader isr = new InputStreamReader(bais, "ms932");
//			BufferedReader br = new BufferedReader(isr);
//			String line = "";
//			while((line = br.readLine()) != null) {
//				line = line.trim();
//				if(line.length() <= 0) continue;
//				//                  12345678901234567
//				if(line.startsWith("sun.java.command=")) {
//					break;
//				}
//			}
//			br.close();
//			isr.close();
//			bais.close();
//		} catch (Exception e) {
//			// NONE
//		}
//		return name;
//	}

	public String[][] get() {
		names = new HashMap<>();
		mems = new HashMap<>();
		hdls = new HashMap<>();
		thds = new HashMap<>();
		HashMap<Integer, Long> map2 = CreateToolhelp32Snapshot();

		int[] is = new int[256];
		String[] ns = new String[256];
		for(int i=0; i<ns.length; i++) ns[i] = "";
		long[] ts = new long[256];
		long[] hs = new long[256];
		long[] ms = new long[256];
		long[] vs = new long[256];
		for(Integer id : map2.keySet()) {
			String n2 = names.get(id);
			Long t2 = thds.get(id);
			Long h2 = hdls.get(id);
			Long m2 = mems.get(id);
			Long v2 = map2.get(id);
			Long v1 = map1.get(id);
			if(v1 != null) {
				v2 -= v1;
			}
			if(v2 > 0) {
				for(int i=0; i<is.length; i++) {
					if(v2 > vs[i]) {
						String n_ = ns[i];
						long t_ = ts[i];
						long h_ = hs[i];
						long m_ = ms[i];
						long v_ = vs[i];
						int i_ = is[i];
						ns[i] = n2;
						ms[i] = m2;
						ts[i] = t2;
						hs[i] = h2;
						vs[i] = v2;
						is[i] = id;
						n2 = n_;
						m2 = m_;
						t2 = t_;
						h2 = h_;
						v2 = v_;
						id = i_;
					}
				}
			}
		}
		names = null;
		mems = null;
		hdls = null;
		thds = null;
		ArrayList<String[]> ls = new ArrayList<>();
		for(int i=0; i<is.length; i++) {
			if(ns[i].length() > 0) {
				int u = (int) ((vs[i] * 100 ) / 100000000L);
				if(ns[i].equalsIgnoreCase("java")
				|| ns[i].equalsIgnoreCase("javaw")) {
					String n = jnfo(is[i]);
					if(n != null) ns[i] += "[" + n + "]";
				}
				long v = (vs[i]/100000) % 10;
				String[] p = new String[5];
				p[0] = "" +is[i];
				p[1] = ns[i];
				p[2] = u + "%";
				p[3] = Util.size((vs[i]/100000), 0) + "." + v + " ms";
				p[4] = Util.size(ts[i],0) + " threads";
				//p[5] = "Mem:" + io.size(ms[i], 0);
				//p[6] = "Hdl:" + hs[i];
				ls.add(p);
			}
		}
		map1 = map2;
		String[][] pp = ls.toArray(new String[ls.size()][]);
		return pp;
	}
}
