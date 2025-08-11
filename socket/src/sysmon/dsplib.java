package sysmon;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.Structure.FieldOrder;

public class dsplib {

    @FieldOrder({"cb", "DeviceName", "DeviceString", "StateFlags", "DeviceID", "DeviceKey"})
    public class DISPLAY_DEVICE extends Structure {
		public int cb;
		public byte[] DeviceName;
    	public byte[] DeviceString;
		public int StateFlags;
		public byte[] DeviceID;
		public byte[] DeviceKey;
        public DISPLAY_DEVICE() {
        	DeviceName = new byte[32];
        	DeviceString = new byte[128];
        	DeviceID = new byte[128];
        	DeviceKey = new byte[128];
        	cb = size();
        }
    }
	
    @SuppressWarnings("deprecation")
    protected interface User32X extends Library {
    	User32X INSTANCE = (User32X) Native.loadLibrary("user32.dll", User32X.class);
    	boolean EnumDisplayDevicesA(Pointer lpDevice, int iDevNum, DISPLAY_DEVICE lpDisplayDevice, int dwFlags);
    }

    protected int no = 0;
	protected WinUser.MONITORINFOEX[] miex = new WinUser.MONITORINFOEX[8];
;

	public dsplib() {
    	no = 0;
        //System.out.println("Installed Physical Monitors: " + User32.INSTANCE.GetSystemMetrics(WinUser.SM_CMONITORS));
        User32.INSTANCE.EnumDisplayMonitors(null, null, new WinUser.MONITORENUMPROC() {
            @Override
            public int apply(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT rect, WinDef.LPARAM lparam)
            {
                enumerate(hMonitor);
                return 1;
            }
        }, new WinDef.LPARAM(0));
	}

	protected void enumerate(WinUser.HMONITOR hMonitor)
    {
		miex[no] = new WinUser.MONITORINFOEX();
        User32.INSTANCE.GetMonitorInfo(hMonitor, miex[no]);
        no++;
    }

	public byte[] capture(int dn) {
		if(dn < 0) dn = 0;
		if(dn < miex.length
		&& miex[dn] != null) {
			try {
				java.awt.Rectangle rc = new java.awt.Rectangle();
				rc.x = miex[dn].rcMonitor.left;
				rc.y = miex[dn].rcMonitor.top;
				rc.width  = miex[dn].rcMonitor.right  - miex[dn].rcMonitor.left;
				rc.height = miex[dn].rcMonitor.bottom - miex[dn].rcMonitor.top;
				Robot robot = new Robot();
				BufferedImage bi = robot.createScreenCapture(rc);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bi, "PNG", baos);
				byte[] buf = baos.toByteArray();
				baos.close();
				return buf;
			} catch (Exception e) {
				// NONE: handle exception
			}
		}
		return null;

	}
}
