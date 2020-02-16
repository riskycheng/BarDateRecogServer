package deepsky.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread {

	public interface UpdateUICallback {
		void updateMsgFromClient(String clientIP, String message);
		
		void updateMesFromUSBClient(String message);
	}

	private UpdateUICallback mUpdateUICallback = null;

	public void setUpdateUICallback(UpdateUICallback uiCallback) {
		this.mUpdateUICallback = uiCallback;
	}

	public void run() {
		Socket client = null;
		String msg = "";
		try {
			while (true) {
				client = responseSocket();
				while (true) {
					msg = receiveMsg(client);
					System.out.println("�յ��ͻ�����Ϣ��" + msg);
					mUpdateUICallback.updateMsgFromClient(client.getInetAddress().getHostAddress(), msg);
					sendMsg(client, msg);
					if (true) {
						break;
					}
				}
				closeSocket(client);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("reset connection");
		}
	}

	public static final int PORT = 9999;
	ServerSocket ss;
	BufferedWriter bw;
	BufferedReader br;

	public void createSocket() throws IOException {
		ss = new ServerSocket(PORT);
		System.out.println("�������Ѿ�����������������");
	}

	public Socket responseSocket() throws IOException {
		Socket client = ss.accept();
		System.out.println("�ͻ����Ѿ����ӡ�����������");
		return client;
	}

	public void closeSocket(Socket s) throws IOException {
		br.close();
		bw.close();
		s.close();
		System.out.println("�ͻ����Ѿ��رա�����������");
	}

	public void sendMsg(Socket s, String msg) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		bw.write(msg + "\n");
		bw.flush();
	}

	public String receiveMsg(Socket s) throws IOException {
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String msg = br.readLine();
		System.out.println("�������յ��ͻ�����Ϣ��" + msg);
		return msg;
	}

	public SocketServer() throws IOException {
		createSocket();
	}

	public static void main(String args[]) throws IOException {
		SocketServer ss = new SocketServer();
		if (ss != null) {
			ss.start();
		}
	}

}