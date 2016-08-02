package in.co.theshipper.www.shipper_driver;

public final class Constants {
    protected static final class Config{
        protected static final String ROOT_PATH = "http://theshipper.ml/loader_mobile/";
//        protected static final String ROOT_PATH = "http://www.theshipper.co.in/loader_mobile/";
//        protected static final String ROOT_PATH = "http://52.77.190.248/";
//        protected static final String ROOT_PATH = "http://192.168.0.100/loader_mobile/";
//        protected static final String ROOT_PATH = "http://192.168.0.101/loader_mobile/";
        protected static final int UPDATE_NEW_LOCATION_DELAY = 0*1000;
        protected static final int UPDATE_NEW_LOCATION_PERIOD = 10*1000;
        protected static final int UPDATE_DRIVER_LOCATION_DELAY = 0*1000;
        protected static final int UPDATE_DRIVER_LOCATION_PERIOD = 30*1000;
        protected static final int GET_CUSTOMER_LOCATION_DELAY = 0*1000;
        protected static final int GET_CUSTOMER_LOCATION_PERIOD = 30*1000;
        protected static final int SEND_DISTANCE_REQUEST_DELAY = 0*1000;
        protected static final int SEND_DISTANCE_REQUEST_PERIOD = 20*1000;
        protected static final long MIN_DATE_DURATION = 1*1000;
        protected static final long MAX_DATE_DURATION = 6*24*60*60*1000;
        public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        public static final long MIN_TIME_BW_UPDATES = 10000 * 1 * 1;
        protected static final String SUPPORT_CONTACT = "08276097972";
        protected static final int NAME_FIELD_LENGTH = 50;
        protected static final int ADDRESS_FIELD_LENGTH = 50;
        public static final String CURRENT_FRAG_TAG = "current_fragment";
        protected static final float MAP_HIGH_ZOOM_LEVEL = 17;
        protected static final float MAP_MID_ZOOM_LEVEL = 15;
        protected static final float MAP_SMALL_ZOOM_LEVEL = 13;
        public static final int DELAY_LOCATION_CHECK = 1000*2*1;
        protected static final int FLASH_TO_MAIN_DELAY = 3*1000;
        protected static final int GPS_INTERVAL = 10*1000;
        protected static final int GPS_FASTEST_INTERVAL = 5*1000;
        protected static final int PROGRESSBAR_DELAY = 2*1000;
        protected static final int ACURATE_DISTANCE_RATIO_FACTOR = 3;
        protected static final int IMAGE_WIDTH = 500;
        protected static final int IMAGE_HEIGHT = 500;
    }
    protected static final class Message{
        protected static final String NEW_USER_ENTER_DETAILS = "Please enter your details";
        protected static final String NO_CURRENT_BOOKING = "No Current Booking";
        protected static final String VEHICLE_ALLOCATION_PENDING = "Vehicle Allocation Pending";
        protected static final String DRIVER_FOUND = "Driver Found";
        protected static final String NETWORK_ERROR = "Unable to connect to server.Check your Internet Connection";
        protected static final String SERVER_ERROR = "Server not responding to request";
        protected static final String FIELD_MISSING = "Some fields are missing...Retry";
        protected static final String INV_CRED = "Invalid Credentials!!";
        protected static final String GPS_NOT_ENABLED = "GPS not enabled !!";
        protected static final String INTERNET_NOT_ENABLED = "Internet not enabled!!";
        protected static final String CONNECTING = "Connecting...";
        protected static final String LOADING = "Loading...";
        protected static final String OTP_VERIFICATION_ERROR = "OTP could not be verified";
        protected static final String FORM_ERROR = "Form contains error";
        protected static final String TRACKING_ERROR = "Error while updating location";
        protected static final String EMPTY_IMAGE = "Upload your Image";
    }
    public static final class Title{
        protected static final String NETWORK_ERROR = "NETWORK ERROR";
        protected static final String SERVER_ERROR = "SERVER ERROR";
        protected static final String OTP_VERIFICATION_ERROR = "VERIFICATION ERROR";
    }
    public static final class Keys{
        protected static final String VEHICLETYPE_ID = "vehicletype_id";
        protected static final String USER_TOKEN = "user_token";
        protected static final String USER_ID = "user_id";
        protected static final String CITY_ID = "city_id";
        protected static final String EXACT_PICKUP_POINT = "exact_pickup_point";
        protected static final String EXACT_DROPOFF_POINT = "exact_dropoff_point";
    }
}
