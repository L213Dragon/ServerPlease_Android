package bbbbb.com.socialdining;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ianpanton.serverplease.R;

import java.util.ArrayList;
import java.util.Arrays;

import me.leolin.shortcutbadger.ShortcutBadger;


/**

 */

public class NotificationTapFragment extends Fragment {

    ListView listView;
    ArrayList arrayList;
    ArrayAdapter adapter;
    Button m_ClearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_notification_tap, container, false);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter("myLocalNotificationIdentifier"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver1, new IntentFilter("clear_notification"));
        listView = (ListView) rootView.findViewById(R.id.fn_listView);

        SharedPreferences preferences = getContext().getSharedPreferences("MyPreferences", getContext().MODE_PRIVATE);
        String serializedTableNumbers = preferences.getString("table_numbers", null);

        ShortcutBadger.with(getContext()).remove();
        MyCustomReceiver.badgeCount = 0;

        m_ClearButton = (Button)rootView.findViewById(R.id.fn_clear);

        m_ClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                arrayList.clear();
                adapter.notifyDataSetChanged();
                SharedPreferences preferences = getContext().getSharedPreferences("MyPreferences", getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear().commit();
            }
        });

        arrayList = new ArrayList();

        ArrayList<String> tmpArr;
        if (serializedTableNumbers == null){
            tmpArr = new ArrayList<String>();
        } else {
            tmpArr = convertToArray(serializedTableNumbers);
            for (int i=0; i< tmpArr.size(); i++){
                String tmpStr = "Number" + tmpArr.get(i) + " is calling service!";
                arrayList.add(tmpStr);
            }
        }

        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
        // adapter = new ArrayAdapter(getContext(), R.layout.rowitemlayout, R.id.fn_rowTextItem, arrayList);

        listView.setAdapter(adapter);

        return rootView;
    }

    private ArrayList<String> convertToArray(String string) {

        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(",")));
        return list;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // specific job according to Intent and its extras...
//            ShortcutBadger.with(getContext()).remove();
//            MyCustomReceiver.badgeCount = 0;

            SharedPreferences preferences = context.getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
            String serializedTableNumbers = preferences.getString("table_numbers", null);
            ArrayList<String> tmpArr = convertToArray(serializedTableNumbers);

            arrayList.clear();

            for (int i=0; i<tmpArr.size() ; i++){
                String tmpStr =  tmpArr.get(i) + " is calling service!";
                arrayList.add(tmpStr);
            }
            // fire the event
            adapter.notifyDataSetChanged();
        }

    };

    private BroadcastReceiver mMessageReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // specific job according to Intent and its extras...
//            ShortcutBadger.with(getContext()).remove();
//            MyCustomReceiver.badgeCount = 0;

            SharedPreferences preferences = context.getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear().commit();

            arrayList.clear();

            // fire the event
            adapter.notifyDataSetChanged();
        }

    };


}
