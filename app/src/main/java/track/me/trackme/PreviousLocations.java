package track.me.trackme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static track.me.trackme.DBHelper.LATITUDE;
import static track.me.trackme.DBHelper.LONGITUDE;
import static track.me.trackme.DBHelper.PLACE;
import static track.me.trackme.DBHelper.TABEL_NAME;
import static track.me.trackme.MainActivity.PERMISSION_ID;

public class PreviousLocations extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DBHelper dbHelper;

    FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        dbHelper=new DBHelper(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#8BCDEC"));
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 14.0f;
        // Add a marker in Sydney and move the camera
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //ArrayList<HashMap<String, String>> userList = new ArrayList<>();

        String query = "SELECT * FROM "+ TABEL_NAME;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            LatLng sydney = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(LATITUDE))),
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(LONGITUDE))));
            mMap.addMarker(new MarkerOptions().position(sydney).title(cursor.getString(cursor.getColumnIndex(PLACE))));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoomLevel));
            Log.v("tag","places atteched "+cursor.getString(cursor.getColumnIndex(PLACE))+"longitude="+Double.parseDouble(cursor.getString(cursor.getColumnIndex(LATITUDE)))
            +"longitude="+Double.parseDouble(cursor.getString(cursor.getColumnIndex(LONGITUDE))));
        }

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            //latTextView.setText(mLastLocation.getLatitude()+"");
            //lonTextView.setText(mLastLocation.getLongitude()+"");
            Log.v("tag", "" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());

        }
    };
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


}