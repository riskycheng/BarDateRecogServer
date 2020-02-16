package deepsky.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import utils.UpdateUICallback;

public class SocketServer extends Thread {

	

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
					System.out.println("收到客户端消息：" + msg);
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
		System.out.println("服务器已经开启······");
	}

	public Socket responseSocket() throws IOException {
		Socket client = ss.accept();
		System.out.println("客户端已经连接······");
		return client;
	}

	public void closeSocket(Socket s) throws IOException {
		br.close();
		bw.close();
		s.close();
		System.out.println("客户端已经关闭······");
	}

	public void sendMsg(Socket s, String msg) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		bw.write(msg + "\n");
		bw.flush();
	}

	public String receiveMsg(Socket s) throws IOException {
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String msg = br.readLine();
		System.out.println("服务器收到客户端消息：" + msg);
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