package socket;

import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class _socket {
	private Socket net;
	private bthlib bth;
	public _socket(Socket _net) {
		net = _net;
	}
	public _socket(bthlib _bth) {
		bth = _bth;
	}
    public _socket(String host, int port) throws UnknownHostException, IOException {
    	net = new Socket(host, port);
    }

    public String getHostName() {
    	if(net != null) {
        	return net.getInetAddress().getHostName();
    	}
    	if(bth != null) {
        	return bth.getHostName();
    	}
    	return null;
    }
    //public InetAddress getInetAddress() {
    //	if(net == null) return null;
    //	return net.getInetAddress();
    //}
    public int getPort() {
    	if(net == null) return 0;
    	return net.getPort();
    }
    //public OutputStream getOutputStream() throws IOException {
    //	if(net == null) return null;
    //	return net.getOutputStream();
    //}
    //public InputStream getInputStream() throws IOException {
    //	if(net == null) return null;
    //	return net.getInputStream();
    //}

    public synchronized void close() throws IOException {
    	if(net != null) {
        	net.close();
        	net = null;
    	}
    	if(bth != null) {
        	bth.close();
        	bth = null;
    	}
    }

    public Long getBthStream() {
    	return bth.getStream();
    }

    public int read(byte b[]) throws IOException {
    	if(net != null) {
            return net.getInputStream().read(b);
    	}
    	if(bth != null) {
            return bth.recv(b, 0, b.length);
    	}
    	return -1;
    }
    public int read(byte b[], int pos, int len) throws IOException {
    	if(net != null) {
            return net.getInputStream().read(b, pos, len);
    	}
    	if(bth != null) {
            return bth.recv(b, pos, len);
    	}
    	return -1;
    }

    public void write(byte b[]) throws IOException {
    	if(net != null) {
            net.getOutputStream().write(b);
    	}
    	if(bth != null) {
            bth.send(b, b.length);
    	}
	}
    public void write(byte b[], int len) throws IOException {
    	if(net != null) {
            net.getOutputStream().write(b, 0, len);
    	}
    	if(bth != null) {
            bth.send(b, len);
    	}
	}
}
