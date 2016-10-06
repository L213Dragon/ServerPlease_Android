package bbbbb.com.socialdining;

import android.app.Application;
import android.location.Location;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

/**
 * Created by emil on 28/12/15.
 */
public class SocialDiningApplication extends Application {

    private Location currentLocation;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    @Override
    public  void  onCreate(){
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId("mzAJwKEtYgmrcjc5ap63zUBmWI4MWmFunwf6R5Pf")
                        .clientKey("H8DSKOk5naAUXcvkcBpCJlTxex6v4k6lHUhDImPw")
                        .server("https://parseapi.back4app.com/").build()
        );

        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(this);
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "856443327390");
        installation.saveInBackground();
    }
}



