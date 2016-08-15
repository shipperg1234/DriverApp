package in.co.theshipper.www.shipper_driver;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzir.runOnUiThread;


public class BookingDetails extends Fragment implements View.OnClickListener {


    protected RequestQueue requestQueue;
    private String TAG = BookingDetails.class.getName();
    protected View view;
    protected Context context;
    private LinearLayout map, start_view, stop_view;
    private TextView location_datetime, map_view;
    private Button callButton, start, stop;
    private SupportMapFragment mMapFragment;
    public GoogleMap mMap = null;
    private LocationManager locationManager;
    // flag for GPS Status
    private boolean isGPSEnabled = false;
    // flag for network status
    private boolean isNetworkEnabled = false;
    private String provider_info;
    private double current_lat, current_lng;
    private boolean stopTimer = false,stopTimerForever = false;
    private Timer timer;
    private Location location;
    private String errFlag, received_customer_name, received_crn_no, received_customer_token, received_customer_current_lat, received_customer_current_lng;
    private String crn_no = "";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private ImageView material_image,popup,vehicle_image;
    private Dialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onAttach Called");
    }

    public BookingDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onCreateView Called");
        if((getActivity().getIntent()!=null)&&(getActivity().getIntent().getExtras()!=null)) {
            Bundle bundle = getActivity().getIntent().getExtras();
            crn_no = Fn.getValueFromBundle(bundle,"crn_no");
            getActivity().getIntent().setData(null);
            getActivity().setIntent(null);
        }else if(this.getArguments()!=null) {
            Bundle bundle = this.getArguments();
            crn_no = Fn.getValueFromBundle(bundle, "crn_no");
        }
        view = inflater.inflate(R.layout.fragment_booking_details, container, false);
        start_view = (LinearLayout) view.findViewById(R.id.start_view);
        start = (Button) view.findViewById(R.id.start);
        map = (LinearLayout) view.findViewById(R.id.map);
        callButton = (Button) view.findViewById(R.id.customer_mobile_no);
        material_image = (ImageView) view.findViewById(R.id.material_image);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(true);
        popup=(ImageView)dialog.findViewById(R.id.image_popup);
        callButton.setOnClickListener(this);
        Fn.logD("Map Added", "Map Added");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onViewCreated Called");
        super.onViewCreated(view, savedInstanceState);
        String booking_status_url = Constants.Config.ROOT_PATH + "get_driver_booking_status";
        mMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map, mMapFragment, "MAP_FRAGMENT").commit();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        // String CrnNo = Fn.getPreference(getActivity(),"current_crn_no");
        String user_token = Fn.getPreference(context, "user_token");
        hashMap.put("crn_no", crn_no);
        hashMap.put("user_token", user_token);
        sendVolleyRequest(booking_status_url, Fn.checkParams(hashMap), "booking_status");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Fn.isMyServiceRunning(GPSService.class,getActivity())==true){
            stopTimerForever = true;
            start_view.setVisibility(View.GONE);
        }
//
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String change_driver_status = Constants.Config.ROOT_PATH + "change_driver_status";
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("user_token", Fn.getPreference(getActivity(), Constants.Keys.USER_TOKEN));
                hashMap.put("status", "1");
                sendVolleyRequest(change_driver_status, Fn.checkParams(hashMap), "change_status");

            }

        });
    }

    protected void sendVolleyRequest(String URL, final HashMap<String, String> hMap, final String method) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse_booking_status", String.valueOf(response));
                Fn.logD("method", method);
                if (method.equals("booking_status")) {
//                    Fn.logD("booking_status", "booking_status");
                    bookingStatusSuccess(response);
                } else if (method.equals("customer_location")) {
//                    Fn.logD("vehicle_location", "vehicle_location");
                    vehicleLocationSuccess(response);
                } else if (method.equals("draw_path")) {
//                    Fn.logD("method", "method");
                    drawPath(response);
                }else if (method.equals("change_status")) {
                    statusChangeSuccess(response);
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

    protected void statusChangeSuccess(String response){
        if(!Fn.CheckJsonError(response)) {
            if (FullActivity.mGoogleApiClient.isConnected()) {
                do {
                    location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                } while (location == null);
                Fn.putPreference(getActivity(), Constants.Keys.EXACT_PICKUP_POINT, Fn.getLocationAddress(location.getLatitude(), location.getLongitude(), getActivity()));
            }else{
                Fn.ToastShort(getActivity(), Constants.Message.NETWORK_ERROR);
            }
            stopTimerForever = true;
            Intent intent = new Intent(getActivity(), GPSService.class);
            intent.putExtra("crn_no", received_crn_no);
            Fn.SystemPrintLn("Started tracking");
            start_view.setVisibility(View.GONE);
            getActivity().startService(Fn.CheckIntent(intent));
        }
    }
    protected void bookingStatusSuccess(String response) {
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "bookingStatusSuccess Called");
        if (!Fn.CheckJsonError(response)) {
//            Fn.logD("bookingStatusSuccess", "bookingStatusSuccess Called");
            Fn.logD("received_json", response);
//            String received_crn_no = "";
//            JSONObject jsonObject;
//            JSONArray jsonArray;
            try {
                JSONObject jsonObject = new JSONObject(response);
                //jsonArray = jsonObject.getJSONArray("likes");
                String errFlag = jsonObject.getString("errFlag");
                String errMsg = jsonObject.getString("errMsg");
                if (errFlag.equals("1")) {
                    Fn.logD("toastNotdone", "toastNotdone");
                } else if (errFlag.equals("0")) {
//                    textView.setTextSize(R.dimen.large_text_size);
//                    textView.setTextColor(Color.BLACK);
//                    textView.setBackgroundColor(R.color.middle_gray);
                    if (jsonObject.has("likes")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;
                        while (count < jsonArray.length()) {
//                            String[] VehicleList = getResources().getStringArray(R.array.vehicle_list);
                            Fn.logD("likes_entered", "likes_entered");
                            JSONObject JO = jsonArray.getJSONObject(count);
                            location_datetime = (TextView) view.findViewById(R.id.location_datetime);
//                            TextView crn_no_view = (TextView) view.findViewById(R.id.crn_no);
//                            TextView vehicle_type_view = (TextView) view.findViewById(R.id.vehicle_type);
                            TextView customer_mobile_no_view = (Button) view.findViewById(R.id.customer_mobile_no);
                            TextView customer_name_view = (TextView) view.findViewById(R.id.customer_name);
//                            crn_no_view.setText(received_crn_no);
                            received_crn_no = JO.getString("crn_no");
                            received_customer_token = JO.getString("customer_token");
                            received_customer_name = JO.getString("customer_name");
                            String received_customer_mobile_no = JO.getString("customer_mobile_no");
                            String received_location_update_datetime = JO.getString("customer_location_datetime");
                            received_customer_current_lat = JO.getString("customer_location_lat");
                            received_customer_current_lng = JO.getString("customer_location_lng");
                            String material_image_url=JO.getString("material_image_url");
                            String profile_pic_url = Constants.Config.ROOT_PATH+material_image_url;
                            Fn.logD("profile_pic_url", profile_pic_url);
                            if(material_image_url.length()>0){
                                downloadBitmapFromURL(profile_pic_url);
                            }else{
                                material_image.setImageResource(R.drawable.addcontact);
                                material_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        popup.setImageResource(R.drawable.addcontact);
                                        dialog.show();
                                    }
                                });
                            }
                            location_datetime.setText("Last Seen: " + Fn.getDateName(received_location_update_datetime));
                            customer_name_view.setText("Customer: "+received_customer_name);
                            customer_mobile_no_view.setText(received_customer_mobile_no);
                            setUpMapIfNeeded();
                            TimerProgramm();
//                          Fn.putPreference(this,"crn_no",received_crn_no);
                            count++;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);
        }
    }
    protected void vehicleLocationSuccess(String response) {
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "vehicleLocationSuccess Called");
        try {
            if (!Fn.CheckJsonError(response)) {
                JSONObject jsonObject = new JSONObject(response);
                //jsonArray = jsonObject.getJSONArray("likes");
                String errFlag = jsonObject.getString("errFlag");
                String errMsg = jsonObject.getString("errMsg");
                if (errFlag.equals("1")) {
                    Fn.logD("toastNotdone", "toastNotdone");
                } else if (errFlag.equals("0")) {
                    if (jsonObject.has("likes")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;
                        while (count < jsonArray.length()) {
                            JSONObject JO = jsonArray.getJSONObject(count);
                            received_customer_current_lat = JO.getString("customer_location_lat");
                            received_customer_current_lng = JO.getString("customer_location_lng");
                            String received_location_update_datetime = JO.getString("customer_location_datetime");
                            location_datetime.setText("Last Seen: " + Fn.getDateName(received_location_update_datetime));
                            map.setVisibility(View.VISIBLE);
//                                map_view.setVisibility(View.GONE);
                            Fn.logD("LocationSuccessCallingMap", "LocationSuccessCallingMap");
                            setUpMapIfNeeded();
                            count++;
                        }
                    }
                }
            } else {
//                    ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);
                Fn.ToastShort(getActivity(),Constants.Message.SERVER_ERROR);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    protected void TimerProgramm() {
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "TimerProgramm Called");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Fn.logD("TimerProgram_running", "TimerProgram_running");
                        if (!stopTimer && !stopTimerForever) {
//                            hashMap.clear();
                            String customer_location_url = Constants.Config.ROOT_PATH + "get_customer_location";
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("customer_token", received_customer_token);
                            sendVolleyRequest(customer_location_url, Fn.checkParams(hashMap), "customer_location");
                        }
                    }
                });
            }
        }, Constants.Config.GET_CUSTOMER_LOCATION_DELAY, Constants.Config.GET_CUSTOMER_LOCATION_PERIOD);
    }
    protected void setUpMapIfNeeded() {
        Fn.logD("setUpMapIfNeeded", "map_setup" + String.valueOf(mMap));
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap != null) {
            mMap.clear();
            mMap = null;
        }
        if (mMap == null) {
//            Try to obtain the map from the SupportMapFragment.
//            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("MAP_FRAGMENT");
            Fn.logD("mMapFragment", String.valueOf(mMapFragment));
            mMap = mMapFragment.getMap();
            Fn.logD("map_not_null", String.valueOf(mMap));
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                if(FullActivity.mGoogleApiClient.isConnected()) {
                    do{
                        location = Fn.getAccurateCurrentlocation(FullActivity.mGoogleApiClient, getActivity());
                    }while(location ==  null);
                    if (location != null) {
                        if (mMap != null)
                        {
                            current_lat = location.getLatitude();
                            current_lng = location.getLongitude();
                            float c = Fn.getBearing(current_lat, current_lng, Double.parseDouble(received_customer_current_lat), Double.parseDouble(received_customer_current_lng));
                            LatLng latlng = new LatLng(current_lat, current_lng);// This methods gets the users current longitude and latitude.
                            //                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));//Moves the camera to users current longitude and latitude
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latlng, Constants.Config.MAP_HIGH_ZOOM_LEVEL, 1, c)));
                            //                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, Constants.Config.MAP_HIGH_ZOOM_LEVEL));//Animates camera and zooms to preferred state on the user's current location.
                            //                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            Fn.logD("received_driver_current_lat", received_customer_current_lat);
                            Fn.logD("received_driver_current_lng", received_customer_current_lng);

                            try {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(received_customer_current_lat), Double.parseDouble(received_customer_current_lng)))
                                        .title(received_customer_name));
                                //                                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble("22.6256"), Double.parseDouble("88.3576"))).title("Driver"));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            String url = makeURL(received_customer_current_lat, received_customer_current_lng, String.valueOf(current_lat), String.valueOf(current_lng));
                            Log.d("made_url", url);
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            sendVolleyRequest(url, Fn.checkParams(hashMap), "draw_path");
                        }
                    }
                }
            }
        }
    }
    protected String makeURL(String sourceLat, String sourceLng, String destLat,String destLng){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        try {
            urlString.append(URLEncoder.encode(sourceLat,"UTF-8"));
            urlString.append(",");
            urlString.append(URLEncoder.encode(sourceLng,"UTF-8"));
            urlString.append("&destination=");// to
            urlString.append(URLEncoder.encode(destLat,"UTF-8"));
            urlString.append(",");
            urlString.append(URLEncoder.encode(destLng,"UTF-8"));
            urlString.append("&sensor=false&mode=driving&alternatives=true");
            urlString.append("&key="+URLEncoder.encode(getResources().getString(R.string.server_APIkey1), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlString.toString();
    }
    protected void drawPath(String  result) {
        Fn.logD("DrawPathRunning", "DrawPathRunning");
        Fn.logD("JsonString", result);
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            if(mMap !=  null) {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                );
            }
            JSONArray legsArray = routes.getJSONArray("legs");
            JSONObject legs = legsArray.getJSONObject(0);
            JSONObject distance = legs.getJSONObject("distance");
            String distance_km  = distance.getString("text");
            JSONObject duration = legs.getJSONObject("duration");
            String duration_min  = duration.getString("text");
            if(getActivity() != null){
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(duration_min + " ( " + distance_km + " ) ");
            }

            Fn.logD("PolyLine Added", "PolyLineAdded");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    protected List<LatLng> decodePoly(String encoded) {
        Fn.logD("DecodePoly Running", "DecodePoly Running");
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }
        return poly;
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(getActivity(), Title, Message);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        startLocationUpdates();
//                        TimerProgramm();
                        break;
                    case Activity.RESULT_CANCELED:
                        Fn.showGpsAutoEnableRequest(FullActivity.mGoogleApiClient, getActivity());//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onClick Called");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + callButton.getText().toString()));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }
    protected void downloadBitmapFromURL(String profile_pic_url){
//        RequestQueue requestQueue;
        final Bitmap[] return_param = new Bitmap[1];
        ImageRequest imageRequest = new ImageRequest(profile_pic_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(final Bitmap response) {
                material_image.setImageBitmap(response);
                material_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.setImageBitmap(response);
                        dialog.show();
                    }
                });
//                driverimage = response;
            }
        }, 0, 0, null, null);
        imageRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, imageRequest, getActivity()
        );
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
        stopTimer = false;
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onResume Called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onPause Called");
        stopTimer = true;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mMap != null){
            mMap = null;
        }
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
        Fn.cancelAllRequest(requestQueue, TAG);
        Fn.logD("BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onDestroyView Called");
    }
}
