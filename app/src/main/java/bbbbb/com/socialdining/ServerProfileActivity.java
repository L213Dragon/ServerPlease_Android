package bbbbb.com.socialdining;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.graphics.Color;
        import android.support.v4.app.FragmentTabHost;
        import android.support.v4.content.LocalBroadcastManager;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.ProgressBar;
        import android.widget.TabHost;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.ianpanton.serverplease.R;
        import com.parse.ParseException;
        import com.parse.ParseGeoPoint;
        import com.parse.ParseInstallation;
        import com.parse.ParseUser;
        import com.parse.SaveCallback;

        import java.util.ArrayList;

        import bbbbb.com.socialdining.activity.BaseActivity;
        import bbbbb.com.socialdining.service.BackgroundLocationService;
        import me.leolin.shortcutbadger.ShortcutBadger;

public class ServerProfileActivity extends BaseActivity {

    // Fragment TabHost as mTabHost
    private FragmentTabHost mTabHost;

    private ProgressBar mProgressBar;

    @Override
    protected void onStop() {
        Intent intent = new Intent(this, BackgroundLocationService.class);
        intent.putExtra("code", 1);
        // стартуем сервис
        //startService(intent);
        //logOut();
        //super.onStop();
        //Intent i = new Intent(ServerProfileActivity.this, ServerProfileActivity.class);
        //startActivity(i);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        Log.i("123", "onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        // Создаем PendingIntent для Task1
        //PendingIntent pi = createPendingResult(1, null, 0);
        // Создаем Intent для вызова сервиса, кладем туда параметр времени
        // и созданный PendingIntent
        Intent intent = new Intent(this, BackgroundLocationService.class);
        intent.putExtra("code", 1);
        // стартуем сервис
        //startService(intent);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_profile);

        ShortcutBadger.with(getApplicationContext()).remove();
        MyCustomReceiver.badgeCount = 0;

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("logout"));

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("main").setIndicator("Profile"),
                MainTabFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("notification").setIndicator("Requests"),
                NotificationTapFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("setting").setIndicator("Setting"),
                SettingTabFragment.class, null);



        for(int i=0;i<mTabHost.getTabWidget().getChildCount();i++)
        {
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setAllCaps(false);
            tv.setTextColor(Color.parseColor("#d3d3d3"));
        }

        TextView tv1 = (TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        tv1.setTextColor(Color.parseColor("#ffffff")); //1st tab selected

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String arg0) {

                setTabColor(mTabHost);
            }
        });



        Intent intent = new Intent(ServerProfileActivity.this, BackgroundLocationService.class);
        intent.putExtra("type", "waiter");
        startService(intent);

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(ServerProfileActivity.this, "Please log out to go to the main page!", Toast.LENGTH_LONG).show();
    }

    //Change The Backgournd Color of Tabs
    public void setTabColor(TabHost tabhost) {

        for(int i=0;i<tabhost.getTabWidget().getChildCount();i++){
            TextView tv1 = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv1.setTextColor(Color.parseColor("#d3d3d3")); //1st tab selected
        }

        TextView tv = (TextView) tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#ffffff")); //1st tab selected
    }

    private void logOut(){
        Log.i("ServerprofileActivity:", "received logout");

        ParseUser user = ParseUser.getCurrentUser();

        if (user == null){
            return;
        }

        user.put("loggedIn", 0);


        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("loggedin", 0);
                    // installation.saveInBackground();
                    installation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser user = ParseUser.getCurrentUser();
                                user.logOutInBackground();
                                Toast.makeText(ServerProfileActivity.this, "Logged Out!", Toast.LENGTH_LONG).show();
                                ServerProfileActivity.this.finish();
                                startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            } else {

                                Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                                ServerProfileActivity.this.finish();
                                startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            }
                        }
                    });
                } else {
                    Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                    ServerProfileActivity.this.finish();
                    startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                }
            }
        });
    }

    private void _logOut(){
        Log.i("ServerprofileActivity:", "received logout");

        ParseUser user = ParseUser.getCurrentUser();

        if (user == null){
            return;
        }

        user.put("loggedIn", 0);

        ArrayList<String> numList = new ArrayList<String>();

        for (int i =1; i < 101; i ++){
            numList.add("0");
        }

        user.put("table_numbers",numList);
        user.put("restaurant_name", "");

        Log.d("123", "делаю запрос");

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("123", "Ответ получен");
                if (e == null) {
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("loggedin", 0);
                    // installation.saveInBackground();
                    installation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser user = ParseUser.getCurrentUser();
                                user.logOutInBackground();
                                Toast.makeText(ServerProfileActivity.this, "Logged Out!", Toast.LENGTH_LONG).show();
                                ServerProfileActivity.this.finish();
                                startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            } else {

                                Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                                ServerProfileActivity.this.finish();
                                startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                            }
                        }
                    });
                } else {
                    Toast.makeText(ServerProfileActivity.this, "Failed to logout", Toast.LENGTH_LONG).show();
                    ServerProfileActivity.this.finish();
                    startActivity(new Intent(ServerProfileActivity.this, MainActivity.class));
                }
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // specific job according to Intent and its extras...
//            ShortcutBadger.with(getContext()).remove();
//            MyCustomReceiver.badgeCount = 0;
            _logOut();
        }

    };
}





