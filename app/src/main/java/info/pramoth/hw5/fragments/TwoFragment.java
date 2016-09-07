package info.pramoth.hw5.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.pramoth.hw5.Twitter.ConstantValues;
import info.androidhive.materialtabs.R;

import android.widget.ArrayAdapter;
import android.widget.Button;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

//import edu.cmu.idrift.R;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;




public class TwoFragment extends Fragment{
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
    public List<String> tweetList;
    public List<String> tweetListnew= new ArrayList<String>();

    public void setTweetList(List<String> tweetList) {
        this.tweetList = tweetList;
    }

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", ConstantValues.TWITTER_CONSUMER_KEY);
        edit.putString("CONSUMER_SECRET", ConstantValues.TWITTER_CONSUMER_SECRET);
        edit.commit();
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""), pref.getString("CONSUMER_SECRET", ""));
        logIn();
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){


//        tv.setText( tweetList.toString().replace(",","\n"));



    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View view =inflater.inflate(R.layout.fragment_two, container,false);
        ListView listView = (ListView) view.findViewById(R.id.listView1);
//
//        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//                "Linux", "OS/2" };

        tweetList=new ArrayList<>();
        if(tweetList.size()>0){

            ArrayAdapter<String> files = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    tweetList);



            if(files!=null){
                listView.setAdapter(files);
            }

        }


        Log.d("yyyyy",tweetList.toString());

//        TextView tv= (TextView) view.findViewById(R.id.tv);
//        tv.setText("Twitter Feed");
//        Log.d("yyyyy",tweetList.toString());
//        tv.setText( tweetList.toString());
        return view;
    }
    @Override
    public void onResume(){
        Log.d("aaaa",tweetList.toString());

//        Log.d("bbbb",TwoFragment.tweetList.toString());

        super.onResume();
    }

    private void logIn() {



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.d(TAG, sharedPreferences.toString());
        if (!sharedPreferences.getBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,false))
        {
            Log.d(TAG, ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN);
            new TokenGet().execute();
        }
        else
        {

            new PostTweet().execute();
//            new ReadTweet().execute();

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
                auth_dialog = new Dialog(getActivity());
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
                            Toast.makeText(getActivity(), "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
                Log.d(TAG, auth_dialog.toString());
                auth_dialog.show();
                auth_dialog.setCancelable(true);



            }else{

                Toast.makeText(getActivity(), "Sorry !, Network Error or Invalid Credentials", Toast.LENGTH_SHORT).show();


            }
        }
    }

    private class AccessTokenGet extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getActivity());
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

//                new PostTweet().execute();
//                new PostTweet().execute();
                //
            }
        }
    }


    private class PostTweet extends AsyncTask<String, String, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("REadTweet", "ReadTweet111");

        }

        @Override
        protected List<String> doInBackground(String... params) {
            tweetList= new ArrayList<String>();
            Log.d("two", "Posttweet");
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
            builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
            AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            ResponseList<twitter4j.Status> statuses = null;
            try {
//                statuses = twitter.getUserTimeline("MobileApp4");
                statuses = twitter.getHomeTimeline();
            } catch (TwitterException e) {
                e.printStackTrace();
            }


            for (twitter4j.Status status1 : statuses) {
                tweetList.add(status1.getText());
            }

            return tweetList;
        }

        @Override
        protected void onPostExecute(List<String> str) {
            run(str);
        }
    }

    private void run(List<String> str) {
//        Log.d("bbbbbbb",str.toString());
//        tweetList.add(str.toString());


        ListView listView = (ListView) getActivity().findViewById(R.id.listView1);
        ArrayAdapter<String> files = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                tweetList);

        listView.setAdapter(files);

//        setTweetList(str);
    }



    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
//           Log.d("111111","222222222");
            if(getActivity()!=null){
                Log.d("aaa","bbbb");
                logIn();
            }
        }else{
            Log.d("3333","$44442");
        }
    }
}
