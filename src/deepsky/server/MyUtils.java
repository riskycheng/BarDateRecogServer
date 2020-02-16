package deepsky.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MyUtils {

	public static String getLocalIP() {
		String IPAddr = null;

		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IPAddr = addr.getHostAddress();
		return IPAddr;
	}
}
