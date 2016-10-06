package bbbbb.com.socialdining;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MyCustomReceiver extends ParsePushBroadcastReceiver {

    private Context context;
    private String m_tableNumber;
    private String m_locataion;
    static public int badgeCount = 0;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.d("Push", "Push received");
        this.context = context;

        badgeCount ++;
        ShortcutBadger.with(context).count(badgeCount); //for 1.1.3

        if (intent == null)
            return ;

        String jsonData = intent.getExtras().getString("com.parse.Data");

        Log.d("Push", "JSON Data ["+jsonData+"]");

        String data = getData(jsonData);
        String table_number = getTableNumber(jsonData);

        // Save the received table number in shared datastore
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        ArrayList<String> tableNumberList;

        String serializedTableNumbers = preferences.getString("table_numbers", null);
        if (serializedTableNumbers == null){
            tableNumberList = new ArrayList<String>();
            tableNumberList.add(table_number);
        } else {
            tableNumberList = convertToArray(serializedTableNumbers);
            //tableNumberList.add(table_number);
            tableNumberList.add(0, table_number);
        }

        String toSaveString = convertToString(tableNumberList);
        editor.putString("table_numbers", toSaveString);
        editor.commit();

        sendLocalNotification(); // send braodcast notification saying that received notification
    }

    private void sendLocalNotification(){
        final Intent intent = new Intent("myLocalNotificationIdentifier");
        intent.putExtra("aKey", "service_calling");
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    private String convertToString(ArrayList<String> list) {

        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list)
        {
            sb.append(delim);
            sb.append(s);;
            delim = ",";
        }
        return sb.toString();
    }

    private ArrayList<String> convertToArray(String string) {

        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(",")));
        return list;
    }



    private String getData(String jsonData) {
        // Parse JSON Data
        try {
            System.out.println("JSON Data [" + jsonData + "]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("alert");
        } catch (JSONException jse) {
            jse.printStackTrace();

        }
        return "";
    }

    private String getTableNumber(String jsonData){
        // Parse JSON Data
        try {
            System.out.println("JSON Data [" + jsonData + "]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("table_number");
        } catch (JSONException jse) {
            jse.printStackTrace();

        }
        return "";
    }

    private String getLocation(String jsonData){
        // Parse JSON Data
        try {
            System.out.println("JSON Data [" + jsonData + "]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("location");
        } catch (JSONException jse) {
            jse.printStackTrace();

        }
        return "";
    }

    @Override
    public Notification getNotification(Context context, Intent intent){
        Notification n = super.getNotification(context, intent);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // n.sound = alarmSound;
        return  n;
    }
}
