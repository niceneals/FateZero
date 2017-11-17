package com.example.user.fatezero;

import java.util.UUID;


public interface Constants {

    String TAG = "Arduino-Android";
    int REQUEST_ENABLE_BT = 1;


    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_SNACKBAR = 4;


    int STATE_NONE = 0;
    int STATE_ERROR = 1;
    int STATE_CONNECTING = 2;
    int STATE_CONNECTED = 3;


    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    String EXTRA_DEVICE  = "EXTRA_DEVICE";
    String SNACKBAR = "toast";


}
