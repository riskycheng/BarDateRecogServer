package exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestADB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cmd_read = "adb shell cat /sdcard/test.txt && rm -rf /sdcard/test.txt";
		long start = System.currentTimeMillis();
		String data = execCMD(cmd_read);
		if (!data.equals(""))
			System.out.println(data + " >>> time-cost >>> " + (System.currentTimeMillis() - start));
		else
			System.out.println("no valid data");

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
