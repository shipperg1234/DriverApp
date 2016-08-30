package in.co.theshipper.www.shipper_driver;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class Calculator extends Fragment {

    protected RequestQueue requestQueue;
    private String TAG = Calculator.class.getName();
    private Button  stop_journey,stop_timer;
    private LinearLayout stop_view,timer_view;
    protected DBController controller;
    private Location location;
    private long loading_stop_time = 0,journey_stop_time = 0;
    private Calendar calendar2,calendar3;
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
        stop_view = (LinearLayout) view.findViewById(R.id.stop_view);
        timer_view = (LinearLayout) view.findViewById(R.id.timer_view);
        stop_journey = (Button) view.findViewById(R.id.stop_journey);
        stop_timer = (Button) view.findViewById(R.id.stop_timer);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "onActivityCreated Called");
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null) {
            Fn.SystemPrintLn("yyyyyy" + Fn.isMyServiceRunning(GPSService.class, getActivity()));
            if (Fn.isMyServiceRunning(GPSService.class, getActivity()) == false) {
                stop_view.setVisibility(View.GONE);
                timer_view.setVisibility(View.GONE);
            }
            final String user_token = Fn.getPreference(getActivity(), Constants.Keys.USER_TOKEN);
            stop_timer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar2 = Calendar.getInstance(tz);
                    loading_stop_time = calendar2.getTimeInMillis();
                    Fn.putPreference(getActivity(), Constants.Keys.UNLOADING_STOP_TIME, String.valueOf(loading_stop_time));
                    String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("user_token", user_token);
                    hashMap.put("status", "0");
                    sendVolleyRequest(change_driver_status, Fn.checkParams(hashMap), "change_status_free");
                }
            });
            stop_journey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
                    calendar3 = Calendar.getInstance(tz);
                    journey_stop_time = calendar3.getTimeInMillis();
                    Fn.putPreference(getActivity(), Constants.Keys.JOURNEY_STOP_TIME, String.valueOf(journey_stop_time));
                    timer_view.setVisibility(View.VISIBLE);
                    stop_view.setVisibility(View.GONE);
                    final Intent intent = new Intent(getActivity(), GPSService.class);
                    getActivity().stopService(intent);
                    Intent i = new Intent(getActivity(), TimerService.class);
                    getActivity().startService(i);
                }
            });
        }
    }
    protected void sendVolleyRequest(String URL, final HashMap<String, String> hMap, final String method) {
        if(getActivity() != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Fn.logD("onResponse_booking_status", String.valueOf(response));
                    Fn.logD("method", method);
                    if (method.equals("change_status_free")) {
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
    }
//    protected void bookingStatusBusySuccess(String response){
//        if(getActivity() != null) {
//            Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "bookingStatusBusySuccess Called");
//            if(!Fn.CheckJsonError(response)) {
//                if (FullActivity.mGoogleApiClient.isConnected()) {
//                    do {
//                        location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
//                    } while (location == null);
//                    Fn.putPreference(getActivity(), Constants.Keys.EXACT_PICKUP_POINT, Fn.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));
//                }
//                Intent intent = new Intent(getActivity(), GPSService.class);
//                Fn.SystemPrintLn("Started tracking");
//                stop_view.setVisibility(View.VISIBLE);
//                getActivity().startService(Fn.CheckIntent(intent));
//            }
//        }
//    }
    protected void bookingStatusFreeSuccess(String response){
        if(getActivity() != null) {
            Fn.logD("CALCULATOR_FRAGMENT_LIFECYCLE", "bookingStatusFreeSuccess Called");
            if (!Fn.CheckJsonError(response)) {
                Intent Intent = new Intent(getActivity(), TimerService.class);
                getActivity().stopService(Intent);
                if (FullActivity.mGoogleApiClient.isConnected()) {
                    do {
                        location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                    } while (location == null);
                    Fn.putPreference(getActivity(), Constants.Keys.EXACT_DROPOFF_POINT, Fn.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));
                }
                Fn.SystemPrintLn("Stopped tracking");
                Intent i = new Intent(getActivity(), FullActivity.class);
                i.putExtra("menuFragment", "BillDetails");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(Fn.CheckIntent(i));
            }
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
