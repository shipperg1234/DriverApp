package in.co.theshipper.www.shipper_driver;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class FinishedBookingDetail extends Fragment{

    protected RequestQueue requestQueue;
    protected HashMap<String,String> hashMap;
    private String TAG = FinishedBookingDetail.class.getName();
    protected View view;
    protected Context context;
    private TextView received_pickup_point_view,received_dropoff_point_view, received_crn_no_view,
                     received_booking_datetime_view,received_customer_name_view,
                     received_customer_mobile_no_view, received_truck_name_view,received_total_fare_view;
    private String crn_no="";
    private String booking_id,received_crn_no;
    private TextView ratingText;
    private RatingBar ratingBar;
    private ImageView material_image,popup,vehicle_image;
    private Dialog dialog;
    private float customer_rating;

    public FinishedBookingDetail() {
        // Required empty public constructor
        super.onAttach(context);
        this.context = context;
        Fn.logD("COMPLETED_BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onAttach Called");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_finished_booking_detail, container, false);
        Fn.logD("COMPLETED_BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onCreateView Called");
        if((getActivity().getIntent()!=null)&&(getActivity().getIntent().getExtras()!=null)) {
            Bundle bundle = getActivity().getIntent().getExtras();
            crn_no = Fn.getValueFromBundle(bundle,"crn_no");
            getActivity().getIntent().setData(null);
            getActivity().setIntent(null);
        }else if(this.getArguments()!=null) {
            Bundle bundle = this.getArguments();
            crn_no = Fn.getValueFromBundle(bundle, "crn_no");
        }
        Fn.logD("bundle_crn_no", crn_no);
        Fn.SystemPrintLn(crn_no);
        received_pickup_point_view=(TextView)view.findViewById(R.id.pickup_point);
        received_dropoff_point_view=(TextView)view.findViewById(R.id.dropoff_point);
        received_booking_datetime_view=(TextView)view.findViewById(R.id.booking_datetime);
        received_truck_name_view=(TextView)view.findViewById(R.id.vehicle_type);
        received_customer_name_view=(TextView)view.findViewById(R.id.customer_name);
        received_customer_mobile_no_view=(TextView)view.findViewById(R.id.customer_mobile_no);
        received_crn_no_view=(TextView)view.findViewById(R.id.crn_no);
        received_total_fare_view=(TextView)view.findViewById(R.id.total_fare);
        ratingText = (TextView)view.findViewById(R.id.ratingText);
        ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);
        material_image = (ImageView) view.findViewById(R.id.material_image);
        vehicle_image = (ImageView) view.findViewById(R.id.vehicle_image);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(true);
        popup=(ImageView)dialog.findViewById(R.id.image_popup);
//        Fn.logD("booking_detail_textview", String.valueOf(booking_detail));
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fn.logD("COMPLETED_BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "onViewCreated Called");
        super.onViewCreated(view, savedInstanceState);
        HashMap<String,String>  hashMap= new HashMap<String,String>();
        String booking_status_url = Constants.Config.ROOT_PATH+"get_driver_completed_booking_status";
        // String CrnNo = Fn.getPreference(getActivity(),"current_crn_no");
        String user_token = Fn.getPreference(getActivity(),"user_token");
        hashMap.put("crn_no", crn_no);
        hashMap.put("user_token", user_token);
        sendVolleyRequest(booking_status_url, Fn.checkParams(hashMap));
    }
    public void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse_booking_status", String.valueOf(response));
                bookingStatusSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse", String.valueOf(error));
                ErrorDialog(Constants.Title.NETWORK_ERROR, Constants.Message.NETWORK_ERROR);
            }
        }){
            @Override
            protected HashMap<String,String> getParams(){
                return hMap;
            }
        };
        stringRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, stringRequest, getActivity());
    }
    protected void bookingStatusSuccess(String response){
        Fn.logD("COMPLETED_BOOKING_DETAILS_FRAGMENT_LIFECYCLE", "bookingStatusSuccess Called");
        if(!Fn.CheckJsonError(response)){
//            Fn.logD("bookingStatusSuccess", "bookingStatusSuccess Called");
            Fn.logD("received_json", response);
            JSONObject jsonObject;
            JSONArray jsonArray;
            try {
                jsonObject = new JSONObject(response);
                //jsonArray = jsonObject.getJSONArray("likes");
                String errFlag = jsonObject.getString("errFlag");
//                errMsg = jsonObject.getString("errMsg");
                if(errFlag.equals("1")){
                    Fn.logD("toastNotdone","toastNotdone");
                }
                else if(errFlag.equals("0"))
                {
                    if(jsonObject.has("likes"))
                    {
                        jsonArray = jsonObject.getJSONArray("likes");
                        int count = 0;
                        while (count < jsonArray.length())
                        {
                            Fn.logD("likes_entered", "likes_entered");
                            JSONObject JO = jsonArray.getJSONObject(count);
                            String received_exact_pickup_point = JO.getString("exact_pickup_point");
                            String received_exact_dropoff_point = JO.getString("exact_dropoff_point");
                            final String received_vehicletype_id = JO.getString("vehicletype_id");
                            String received_booking_datetime = Fn.getDateName(JO.getString("booking_datetime"));
                            String received_total_fare = JO.getString("total_fare");
                            received_crn_no = JO.getString("crn_no");
//                            crn_no_view.setText(received_crn_no);
                            String received_truck_name = Fn.VehicleName(received_vehicletype_id, getActivity());
                            String received_customer_name = JO.getString("customer_name");
                            String received_customer_mobile_no = JO.getString("customer_mobile_no");
                            booking_id = JO.getString("booking_id");
                            customer_rating = Float.parseFloat(JO.getString("customer_rating"));
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
                            if(customer_rating == 0.0f){
                                handleRating();
                            }
                            else{
                                ratingText.setText("You Rated");
                                ratingBar.setRating(customer_rating);
                                ratingBar.setIsIndicator(true);
                            }
                            received_pickup_point_view.setText(received_exact_pickup_point);
                            received_customer_mobile_no_view.setText(received_customer_mobile_no);
                            received_dropoff_point_view.setText(received_exact_dropoff_point);
                            received_booking_datetime_view.setText(received_booking_datetime);
                            received_customer_name_view.setText(received_customer_name);
                            received_crn_no_view.setText(received_crn_no);
                            received_truck_name_view.setText(received_truck_name);
                            received_total_fare_view.setText(received_total_fare+" Rs");
                            vehicle_image.setImageResource(Fn.getVehicleImage(Integer.parseInt(received_vehicletype_id)));
                            vehicle_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    popup.setImageResource(Fn.getVehicleImage(Integer.parseInt(received_vehicletype_id)));
                                    dialog.show();
                                }
                            });
                            count++;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
        }
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(getActivity(), Title, Message);
    }

    protected void handleRating(){
        Fn.SystemPrintLn("*************!!!!!!!!!inside handle rating$$$$$$$$$$");
        ratingText.setText("Rate your Customer");
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                RatingDialog rd = new RatingDialog();
                Bundle bundle = new Bundle();
                bundle.putString("rating", String.valueOf(rating));
                bundle.putString("booking_id", booking_id);
                bundle.putString("crn_no", received_crn_no);
                Fn.SystemPrintLn("booking_id" + booking_id + "rating" + rating + "crn_no" + crn_no);
                rd.setArguments(Fn.CheckBundle(bundle));
                rd.show(getActivity().getFragmentManager(), "ABC");

            }
        });
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
    public void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
    }

    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fn.cancelAllRequest(requestQueue,TAG);
    }
}
