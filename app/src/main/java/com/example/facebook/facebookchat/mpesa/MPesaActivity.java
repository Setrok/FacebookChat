package com.example.facebook.facebookchat.mpesa;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.bdhobare.mpesa.Mode;
import com.bdhobare.mpesa.Mpesa;
import com.bdhobare.mpesa.interfaces.AuthListener;
import com.bdhobare.mpesa.interfaces.MpesaListener;
import com.bdhobare.mpesa.models.STKPush;
import com.bdhobare.mpesa.models.STKPush.Builder;
import com.bdhobare.mpesa.utils.Pair;
import com.example.facebook.facebookchat.tools.AssetLoader;
import com.google.firebase.iid.FirebaseInstanceId;
import com.example.facebook.facebookchat.R;

public class MPesaActivity extends AppCompatActivity implements AuthListener, MpesaListener {
    //TODO: Replace these values from
    public static final String BUSINESS_SHORT_CODE = "174379";// "855310";//"174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";// "b4168f9de4b8537042929f959db5ca90743132bc13ce5ca22c8561bffa1c968e";//"bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String CONSUMER_KEY = "AOti7P1rEdnLwTKMIDqDrRcGAlJALnz5";// "OsIF56jn5C0w1gJ2Y5BABeomZuIqAkBk";//"AOti7P1rEdnLwTKMIDqDrRcGAlJALnz5";
    public static final String CONSUMER_SECRET = "Tzf8r1P1WBegxgYp";// "zx4ndweH1db32OSM";//"Tzf8r1P1WBegxgYp";
    public static final String CALLBACK_URL = "http://skillbar.esy.es/mpesa/mpesa.php";

    public static final String NOTIFICATION = "PushNotification";
    public static final String SHARED_PREFERENCES = "com.example.facebook.facebookchat";

    public static final Integer PRICE_OF_SEARCH = 1;

    Button pay;
    ProgressDialog dialog;
    EditText phone;
    EditText amount;

    LinearLayout mpesaMainLayout;
    LinearLayout mpesaMessageLayout;
    LinearLayout mpesaInputLayout;

    LinearLayout resultDialog;
    Button dialogOk;
    TextView resultTitle;
    TextView resultDesc;
    TextView mpesaMessage;
    TextView mpesaPriceOfSearch;
    ImageView resultImage;
    ProgressBar mainProgressBar;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa);
        pay = (Button)findViewById(R.id.mpesa_pay);
        phone = (EditText)findViewById(R.id.mpesa_phone);
        amount = (EditText)findViewById(R.id.mpesa_amount);

        mpesaMainLayout = (LinearLayout) findViewById(R.id.mpesa_main_layout);
        mpesaMessageLayout = (LinearLayout) findViewById(R.id.mpesa_message_layout);
        mpesaInputLayout = (LinearLayout) findViewById(R.id.mpesa_input_layout);

        resultDialog = (LinearLayout) findViewById(R.id.mpesa_result_dialog_layout);
        dialogOk = (Button) findViewById(R.id.mpesa_dialog_ok_button);
        resultTitle = (TextView)findViewById(R.id.mpesa_text_result_title);
        resultDesc = (TextView)findViewById(R.id.mpesa_text_result_desc);
        mpesaMessage = (TextView) findViewById(R.id.mpesa_message);
        mpesaPriceOfSearch = (TextView) findViewById(R.id.mpesa_text_price_of_search);
        resultImage = (ImageView) findViewById(R.id.mpesa_image_result);
        mainProgressBar = (ProgressBar) findViewById(R.id.mpesa_main_progress_bar);

        mpesaPriceOfSearch.setText("1 View  =  "+PRICE_OF_SEARCH+" KES");

        Mpesa.with(this, CONSUMER_KEY, CONSUMER_SECRET, Mode.SANDBOX);//Mode.PRODUCTION
        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing");
        dialog.setIndeterminate(true);


        //FONTS
//        AssetLoader.loadAssets(this);
//
//        pay.setTypeface(AssetLoader.getMainBoldFont());
//        dialogOk.setTypeface(AssetLoader.getMainBoldFont());
//        resultTitle.setTypeface(AssetLoader.getMainBoldFont());
//        mpesaPriceOfSearch.setTypeface(AssetLoader.getMainBoldFont());
//        resultDesc.setTypeface(AssetLoader.getMainFont());
//        mpesaMessage.setTypeface(AssetLoader.getMainFont());
//        phone.setTypeface(AssetLoader.getMainFont());
//        amount.setTypeface(AssetLoader.getMainFont());


        //on init
        goneViews(resultDialog,mainProgressBar,mpesaMessageLayout);
        visibleViews(mpesaMainLayout,mpesaInputLayout);

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!amount.getText().toString().isEmpty()) {

                    try {

                        Integer amInt = Integer.parseInt(amount.getText().toString());

                        if (amInt > 1)
                            mpesaPriceOfSearch.setText(amInt + " Views  =  " + (amInt * PRICE_OF_SEARCH) + " KES");
                        else
                            mpesaPriceOfSearch.setText(amInt + " View  =  " + (amInt * PRICE_OF_SEARCH) + " KES");

                    } catch (Exception e) {
                        amount.setError("Too large");//a>2147483600
                    }

                }else {
                    mpesaPriceOfSearch.setText("1 View  =  "+PRICE_OF_SEARCH+" KES");
                    amount.setError("Empty field");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p = phone.getText().toString();
                String am = amount.getText().toString();

                if (!am.isEmpty()) {

                    am = ""+Integer.parseInt(amount.getText().toString()) * PRICE_OF_SEARCH;

                    try {

                        int a = Integer.valueOf(am);
                        if (p.isEmpty()) {
                            phone.setError("Enter phone.");
                            return;
                        }

                        if (a >= 1)
                            pay(p, a);
                        else
                            amount.setError("1 or more");
//                        Toast.makeText(getApplicationContext(), "Too small", Toast.LENGTH_LONG).show();

                    }catch (Exception e){
                        amount.setError("Too large");//a>2147483600
                    }



                }else {
                    amount.setError("Empty field");
//                    Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();

                }


            }
        });

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goneViews(resultDialog);
                finish();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NOTIFICATION)) {
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    int code = intent.getIntExtra("code", 0);
                    showDialog(title, message, code);
                }
            }
        };
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private void visibleViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void goneViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAuthError(Pair<Integer, String> result) {
        Log.e("Error", result.message);
    }

    @Override
    public void onAuthSuccess() {
        //TODO make payment
        pay.setEnabled(true);
    }
    private void pay(String phone, int amount){
//        dialog.show();
        //progressBar show
        goneViews(mpesaMainLayout, resultDialog);
        visibleViews(mainProgressBar);

        STKPush.Builder builder = new Builder(BUSINESS_SHORT_CODE, PASSKEY, amount,BUSINESS_SHORT_CODE, phone);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String token = sharedPreferences.getString("InstanceID", "");

        if (token.isEmpty()){
            // Get updated InstanceID token.
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            sharedPreferences.edit().putString("InstanceID", refreshedToken).apply();
            token = refreshedToken;
        }

        Log.i("GLOBAL STEP TOKEN","T="+token);

        builder.setFirebaseRegID(token);

        builder.setCallBackURL(CALLBACK_URL);

        STKPush push = builder.build();

        Mpesa.getInstance().pay(this, push);

    }
    private void showDialog(String title, String message,int code){

        resultTitle.setText(title);
        resultDesc.setText(message);
        if (code == 0){
            resultImage.setImageResource(R.drawable.icon_success);
        }else {
            resultImage.setImageResource(R.drawable.icon_no_success);
        }

        goneViews(mpesaMainLayout);
        visibleViews(resultDialog);

//        MaterialDialog dialog = new MaterialDialog.Builder(this)
//                .title(title)
//                .titleGravity(GravityEnum.CENTER)
//                .customView(R.layout.success_dialog, true)
//                .positiveText("OK")
//                .cancelable(false)
//                .widgetColorRes(R.color.colorPrimary)//TEST
//                .callback(new ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        super.onPositive(dialog);
//                        dialog.dismiss();
//                        finish();
//                    }
//                })
//                .build();
//        View view=dialog.getCustomView();
//        TextView messageText = (TextView)view.findViewById(R.id.message);
//        ImageView imageView = (ImageView)view.findViewById(R.id.success);
//        if (code != 0){
//            imageView.setVisibility(View.GONE);
//        }else {
////            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG);
//        }
//        messageText.setText(message);
//        dialog.show();



    }

    @Override
    public void onMpesaError(Pair<Integer, String> result) {
//        dialog.hide();
        //progressBar hide
        goneViews(mainProgressBar, resultDialog, mpesaMessageLayout);
        visibleViews(mpesaMainLayout, mpesaInputLayout);

        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMpesaSuccess(String MerchantRequestID, String CheckoutRequestID, String CustomerMessage) {
//        dialog.hide();

        mpesaMessage.setText(CustomerMessage + "\nPlease, confirm payment");

        //progressBar hide
        goneViews(mainProgressBar, resultDialog, mpesaInputLayout);
        visibleViews(mpesaMainLayout, mpesaMessageLayout);

        Toast.makeText(this, CustomerMessage, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NOTIFICATION));

    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}