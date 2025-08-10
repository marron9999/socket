package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class _ssocket {
	ServerSocket net;
	bthlib bth;
	_logger log;

	public _ssocket(int port, _logger log) throws IOException {
		this.log = log;
		net = new ServerSocket(port);
	}
	public _ssocket(_logger log) throws IOException {
		this.log = log;
		bthlib.log = log;
		String h = System.getenv("COMPUTERNAME");
		bth = bthlib.socket(h.toUpperCase());
}

    public void close() throws IOException {
    	if(net != null) {
        	net.close();
        	net = null;
    	}
    	if(bth != null) {
        	bth.close();
        	bth = null;
    	}
    }
	
    public _socket accept() throws IOException {
    	if(net != null) {
    		try {
            	Socket s = net.accept(); 
            	return new _socket(s);
			} catch (Exception e) {
				// NONE
			}
    	}
    	if(bth != null) {
    		try {
	    		bthlib b = bth.accept();
	        	return new _socket(b);
			} catch (Exception e) {
				// NONE
			}
    	}
    	return null;
    }

    public void join(Thread t) throws Exception {
    	if(bth != null) {
    		t.join();
    	}
    }
}
