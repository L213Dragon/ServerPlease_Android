package bbbbb.com.socialdining.activity;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ianpanton.serverplease.R;

import bbbbb.com.socialdining.MainActivity;

public class BaseActivity extends AppCompatActivity {

    private BroadcastReceiver br;
    public final static String BROADCAST_ACTION = "bbbbb.com.socialdining";
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        alertDialogBuilder.setTitle("Warning")
                .setMessage("Did you leave? Automatically logging you off in 15 seconds")
                .setIcon(R.drawable.img_logo)
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        alert = alertDialogBuilder.create();

        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                boolean b = intent.getBooleanExtra("show", false);
                if(b)
                    alert.show();
                else
                    alert.hide();

                if(intent.getBooleanExtra("logout", false)) {
                    Intent new_intent = new Intent(BaseActivity.this, MainActivity.class);
                    new_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    new_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(new_intent);
                }

                //Log.d(LOG_TAG, "onReceive: task = " + task + ", status = " + status);

            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);
    }
}

