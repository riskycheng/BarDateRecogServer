package deepsky.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class MyRobot {

	private static Robot mRobot = null;

	public static void simulationInput(String content) throws AWTException {
		if (mRobot == null) {
			mRobot = new Robot();
		}

		System.out.println("robot running...");
//		mRobot.setAutoWaitForIdle(true);

		
		//before that, we need to remove all
		for (int i = 0; i < 20; i++) {
			mRobot.keyPress(KeyEvent.VK_BACK_SPACE);
			mRobot.keyRelease(KeyEvent.VK_BACK_SPACE);
		}
		
		char[] array = content.toCharArray();

		for (char value : array) {
			switch (value) {
			case '0':
				mRobot.keyPress(KeyEvent.VK_0);
				mRobot.keyRelease(KeyEvent.VK_0);
				mRobot.delay(10);
				break;
			case '1':
				mRobot.keyPress(KeyEvent.VK_1);
				mRobot.keyRelease(KeyEvent.VK_1);
				mRobot.delay(10);
				break;
			case '2':
				mRobot.keyPress(KeyEvent.VK_2);
				mRobot.keyRelease(KeyEvent.VK_2);
				mRobot.delay(10);
				break;
			case '3':
				mRobot.keyPress(KeyEvent.VK_3);
				mRobot.keyRelease(KeyEvent.VK_3);
				mRobot.delay(10);
				break;
			case '4':
				mRobot.keyPress(KeyEvent.VK_4);
				mRobot.keyRelease(KeyEvent.VK_4);
				mRobot.delay(10);
				break;
			case '5':
				mRobot.keyPress(KeyEvent.VK_5);
				mRobot.keyRelease(KeyEvent.VK_5);
				mRobot.delay(10);
				break;
			case '6':
				mRobot.keyPress(KeyEvent.VK_6);
				mRobot.keyRelease(KeyEvent.VK_6);
				mRobot.delay(10);
				break;
			case '7':
				mRobot.keyPress(KeyEvent.VK_7);
				mRobot.keyRelease(KeyEvent.VK_7);
				mRobot.delay(10);
				break;
			case '8':
				mRobot.keyPress(KeyEvent.VK_8);
				mRobot.keyRelease(KeyEvent.VK_8);
				mRobot.delay(10);
				break;

			case '9':
				mRobot.keyPress(KeyEvent.VK_9);
				mRobot.keyRelease(KeyEvent.VK_9);
				mRobot.delay(10);
				break;
			
			default:
				break;

			}
		}
		
		//at last, will perform the enter
		mRobot.keyPress(KeyEvent.VK_ENTER);
		mRobot.keyRelease(KeyEvent.VK_ENTER);
		System.out.println("robot running done ...");
	}

}
