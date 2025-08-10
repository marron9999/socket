import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class server {

	protected void LOG(Object cls, Exception e) { }
	protected void LOG(Object cls, String arg) { }
	
	protected Tomcat server;

	public void stop_tomcat() {
		LOG(this, "accept: stop_tomcat()");
		try {
		    Thread t = new Thread() {
		    	@Override
		    	public void run() {
		    		try {
		    			server.stop();
		    			LOG(this, "success to stop");
		    		} catch (Exception ex) {
		    			LOG(this, "Failed to stop");
		    		}
		    	}
		    };
		    t.start();
		    t.join();
		} catch (Exception e) {
			LOG(this, e);
		}
	}
	
//	protected void builder_vmhost(int port) {
//		try {
//			HttpClient client = HttpClient.newHttpClient();
//			HttpRequest request = HttpRequest.newBuilder(
//					URI.create("http://localhost:" + port + "/builder/builder_api?vmhost=all"))
//					.build();
//			//HttpResponse<String> response =
//			client.send(request, HttpResponse.BodyHandlers.ofString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public void run(int port) throws Exception {
		LOG(this, "Wait tomcat.thread");

		server = new Tomcat();
		server.setPort(port);

		{
			File work = new File("../logs");
			try { work = work.getCanonicalFile(); } catch (Exception e) { }
			server.setBaseDir(work.getAbsolutePath());
			server.addWebapp("", work.getAbsolutePath());
		}

		deploy();

		LOG(this, "Start tomcat.thread, port:" + port);
		server.start();
		server.getService().addConnector(server.getConnector());
//		if(builder) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					builder_vmhost(port);
//				}
//			}).start();
//		}
		server.getServer().await();
		LOG(this, "exit tomcat.thread");
	}

	protected Context deploy(String root, String path) {
//		if(root.equalsIgnoreCase("/builder")) builder = true;
        File dir = new File(path);
		try { dir = dir .getCanonicalFile(); } catch (Exception e) { }
		Context context = server.addWebapp(root, dir.getAbsolutePath());
		return context;
	}
	
	protected void deploy() {
		deploy("/server", "../server/WebContent");
	}
	
	public static void main(String[] args) {
		try {
			int port = 8080;
			if(args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			server instance = new server();
			instance.run(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
