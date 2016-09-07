package info.pramoth.hw5.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.androidhive.materialtabs.R;
import info.pramoth.hw5.Twitter.ConstantValues;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = "TweetService";

    private boolean isUseStoredTokenKey = false;
    private boolean isUseWebViewForAuthentication = false;
    //Twitter
    private Button checkInButton;
    private static String message;
    RequestToken requestToken ;
    AccessToken accessToken;
    String oauth_url,oauth_verifier,profile_url;
    Dialog auth_dialog;
    WebView web;
    SharedPreferences pref;
    ProgressDialog progress;
    Twitter twitter;
    String tweetMessage="";
    public static List<String> tweetList= new ArrayList<String>();
    //private static String CONSUMER_KEY = "4vPUq1nMXpU4Xj1ir80wVJbrN";
    //private static String CONSUMER_SECRET = "gB2GLKhyrZgl7n8oSSbOhcdXimxZFTEUlqBqepiFnxzjjO6fP0";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    String event="";
    String eventLocation="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        event=getIntent().getExtras().getString("event");
        event=event.replaceAll(" +"," ");
        Log.d("sixe",String.valueOf(event.length()));
        eventLocation=getIntent().getExtras().getString("eventLocation");
        TextView textView=(TextView) findViewById(R.id.event);
        textView.setText(event);

        setUpMapIfNeeded();
        mMap.clear();
        List<Address> addressList = null;
        String location=eventLocation;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                Log.d("qwqw",location);
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("cccccc",String.valueOf(addressList.size()));

            if(addressList.size()!=0){
                Address address = addressList.get(0);
                Log.d("address",address.toString());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getAddressLine(1)));
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getFeatureName()+","+addressList.get(0).getLocality()+","+addressList.get(0).getThoroughfare()+","+addressList.get(0).getPostalCode()+","+addressList.get(0).getCountryCode()+","+addressList.get(0).getCountryName()));
                String exact="";
                for(int i=0; i< addressList.get(0).getMaxAddressLineIndex();i++ ){
                    exact+=addressList.get(0).getAddressLine(i);
                }
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getAddressLine(0)));
                mMap.addMarker(new MarkerOptions().position(latLng).title(exact));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            }

//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location., location.getLongitude()), 12.0f));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    public void onClickTweet(View view)
    {
        Log.d("sss","Tweet");
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", ConstantValues.TWITTER_CONSUMER_KEY);
        edit.putString("CONSUMER_SECRET", ConstantValues.TWITTER_CONSUMER_SECRET);
        edit.commit();

        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""), pref.getString("CONSUMER_SECRET", ""));
        logIn();

    }

    private void logIn() {



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, sharedPreferences.toString());
        if (!sharedPreferences.getBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,false))
        {
            Log.d(TAG, ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN);
            new TokenGet().execute();
        }
        else
        {

            new PostTweet().execute();


        }
    }
    private class TokenGet extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            try {
                requestToken = twitter.getOAuthRequestToken();
                oauth_url = requestToken.getAuthorizationURL();
                Log.d("oauth_url", oauth_url);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                if(401 == e.getStatusCode()){
                    Log.d("error","message - Invalid or expired token.");
                }else{e.printStackTrace();}

            }
            return oauth_url;
        }
        @Override
        protected void onPostExecute(String oauth_url) {
            if(oauth_url != null){
                Log.e("URL", oauth_url);
                auth_dialog = new Dialog(getApplicationContext());
                auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                auth_dialog.setContentView(R.layout.oauth_dialog);
                web = (WebView)auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(oauth_url);
                web.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon){
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains("oauth_verifier") && authComplete == false){
                            authComplete = true;
                            Log.e("Url",url);
                            Uri uri = Uri.parse(url);
                            oauth_verifier = uri.getQueryParameter("oauth_verifier");

                            auth_dialog.dismiss();
                            new AccessTokenGet().execute();
                        }else if(url.contains("denied")){
                            auth_dialog.dismiss();
//                            Toast.makeText(this, "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
                Log.d(TAG, auth_dialog.toString());
                auth_dialog.show();
                auth_dialog.setCancelable(true);



            }else{

//                Toast.makeText(getActivity(), "Sorry !, Network Error or Invalid Credentials", Toast.LENGTH_SHORT).show();


            }
        }
    }

    private class AccessTokenGet extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MapsActivity.this);
            progress.setMessage("Fetching Data ...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

        }


        @Override
        protected Boolean doInBackground(String... args) {

            try {


                accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("ACCESS_TOKEN", accessToken.getToken());
                edit.putString("ACCESS_TOKEN_SECRET", accessToken.getTokenSecret());
                edit.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, true);
                User user = twitter.showUser(accessToken.getUserId());
                profile_url = user.getOriginalProfileImageURL();
                edit.putString("NAME", user.getName());
                edit.putString("IMAGE_URL", user.getOriginalProfileImageURL());

                edit.commit();


            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();


            }

            return true;
        }
        @Override
        protected void onPostExecute(Boolean response) {
            if(response){
                progress.hide();

                new PostTweet().execute();
                //
            }
        }


    }

    private class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MapsActivity.this);
            progress.setMessage("Posting tweet ...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);

            progress.show();

        }

        protected String doInBackground(String... args) {

            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
            builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));


            AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            String currentDateTime = sdf.format(new Date());

//            String status = "prengana"  ;

//            String status = "@MobileApp4 " + tweetMessage;
            String status = "@MobileApp4 prengana " + event;
            Log.d("wewe",String.valueOf(status.length()));


            try {

                twitter4j.StatusUpdate statusUpdate = new StatusUpdate("Hello Twitter");


                //File file = new File("//imageFilePath");
                //statusUpdate.setMedia(file);
                //twitter4j.Status response = twitter.updateStatus(statusUpdate);

                twitter4j.Status response = twitter.updateStatus(status);
//                Log.d("one", "-------------------" + twitter.getScreenName());
//                Log.d("one", "-------------------" + twitter.getUserTimeline("MobileApp4"));
                ResponseList<twitter4j.Status> statuses = twitter.getUserTimeline("chetanyuvaraj");
//                Log.d("one", "-------------------");
                for (twitter4j.Status status1 : statuses) {

                    tweetList.add(status1.getUser().getName() + ":" +
                            status1.getText());
//                    Log.d("tweet", "-------------------" + status1.getUser().getName() + ":" +
//                            status1.getText());
                }

//                for(String i: tweetList){
//                    Log.d("tweet",i);
//                }

                return response.toString();
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else if(403 == te.getStatusCode() && te.getMessage().contains("140")){
                    Log.d("ccc","14000");
                    return "140";
                }else if (403 == te.getStatusCode()) {
                    System.out.println("Duplicate Tweet!");
                    te.printStackTrace();
                }
                else if (400 == te.getStatusCode()) {
                    System.out.println("Internal Server Error!");
                } else if (-1 == te.getStatusCode()) {
                    System.out.println("check your internet connection!");
                } else {
                    Log.d("error", String.valueOf(te.getStatusCode()));
                    te.printStackTrace();
                }
            }


            return null;
        }

        protected void onPostExecute(String res) {
//            Twitter twitter1 = TwitterFactory.getSingleton();


            if (res != null && res.contains("140")) {
                progress.dismiss();
                Toast.makeText(MapsActivity.this, "Tweet exceeds 140 characters ", Toast.LENGTH_SHORT).show();

            }
            if (res != null ) {
                progress.dismiss();
                Toast.makeText(MapsActivity.this, "Tweet Sucessfully Posted ! ", Toast.LENGTH_SHORT).show();

            } else {
                progress.dismiss();
                Toast.makeText(MapsActivity.this, "Error while tweeting !", Toast.LENGTH_SHORT).show();
            }

        }

    }




    public void changeType(View view)
    {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
//        mMap.setMyLocationEnabled(true);


    }
}
