package usb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import utils.UpdateUICallback;

public class SockectClient implements Runnable {
	private Socket mSocket = null;
	private static final int PORT = 54321;

	Thread mThread = null;

	public SockectClient() {
		mThread = new Thread(this);
	}

	private UpdateUICallback mUpdateUICallback = null;

	public void setUpdateUICallback(UpdateUICallback uiCallback) {
		this.mUpdateUICallback = uiCallback;
	}

	public boolean adbCmd() {
		String cmd = "adb forward tcp:54321 tcp:12345";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return true;
	}

	public void test() {
		try {
			mSocket = new Socket("127.0.0.1", PORT);
			System.out.println("socket:" + mSocket.toString());

			DataInputStream dis = new DataInputStream(mSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());

			while (true) {
				String data = "sendTime:" + System.currentTimeMillis();
				dos.writeUTF(data);
				dos.flush();

				final String s = dis.readUTF();
				if (!s.equals("")) {
					System.out.println("receive:" + s);
					Thread thread = new Thread() {
						@Override
						public void run() {
							mUpdateUICallback.updateMesFromUSBClient(s);
						}
					};
					thread.start();
				}
				Thread.sleep(50);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		mThread.start();
	}
	
	
	public void stop(){
		mThread.interrupt();
		mThread = null;
	}

	@Override
	public void run() {
		if (adbCmd()) {
			test();
		}

	}
}
