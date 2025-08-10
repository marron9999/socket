package win32;


import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

public class CpuInfo {
	private WinBase.FILETIME i1;
	private WinBase.FILETIME k1;
	private WinBase.FILETIME u1;

	public CpuInfo() {
		i1 = new WinBase.FILETIME();
		k1 = new WinBase.FILETIME();
		u1 = new WinBase.FILETIME();
		Kernel32.INSTANCE.GetSystemTimes(i1, k1, u1);
	}

	private long L(WinBase.FILETIME ft) {
		long v = ((long) ft.dwLowDateTime) & 0x00ffffffffL;
		return ((long) ft.dwHighDateTime) << 32 | v;
	}

	public String[] _get() {
		String[] p = new String[8];
		//p[0][0] = "CPU使用率";
		p[0] = "0%";
		//p[1][0] = "メモリ使用率";
		p[1] = "0%";
		//p[2][0] = "メモリ量";
		p[2] = "0/0";
		//p[3][0] = "プロセス数";
		p[3] = "0";
		//p[4][0] = "ハンドル数";
		p[4] = "0";
		//p[5][0] = "コア数";
		p[5] = "0";
		//p[6][0] = "プロセッサー数";
		p[6] = System.getenv("NUMBER_OF_PROCESSORS");
		//p[7][0] = "プロセッサー名";
		p[7] = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
				"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
				"ProcessorNameString");
		WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION[] lpis =
				Kernel32Util.getLogicalProcessorInformation();
		int physicalProcessorCount = 0;
		for(WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION lpi : lpis) {
			switch(lpi.relationship) {
			case WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorCore:
				physicalProcessorCount++;
				break;
			}
		}	
		p[5] = "" + physicalProcessorCount;
		return p;
	}

	public String[] get() {
		WinBase.FILETIME i2 = new WinBase.FILETIME();
		WinBase.FILETIME k2 = new WinBase.FILETIME();
		WinBase.FILETIME u2 = new WinBase.FILETIME();
		Kernel32.INSTANCE.GetSystemTimes(i2, k2, u2);
		long idle1 = L(i1);
		long idle2 = L(i2);
		long time1 = L(k1) + L(u1) - idle1; 
		long time2 = L(k2) + L(u2) - idle2;
		long full1 = time1 + idle1; 
		long full2 = time2 + idle2;
		long diff1 = time2 - time1; 
		long diff2 = full2 - full1;

		int u = 100;
		if(idle1 != idle2) {
			u = (int) ((diff1 * 100 ) / diff2);
		}
		String[] p = _get();
		p[0] = u + "%";

		Psapi.PERFORMANCE_INFORMATION pi = new Psapi.PERFORMANCE_INFORMATION();
		/*BOOL rc =*/ Psapi.INSTANCE.GetPerformanceInfo(pi, pi.size());
		//int u = 0;
		long ub = 0;
		long tb = 0;
		if(pi.PageSize.longValue() > 0) {
			tb = pi.PhysicalTotal.longValue() * pi.PageSize.longValue();
			long fb = pi.PhysicalAvailable.longValue() * pi.PageSize.longValue();
			ub = tb - fb;
			u = 100 - (int)(fb * 100 / tb);
		}
		p[1] = u + "%";
		p[2] = Util.ksize(ub, 0) + "/" + Util.ksize(tb, 0).trim();
		p[3] = Util.size(pi.ProcessCount.intValue(), 0);
		p[4] = Util.size(pi.HandleCount.intValue(), 0);

		i1 = i2;
		k1 = k2;
		u1 = u2;
		return p;
	}
}
