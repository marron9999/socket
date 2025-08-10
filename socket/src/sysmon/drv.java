package sysmon;

import java.util.ArrayList;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Mpr;
import com.sun.jna.platform.win32.Winnetwk;

public class drv extends info {

	private static final int NAME = 0;
	private static final int TYPE = 1;
	private static final int USED = 2;
	private static final int SPACE = 3;
	private static final int MAX_DRV = SPACE + 1;
	private info[] drv = new info[26];
	private byte[] rmv = new byte[26];
	
	public drv() {
		super('@', 0);
		get();
	}

	@Override
	public void get(ArrayList<String> list) {
		try {
			for(int i=0; i<drv.length; i++) {
				if(drv[i] != null) {
					byte[] b = drv[i].getByte();
					if(b != null) {
						list.add(new String(b, "ms932"));
					}
				} else {
					if(rmv[i] != 0) {
						rmv[i] = 0;
						String s = "" + (char)('A'+i);
						list.add(s);
					}
				}
			}
		} catch (Exception e) {
			// NONE
		}
	}

	public void get() {
		for(int i=0; i<26; i++) {
			String n = (char)('A'+i) + ":";
			int d = Kernel32.INSTANCE.GetDriveType(n);
			String s = null;
			switch(d) {
			case 2: s = "リムーバブルメディア"; break;
			case 3: s = "ハードディスク"; break;
			case 5: s = "CD-ROMドライブ"; break;
			case 6: s = "RAMディスク"; break;
			case 4: s = "ネットワークドライブ";
				try {
					Pointer p = new Memory(256);
					IntByReference sz = new IntByReference(256);
					Mpr.INSTANCE.WNetGetUniversalName(n,
							Winnetwk.UNIVERSAL_NAME_INFO_LEVEL, p, sz);
					s = p.getPointer(0).getWideString(0);
				} catch (Exception e) {
					// NONE
				}
				break;
			}
			if(s == null) {
				drv[i] = null;
				rmv[i] = 1;
				continue;
			}
			if(drv[i] == null) {
				drv[i] = new info((char)('A'+i), MAX_DRV);
				drv[i].label[NAME ] = n + " ラベル";
				drv[i].label[TYPE ] = n + " 種別";
				drv[i].label[USED ] = n + " 利用";
				drv[i].label[SPACE] = n + " 容量";
			}
			drv[i].value[NAME] = "";
			char volName[] = new char[256], fsName[] = new char[256];
			/*boolean ok =*/
			Kernel32.INSTANCE.GetVolumeInformation(n + "\\", volName, 256, null, null, null, fsName, 256);
			drv[i].value[NAME] += "[" + new String(volName).replace((char)0, ' ').trim() + "]";
			if(fsName[0] != 0) {
				drv[i].value[NAME] += "[" + new String(fsName).replace((char)0, ' ').trim() + "]";
			}
			drv[i].value[NAME] = drv[i].value[NAME].trim();
			drv[i].value[TYPE] = s;
			drv[i].value[USED] = "";
			drv[i].value[SPACE] = "";
		
			WinNT.LARGE_INTEGER freeBytes = new WinNT.LARGE_INTEGER();
			WinNT.LARGE_INTEGER totalBytes = new WinNT.LARGE_INTEGER();
			WinNT.LARGE_INTEGER totalFreeBytes = new WinNT.LARGE_INTEGER();
			Kernel32.INSTANCE.GetDiskFreeSpaceEx(n,
					freeBytes, totalBytes, totalFreeBytes);
			if(totalBytes.getValue() > 0) {
				drv[i].value[USED] = ksize(totalBytes.getValue() - totalFreeBytes.getValue(), 0);
				drv[i].value[USED] += "B " + (100 - (int)(totalFreeBytes.getValue() * 100 / totalBytes.getValue())) + "%";
				drv[i].value[SPACE] = ksize(totalBytes.getValue(), 0) + "B";
			}
		}
	}
}
