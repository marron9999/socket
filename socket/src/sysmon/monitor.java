package sysmon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import socket.bthlib;

public class monitor implements Runnable, Listener {

	private String host = null;
	private String sec = null;
	private int port = 9999;
	private ServerSocket server_socket = null;
	private Socket socket = null;

	private Shell shell = null;
	private Display display = null;
	private Label label = null; 
	private Image image = null;
	private Image img50 = null;
	private Image img20 = null;
	private Image img10 = null;
	private int[] image_y = new int[1];
	private int avg_t = 0;
	private int avg_c = 0;
	private int image_h = 50;
	private Table table;
	private TableColumn table_c1;
	private TableColumn table_c2;
	private HashMap<String, String[]> map = new HashMap<>();
	
	public monitor() {
	}
	
	private void set(info info) {
		table.setRedraw(false);
		synchronized(map) {
			for(int i=0; i<info.label.length; i++) {
				String c = "" + info.id + i;
				String[] v = map.get(c);
				if(v == null) {
					v = new String[2];
					v[0] = "";
					v[1] = "";
					map.put(c, v);
				}
				if(info.label[i].length() > 0) {
					v[0] = info.label[i];
					v[1] = info.value[i];
				}
			}
			Set<String> ks = map.keySet();
			String[] key = ks.toArray(new String[ks.size()]);
			if(info.label.length == 0) {
				for(int i=0; i<key.length; i++) {
					if(key[i].charAt(0) == info.id) {
						map.remove(key[i]);
					}
				}
				ks = map.keySet();
				key = ks.toArray(new String[ks.size()]);
			}
			Arrays.sort(key);
			while(table.getItemCount() > key.length) {
				table.remove(table.getItemCount() - 1);
			}
			while(table.getItemCount() < key.length) {
				new TableItem(table, SWT.NONE);
			}
			for(int i=0; i<key.length; i++) {
				TableItem item = table.getItem(i);
				if(item == null) {
					item = new TableItem(table, SWT.NONE);
				}
				String[] v = map.get(key[i]);
				item.setText(new String[] { v[0], v[1].split("\f")[0] });
			}
		}
		table_c1.pack();
		table_c2.pack();
		table.setRedraw(true);
		table.redraw();
	}
	
	private void copy() {
		String text = "";
		synchronized(map) {
			Set<String> ks = map.keySet();
			String[] key = ks.toArray(new String[ks.size()]);
			Arrays.sort(key);
			for(int i=0; i<key.length; i++) {
				String[] v = map.get(key[i]);
				text += v[0] + "\t" + v[1].split("\f")[0] + "\n";
			}
		}
		Clipboard clipboard = new Clipboard(display);
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[]{text}, new Transfer[]{textTransfer});
		clipboard.dispose();
	}
	
	private Image init(int width) {
		Image img = new Image (display, width, image_h);
		GC gc = new GC (img);
		gc.setBackground (new Color(255,255,255));
		gc.fillRectangle(0, 0, width, image_h);
		gc.dispose ();
		return img;
	}
	
	private Image draw(Image image, int[] y, int mlt) {
        Rectangle rc0 = image.getBounds();
        Rectangle rc = label.getBounds();
        Image img = new Image (display, rc.width, image_h);
        GC gc = new GC (img);
        gc.setBackground (new Color(255,255,255));
        gc.fillRectangle(rc);
        gc.drawImage(image, rc.width - rc0.width - 3, 0);
        for(int j=y.length-1; j>=0; j--) {
            gc.setForeground(
            		(j==0)? new Color(0, 0, 0) :
           			new Color(0, 0, 192));
            gc.drawLine(
            	rc.width - 3, image_h - image_y[j] * mlt / 2 - 1,
            	rc.width - 1, image_h - y      [j] * mlt / 2 - 1);
        }
        gc.dispose ();
        return img;
	}
	
	private void image() {
    	int[] y = new int[1];
        synchronized(map) {
        	Set<String> ks = map.keySet();
        	String[] key = ks.toArray(new String[ks.size()]);
        	Arrays.sort(key);
        	for(int i=0; i<key.length; i++) {
        		String[] v = map.get(key[i]);
        		if(v[1].indexOf("\f") > 0) {
        			String[] v1 = v[1].split("\f");
        	    	y = new int[v1.length - 1];
        	    	if(image_y.length != v1.length - 1)
        	    		image_y = new int[v1.length - 1];
        	    	for(int j=0; j<y.length; j++) {
            			try { y[j] = Integer.parseInt(v1[j + 1]); } catch (Exception e) { }
        	    	}
        			break;
        		}
        	}
        }
        Image ix01 = draw(image, y, 1);
        Image ix02 = draw(img50, y, 2);
        Image ix05 = draw(img20, y, 5);
        Image ix10 = draw(img10, y, 10);
        for(int j=0; j<y.length; j++) {
            image_y[j] = y[j];
        }
        avg_c++;
        if(avg_c > 10) {
        	avg_c--;
        	avg_t -= avg_t / 10;
        }
    	avg_t += image_y[0];
		String t = shell.getText().split(" ")[0];
    	if(avg_t / avg_c > 45) {
            label.setImage(ix01);
    		shell.setText(t);
    	} else if(avg_t / avg_c > 15) {
            label.setImage(ix02);
    		shell.setText(t + " [x2]");
    	} else if(avg_t / avg_c > 9) {
            label.setImage(ix05);
    		shell.setText(t + " [x5]");
    	} else {
            label.setImage(ix10);
    		shell.setText(t + " [x10]");
    	}
        image.dispose();
        img50.dispose();
        img20.dispose();
        img10.dispose();
        image = ix01;
        img50 = ix02;
        img20 = ix05;
        img10 = ix10;
        label.redraw();
 	}

	@Override
	public void handleEvent(Event arg0) {
		try {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					Rectangle rect = shell.getBounds();
					bthlib.DefProfile(host);
					bthlib.SetProfInt(sec, "left", rect.x);
					bthlib.SetProfInt(sec, "top", rect.y);
					bthlib.SetProfInt(sec, "width", rect.width);
					bthlib.SetProfInt(sec, "height", rect.height);
				}
			});
		} catch (Exception e) {
			// NONE
		}
	}
	
	private void frame(String host, String sec) {
		this.host = host;
		this.sec = sec;
		display = new Display();
		shell = new Shell(display, SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.TOOL) ;
		shell.setText(host);
		bthlib.DefProfile(host);
		int width  = bthlib.GetProfInt(sec, "width");
		int height = bthlib.GetProfInt(sec, "height");
		if(width > 0) {
			int left   = bthlib.GetProfInt(sec, "left");
			int top    = bthlib.GetProfInt(sec, "top");
			shell.setLocation(left, top);
		} else {
			width = 200;
			height = 400;
		}
		shell.setSize(width, height);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);
		//shell.setBackground(new Color(224, 224, 224));
		shell.addListener(SWT.Close, this);

		if(sec.equalsIgnoreCase("sysmon")) {
			image = init(width);
			img50 = init(width);
			img20 = init(width);
			img10 = init(width);
			//image_y = 0;
			
			label = new Label(shell, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = image_h;
			label.setLayoutData(gd);
			label.setImage(image);
		}

		table = new Table(shell, SWT.NONE);
		{
			GridData gd = new GridData(GridData.FILL_BOTH);
			table.setLayoutData(gd);
		}
		table.setHeaderVisible(false);
		table_c1 = new TableColumn(table, SWT.LEFT);
		table_c1.setText("Name");
		table_c1.setWidth(1);
		table_c2 = new TableColumn(table, SWT.LEFT);
		table_c2.setText("value");
		table_c2.setWidth(1);
		table_c1.pack();
		table_c2.pack();

		Menu menu = new Menu(shell, SWT.POP_UP);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("Copy to clipboard");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) { copy(); }
		});
		table.setMenu(menu);
		
		shell.open();
		if(label != null) {
			display.timerExec(1000, new Runnable() {
		        public void run() {
		          if (shell.isDisposed()) return;
		          image();
		          display.timerExec(1000, this);
		        }
			});
		}
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		if(image != null) {
			image.dispose();
			img50.dispose();
			img20.dispose();
			img10.dispose();
		}
		shell = null;
		display.dispose();
	}

	@Override
	public void run() {
		Thread.currentThread().setName("monitor");
		//System.out.println("start");
		try {
			server_socket = new ServerSocket(port);
			socket = server_socket.accept();
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "ms932");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				byte[] b = line.getBytes("MS932");
				info info = new info(' ', 0); 
				info.parse(b);
				try {
					display.syncExec(new Runnable() {
						@Override
						public void run() { set(info); }
					});
				} catch (Exception e) {
					// NONE
				}
			}
			br.close();
			isr.close();
			//System.out.println("stop");
		} catch (Exception e) {
			// NONE
		}
		try {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					try { shell.close(); } catch (Exception e) { }
				}
			});
		} catch (Exception e) {
			// NONE
		}
		try {
			socket.close();
		} catch (Exception e) {
			// NONE
		}
		try {
			server_socket.close();
		} catch (Exception e) {
			// NONE
		}
		Thread.currentThread().setName("-");
	}

	public static void main(String[] args) {
		monitor mon = new monitor();
		try {
			try { mon.port = Integer.parseInt(args[1]); } catch (Exception e) { }
			Thread t = new Thread(mon);
			t.start();
			mon.frame(args[0], args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static Socket open(String host, String sec) {
		try {
			int port = 0;
			Socket socket = null;
			if(port <= 0) {
				socket = new Socket();
			    socket.bind(null);
				port = socket.getLocalPort();
			    socket.close();
			    String[] cmd = {
		    		"cmd.exe", "/c",
			    	System.getProperty("java.home") + "\\bin\\javaw.exe",
			    	"-cp", System.getProperty("java.class.path"), 
			    	monitor.class.getTypeName(), host, "" + port, sec};
			    try {
					//System.out.println(String.join(" ", cmd));
				    Runtime.getRuntime().exec(cmd);
			    } catch (Exception e) {
					e.printStackTrace();
				}
			}

		    socket = null;
			int c = 0;
			while (socket == null) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// NONE
				}
				try {
					socket = new Socket("localhost", port);
				} catch (Exception e) {
					//e.printStackTrace();
					socket = null;
					if(++c > 10) break;
				}
			}
			return socket;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Socket open(int port) {
		try {
			Socket socket = null;
			int c = 0;
			while (socket == null) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// NONE
				}
				try {
					socket = new Socket("localhost", port);
				} catch (Exception e) {
					//e.printStackTrace();
					socket = null;
					if(++c > 10) break;
				}
			}
			return socket;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
