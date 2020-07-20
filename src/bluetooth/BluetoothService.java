package bluetooth;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import utils.UpdateUICallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService implements Runnable {
    private static final UUID SERVER_UUID = new UUID("0000110100001000800000805F9B34FB", false);

    private boolean isListening;
    private StreamConnectionNotifier streamConnectionNotifier;
    private byte[] buffer = new byte[200];

    Thread mThread;
    
    LocalDevice mLocalDevice = null;
    
    
    private UpdateUICallback mUpdateUICallback = null;

   	public void setUpdateUICallback(UpdateUICallback uiCallback) {
   		this.mUpdateUICallback = uiCallback;
   	}
    
    
    public BluetoothService() {
        isListening = true;
    }

    public boolean init() {
        try {
        	if (mLocalDevice == null) {
        		mLocalDevice = LocalDevice.getLocalDevice();
        		mLocalDevice.setDiscoverable(DiscoveryAgent.GIAC);
        	}
        		
        	mThread = new Thread(this);
            streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + SERVER_UUID.toString());

            ServiceRecord serviceRecord = LocalDevice.getLocalDevice().getRecord(streamConnectionNotifier);
            serviceRecord.setAttributeValue(0x0008, new DataElement(DataElement.U_INT_1, 0xFF));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

   
    public void startListening() {
        mThread.start();
    }

    @Override
    public void run() {
    	System.out.println("Listener start");
    	StreamConnection streamConnection = null;
    	InputStream inputStream=null;
    	OutputStream outputStream =null;
    	System.out.println("try to accept and open");
    	
        try {
			
			while (isListening) {
				
				if(streamConnection == null)
					streamConnection = streamConnectionNotifier.acceptAndOpen();
				if(streamConnection == null) {
					System.out.println("init again");
					init();
					streamConnection = streamConnectionNotifier.acceptAndOpen();
				}
				if(inputStream == null && streamConnection != null)
					inputStream = streamConnection.openInputStream();
				if(outputStream == null && streamConnection != null)
					outputStream = streamConnection.openOutputStream();
				
				System.out.println("message is comming");
                try{
                	outputStream.write("hello android BT".getBytes());
                }catch(Exception e) {
                	System.out.println("client is disconnected, try again here...");
                	//break;
                	Thread.sleep(1000);
                	outputStream.close();
                	inputStream.close();
                	streamConnection.close();
                	outputStream = null;
                	inputStream = null;
                	streamConnection = null;
                	continue;
                }
                
              for(int s = 0; s < buffer.length; s++) {
            	  buffer[s] = 0;
              }
                
                inputStream.read(buffer);
                String message = new String(buffer);
                System.out.println("Receive message new: " + message);
                
                mUpdateUICallback.updateMessageFromBlueToothClient(message);
                
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
				stop();
				System.out.println("finally release all");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public boolean start(){
        boolean blueToothSupported = init();
        if(!blueToothSupported) return false;
        startListening();
        return true;
    }
    
    public void stop(){
    	mThread.interrupt();
    	mThread = null;
    }
}
