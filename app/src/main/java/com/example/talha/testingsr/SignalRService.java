package com.example.talha.testingsr;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

import static com.example.talha.testingsr.MainActivity.MsgArea;


public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients

    private String str_name;

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        str_name = extras.getString("name");

        //int result = super.onStartCommand(intent, flags, startId);
        startSignalR();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.

        Bundle extras = intent.getExtras();
        str_name = extras.getString("name");

        startSignalR();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    /**
     * method for clients (activities)
     */
    public void sendMessage(String message) {
        String SERVER_METHOD_SEND = "Send";
        mHubProxy.invoke(SERVER_METHOD_SEND, message);
    }


    /**
     * method for clients (activities)
     */
    public void sendMessage_To(String receiverName, String message) {
        String SERVER_METHOD_SEND_TO = "SendChatMessage";
        mHubProxy.invoke(SERVER_METHOD_SEND_TO, receiverName, message);
    }

    public void getConnectedUsers() {
        String GET_CONNECTED_USERS = "GetConnectedUsers";
        mHubProxy.invoke(GET_CONNECTED_USERS);
    }

    private void startSignalR() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                //request.addHeader("User-Name", name);
                request.addHeader("User-Name", str_name);
            }
        };

        String serverUrl = "http://testingsr.apphb.com/signalr";
        mHubConnection = new HubConnection(serverUrl);
        mHubConnection.setCredentials(credentials);
        String SERVER_HUB_CHAT = "SignalRChatHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);

        try {
            signalRFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("SimpleSignalR", e.toString());
            return;
        }

        //sendMessage("Hello from BNK!");

        //Admin Code(Code For Admin)

        String CLIENT_METHOD_ADMIN_MESSAGE = "adminMessage";
        mHubProxy.on(CLIENT_METHOD_ADMIN_MESSAGE,
                new SubscriptionHandler1<CustomMessage>() {
                    @Override
                    public void run(final CustomMessage msg) {
                        final String To = msg.UserName;

                        final String MSgFor = msg.MsgFor;

                        final String finalMsg = "Sender: <" + msg.UserName + ">" + "\n" + "Recipient: <" + msg.MsgFor + ">" + "\n" + msg.Message + "\n" + "\n" + "This message was sent by CAO(Chat Always On) App.";

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                MsgArea.append(finalMsg);
                                MsgArea.append("\n"); MsgArea.append("\n");

                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(MSgFor, null, finalMsg, null, null);
                                    Toast.makeText(getApplicationContext(), "Message Sent",
                                            Toast.LENGTH_LONG).show();

                                    MsgArea.append("SMS has been sent to recipient!!!");
                                    MsgArea.append("\n"); MsgArea.append("\n");
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }


                                //Toast.makeText(getApplicationContext(), "Sent!!!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                , CustomMessage.class);


        //Real Flow General Standard Code (Code For User Not Admin)

        String CLIENT_METHOD_BROADAST_MESSAGE = "broadcastMessage";
        mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                new SubscriptionHandler1<CustomMessage>() {
                    @Override
                    public void run(final CustomMessage msg) {
                        final String finalMsg = msg.UserName + " says " + msg.Message;
                        //final String finalMsg = " says ";

                        //Test data
                        final String MsgFor = msg.MsgFor;
                        //Test data

                        // display Toast message
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                MsgArea.append(finalMsg);
                                MsgArea.append("\n");



                                Toast.makeText(getApplicationContext(), finalMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                , CustomMessage.class);
    }
}
