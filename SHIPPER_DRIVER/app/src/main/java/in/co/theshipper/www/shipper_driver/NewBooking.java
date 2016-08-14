package in.co.theshipper.www.shipper_driver;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.util.HashMap;


public class NewBooking extends Fragment {

    private String TAG = NewBooking.class.getName();
    private View view;
    private TextView crn_no,booking_datetime,vehicle_type,customer_mobile_no,material_weight,pickup_point,dropoff_point;
    private String user_token,received_crn_no;
    private Button Accept,Reject;
    private int accept_flag;
    private RequestQueue requestQueue;
    private ImageView material_image,popup;
    private Dialog dialog;
    public NewBooking() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_booking, container, false);
        crn_no = (TextView) view.findViewById(R.id.crn_no);
        booking_datetime = (TextView) view.findViewById(R.id.booking_datetime);
        vehicle_type = (TextView) view.findViewById(R.id.vehicle_type);
        customer_mobile_no = (TextView) view.findViewById(R.id.customer_mobile_no);
        material_weight = (TextView) view.findViewById(R.id.material_weight);
        pickup_point = (TextView) view.findViewById(R.id.pickup_point);
        dropoff_point = (TextView) view.findViewById(R.id.dropoff_point);
        material_image = (ImageView) view.findViewById(R.id.material_image);
        Accept = (Button) view.findViewById(R.id.Accept);
        Reject = (Button) view.findViewById(R.id.Reject);
        Accept.setEnabled(false);
        Reject.setEnabled(false);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(true);
        popup=(ImageView)dialog.findViewById(R.id.image_popup);
        if((getActivity().getIntent()!=null)&&(getActivity().getIntent().getExtras()!=null)) {
            Bundle bundle = getActivity().getIntent().getExtras();
            String crn_no = Fn.getValueFromBundle(bundle, "crn_no");
            getActivity().getIntent().removeExtra("crn_no");
            Fn.SystemPrintLn("CRN_NO received" + crn_no);
            getActivity().getIntent().setData(null);
            getActivity().setIntent(null);
            HashMap<String,String>  hashMap= new HashMap<String,String>();
            final String get_new_booking_url = Constants.Config.ROOT_PATH+"get_new_booking";
            // String CrnNo = Fn.getPreference(getActivity(),"current_crn_no");
            Fn.logD("get_new_booking_url",get_new_booking_url);
            hashMap.put("crn_no", crn_no);
            hashMap.put("user_token", user_token);
            sendVolleyRequest(get_new_booking_url, Fn.checkParams(hashMap), "get_new_booking");
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Fn.logD("BILLING_DETAILS_FRAGMENT_LIFECYCLE", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        user_token = Fn.getPreference(getContext(), Constants.Keys.USER_TOKEN);
//        requestQueue= Volley.newRequestQueue(getActivity());
        final String accept_booking_url=Constants.Config.ROOT_PATH+"accept_booking";
        Accept.setEnabled(true);
        Reject.setEnabled(true);
        Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept_flag = 1;
                final HashMap<String, String> hmap = new HashMap<String, String>();
                hmap.put("crn_no", received_crn_no);
                hmap.put("accept_flag", String.valueOf(accept_flag));
                hmap.put("user_token", user_token);
                sendVolleyRequest(accept_booking_url, Fn.checkParams(hmap), "accept_booking");
            }
        });
        Reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept_flag = 0;
                final HashMap<String,String> hmap=new HashMap<String, String>();
                hmap.put("crn_no",received_crn_no);
                hmap.put("accept_flag", String.valueOf(accept_flag));
                hmap.put("user_token",user_token );
                sendVolleyRequest(accept_booking_url, Fn.checkParams(hmap),"accept_booking");
            }
        });
        customer_mobile_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customer_mobile_no.getText().toString()));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });
    }
    protected void sendVolleyRequest(String url, final HashMap<String,String> hashMap, final String method){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse_new_booking", String.valueOf(response));
                if(method.equals("get_new_booking")){
                    Fn.logD("NEW_BOOKING_RESPONSE",response);
                    uiUpdate(response);
                }else if(method.equals("accept_booking")){
                    acceptSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fn.logD("onErrorResponse from NewBooking", String.valueOf(error));
            }
        }){
            @Override
            protected HashMap<String,String> getParams(){
                return hashMap;
            }
        };
        stringRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, stringRequest, getActivity());

    }
    protected void uiUpdate(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            //jsonArray = jsonObject.getJSONArray("likes");
            String errFlag = jsonObject.getString("errFlag");
//            String errMsg = jsonObject.getString("errMsg");
            Fn.logD("errFlag",errFlag);
//            Fn.logD("errMsg",errMsg);
            if(errFlag.equals("1")){
                Fn.logD("toastNotdone", "toastNotdone");
                ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
            }
            else if(errFlag.equals("0"))
            {
                if(jsonObject.has("likes")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("likes");
//                    Fn.Toast(this,errMsg);
                    Fn.logD("toastdone", "toastdone");
                    int count = 0;
                    while (count < jsonArray.length())
                    {
                        Fn.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        received_crn_no = JO.getString("crn_no");
                        String received_booking_datetime = JO.getString("booking_datetime");
                        String received_vehicletype_id = JO.getString("vehicletype_id");
                        String received_customer_mobile_no = JO.getString("customer_mobile_no");
                        String received_material_weight = JO.getString("material_weight");
                        String received_pickup_point = JO.getString("pickup_point");
                        String received_dropoff_point = JO.getString("dropoff_point");
                        crn_no.setText(received_crn_no);
                        booking_datetime.setText("Booking DateTime: "+Fn.getDateName(received_booking_datetime));
                        vehicle_type.setText("Truck: " + Fn.VehicleName(received_vehicletype_id,getActivity()));
                        customer_mobile_no.setText(received_customer_mobile_no);
                        material_weight.setText("Material Weight: "+received_material_weight);
                        pickup_point.setText(received_pickup_point);
                        dropoff_point.setText(received_dropoff_point);
                        String received_material_image_url = JO.getString("material_image_url");
                        String download_received_material_image_url = Constants.Config.ROOT_PATH+received_material_image_url;
                        Fn.logD("download_received_material_image_url",download_received_material_image_url);
                        if(received_material_image_url.length()>0){
                            downloadBitmapFromURL(download_received_material_image_url);
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
                        count++;
                    }
                }
                else
                {
                    Fn.Toast(getActivity(),Constants.Message.NEW_USER_ENTER_DETAILS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void acceptSuccess(String response){
        if(Fn.CheckJsonError(response)==false){
            Fn.logD("haha my response is","true");
            Fragment fragment = new Fragment();
            fragment = new BookingDetails();
            Bundle bundle = new Bundle();
            bundle.putString("crn_no", received_crn_no);
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = FullActivity.fragmentManager;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
//                Fragment fragment = new BookNow();
            transaction.replace(R.id.main_content, fragment, Constants.Config.CURRENT_FRAG_TAG);
            if((FullActivity.homeFragmentIndentifier == -5)){
                transaction.addToBackStack(null);
                FullActivity.homeFragmentIndentifier =  transaction.commit();
            }else{
                transaction.commit();
                Fn.logD("fragment instanceof Book","homeidentifier != -1");
            }
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_bill_detail_fragment);
        }
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
            }
        }, 0, 0, null, null);
        imageRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, imageRequest,getActivity());
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(getActivity(), Title, Message);
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
        Fn.logD("NEW_BOOKING_FRAGMENT_LIFECYCLE", "onResume Called");
    }
    @Override
    public void onPause() {
        super.onPause();
       Fn.stopAllVolley(requestQueue);
        Fn.logD("NEW_BOOKING_FRAGMENT_LIFECYCLE", "onPause Called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fn.cancelAllRequest(requestQueue, TAG);
    }
}
