package track.me.trackme;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import static track.me.trackme.DBHelper.TOTAL_ADDRESS;
import static track.me.trackme.MainActivity.PERMISSION_ID;

public class MapView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    DBHelper dbHelper;

    FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#8BCDEC"));
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        dbHelper=new DBHelper(this);

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
        float zoomLevel = 17.0f;
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Vinay is Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoomLevel));*/
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //getLastLocation();
                mMap.clear();
                String address=getCompleteAddressString(latLng.latitude,latLng.longitude);
                Log.v("tag","address is: "+address);
                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel));
                String[] addressArray=address.split(",");
                if(isNeedToInsert(latLng.latitude,latLng.longitude))
                    dbHelper.insertUserDetails(addressArray[0],"","",String.valueOf(latLng.latitude),String.valueOf(latLng.longitude),address);
            }
        });
        getLastLocation();
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

    private void getLastLocation() {
        if (checkPermissions()) {

            if (isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestPermissions();
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                }else {
                                    //latTextView.setText(location.getLatitude()+"");
                                    //lonTextView.setText(location.getLongitude()+"");
                                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(sydney));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16.0f));
                                    Log.v("tag", "" + location.getLatitude() + " " + location.getLongitude());

                                    String address=getCompleteAddressString(location.getLatitude(),location.getLongitude());
                                    String[] addressArray=address.split(",");
                                    if(isNeedToInsert(location.getLatitude(),location.getLongitude()))
                                    dbHelper.insertUserDetails(addressArray[0],"","",String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),address);
                                    Log.v("tag","address: "+address);
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private boolean isNeedToInsert(Double lat,Double lang) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM "+ TABEL_NAME;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            //user.put(LATITUDE,""+cursor.getString(cursor.getColumnIndex(LATITUDE)));
            //user.put(LONGITUDE,""+cursor.getString(cursor.getColumnIndex(LONGITUDE)));
            if (cursor.getString(cursor.getColumnIndex(LATITUDE)).equals(String.valueOf(lat))&&cursor.getString(cursor.getColumnIndex(LONGITUDE)).equals(String.valueOf(lang))) {
                Log.v("tag","This address already in DB");
                return false;
            }
        }
        return true;
    }

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Log.v("tag", "Address--> "+strReturnedAddress.toString());
                Log.v("tag", "Address is--> "+addresses);
            } else {
                Log.v("tag", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("tag", "Canont get Address!");
        }
        return strAdd;
    }

}