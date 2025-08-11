package sysmon;

import java.util.ArrayList;

import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.Dxva2;
import com.sun.jna.platform.win32.PhysicalMonitorEnumerationAPI;
import com.sun.jna.platform.win32.LowLevelMonitorConfigurationAPI;

public class dsp extends info {

	private static final int NAME = 0;
	private static final int RECT = 1;
	private static final int WORK = 2;
	private static final int V_HZ = 3;
	private static final int ADPT = 4;
	private static final int MAX_DSP = ADPT + 1;
	private info[] dsp = new info[8];

	public dsp() {
		super('0', 0);
		get();
	}

	public void get() {
		new dsplib() {
			@Override
		    protected void enumerate(WinUser.HMONITOR hMonitor)
		    {
				{
					int bn = no;
					super.enumerate(hMonitor);
					no = bn;
				}
				if(dsp[no] == null) {
					dsp[no] = new info((char)('0' + no), MAX_DSP);
					dsp[no].label[NAME] = "Monitor " + (char)('0' + no) + " Name";
					dsp[no].label[RECT] = "Monitor " + (char)('0' + no) + " Rect";
					dsp[no].label[WORK] = "Monitor " + (char)('0' + no) + " Work";
					dsp[no].label[V_HZ] = "Monitor " + (char)('0' + no) + " Vert";
					dsp[no].label[ADPT] = "Monitor " + (char)('0' + no) + " Adaptor";
				}

//		        boolean isPrimary = (info.dwFlags & WinUser.MONITORINFOF_PRIMARY) != 0;
//		        System.out.println("Primary? " + (isPrimary ? "yes" : "no"));
		        //System.out.println("Device " + new String(info.szDevice));
		        WinDef.DWORDByReference pdwNumberOfPhysicalMonitors = new WinDef.DWORDByReference();
		        Dxva2.INSTANCE.GetNumberOfPhysicalMonitorsFromHMONITOR(hMonitor, pdwNumberOfPhysicalMonitors);
		        int monitorCount = pdwNumberOfPhysicalMonitors.getValue().intValue();

//		        System.out.println("HMONITOR is linked to " + monitorCount + " physical monitors");

		        PhysicalMonitorEnumerationAPI.PHYSICAL_MONITOR[] physMons = new PhysicalMonitorEnumerationAPI.PHYSICAL_MONITOR[monitorCount];
		        Dxva2.INSTANCE.GetPhysicalMonitorsFromHMONITOR(hMonitor, /*monitorCount*/1, physMons);

		        //for (int i = 0; i < monitorCount; i++)
		        { int i = 0;
		        	WinNT.HANDLE hPhysicalMonitor = physMons[0].hPhysicalMonitor;
		            //System.out.println("Monitor " + di.no + " - " + new String(physMons[i].szPhysicalMonitorDescription));
			        String s = new String(physMons[i].szPhysicalMonitorDescription);
		    		dsp[no].value[NAME] = s.replace(""+(char)0x00, "");

		            //System.out.println("Screen " + info.rcMonitor);
		    		dsp[no].value[RECT] = ("" + miex[no].rcMonitor).replace("[", "").replace("]", "");
		            //System.out.println("Work area " + info.rcWork);
		    		dsp[no].value[WORK] = ("" + miex[no].rcWork).replace("[", "").replace("]", "");
		     
		    		LowLevelMonitorConfigurationAPI.MC_TIMING_REPORT pmtrMonitorTimingReport = new LowLevelMonitorConfigurationAPI.MC_TIMING_REPORT();
		            Dxva2.INSTANCE.GetTimingReport(hPhysicalMonitor, pmtrMonitorTimingReport);
		            //System.out.println("HorizontalFrequencyInHZ " + pmtrMonitorTimingReport.dwHorizontalFrequencyInHZ);
		            //System.out.println("VerticalFrequencyInHZ " + pmtrMonitorTimingReport.dwVerticalFrequencyInHZ);
		    		dsp[no].value[V_HZ] = (pmtrMonitorTimingReport.dwVerticalFrequencyInHZ.intValue() / 100) + "Hz";
		        }

		        Dxva2.INSTANCE.DestroyPhysicalMonitors(monitorCount, physMons);

		        DISPLAY_DEVICE dd = this.new DISPLAY_DEVICE();
		        User32X.INSTANCE.EnumDisplayDevicesA(null, no, dd, 0);
		        String s = new String(dd.DeviceString);
				dsp[no].value[ADPT] = s.replace(""+(char)0x00, "");
		        
		        no++;
		    }
		};
	}

	@Override
	public void get(ArrayList<String> list) {
		try {
			for(int i=0; i<dsp.length; i++) {
				if(dsp[i] != null) {
					byte[] b = dsp[i].getByte();
					if(b != null) {
						list.add(new String(b, "ms932"));
					}
				}
			}
		} catch (Exception e) {
			// NONE
		}
	}
}
