package bluetooth;

import java.io.IOException;

public class testBlueTooth {

    public static void main(String[] args) throws IOException {
        BluetoothService bluetoothService = new BluetoothService();
        bluetoothService.init();
        bluetoothService.startListening();
    }
}
