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

import deepsky.server.SocketServer.UpdateUICallback;
import usb.SockectClient;

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

public class BarCodeRecog extends JFrame {

	public final static String ACTION_TYPE_GENERATE_QRCODE = "generateQRCode";
	public final static String ACTION_TYPE_GENERATE_START_SERVICE = "startService";
	public final static String ACTION_TYPE_GENERATE_START_SERVICE_USB = "startServiceUSB";

	private SocketServer mServer = null;
	
	private SockectClient mUSBSocketClient = null;

	JLabel lblQrcode = new JLabel();
	JLabel label_IP = new JLabel("");
	JButton buttonStartService = new JButton("启动服务");
	JButton buttonStartUSBService = new JButton("启动USB服务");
	JLabel lblLogs = new JLabel("");

	private JPanel contentPane;

	private UpdateUICallback updateUICallback = new UpdateUICallback() {

		@Override
		public void updateMsgFromClient(String clientIP, String message) {
			// get the messages from client
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			lblLogs.setText(df.format(new Date()) + " IP (" + clientIP + ") :" + message);
			// call the robot to notify
			try {
				MyRobot.simulationInput(message);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void updateMesFromUSBClient(String message) {
			// TODO Auto-generated method stub
			try {
				MyRobot.simulationInput(message);
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
		setTitle("智能条码日期识别应用 v1.0");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(BarCodeRecog.class.getResource("/resources/barcode_logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 400, 400);
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
		label_status.setBounds(0, 356, 394, 15);
		contentPane.add(label_status);

		JButton button_refreshQRCode = new JButton("刷新");
		button_refreshQRCode.setBounds(319, 48, 65, 20);
		button_refreshQRCode.addActionListener(new MyActionListener());
		button_refreshQRCode.setActionCommand(ACTION_TYPE_GENERATE_QRCODE);
		contentPane.add(button_refreshQRCode);

		buttonStartService.setBounds(26, 79, 107, 23);
		buttonStartService.setActionCommand(ACTION_TYPE_GENERATE_START_SERVICE);
		buttonStartService.addActionListener(new MyActionListener());
		contentPane.add(buttonStartService);

		lblLogs.setBounds(4, 310, 378, 43);
		contentPane.add(lblLogs);
		
		buttonStartUSBService.setBounds(26, 112, 107, 23);
		buttonStartUSBService.setActionCommand(ACTION_TYPE_GENERATE_START_SERVICE_USB);
		buttonStartUSBService.addActionListener(new MyActionListener());
		contentPane.add(buttonStartUSBService);

		// add listener
		

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

			case ACTION_TYPE_GENERATE_START_SERVICE:
				try {
					mServer = new SocketServer();
					mServer.start();
					mServer.setUpdateUICallback(updateUICallback);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				case ACTION_TYPE_GENERATE_START_SERVICE_USB:
					System.out.println("USB server starting...");
					mUSBSocketClient = new SockectClient();
					mUSBSocketClient.setUpdateUICallback(updateUICallback);
			        if (mUSBSocketClient.adbCmd()) {
			        	mUSBSocketClient.test();
			        }
					
					break;
			default:
				break;
			}
		}

	}
}
