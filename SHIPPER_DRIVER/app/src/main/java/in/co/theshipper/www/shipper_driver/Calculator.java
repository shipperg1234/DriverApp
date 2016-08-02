package in.co.theshipper.www.shipper_driver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Calculator extends Fragment {

    protected RequestQueue requestQueue;
    private String TAG = Calculator.class.getName();
    private Button start, stop;
    private LinearLayout start_view,stop_view;
    protected DBController controller;
    private Location location;
    public Calculator() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onCreateView Called");
        controller = new DBController(getActivity());
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);
        start_view = (LinearLayout) view.findViewById(R.id.start_view);
        stop_view = (LinearLayout) view.findViewById(R.id.stop_view);
        start = (Button) view.findViewById(R.id.start);
        stop = (Button) view.findViewById(R.id.stop);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onActivityCreated Called");
        super.onActivityCreated(savedInstanceState);
        Fn.SystemPrintLn("yyyyyy"+Fn.isMyServiceRunning(GPSService.class,getActivity()));
        if(Fn.isMyServiceRunning(GPSService.class,getActivity())==true)start_view.setVisibility(View.GONE);
        if(Fn.isMyServiceRunning(GPSService.class,getActivity())==false)stop_view.setVisibility(View.GONE);
//        }
        final String user_token =Fn.getPreference(getActivity(), Constants.Keys.USER_TOKEN);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("user_token",user_token);
                hashMap.put("status", "1");
                sendVolleyRequest(change_driver_status, Fn.checkParams(hashMap), "change_status_busy");
            }

        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("user_token",user_token);
                hashMap.put("status", "0");
                sendVolleyRequest(change_driver_status, Fn.checkParams(hashMap), "change_status_free");
            }
        });
    }
    protected void sendVolleyRequest(String URL, final HashMap<String, String> hMap, final String method) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse_booking_status", String.valueOf(response));
                Fn.logD("method", method);
                if (method.equals("change_status_busy")) {
                    bookingStatusBusySuccess(response);
                } else if (method.equals("change_status_free")) {
                    bookingStatusFreeSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                Fn.ToastShort(getActivity(), Constants.Message.NETWORK_ERROR);
            }
        }) {
            @Override
            protected HashMap<String, String> getParams() {
                return hMap;
            }
        };
        stringRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, stringRequest, getActivity());
    }
    protected void bookingStatusBusySuccess(String response){
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "bookingStatusBusySuccess Called");
        if(!Fn.CheckJsonError(response)) {
            if (FullActivity.mGoogleApiClient.isConnected()) {
                do {
                    location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                } while (location == null);
                Fn.putPreference(getActivity(), Constants.Keys.EXACT_PICKUP_POINT, Fn.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));
            }
            Intent intent = new Intent(getActivity(), GPSService.class);
            Fn.SystemPrintLn("Started tracking");
            stop_view.setVisibility(View.VISIBLE);
            start_view.setVisibility(View.GONE);
            getActivity().startService(Fn.CheckIntent(intent));
        }
    }
    protected void bookingStatusFreeSuccess(String response){
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "bookingStatusFreeSuccess Called");
        if(!Fn.CheckJsonError(response)) {
            final Intent intent = new Intent(getActivity(), GPSService.class);
            if(FullActivity.mGoogleApiClient.isConnected()){
                do {
                    location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                } while (location == null);
                Fn.putPreference(getActivity(),Constants.Keys.EXACT_DROPOFF_POINT,Fn.getLocationAddress(location.getLatitude(),location.getLongitude(),getActivity()));
            }
            Fn.SystemPrintLn("Stopped tracking");
            start_view.setVisibility(View.VISIBLE);
            stop_view.setVisibility(View.GONE);
            getActivity().stopService(intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onResume Called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onPause Called");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fn.cancelAllRequest(requestQueue, TAG);
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onDestroyView Called");
    }
}
