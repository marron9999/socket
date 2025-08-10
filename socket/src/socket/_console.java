package socket;


import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
//import com.sun.jna.platform.win32.WinUser; 
import com.sun.jna.ptr.IntByReference;

public class _console {
	private WinNT.HWND console;
	private WinNT.HANDLE stdout;
	private TrayIcon tray;
	private CheckboxMenuItem consmenu;
	private Kernel32 kernel32;
	private User32 user32;
	public static _console INSTANCE;
	static {
		INSTANCE = new _console();
		INSTANCE.kernel32 = Kernel32.INSTANCE;
		INSTANCE.user32 = User32.INSTANCE;
	}

	public void alloc(String text, boolean show) {
		kernel32.AllocConsole();
		console = kernel32.GetConsoleWindow();
		user32.ShowWindow(console, (show)? User32.SW_SHOWNORMAL : User32.SW_HIDE);
		stdout = kernel32.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
		IntByReference mode = new IntByReference(0);
		kernel32.GetConsoleMode(stdout, mode);
		kernel32.SetConsoleMode(stdout, mode.getValue()
				| Kernel32.ENABLE_PROCESSED_OUTPUT
				| Kernel32.ENABLE_VIRTUAL_TERMINAL_PROCESSING);
		kernel32.SetConsoleTitle(text);
	}

	protected void write(String line) {
		if(console == null) {
			System.out.print(line);
			return;
		}
		char [] buf = line.toCharArray();
		write(buf, buf.length);
	}

	protected void write(char[] buf, int len) {
		if(console == null) {
			String line = new String(buf, 0, len);
			System.out.print(line);
			return;
		}
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<len; i++) {
			if (buf[i] == 0x1f || buf[i] == 0x1e) continue;
			sb.append(buf[i]);
			if (buf[i] == '\n') {
				String t = sb.toString();
				kernel32.WriteConsole(stdout, t, t.length(), null, null);
				sb.setLength(0);
			}
		}
		if(sb.length() > 0) {
			String t = sb.toString();
			kernel32.WriteConsole(stdout, t, t.length(), null, null);
		}
	}

	protected void quit() {
    	if(tray != null) {
    		try { SystemTray.getSystemTray().remove(tray); } catch (Exception e) { }
       	}
	}

    public void toggle() {
	}

	protected PopupMenu tray(String name, String icon, ActionListener quit) {
        Image image = Toolkit.getDefaultToolkit().createImage(_base.class.getResource(icon));
        PopupMenu popup = new PopupMenu();
        if(console != null) {
        	consmenu = new CheckboxMenuItem("Console");
        	consmenu.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean show = user32.IsWindowVisible(console);
					user32.ShowWindow(console, show? User32.SW_HIDE : User32.SW_SHOWNORMAL);
					consmenu.setState(show? false : true);
				}
        	});
            popup.add(consmenu);
        }
        {
            MenuItem item = new MenuItem("Exit");
            item.addActionListener(quit);
            popup.add(item);
        }
        tray = new TrayIcon(image, name, popup);
        tray.setImageAutoSize(true);
        try { SystemTray.getSystemTray().add(tray); } catch (Exception e) { }
        if(console != null) {
    		boolean show = user32.IsWindowVisible(console);
    		consmenu.setState(show? true : false);
        }
        return popup;
	}
}
