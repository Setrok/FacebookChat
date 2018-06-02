package com.example.facebook.facebookchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class StartActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView email;
    LoginButton loginButton;
    Button logout_btn;
    ProgressBar progressBar;
    ImageView avatar;
    AccessTokenTracker accessTokenTracker;
    LoginManager manager;
    //URL profile_picture;
    String nameStr,picStr,birthdayStr,emailStr;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //printKeyHash();
        callbackManager = CallbackManager.Factory.create();
        manager = LoginManager.getInstance();

        pref = getApplicationContext().getSharedPreferences("FBPref", MODE_PRIVATE);
        editor = pref.edit();

        //if( !pref.getString("user_name","").equals("") )
            nameStr = pref.getString("user_name","");
        //if( !pref.getString("user_pic","").equals("") )
            picStr = pref.getString("user_pic","");

            birthdayStr = pref.getString("user_birthday","");

            emailStr = pref.getString("user_email","");

        avatar = findViewById(R.id.profile_avatar);
        email = findViewById(R.id.profil_email);
        //birthday = findViewById(R.id.profil_birthday);
        logout_btn = findViewById(R.id.redirect_button);

        email.setVisibility(View.INVISIBLE);
        //birthday.setVisibility(View.INVISIBLE);
        avatar.setVisibility(View.INVISIBLE);

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_birthday"));

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect();
            }
        });

//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(AccessToken.getCurrentAccessToken()!=null)
//                {
//                    Log.v("User is login","YES");
//
//                }
//                else
//                {
//                    Log.v("User is not logged in","OK");
//                    //LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity1.this, (Arrays.asList("public_profile", "user_friends","user_birthday","user_about_me","email")));
//                }
//            }
//        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                progressBar.setVisibility(View.VISIBLE);

                //Log.i("AppInfo","Tapped");

                String accessToken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        progressBar.setVisibility(View.INVISIBLE);

                        Log.i("fb_response",response.toString());

                        getFacebookData(object);

                    }
                });

                //Request Graph API
                Bundle parameters = new Bundle();
                parameters.putString("fields","id,first_name,last_name,birthday,email");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i("InfoApp","OnStart triggered");

        Log.e("InfoApp","On Start " + nameStr);
        Log.e("InfoApp","On Start " + birthdayStr);
        Log.e("InfoApp","On Start " + picStr);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    Log.i("AppInfo","Not logged in");
                    email.setVisibility(View.INVISIBLE);
                    avatar.setVisibility(View.INVISIBLE);
                    logout_btn.setEnabled(false);
                    logout_btn.setVisibility(View.INVISIBLE);

                } else {
                    Log.i("AppInfo","Logged in");
                    email.setVisibility(View.VISIBLE);
                    avatar.setVisibility(View.VISIBLE);
                    email.setText(nameStr);
                    loadImageInto(picStr);

                    logout_btn.setEnabled(true);
                    logout_btn.setVisibility(View.VISIBLE);
                    //email.setText(AccessToken.getCurrentAccessToken().getUserId());
                }
            }
        };

        if(AccessToken.getCurrentAccessToken()!=null){
            Log.d("AppInfo",picStr);
            email.setVisibility(View.VISIBLE);
            avatar.setVisibility(View.VISIBLE);

            email.setText(nameStr);
            loadImageInto(picStr);

            logout_btn.setEnabled(true);
            logout_btn.setVisibility(View.VISIBLE);

        } else {
            logout_btn.setEnabled(false);
            logout_btn.setVisibility(View.INVISIBLE);
        }


    }

    private void getFacebookData(JSONObject object) {
        try{
            picStr = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250").toString();
            loadImageInto(picStr);

            nameStr = object.getString("first_name") + " " + object.getString("last_name");
            birthdayStr = object.getString("birthday");

            email.setText(nameStr);

            try{
                emailStr = object.getString("email");
                editor.putString("user_email",emailStr);
            } catch (JSONException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            //birthdayStr = object.getString("birthday");
            editor.putString("user_name",nameStr);
            editor.putString("user_pic",picStr);
            editor.putString("user_birthday",birthdayStr);
            editor.commit();

            Log.e("InfoApp","Has child Register" + nameStr);
            Log.e("InfoApp","Has child Register" + birthdayStr);
            Log.e("InfoApp","Has child Register" + picStr);
            //editor.putString("user_name",nameStr);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            //email.setText();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }


    private void loadImageInto(String url){
        try{

            Uri uri = Uri.parse(url);
            Glide.with(getApplicationContext()).load(uri).into(avatar);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void redirect(){

        if(!checkNetwork()){

        }

        Log.e("InfoApp","Redirect name" + nameStr);
        Log.e("InfoApp","Redirect birthday" + birthdayStr);
        Log.e("InfoApp","Redirect email" + emailStr);
        Log.e("InfoApp","Redirect pic" + picStr);


        Intent intent = new Intent(getApplicationContext(),PhoneVerification.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(!nameStr.isEmpty() && !picStr.isEmpty()) {
            Log.e("InfoApp","Redirect completed");
            intent.putExtra("name", nameStr);
            intent.putExtra("birthday", birthdayStr);
            intent.putExtra("email", emailStr);
            intent.putExtra("URL", picStr);
        }
//        if(null != emailStr){
//            intent.putExtra("email",emailStr);
//        }

        startActivity(intent);

        //manager.logOut();

    }

    public void printKeyHash(){

        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.facebook.facebookchat", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNetwork() {
        try {
            boolean wifiDataAvailable = false;
            boolean mobileDataAvailable = false;
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
            for (NetworkInfo netInfo : networkInfo) {
                if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                    if (netInfo.isConnected())
                        wifiDataAvailable = true;
                if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (netInfo.isConnected())
                        mobileDataAvailable = true;
            }
            return wifiDataAvailable || mobileDataAvailable;
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_LONG).show();
            return false;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        accessTokenTracker.stopTracking();

    }
}
