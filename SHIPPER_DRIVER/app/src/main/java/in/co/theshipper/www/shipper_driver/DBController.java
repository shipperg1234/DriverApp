package in.co.theshipper.www.shipper_driver;

import java.text.ParseException;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBController  extends SQLiteOpenHelper {

    protected static final String id = BaseColumns._ID;//for contacts database
    protected static final String _ID = BaseColumns._ID;
    protected static final String ID = BaseColumns._ID;
    protected static final String TABLE_VIEW_CITY = "view_city";
    protected static final String CITY_ID = "city_id";
    protected static final String CITY_NAME = "city_name";
    protected static final String UPDATE_DATE = "update_date";
    protected static final String TABLE_VIEW_VEHICLE_TYPE = "view_vehicle_type";
    protected static final String VEHICLETYPE_ID = "vehicletype_id";
    protected static final String VEHICLE_NAME = "vehicle_name";
    protected static final String IS_ACTIVE = "is_active";
    protected static final String TABLE_VIEW_PRICING ="view_pricing";
    protected static final String FROM_DISTANCE ="from_distance";
    protected static final String TO_DISTANCE ="to_distance";
    protected static final String PRICE_KM ="price_km";
    protected static final String TABLE_VIEW_BASE_FARE = "view_base_fare";
    protected static final String BASE_FARE = "base_fare";
    protected static final String MAXIMUM_WEIGHT ="maximum_weight";
    protected static final String FREEWAITING_TIME ="freewaiting_time";
    protected static final String WAITING_CHARGE ="waiting_charge";
    protected static final String NIGHT_HOLDING_CHARGE ="night_holding_charge";
    protected static final String HARD_COPY_CHALLAN ="hard_copy_challan";
    protected static final String DIMENSION="dimension";
    protected static final String TRANSIT_CHARGE ="transit_charge";

    private static final String CREATE_TABLE_VIEW_CITY = "CREATE TABLE " + TABLE_VIEW_CITY
            + "(" + CITY_ID + " INTEGER PRIMARY KEY," + CITY_NAME + " TEXT,"
            + IS_ACTIVE + " INTEGER CHECK ( " + IS_ACTIVE +" IN (0,1) ),"
            + UPDATE_DATE + " TEXT" + ")";

    private static final String CREATE_TABLE_VIEW_VEHICLE_TYPE = "CREATE TABLE " + TABLE_VIEW_VEHICLE_TYPE
            + "(" + VEHICLETYPE_ID + " INTEGER PRIMARY KEY," + VEHICLE_NAME + " TEXT,"
            + IS_ACTIVE + " INTEGER CHECK ( " + IS_ACTIVE +" IN (0,1) ),"
            + UPDATE_DATE + " TEXT" + ")";

    private static final String CREATE_TABLE_VIEW_PRICING ="CREATE TABLE " +TABLE_VIEW_PRICING
            + "(" + _ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," +VEHICLETYPE_ID + " INTEGER," + CITY_ID + " INTEGER," + VEHICLE_NAME + " TEXT,"
            + FROM_DISTANCE + " INTEGER," + TO_DISTANCE + " INTEGER," + PRICE_KM + " INTEGER,"
            + IS_ACTIVE + " INTEGER CHECK ( " + IS_ACTIVE +" IN (0,1) ),"
            + UPDATE_DATE + " TEXT" + ")";

    private static final String CREATE_TABLE_VIEW_BASE_FARE ="CREATE TABLE " + TABLE_VIEW_BASE_FARE
            + "(" + ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," + VEHICLETYPE_ID + " INTEGER," + CITY_ID + " INTEGER," + VEHICLE_NAME + " TEXT,"
            + BASE_FARE + " REAL," + MAXIMUM_WEIGHT + " REAL," + FREEWAITING_TIME + " TEXT,"
            + WAITING_CHARGE + " INTEGER," + NIGHT_HOLDING_CHARGE + " INTEGER," + HARD_COPY_CHALLAN
            + " INTEGER," + DIMENSION + " TEXT," + TRANSIT_CHARGE + " INTEGER,"
            + IS_ACTIVE + " INTEGER CHECK ( " + IS_ACTIVE +" IN (0,1) ),"
            + UPDATE_DATE + " TEXT " + ")";

    private static final String CREATE_TABLE_CONTACTSDB= "CREATE TABLE contactsdb (" + id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " name TEXT, number TEXT )";

    private static final String DELETE = "DELETE FROM ";
    private static final String DELETE_VIEW_CITY = "DROP TABLE IF EXISTS " + TABLE_VIEW_CITY;
    private static final String DELETE_VIEW_VEHICLE_TYPE = "DROP TABLE IF EXISTS " + TABLE_VIEW_VEHICLE_TYPE;
    private static final String DELETE_VIEW_BASE_FARE = "DROP TABLE IF EXISTS " + TABLE_VIEW_BASE_FARE;
    private static final String DELETE_VIEW_PRICING = "DROP TABLE IF EXISTS " + TABLE_VIEW_PRICING;
    private static final String DELETE_CONTACTSDB = " DROP TABLE IF EXISTS contactsdb";

    public DBController(Context context) {
        super(context, "theShipper.db", null, 1);
    }
    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        Fn.SystemPrintLn("DBCONTROLLER_onCreate");
        try {
            database.execSQL(CREATE_TABLE_VIEW_BASE_FARE);
            database.execSQL(CREATE_TABLE_VIEW_CITY);
            database.execSQL(CREATE_TABLE_VIEW_PRICING);
            database.execSQL(CREATE_TABLE_VIEW_VEHICLE_TYPE);
            database.execSQL(CREATE_TABLE_CONTACTSDB);
//            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        Fn.SystemPrintLn("DBCONTROLLER_onUpgrade");
        database.execSQL(DELETE_VIEW_BASE_FARE);
        database.execSQL(DELETE_VIEW_CITY);
        database.execSQL(DELETE_VIEW_PRICING);
        database.execSQL(DELETE_VIEW_VEHICLE_TYPE);
        database.execSQL(DELETE_CONTACTSDB);
        onCreate(database);
    }
    protected void createTable(int table_no)
    {
        try {
            Fn.SystemPrintLn("DBCONTROLLER_createTable");
            SQLiteDatabase database = this.getWritableDatabase();
            if(table_no==0){
                database.execSQL(CREATE_TABLE_VIEW_BASE_FARE);
            }
            else if(table_no==1){
                database.execSQL(CREATE_TABLE_VIEW_CITY);
            }
            else if(table_no==2){
                database.execSQL(CREATE_TABLE_VIEW_PRICING);
            }
            else{
                database.execSQL(CREATE_TABLE_VIEW_VEHICLE_TYPE);
            }
//            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    protected void deleteTable(int table_no) {
        Fn.SystemPrintLn("DBCONTROLLER_deleteTable");
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            if (table_no == 0) {
                database.execSQL(DELETE + TABLE_VIEW_BASE_FARE);
            } else if (table_no == 1) {
                database.execSQL(DELETE + TABLE_VIEW_CITY);
            } else if (table_no == 2) {
                database.execSQL(DELETE + TABLE_VIEW_PRICING);
            } else  {
                database.execSQL(DELETE + TABLE_VIEW_VEHICLE_TYPE);
            }
//            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void deleteContacts(String number)
    {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            database.execSQL("DELETE FROM contactsdb WHERE number = '"+number+"'");
//            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Inserts User into SQLite DB
     * @param queryValues
     */
    protected void insert(HashMap<String, String> queryValues,int table_no) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            Fn.SystemPrintLn("DBCONTROLLER_insert");
            if(table_no==0){
                Fn.SystemPrintLn("DBCONTROLLER_table_no_0");
                values.put(VEHICLETYPE_ID, queryValues.get(VEHICLETYPE_ID));
                values.put(CITY_ID, queryValues.get(CITY_ID));
                values.put(VEHICLE_NAME, queryValues.get(VEHICLE_NAME));
                values.put(BASE_FARE, queryValues.get(BASE_FARE));
                values.put(MAXIMUM_WEIGHT, queryValues.get(MAXIMUM_WEIGHT));
                values.put(FREEWAITING_TIME, queryValues.get(FREEWAITING_TIME));
                values.put(WAITING_CHARGE, queryValues.get(WAITING_CHARGE));
                values.put(NIGHT_HOLDING_CHARGE, queryValues.get(NIGHT_HOLDING_CHARGE));
                values.put(HARD_COPY_CHALLAN, queryValues.get(HARD_COPY_CHALLAN));
                values.put(DIMENSION, queryValues.get(DIMENSION));
                values.put(TRANSIT_CHARGE, queryValues.get(TRANSIT_CHARGE));
                values.put(IS_ACTIVE, queryValues.get(IS_ACTIVE));
                values.put(UPDATE_DATE, queryValues.get(UPDATE_DATE));
                database.insert(TABLE_VIEW_BASE_FARE, null, values);
//                database.close();
            }
            else if(table_no==1){

                Fn.SystemPrintLn("DBCONTROLLER_table_no_1");
                values.put(CITY_ID, queryValues.get(CITY_ID));
                values.put(CITY_NAME, queryValues.get(CITY_NAME));
                values.put(IS_ACTIVE, queryValues.get(IS_ACTIVE));
                values.put(UPDATE_DATE, queryValues.get(UPDATE_DATE));
                database.insert(TABLE_VIEW_CITY, null, values);
//                database.close();
            }
            else if(table_no==2){

                Fn.SystemPrintLn("DBCONTROLLER_table_no_2");
                values.put(VEHICLETYPE_ID, queryValues.get(VEHICLETYPE_ID));
                values.put(CITY_ID, queryValues.get(CITY_ID));
                values.put(VEHICLE_NAME, queryValues.get(VEHICLE_NAME));
                values.put(FROM_DISTANCE, queryValues.get(FROM_DISTANCE));
                values.put(TO_DISTANCE, queryValues.get(TO_DISTANCE));
                values.put(PRICE_KM, queryValues.get(PRICE_KM));
                values.put(IS_ACTIVE, queryValues.get(IS_ACTIVE));
                values.put(UPDATE_DATE, queryValues.get(UPDATE_DATE));
                database.insert(TABLE_VIEW_PRICING, null, values);
//                database.close();
            }
            else if(table_no==3){

                Fn.SystemPrintLn("DBCONTROLLER_table_no_3");
                values.put(VEHICLETYPE_ID, queryValues.get(VEHICLETYPE_ID));
                values.put(VEHICLE_NAME, queryValues.get(VEHICLE_NAME));
                values.put(IS_ACTIVE, queryValues.get(IS_ACTIVE));
                values.put(UPDATE_DATE, queryValues.get(UPDATE_DATE));
                database.insert(TABLE_VIEW_VEHICLE_TYPE, null, values);
//                database.close();
            }
            else{
                values.put("name",queryValues.get("name"));
                values.put("number", queryValues.get("number"));
                database.insert("contactsdb", null, values);
//                database.close();
            }
//            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    protected void getAll() throws ParseException {
        try {
            String selectQuery = "SELECT  base_fare, transit_charge  FROM view_base_fare";
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            Fn.SystemPrintLn("DBCONTROLLER_ArrayList");
            if (cursor.moveToFirst()) {
                do {

                    Fn.SystemPrintLn("DBCONTROLLER_onCreate"+ cursor.getString(0) + " " + cursor.getString(1));


                } while (cursor.moveToNext());
            }
//            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

