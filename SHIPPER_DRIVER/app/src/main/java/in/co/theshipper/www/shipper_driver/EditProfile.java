package in.co.theshipper.www.shipper_driver;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.EntityReference;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    private DBController controller;
    protected  String TAG = EditProfile.class.getName();
    protected RequestQueue requestQueue;
    private EditText name,address;
    private String received_usertoken,received_vehicletype_id,received_city_id,vehicletype_id,city_id,json_string,errFlag,errMsg;
    private JSONObject  jsonObject;
    private JSONArray jsonArray;
    private Spinner truckspinner,cityspinner;
    private String query2,query3;
    private ArrayList<String> truck,city;
    private ArrayAdapter<String> adapter_truck,adapter_city;
    private ImageView driver_image;
    private Bitmap driverimage = null;
    private static int RESULT_LOAD_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fn.logW("EDIT_PROFILE_ACTIVITY_LIFECYCLE", "onCreate called");
        super.onCreate(savedInstanceState);
        truck = new ArrayList<String>();
        city = new ArrayList<String>();
        setContentView(R.layout.activity_edit_profile);
        controller = new DBController(this);
        SQLiteDatabase database = controller.getWritableDatabase();

        query2 = "select distinct(vehicle_name) from view_vehicle_type";
        Cursor truc = database.rawQuery(query2, null);
        try {
            if (truc.moveToFirst()) {
                do {
                    truck.add(truc.getString(0));
                } while (truc.moveToNext());
            }
        } catch (Exception e) {
            Fn.SystemPrintLn("no truc rows");
        }

        query3 =  "select distinct(city_name) from view_city";
        Cursor cit = database.rawQuery(query3, null);
        Fn.logD("city_cursor",String.valueOf(cit));
        try {
            if (cit.moveToFirst()) {
                do {
                    city.add(cit.getString(0));
                } while (cit.moveToNext());
            }
        } catch (Exception e) {
          Fn.SystemPrintLn("no city rows");
        }
        database.close();
        Fn.logD("city_list", String.valueOf(city));
        adapter_truck = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, truck);
        adapter_city = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, city);
        Fn.SystemPrintLn(truck);
        truckspinner = (Spinner) findViewById(R.id.truckspinner);
        truckspinner.setAdapter(adapter_truck);
        cityspinner = (Spinner) findViewById(R.id.cityspinner);
        cityspinner.setAdapter(adapter_city);
        //   profile_pic = (ImageView) findViewById(R.id.profile_pic);
        /*
        * Receive the user information from server
        * display it on layout
        * then edit it to change the user ifno
        * */
        name = (EditText) findViewById(R.id.name);
        driver_image = (ImageView) findViewById(R.id.driver_image);
        driver_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        address = (EditText) findViewById(R.id.address);
        json_string = getIntent().getStringExtra("JSON_STRING");
        Fn.logD("json_string",json_string);
//        Fn.Toast(this,json_string);
        try {
            jsonObject = new JSONObject(json_string);
            //jsonArray = jsonObject.getJSONArray("likes");
            errFlag = jsonObject.getString("errFlag");
            errMsg = jsonObject.getString("errMsg");
            Fn.logD("errFlag",errFlag);
            Fn.logD("errMsg",errMsg);
            if(errFlag.equals("1")){
                Fn.Toast(this,errMsg);
                Fn.logD("toastNotdone", "toastNotdone");
                ErrorDialog(Constants.Title.SERVER_ERROR,Constants.Message.SERVER_ERROR);
            }
            else if(errFlag.equals("0"))
            {
                if(jsonObject.has("likes")) {
                    jsonArray = jsonObject.getJSONArray("likes");
                    Fn.Toast(this,errMsg);
                    Fn.logD("toastdone", "toastdone");
                    int count = 0;
                    while (count < jsonArray.length())
                    {
                        Fn.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        String received_username = JO.getString("name");
                        received_usertoken = JO.getString("user_token");
                        received_vehicletype_id = JO.getString("vehicletype_id");
                        received_city_id =  JO.getString("city_id");
                        Fn.logD("received_vehicletype_id",received_vehicletype_id);
                        String  received_useraddress = JO.getString("postal_address");
                        String  profile_pic_url= JO.getString("profile_pic_url");
                        String download_profile_pic_url = Constants.Config.ROOT_PATH+JO.getString("profile_pic_url");
                        Fn.logD("profile_pic_url",profile_pic_url);
                        if(profile_pic_url.length()>0){
                            downloadBitmapFromURL(download_profile_pic_url);
                        }
                        if(received_vehicletype_id.length()>0)
                        {
                            String received_vehicle_name = Fn.VehicleName(received_vehicletype_id, this);
                            Fn.logD("vehicle_name", received_vehicle_name);
                            int index = truck.indexOf(received_vehicle_name);
                            truckspinner.setSelection(index);
                            truckspinner.setEnabled(false);
                        }
                        if(received_city_id.length()>0)
                        {
                            String received_city_name = Fn.CityName(received_city_id, this);
                            Fn.logD("vehicle_name", received_city_name);
                            int index = city.indexOf(received_city_name);
                            cityspinner.setSelection(index);
                            cityspinner.setEnabled(false);
                        }
                        name.setText(received_username);
                        address.setText(received_useraddress);
                        Fn.logD("received_usertoken",received_usertoken);
                        String stored_usertoken = Fn.getPreference(this,"user_token");
                        Fn.logD("stored_usertoken",stored_usertoken);
                        count++;
                    }
                }
                else
                {
                    Fn.Toast(this,Constants.Message.NEW_USER_ENTER_DETAILS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //  profile_pic.setOnClickListener(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            Fn.logD("picturePath",picturePath);
            cursor.close();
//            Bitmap bmp = null;
            try {
                Uri reduceSizePath = Fn.getImageContentUri(this, Fn.decodeFile(picturePath, Constants.Config.IMAGE_WIDTH, Constants.Config.IMAGE_HEIGHT));
                driverimage = getBitmapFromUri(reduceSizePath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            driver_image.setImageBitmap(driverimage);
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    public void editProfile(View view) {
        String imgstring="";
        if(driverimage==null)
            Fn.ToastShort(EditProfile.this,Constants.Message.EMPTY_IMAGE);
        else {
            imgstring=Fn.getStringImage(driverimage);
            if (checkValidation()) {
                String truck_selected = truckspinner.getSelectedItem().toString();
                String city_selected = cityspinner.getSelectedItem().toString();
                vehicletype_id = Fn.VehicleID(truck_selected, this);
                city_id = Fn.CityID(city_selected, this);
                String username = name.getText().toString();
                String useraddress = address.getText().toString();
                String edit_customer_profile_url = Constants.Config.ROOT_PATH + "edit_driver_info";
                Fn.logD("useraddress", useraddress);
                Fn.logD("username", username);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("name", username);
                hashMap.put("vehicletype_id", vehicletype_id);
                hashMap.put("city_id", city_id);
                hashMap.put("postal_address", useraddress);
                hashMap.put("profile_pic",imgstring );
                hashMap.put("user_token", received_usertoken);
                sendVolleyRequest(edit_customer_profile_url, Fn.checkParams(hashMap));
            } else {
                Toast.makeText(EditProfile.this, "Form Contains Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean checkValidation() {
        boolean ret = true;
        if (!FormValidation.isRequired(name,Constants.Config.NAME_FIELD_LENGTH)) ret = false;
        if(!FormValidation.isRequired(address,Constants.Config.ADDRESS_FIELD_LENGTH)) ret = false;
        return ret;
    }
    protected void sendVolleyRequest(String URL, final HashMap<String,String> hMap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Fn.logD("onResponse", String.valueOf(response));
                editProfileSuccess(response);
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
        Fn.addToRequestQue(requestQueue, stringRequest, this);
    }
    protected void editProfileSuccess(String response){
        if(!Fn.CheckJsonError(response)){
            Intent i = new Intent(this, RegistrationIntentService.class);
            startService(i);
            Fn.putPreference(this, Constants.Keys.USER_TOKEN, received_usertoken);
            Fn.putPreference(this,Constants.Keys.VEHICLETYPE_ID,vehicletype_id);
            Fn.putPreference(this,Constants.Keys.CITY_ID,city_id);
            Intent intent = new Intent(this, FullActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            ErrorDialog(Constants.Title.SERVER_ERROR, Constants.Message.SERVER_ERROR);
        }
    }
    protected void downloadBitmapFromURL(String profile_pic_url){
//        RequestQueue requestQueue;
        final Bitmap[] return_param = new Bitmap[1];
        ImageRequest imageRequest = new ImageRequest(profile_pic_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                driver_image.setImageBitmap(response);
                driverimage = response;
            }
        }, 0, 0, null, null);
        imageRequest.setTag(TAG);
        Fn.addToRequestQue(requestQueue, imageRequest,this);
    }
    private void ErrorDialog(String Title,String Message){
        Fn.showDialog(this, Title, Message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Fn.stopAllVolley(requestQueue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fn.startAllVolley(requestQueue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fn.cancelAllRequest(requestQueue,TAG);
    }
}
