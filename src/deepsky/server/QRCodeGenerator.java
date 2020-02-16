package deepsky.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeGenerator {
	/**
	 * �������ڸ�ʽ
	 */
	public static DateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
 
	/**
	 * ��ά��ߴ�
	 */
	public static final int QRCODE_SIZE = 300;
 
	/**
	 * ��ά��Я��������Ϣ
	 */
	public static final String CONTENT = "http://blog.csdn.net/magi1201";
 
	/**
	 * ���ɶ�ά�� ֱ�ӽ���ά��ͼƬд��ָ���ļ�Ŀ¼
	 * 
	 * @param content ��ά������
	 * @param width   ��ά����
	 * @param height  ��ά��߶ȣ�ͨ�������ά���Ⱥ͸߶���ͬ
	 * @param format  ��ά��ͼƬ��ʽ��JPG / PNG
	 */
	public static void generateQRcodePic(String content, int width, int height, String picFormat, String fileDir) {
 
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 1);
 
		try {
			// �����ά�ֽھ���
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
 
			// �����ļ�Ŀ¼����Ŀ¼�����ڣ��򴴽�Ŀ¼
			if (!new File(fileDir).exists()) {
				new File(fileDir).mkdirs();
			}
			Path file = new File(fileDir + File.separator + "qrcode." + picFormat).toPath();
 
			// ����λ�ֽھ�����ָ��ͼƬ��ʽ��д��ָ���ļ�Ŀ¼�����ɶ�ά��ͼƬ
			MatrixToImageWriter.writeToPath(bitMatrix, picFormat, file);
		} catch (WriterException | IOException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * ���ɶ�ά�� ���ɶ�ά��ͼƬ�ֽ���
	 * 
	 * @param content ��ά������
	 * @param width   ��ά���Ⱥ͸߶�
	 * @param format  ��ά��ͼƬ��ʽ
	 */
	public static byte[] generateQRcodeByte(String content, int width) {
		byte[] codeBytes = null;
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 1);
		try {
			// �����ά�ֽھ��󣬽���λ�ֽھ�����ȾΪ��ά����ͼƬ
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, width, hints);
			BufferedImage image = toBufferedImage(bitMatrix);
 
			// ���������������ά����ͼƬд��ָ�������
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", out);
 
			// �������ת��Ϊ�ֽ�����
			codeBytes = out.toByteArray();
 
		} catch (WriterException | IOException e) {
			e.printStackTrace();
		}
		return codeBytes;
	}
 
	/**
	 * ����ά�ֽھ�����ȾΪ��ά����ͼƬ
	 * 
	 * @param matrix ��ά�ֽھ���
	 * @return ��ά����ͼƬ
	 */
	public static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int onColor = 0xFF000000;
		int offColor = 0xFFFFFFFF;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
			}
		}
		return image;
	}
 
	/**
	 * ������ά������
	 * 
	 * @param filepath ��ά��·��
	 */
	public static void readQRcode(String filepath) {
 
		MultiFormatReader multiFormatReader = new MultiFormatReader();
		File file = new File(filepath);
 
		// ͼƬ����
		BufferedImage image = null;
 
		// �����Ʊ���ͼ
		BinaryBitmap binaryBitmap = null;
 
		// ��ά����
		Result result = null;
 
		try {
			image = ImageIO.read(file);
			binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
			result = multiFormatReader.decode(binaryBitmap);
		} catch (IOException | NotFoundException e1) {
			e1.printStackTrace();
		}
 
		System.out.println("��ȡ��ά�룺 " + result.toString());
		System.out.println("��ά���ʽ�� " + result.getBarcodeFormat());
		System.out.println("��ά�����ݣ� " + result.getText());
	}
 
	/**
	 * main ���Է���
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
 
		// ���ɶ�ά�룬ֱ��д������
		generateQRcodePic(CONTENT, QRCODE_SIZE, QRCODE_SIZE, "jpg", "c:/test/");
 
//		// ���Զ�ά����Ϣ����
//		String filepath = "c:/test" + File.separator + "image" + File.separator + sf.format(new Date())
//				+ File.separator + "qrcode.jpg";
//		readQRcode(filepath);
 
		// ���ɶ�ά�룬�����ֽ�����
		String path = "c:/test" + File.separator + "image" + File.separator + sf.format(new Date());
		File pathDir = new File(path);
		if (!pathDir.exists()) {
			pathDir.mkdirs();
		}
 
		File pathFile = new File(path + File.separator + "qrcode_2.jpg");
		byte[] fileIo = generateQRcodeByte(CONTENT, QRCODE_SIZE);
		try {
			OutputStream os = new FileOutputStream(pathFile);
			os.write(fileIo);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
