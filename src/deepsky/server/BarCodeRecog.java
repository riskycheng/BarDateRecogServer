package deepsky.server;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import bluetooth.BluetoothService;
import usb.SockectClient;
import usb.USBAccessityUtils;
import utils.UpdateUICallback;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.SystemColor;
import java.awt.Color;
import java.awt.Dialog;

public class BarCodeRecog extends JFrame {

	public final static String ACTION_TYPE_GENERATE_QRCODE = "generateQRCode";
	public final static String ACTION_TYPE_START_SERVICE_WIFI = "startServiceWiFi";
	public final static String ACTION_TYPE_STOP_SERVICE_WIFI = "stopServiceWiFi";
	public final static String ACTION_TYPE_START_SERVICE_USB = "startServiceUSB";
	public final static String ACTION_TYPE_STOP_SERVICE_USB = "stopServiceUSB";
	public final static String ACTION_TYPE_START_SERVICE_BLUETOOTH = "startServiceBlueTooth";
	public final static String ACTION_TYPE_STOP_SERVICE_BLUETOOTH = "stopServiceBlueTooth";

	private SocketServer mServer = null;

	//USB 通信相关
	private SockectClient mUSBSocketClient = null;
	private USBAccessityUtils mUsbAccessityUtils = null;
	
	JLabel lblQrcode = new JLabel();
	JLabel label_IP = new JLabel("");
	JButton buttonStartService = new JButton("启动WiFi服务");
	JButton buttonStartUSBService = new JButton("启动USB服务");

	JButton buttonStopService = new JButton("停止WiFi服务");
	JButton buttonStopUSBService = new JButton("停止USB服务");

	BluetoothService mBlueToothService = null;
	JButton buttonStartBlueToothService = new JButton("启动蓝牙服务");
	JButton buttonStopBlueToothService = new JButton("停止蓝牙服务");

	JLabel lblLogs = new JLabel("");
	JLabel lblStatus = new JLabel("");

	private JPanel contentPane;
	
	

	private UpdateUICallback updateUICallback = new UpdateUICallback() {

		@Override
		public void updateMsgFromClient(String clientIP, String message) {
			lblStatus.setText("status:connected via WiFi");
			// get the messages from client
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// call the robot to notify
			try {
				MyRobot.simulationInput(message);
				lblLogs.setText("receive message : " + message);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void updateMesFromUSBClient(String message) {
			lblStatus.setText("status:connected via USB");
			// TODO Auto-generated method stub
			try {
				MyRobot.simulationInput(message);
				lblLogs.setText("receive message : " + message);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void updateMessageFromBlueToothClient(String message) {
			lblStatus.setText("status:connected via BlueTooth");
			// TODO Auto-generated method stub
			System.out.println("message from BlueTooth client >>>> " + message);
			try {
				MyRobot.simulationInput(message);
				lblLogs.setText("receive message : " + message);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BarCodeRecog frame = new BarCodeRecog();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BarCodeRecog() {
		setResizable(false);
		setTitle("\u667A\u80FD\u6761\u7801\u65E5\u671F\u8BC6\u522B\u5E94\u7528 v1.0_20200305");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(BarCodeRecog.class.getResource("/resources/barcode_logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 400, 800);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(32, 178, 170));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		BufferedImage image = null;
		try {
			image = ImageIO.read(BarCodeRecog.class.getResource("/resources/qrcode_expired.jpeg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		lblQrcode.setBounds(184, 80, 200, 200);

		Image resizedImage = image.getScaledInstance(lblQrcode.getWidth(), lblQrcode.getHeight(), Image.SCALE_SMOOTH);

		ImageIcon imageIcon = new ImageIcon(resizedImage);
		lblQrcode.setIcon(imageIcon);

		contentPane.add(lblQrcode);

		JLabel lblNewLabel = new JLabel("扫描二维码连接:");
		lblNewLabel.setFont(new Font("SimSun", Font.PLAIN, 16));
		lblNewLabel.setBounds(184, 50, 125, 15);
		contentPane.add(lblNewLabel);

		JLabel label = new JLabel("局域网IP地址：");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(184, 290, 100, 15);
		contentPane.add(label);

		label_IP.setHorizontalAlignment(SwingConstants.CENTER);
		label_IP.setBounds(284, 290, 100, 15);
		contentPane.add(label_IP);

		JLabel label_status = new JLabel("北京金朗维科技有限公司");
		label_status.setFont(new Font("FZXS12", Font.PLAIN, 12));
		label_status.setHorizontalAlignment(SwingConstants.CENTER);
		label_status.setBounds(0, 756, 394, 15);
		contentPane.add(label_status);

		JButton button_refreshQRCode = new JButton("刷新");
		button_refreshQRCode.setBounds(319, 48, 65, 20);
		button_refreshQRCode.addActionListener(new MyActionListener());
		button_refreshQRCode.setActionCommand(ACTION_TYPE_GENERATE_QRCODE);
		contentPane.add(button_refreshQRCode);

		buttonStartService.setBounds(26, 79, 140, 30);
		buttonStartService.setActionCommand(ACTION_TYPE_START_SERVICE_WIFI);
		buttonStartService.addActionListener(new MyActionListener());
		contentPane.add(buttonStartService);
		
		buttonStopService.setBounds(26, 125, 140, 30);
		buttonStopService.setActionCommand(ACTION_TYPE_STOP_SERVICE_WIFI);
		buttonStopService.addActionListener(new MyActionListener());
		contentPane.add(buttonStopService);
		
		// USB related
		mUSBSocketClient = new SockectClient();
		buttonStartUSBService.setBounds(26, 178, 140, 30);
		buttonStartUSBService.setActionCommand(ACTION_TYPE_START_SERVICE_USB);
		buttonStartUSBService.addActionListener(new MyActionListener());
		contentPane.add(buttonStartUSBService);

		buttonStopUSBService.setBounds(26, 228, 140, 30);
		buttonStopUSBService.setActionCommand(ACTION_TYPE_STOP_SERVICE_USB);
		buttonStopUSBService.addActionListener(new MyActionListener());
		contentPane.add(buttonStopUSBService);
		
		//USB try the read text solution
		mUsbAccessityUtils = new USBAccessityUtils();
		mUsbAccessityUtils.setUpdateUICallback(updateUICallback);
		mUsbAccessityUtils.start();

		/******** BlueTooth related ***********/
		mBlueToothService = new BluetoothService();
		
		buttonStartBlueToothService.setBounds(26, 286, 140, 30);
		buttonStartBlueToothService.setActionCommand(ACTION_TYPE_START_SERVICE_BLUETOOTH);
		buttonStartBlueToothService.addActionListener(new MyActionListener());
		contentPane.add(buttonStartBlueToothService);

		buttonStopBlueToothService.setBounds(26, 339, 140, 30);
		buttonStopBlueToothService.setActionCommand(ACTION_TYPE_STOP_SERVICE_BLUETOOTH);
		buttonStopBlueToothService.addActionListener(new MyActionListener());
		contentPane.add(buttonStopBlueToothService);
		
		lblStatus.setBounds(26, 400, 300, 40);
		lblStatus.setText("status:");
		contentPane.add(lblStatus);
		
		lblLogs.setBounds(26, 440, 300, 40);
		lblLogs.setText("logs:");
		contentPane.add(lblLogs);

		

	}

	public class MyActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			switch (e.getActionCommand()) {
			case ACTION_TYPE_GENERATE_QRCODE:
				String ipAddr = MyUtils.getLocalIP();
				byte[] data = QRCodeGenerator.generateQRcodeByte(ipAddr, 300);
				label_IP.setText(ipAddr);

				ByteArrayInputStream in = new ByteArrayInputStream(data);

				try {
					BufferedImage image = ImageIO.read(in);
					Image resizedImage = image.getScaledInstance(lblQrcode.getWidth(), lblQrcode.getHeight(),
							Image.SCALE_SMOOTH);
					lblQrcode.setIcon(new ImageIcon(resizedImage));
					// update the IP address
					label_IP.setText(MyUtils.getLocalIP());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				break;

			case ACTION_TYPE_START_SERVICE_WIFI:
				System.out.println("starting Wifi service...");
				try {
					mServer = new SocketServer();
					mServer.start();
					mServer.setUpdateUICallback(updateUICallback);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				
				
			case ACTION_TYPE_STOP_SERVICE_WIFI:
				System.out.println("stopping Wifi service...");
				new Thread(){
					public void run(){
					try {
					Thread.sleep(500);
					JOptionPane.showConfirmDialog(
	                        BarCodeRecog.this,
	                        "停止WiFi服务并退出", "消息提示",
	                        JOptionPane.CLOSED_OPTION
	                );
					System.exit(0);
					} catch (InterruptedException e) { }
					}
					}.start();
				break;
				
				
				
				
				
			case ACTION_TYPE_START_SERVICE_USB:
				System.out.println("starting usb service...");
				mUSBSocketClient.setUpdateUICallback(updateUICallback);
				int result = -1;
				result = JOptionPane.showConfirmDialog(
                        BarCodeRecog.this,
                        "请先在手机客户端选择USB，并且打开扫码界面", "消息提示",
                        JOptionPane.CLOSED_OPTION
                );
				if(result == 0)
					mUSBSocketClient.start();
				break;
				
			case ACTION_TYPE_STOP_SERVICE_USB:
				System.out.println("stopping usb service...");
				mUSBSocketClient.stop();
				new Thread(){
					public void run(){
					try {
					Thread.sleep(500);
					JOptionPane.showConfirmDialog(
	                        BarCodeRecog.this,
	                        "停止USB服务并退出", "消息提示",
	                        JOptionPane.CLOSED_OPTION
	                );
					System.exit(0);
					} catch (InterruptedException e) { }
					}
					}.start();
				break;

				
				
				
				
			case ACTION_TYPE_START_SERVICE_BLUETOOTH:
				System.out.println("starting blueTooth service...");
				mBlueToothService.setUpdateUICallback(updateUICallback);
				JOptionPane.showConfirmDialog(
                        BarCodeRecog.this,
                        "首先启动该服务端后再操作手机客户端", "消息提示",
                        JOptionPane.CLOSED_OPTION
                );
				mBlueToothService.start();
				break;
				
			case ACTION_TYPE_STOP_SERVICE_BLUETOOTH:
				System.out.println("stopping blueTooth service...");
				mBlueToothService.stop();
				new Thread(){
					public void run(){
					try {
					Thread.sleep(500);
					JOptionPane.showConfirmDialog(
	                        BarCodeRecog.this,
	                        "停止蓝牙服务并退出", "消息提示",
	                        JOptionPane.CLOSED_OPTION
	                );
					System.exit(0);
					} catch (InterruptedException e) { }
					}
					}.start();
			default:
				break;
			}
		}

	}
}
