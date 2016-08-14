package in.co.theshipper.www.shipper_driver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    private  String TAG = MainActivity.class.getName();
    private  EditText MOBILE_NO;
    protected  RequestQueue requestQueue;
    private int otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MOBILE_NO = (EditText) findViewById(R.id.editText);
    }
    public void userReg(View view){
        Fn.logD("MAIN_ACTIVITY_LIFECYCLE", "userReg called");
        if ( checkValidation () ) {
//            Fn.showProgressDialog(Constants.Message.LOADING,this);
            String reg_url = Constants.Config.ROOT_PATH + "driver_registration";
            Random ran = new Random();
            otp = (100000 + ran.nextInt(900000));
//            Log.d("OTP",String.valueOf(otp));
            String mobile_no = MOBILE_NO.getText().toString();
            HashMap<String,String> hashMap = new HashMap<String, String>();
            hashMap.put("mobile_no", mobile_no);
            hashMap.put("OTP", String.valueOf(otp));
            Fn.putPreference(this, "OTP", String.valueOf(otp));
            Fn.putPreference(this, "mobile_no", mobile_no);
            Fn.logD("OTP", Fn.getPreference(this, "OTP"));
            sendVolleyRequest(reg_url, Fn.checkParams(hashMap));

        }
        else
            Toast.makeText(MainActivity.this, "Form contains error", Toast.LENGTH_LONG).show();

    }
    private boolean checkValidation() {
        boolean ret = true;
        if (!FormValidation.isPhoneNumber(MOBILE_NO, true)) ret = false;

        return ret;
    }
    protected void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                String trimmed_response = response.substring(response.indexOf("{"));
                Fn.logD("trimmed_response", trimmed_response);
                registerSuccess(trimmed_response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                ErrorDialog(Constants.Title.NETWORK_ERROR,Constants.Message.NETWORK_ERROR);
            }
        }){
            @Override
            protected HashMap<String,String> getParams(){
                return hMap;
            }
        };
        stringRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, stringRequest, this);
    }

    protected void registerSuccess(String response){
        if(!Fn.CheckJsonError(response)) {
            Intent intent = new Intent(this, OtpVerification.class);
            intent.putExtra("OTP", otp);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);
        }
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(this, Title, Message);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Fn.logE("MAIN_ACTIVITY_LIFECYCLE", "onRestart called");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
        Fn.logE("MAIN_ACTIVITY_LIFECYCLE", "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
        Fn.logE("MAIN_ACTIVITY_LIFECYCLE", "onPause called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fn.cancelAllRequest(requestQueue,TAG);
        Fn.logE("MAIN_ACTIVITY_LIFECYCLE", "onDestroy called");
    }


}
