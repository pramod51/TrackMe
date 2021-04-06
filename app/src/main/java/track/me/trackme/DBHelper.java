package track.me.trackme;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "trackme.db";
    public static final String PLACE = "place";
    public static final String TABEL_NAME = "tablName";
    public static final String PIN_CODE = "pincode";
    public static final String COUNTRY = "country";
    public static final String KEY_ID = "id";
    public static final String STATE = "state";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TABLE_Users = "userdetails";
    public static final String TOTAL_ADDRESS="address";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABEL_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    PLACE + " TEXT," +
                    LATITUDE + " TEXT," +
                    LONGITUDE + " TEXT," +
                    TOTAL_ADDRESS + " TEXT," +
                    COUNTRY + " TEXT)";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void insertUserDetails(String place,String country,String pinCode,String latitude,String longitude,String address ) {
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(PLACE,place);
        //cValues.put(COUNTRY,country);
        //cValues.put(PIN_CODE,pinCode);
        cValues.put(LATITUDE,latitude);
        cValues.put(LONGITUDE,longitude);
        cValues.put(TOTAL_ADDRESS,address);


        //Insert the new row, returning the primary key value of the new row
        long l=db.insert(TABEL_NAME, null, cValues);
        //Log.v("tag",cValues.)
        Log.v("tag","data inserted"+l+place+country+latitude+longitude);
        //db.close();
    }

}
