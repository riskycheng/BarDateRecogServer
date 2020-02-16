package utils;

public interface UpdateUICallback {
	void updateMsgFromClient(String clientIP, String message); // for WiFi
	
	void updateMesFromUSBClient(String message); //for USB
	
	void updateMessageFromBlueToothClient(String message); //for blueTooth
}
