package com.example.user.fatezero;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.UUID;
import java.io.ByteArrayOutputStream;


public class BluetoothService {


    private Handler myHandler;
    private int state;
    BluetoothDevice myDevice;

    ConnectThread connectThread;
    ConnectedThread connectedThread;



    public BluetoothService(Handler handler, BluetoothDevice device) {
        state = Constants.STATE_NONE;
        myHandler = handler;
        myDevice = device;
    }

    public synchronized void connect() {
        Log.d(Constants.TAG, "Connecting to: " + myDevice.getName() + " - " + myDevice.getAddress());


        setState(Constants.STATE_CONNECTING);
        connectThread = new ConnectThread(myDevice);
        connectThread.start();
    }

    public synchronized void stop() {
        cancelConnectThread();
        cancelConnectedThread();
        setState(Constants.STATE_NONE);
    }

    private synchronized void setState(int state) {
        Log.d(Constants.TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        myHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return state;
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(Constants.TAG, "connected to: " + device.getName());

        cancelConnectThread();

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(Constants.STATE_CONNECTED);

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.e(Constants.TAG, "Connection Failed");
        // Send a failure item_message back to the Activity
        Message msg = myHandler.obtainMessage(Constants.MESSAGE_SNACKBAR);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SNACKBAR, "Unable to connect");
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(Constants.STATE_ERROR);
        cancelConnectThread();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.e(Constants.TAG, "Connection Lost");
        Message msg = myHandler.obtainMessage(Constants.MESSAGE_SNACKBAR);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SNACKBAR, "Cconnection was lost");
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(Constants.STATE_ERROR);
        cancelConnectedThread();
    }

    private void cancelConnectThread() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    private void cancelConnectedThread() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        synchronized (this) {
            if (state != Constants.STATE_CONNECTED) {
                Log.e(Constants.TAG, "Trying to send but not connected");
                return;
            }
            r = connectedThread;
        }



        r.write(out);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                UUID uuid = Constants.myUUID;
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Create RFcomm socket failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            try {

                mmSocket.connect();
            } catch (IOException connectException) {

                Log.e(Constants.TAG, "Unable to connect", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(Constants.TAG, "Unable to close() socket during connection failure", closeException);
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this) {
                connectThread = null;
            }


            connected(mmSocket, mmDevice);
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Close() socket failed", e);
            }
        }
    }


    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }
        private  void copyCompletely(InputStream input, OutputStream output) throws IOException {
            // if both are file streams, use channel IO
            if ((output instanceof FileOutputStream) && (input instanceof FileInputStream)) {
                try {
                    FileChannel target = ((FileOutputStream) output).getChannel();
                    FileChannel source = ((FileInputStream) input).getChannel();

                    source.transferTo(0, Integer.MAX_VALUE, target);

                    source.close();
                    target.close();

                    return;
                } catch (Exception e) {
                }
            }

            byte[] buf = new byte[8192];
            while (true) {
                int length = input.read(buf);
                if (length < 0)
                    break;
                output.write(buf, 0, length);
            }

            try {
                input.close();
            } catch (IOException ignore) {
            }
            try {
                output.close();
            } catch (IOException ignore) {
            }
        }




        public void run() {
            Log.i(Constants.TAG, "Begin connectedThread");
            byte[] buffer = new byte[32768];  // buffer store for the stream
            int bytes; // bytes returned from read()
            StringBuilder readMessage = new StringBuilder();
            while (true) {
               // int readBytesCount =mmInStream.read(buffer);
                try {

                    bytes = mmInStream.read(buffer);
                    String read = new String(buffer, 0, bytes);
                    readMessage.append(read);

                    if (read.contains("\n")) {

                        myHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, readMessage.toString()).sendToTarget();
                        readMessage.setLength(0);
                    }

                } catch (IOException e) {

                    Log.e(Constants.TAG, "Connection Lost", e);
                    connectionLost();
                    break;
                }

            }

        }

        public void saving() throws IOException {
            byte[] something = new byte[1024*64];
            // Открой входной поток,линивая задница
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // а теперь пора поработать
            while (true){
                // прочитай данные в буфер
                int readBytesCount = mmInStream.read(something);
                if (readBytesCount == -1) {
                    // у тебя данные закончились, лол
                    break;
                }
                if (readBytesCount > 0) {
                    // данные считаны, можешь записать
                    baos.write(something, 0, readBytesCount);
                }
            }
            baos.flush();
            baos.close();
            byte[] data = baos.toByteArray();
            baos.write(data);
            FileOutputStream fr_out = new FileOutputStream("output.log");
            baos.writeTo(fr_out);

        }

        /*
        public void copyInputStreamToFile(InputStream in, File file) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                byte[] buf = new byte[1024*64];
                int len;
                while((len=in.read(buf))>0)
                {
                    out.write(buf,0,len);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
// Ensure that the InputStreams are closed even if there's an exception.
                try {
                    if ( out != null ) {
                        out.close(); }
// If you want to close the "in" InputStream yourself then remove this // from here but ensure that you close it yourself eventually.
                    in.close();
                }
                catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

*/



        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                myHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Exception during write", e);
            }
        }



        public void cancel() {

            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "close() of connect socket failed", e);}
        }
    }


}
