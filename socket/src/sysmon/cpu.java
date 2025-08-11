package sysmon;

import java.util.ArrayList;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.PowrProf;

public class cpu extends info {
	private cpulib lib = new cpulib();
	//private WinBase.FILETIME i1;
	//private WinBase.FILETIME k1;
	//private WinBase.FILETIME u1;

	private static final int CPU = 0;
	private static final int NAME = 1;
	private static final int CLOCK = 2;
	private static final int LGCPU = 3;
	private static final int CORE = 4;
	private static final int MEM = 5;
	private static final int SIZE = 6;
	private static final int PROC = 7;
	private static final int HNDL = 8;
	private static final int MAX = HNDL+ 1;
	
	public cpu() {
		super('#', MAX);
		label[CPU  ] = "CPU Used";
		label[CLOCK] = "Clock";
		label[CORE ] = "Core";
		label[LGCPU] = "Processer";
		label[NAME ] = "CPU Name";
		label[MEM  ] = "Mem Used";
		label[SIZE ] = "Mem Total";
		label[PROC ] = "Process";
		label[HNDL ] = "Handle";
		
		//i1 = new WinBase.FILETIME();
		//k1 = new WinBase.FILETIME();
		//u1 = new WinBase.FILETIME();
		//Kernel32.INSTANCE.GetSystemTimes(i1, k1, u1);
		lib.get();

		if(value[NAME] == null) {
			value[NAME] = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
					"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
					"ProcessorNameString");
		}
		WinBase.SYSTEM_INFO info = new WinBase.SYSTEM_INFO();
		Kernel32.INSTANCE.GetSystemInfo(info);
		int numProcs = info.dwNumberOfProcessors.intValue();
		//if(value[LGCPU] == null) 
		{
			// value[SLOT] = System.getenv("NUMBER_OF_PROCESSORS");
			value[LGCPU] = "" + numProcs;
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
			value[CORE] = "" + physicalProcessorCount;
		}
		int bufferSize = 1;
		Memory mem = new Memory(bufferSize);
		PowrProf.INSTANCE.CallNtPowerInformation(PowrProf.POWER_INFORMATION_LEVEL.ProcessorInformation,
				null, 0, mem, bufferSize);
		WinNT.PROCESSOR_POWER_INFORMATION ppi = new WinNT.PROCESSOR_POWER_INFORMATION();
		bufferSize = ppi.size() * numProcs;
		mem = new Memory(bufferSize);
		PowrProf.INSTANCE.CallNtPowerInformation(PowrProf.POWER_INFORMATION_LEVEL.ProcessorInformation,
				null, 0, mem, bufferSize);
		//long[] freqs = new long[numProcs];
		{	int i = 0;
			ppi = new WinNT.PROCESSOR_POWER_INFORMATION(mem.share(i * (long) ppi.size()));
		}
		value[CLOCK] = /*ppi.CurrentMhz + "MHz / Max " +*/ size(ppi.MaxMhz, 0) + "MHz";
	}

	//private long L(WinBase.FILETIME ft) {
	//	long v = ((long) ft.dwLowDateTime) & 0x00ffffffffL;
	//	return ((long) ft.dwHighDateTime) << 32 | v;
	//}
	
	@Override
	public void get(ArrayList<String> list) {
		try {
			byte[] b = getByte();
			if(b != null) {
				list.add(new String(b, "ms932"));
			}
		} catch (Exception e) {
			// NONE
		}
	}

	public void get() {
		int[] u = lib.get();
		//WinBase.FILETIME i2 = new WinBase.FILETIME();
		//WinBase.FILETIME k2 = new WinBase.FILETIME();
		//WinBase.FILETIME u2 = new WinBase.FILETIME();
		//Kernel32.INSTANCE.GetSystemTimes(i2, k2, u2);
		//long idle1 = L(i1);
		//long idle2 = L(i2);
		//long time1 = L(k1) + L(u1) - idle1; 
		//long time2 = L(k2) + L(u2) - idle2;
		//long full1 = time1 + idle1; 
		//long full2 = time2 + idle2;
		//long diff1 = time2 - time1; 
		//long diff2 = full2 - full1;

		//int u = 100;
		//if(idle1 != idle2) {
		//	u = (int) ((diff1 * 100 ) / diff2);
		//}
		value[CPU] = u[0] + "% (User " + u[1] + "%)";
		value[CPU] += "\f" + u[0] + "\f" + u[1];

		Psapi.PERFORMANCE_INFORMATION pi = new Psapi.PERFORMANCE_INFORMATION();
		/*BOOL rc =*/ Psapi.INSTANCE.GetPerformanceInfo(pi, pi.size());
		//int u = 0;
		long ub = 0;
		long tb = 0;
		if(pi.PageSize.longValue() > 0) {
			tb = pi.PhysicalTotal.longValue() * pi.PageSize.longValue();
			long fb = pi.PhysicalAvailable.longValue() * pi.PageSize.longValue();
			ub = tb - fb;
			u[0] = 100 - (int)(fb * 100 / tb);
		}
		value[MEM] = ksize(ub, 0) + "B " + u[0] + "%";
		value[SIZE] = ksize(tb, 0) + "B";
		value[PROC] = size(pi.ProcessCount.intValue(), 0);
		value[HNDL] = size(pi.HandleCount.intValue(), 0);

		//i1 = i2;
		//k1 = k2;
		//u1 = u2;
	}
}
