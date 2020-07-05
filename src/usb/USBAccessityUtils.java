package usb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import utils.UpdateUICallback;

public class USBAccessityUtils {

	public UpdateUICallback mUpdateUICallback = null;

	public void setUpdateUICallback(UpdateUICallback uiCallback) {
		this.mUpdateUICallback = uiCallback;
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("start listening to the barCode...");
				while (true) {
					String cmd_read = "adb shell cat /sdcard/barCodeRes.txt && rm -rf /sdcard/barCodeRes.txt";
					long start = System.currentTimeMillis();
					String data = execCMD(cmd_read);
					if (!data.equals("")) {
						//System.out.println(data + " >>> time-cost >>> " + (System.currentTimeMillis() - start));
						mUpdateUICallback.updateMesFromUSBClient(data);
					}
						
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		thread.start();
	}
	
	
	public static String execCMD(String command) {
        StringBuilder sb =new StringBuilder();
        try {
            Process process=Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while((line=bufferedReader.readLine())!=null)
            {
                sb.append(line+"\n");
            }
        } catch (Exception e) {
            return e.toString();
        }
        return sb.toString();
    }

}
