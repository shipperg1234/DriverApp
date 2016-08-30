package in.co.theshipper.www.shipper_driver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final static String ACTION = "ACTION";
    private Location latestLocation, PreviousLocation;
    private Double totDist = (double) 0,StLineDistance = (double)0;
    private Timer timer;
    private GoogleApiClient mGoogleApiClient;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private String distance, truck_name, crn_no = "",ToastString = "";
    private Double distancef, durationf, base_fare_min, total_fare, distance_fare = (double) 0;
    private JSONObject jsonObject = null;
    private JSONArray rows = null;
    private JSONObject firstObject = null;
    private JSONArray elements = null;
    private JSONObject elementsFirst = null;
    private JSONObject distanceObject = null;
    private int i = 0, active = 0, selected_vehicle;
    private Calendar calendar1, calendar2;
    private long time1 = 0, diff = 0, diffsec = 0, diffmin = 0, diffHours = 0;
    private Boolean responseReceived = true;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Notification notification;
    private long start_time = 0;

    @Override
    public void onCreate() {

        if(getApplicationContext() != null){
            start_time = Long.parseLong(Fn.getPreference(getApplicationContext(),Constants.Keys.LOADING_START_TIME));
        }
        Fn.SystemPrintLn("entered on create service");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Intent intent = getIntent(); // this getter is just for example purpose, can differ
        if ((intent != null) && (intent.getExtras() != null)) {
            crn_no = intent.getExtras().getString("crn_no");
            Fn.logD("GPS_SERVICE_crn_no", crn_no);
        }
        Intent i = new Intent(this, FullActivity.class);
        i.putExtra("menuFragment", "Calculator");
        i.putExtra("method", "push");
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, Fn.CheckIntent(i), PendingIntent.FLAG_CANCEL_CURRENT);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(this);
        notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.vehicle_1).setTicker("SHIPPER").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle("SHIPPER")
                .setContentText("Journey in progress").build();
        // mNM.notify(NOTIFICATION, notification);
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1317, notification);
        Fn.SystemPrintLn("GPS Service start");
        startTracking();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Fn.SystemPrintLn("GPS_Service_onDestroy");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Fn.SystemPrintLn("onDestroy entered");
        stopForeground(true);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        Fn.SystemPrintLn("diffHours:" + diffHours + "diffmin :" + diffmin + "diffsec:" + diffsec + "totDist:" + totDist);
        do {
            if (getApplicationContext() != null) {
                Fn.putPreference(getApplicationContext(), Constants.Keys.CRN_NO, crn_no);
                Fn.putPreference(getApplicationContext(), Constants.Keys.TOTAL_DISTANCE_TAVELLED, String.valueOf(totDist));
            }
        }while(getApplicationContext() == null);
//        Intent i = new Intent(this, FullActivity.class);
//        i.putExtra("menuFragment", "BillDetails");
//        i.putExtra("crn_no", crn_no);
//        i.putExtra("seconds", diffsec);
//        i.putExtra("minutes", diffmin);
//        i.putExtra("hours", diffHours);
//        i.putExtra("distance", totDist);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(Fn.CheckIntent(i));
//             Fn.SystemPrintLn("onDestroy entered");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startTracking() {
        Fn.SystemPrintLn(" service tracking started");
        requestQueue = Volley.newRequestQueue(this);
        timer = new Timer();
        //timer.schedule(timerTask, 2000, 2000);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                caculateFare();
            }
        }, Constants.Config.SEND_DISTANCE_REQUEST_DELAY, Constants.Config.SEND_DISTANCE_REQUEST_PERIOD);
    }

    public void caculateFare() {
        ToastString = "";
        Fn.SystemPrintLn("caculateFare");
        if (mGoogleApiClient.isConnected()) {
            Fn.SystemPrintLn("mGoogleApiClient.isConnected");
            do{
            latestLocation = Fn.getAccurateCurrentlocationService(mGoogleApiClient, this);
            }while(latestLocation == null);
            Fn.SystemPrintLn("hahahaha" + String.valueOf(mGoogleApiClient));
            if (latestLocation != null) {
                Fn.SystemPrintLn("latestLocation:" + "Latitude:" + String.valueOf(latestLocation.getLatitude()) + ", Longitude:" + String.valueOf(latestLocation.getLongitude()));
                if (PreviousLocation != null) {
                    StringBuilder urlString = new StringBuilder();
                    final double PreviousLattitde = PreviousLocation.getLatitude();
                    final double PreviousLongitude = PreviousLocation.getLongitude();
                    final double LatestLattitde = latestLocation.getLatitude();
                    final double LatestLongitude = latestLocation.getLongitude();
                    try {
                        Fn.SystemPrintLn("PreviousLocation:" + "Latitude:" + String.valueOf(PreviousLattitde) + ", Longitude:" + String.valueOf(PreviousLongitude));
                        urlString.append(" https://maps.googleapis.com/maps/api/distancematrix/json?units=metric");
                        urlString.append("&origins=");
                        urlString.append(String.valueOf(PreviousLattitde) + "," + String.valueOf(PreviousLongitude));
                        urlString.append("&destinations=");
                        urlString.append(String.valueOf(LatestLattitde) + "," + String.valueOf(LatestLongitude));
                        urlString.append("&key=" + URLEncoder.encode(getResources().getString(R.string.server_APIkey1), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String url = urlString.toString();
                    stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Fn.logD("onResponse_url_distance", String.valueOf(response));
                            // DistanceRecieveSuccess(response);
                            responseReceived = true;
//                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
                            try {
                                jsonObject = new JSONObject(response);
                                rows = jsonObject.getJSONArray("rows");
                                firstObject = rows.getJSONObject(0);
                                elements = firstObject.getJSONArray("elements");
                                elementsFirst = elements.getJSONObject(0);
                                distanceObject = elementsFirst.getJSONObject("distance");
                                distance = distanceObject.getString("text");
                                distancef = ((double)(distanceObject.getDouble("value")) / 1000);
                                StLineDistance = Fn.getDistanceFromLatLonInKm(PreviousLattitde,PreviousLongitude,LatestLattitde,LatestLongitude);
                                if(distancef>(Constants.Config.ACURATE_DISTANCE_RATIO_FACTOR*StLineDistance)){
                                    ToastString = "Distance Exceeded its limit";
                                    distancef = StLineDistance;
                                }
//                                Fn.SystemPrintLn("Distance_dummy"+((double)10/1000));
//                                Fn.Toast(getApplicationContext(),String.valueOf(distancef));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            totDist = totDist + distancef;
                            i++;
                            calendar2 = Calendar.getInstance();
                            diff = (calendar2.getTimeInMillis() - start_time);
                            diffsec = (diff / (1000) )% 60;
                            diffmin = (diff / (60 * 1000)) % 60;
                            diffHours = diff / (60 * 60 * 1000);
//                            Fn.SystemPrintLn(diffHours + ":" + diffmin + ":" + diffsec+" Total Distance: "+totDist);
//                            notification.setContentText("");
                            ToastString = ToastString+"Straight Distance"+String.valueOf(StLineDistance)+System.getProperty("line.separator")+System.getProperty("line.separator")+
                                    "Distance Received"+String.valueOf(distancef)+System.getProperty("line.separator")+System.getProperty("line.separator")+
                                    "Total Distance"+String.valueOf(totDist)+System.getProperty("line.separator")+System.getProperty("line.separator")+
                                    String.valueOf(diffHours) + "hrs :" + String.valueOf(diffmin) + "min :" + String.valueOf(diffsec)+"sec";
                            if(getApplicationContext() != null) {
                                Fn.ToastShort(getApplicationContext(), ToastString);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(getApplicationContext() != null) {
                                Fn.ToastShort(getApplicationContext(), "distance mistake-Error: " + String.valueOf(error));
                            }
                        }
                    });
                    volley(stringRequest);
                }
                if(responseReceived) {
                    Fn.SystemPrintLn("response_received_true");
                    PreviousLocation = latestLocation;
                    responseReceived = false;
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                }
                latestLocation = null;
            } else {
                //TODO
            }
        }
    }
    public void volley(StringRequest request)
    {
        requestQueue.add(request);
    }
    @Override
    public void onConnected(Bundle bundle) {
        Fn.SystemPrintLn("service apiclient connected");
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        if (connectionResult.hasResolution()) {
//            try {
//                // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            } catch (IntentSender.SendIntentException e) {
//                e.printStackTrace();
//            }
//        } else {
////            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
//        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
