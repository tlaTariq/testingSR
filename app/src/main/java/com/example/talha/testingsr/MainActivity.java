package com.example.talha.testingsr;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final Context mContext = this;
    public static SignalRService mService;
    public static boolean mBound = false;

    public static TextView MsgArea;

    private String uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        MsgArea = (TextView) findViewById(R.id.tv_msgs);
        MsgArea.setMovementMethod(new ScrollingMovementMethod());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your user-name! :)");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //For password field
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uname = input.getText().toString();

                Intent intent = new Intent(mContext, SignalRService.class);
                intent.putExtra("name", input.getText().toString());
                //intent.setClass(mContext, SignalRService.class);
                //startService(intent);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void sendMessage(View view) {

        if (mBound) {
            // Call a method from the SignalRService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.

            //User Code
            EditText editText = (EditText) findViewById(R.id.edit_message);
            EditText editText_Receiver = (EditText) findViewById(R.id.edit_receiver);
            if (editText != null && editText.getText().length() > 0) {
                String receiver = editText_Receiver.getText().toString();
                String message = editText.getText().toString();
                editText.setText("");
                mService.sendMessage_To(receiver, message);


            }

            //Admin Code
            //mService.getConnectedUsers();
        }
    }
    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }





    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
