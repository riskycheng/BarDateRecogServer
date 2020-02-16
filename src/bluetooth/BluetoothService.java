package bluetooth;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService implements Runnable {
    private static final UUID SERVER_UUID = new UUID("0000110100001000800000805F9B34FB", false);

    private boolean isListening;
    private StreamConnectionNotifier streamConnectionNotifier;
    private byte[] buffer = new byte[200];

    public BluetoothService() {
        isListening = true;
    }

    public void init() {
        try {
            LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
            streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + SERVER_UUID.toString());

            ServiceRecord serviceRecord = LocalDevice.getLocalDevice().getRecord(streamConnectionNotifier);
            serviceRecord.setAttributeValue(0x0008, new DataElement(DataElement.U_INT_1, 0xFF));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(this).start();
    }

    @Override
    public void run() {
    	System.out.println("Listener start");
    	StreamConnection streamConnection = null;
    	InputStream inputStream=null;
    	OutputStream outputStream =null;
    	System.out.println("try to accept and open");
    	
        try {
			streamConnection = streamConnectionNotifier.acceptAndOpen();
			inputStream = streamConnection.openInputStream();
            outputStream = streamConnection.openOutputStream();
            
			while (isListening) {
				if ((inputStream.available()) <= 0) {
					Thread.sleep(1000);
				}
				System.out.println("message is comming");
                
                outputStream.write("hello android BT".getBytes());

                //noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                String message = new String(buffer);
                System.out.println("Receive message : " + message);
                
                if (message.contains("EXIT_APP")) {
                	System.out.println("Listener closed");
                    isListening = false;
                }
	        }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
                outputStream.close();
				streamConnection.close();
				System.out.println("finally");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
    }
}
