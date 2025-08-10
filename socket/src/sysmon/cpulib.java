package sysmon;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;

public class cpulib {
	private WinBase.FILETIME i1 = new WinBase.FILETIME();
	private WinBase.FILETIME k1;
	private WinBase.FILETIME u1;

	
	public cpulib() {
		i1.dwHighDateTime = i1.dwLowDateTime = 0;
		//i1 = new WinBase.FILETIME();
		//k1 = new WinBase.FILETIME();
		//u1 = new WinBase.FILETIME();
		//Kernel32.INSTANCE.GetSystemTimes(i1, k1, u1);
	}

	public int[] get() {
		WinBase.FILETIME i2 = new WinBase.FILETIME();
		WinBase.FILETIME k2 = new WinBase.FILETIME();
		WinBase.FILETIME u2 = new WinBase.FILETIME();
		Kernel32.INSTANCE.GetSystemTimes(i2, k2, u2);
		
		int u[] = new int[] {0, 0};
		long idle1 = L(i1);
		if(idle1 > 0) {
			long idle2 = L(i2);
			long time1 = L(k1) + L(u1) - idle1; 
			long time2 = L(k2) + L(u2) - idle2;
			long full1 = time1 + idle1; 
			long full2 = time2 + idle2;
			long diff1 = time2 - time1; 
			long diff2 = full2 - full1;
			u[0] = 100;
			if(idle1 != idle2) {
				u[0] = (int) ((diff1 * 100 ) / diff2);
			}

			time1 = L(u1); 
			time2 = L(u2);
			full1 = time1 + idle1; 
			full2 = time2 + idle2;
			diff1 = time2 - time1; 
			diff2 = full2 - full1;
			u[1] = 100;
			if(idle1 != idle2) {
				u[1] = (int) ((diff1 * 100 ) / diff2);
			}

			//time1 = L(k1) - idle1; 
			//time2 = L(k2) - idle2;
			//full1 = time1 + idle1; 
			//full2 = time2 + idle2;
			//diff1 = time2 - time1; 
			//diff2 = full2 - full1;
			//u[2] = 100;
			//if(idle1 != idle2) {
			//	u[2] = (int) ((diff1 * 100 ) / diff2);
			//}
		}
		
		i1 = i2;
		k1 = k2;
		u1 = u2;

		return u;
	}

	private long L(WinBase.FILETIME ft) {
		long v = ((long) ft.dwLowDateTime) & 0x00ffffffffL;
		return ((long) ft.dwHighDateTime) << 32 | v;
	}
	
}
