package com.gozem.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.gozem.test.model.Content;
import com.gozem.test.model.Root;
import com.gozem.test.providers.localDB.DbLocation;
import com.gozem.test.service.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    SupportMapFragment mapFragment;
    GoogleMap mMap;
    Marker marker;
    LocationBroadcastReceiver receiver;
    @BindView(R.id.tv_fullname)
    TextView tv_fullname;
    @BindView(R.id.tv_addressEmail)
    TextView tv_addressEmail;
    @BindView(R.id.imgProfil)
    ImageView imgProfil;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_source)
    TextView tv_source;
    @BindView(R.id.tv_value)
    TextView tv_value;
    private List<Root> rootList = new ArrayList<>();
    private Content content;
    private Context context;
    //private ArrayList<LatLng> latLngArrayList;
    private DbLocation dbLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        addItemsFromJSON();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //Req Location Permission
                startLocService();
            }
        } else {
            //Start the Location Service
            startLocService();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        context = this;
        ButterKnife.bind(this);
        dbLocation = new DbLocation(context);
        dbLocation.clearLocations();
        receiver = new LocationBroadcastReceiver();
    }


    void startLocService() {
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(receiver, filter);
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocService();
                } else {
                    Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        for (int i = 0; i < rootList.size(); i++) {
            content = rootList.get(i).content;
            String type = rootList.get(i).type;

            if (type.trim().equals("profile")) {
                tv_fullname.setText(content.getName());
                tv_addressEmail.setText(content.getEmail());
                Glide.with(context)
                        .load(content.getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .error(R.drawable.ic_baseline_account_circle_24)
                        .into(imgProfil);
            }


            if (type.trim().equals("map")) {
                LatLng position = new LatLng(content.getLat(), content.getLng());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(position);
                markerOptions.title(content.getTitle());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                //markerOptions.snippet("yaounde");
                mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 5));
                /*mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mMap.animateCamera(CameraUpdateFactory.zoomBy(14));*/
            }


            if (type.trim().equals("data")) {
                tv_title.setText(content.getTitle());
                tv_source.setText(content.getSource());
                tv_value.setText(content.getValue());
            }


            /*for(int j=0;j<dbLocation.getAllLocations().size();j++){
                Log.w(TAG, "onReceive---: " + dbLocation.getAllLocations().get(j) );

                if(!dbLocation.getAllLocations().get(j).equals(dbLocation.getAllLocations().get(j++))){
                    Log.w(TAG, "onReceive: j = " + j + "------" +  dbLocation.getAllLocations().get(j));
                    mMap.addMarker(new MarkerOptions().position(dbLocation.getAllLocations().get(j)).title(getString(R.string.autre_position)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dbLocation.getAllLocations().get(j), 5));
                }
            }*/

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbLocation.clearLocations();
    }

    private void addItemsFromJSON() {
        try {

            String jsonDataString = readJSONDataFromFile();
            JSONArray jsonArray = new JSONArray(jsonDataString);

            for (int i = 0; i < jsonArray.length(); ++i) {

                JSONObject itemObj = jsonArray.getJSONObject(i);

                String type = itemObj.getString("type");
                JSONObject contentObj = itemObj.getJSONObject("content");

                Log.w(TAG, "addItemsFromJSON: " + type + "-----" + contentObj);
                Content content = new Gson().fromJson(contentObj.toString(), new TypeToken<Content>() {
                }.getType());
                Log.w(TAG, "addItemsFromJSON: " + type + "+++++++" + content);
                Root root = new Root(type, content);
                rootList.add(root);
            }

        } catch (JSONException | IOException e) {
            Log.w(TAG, "addItemsFromJSON: ", e);
        }
    }

    private String readJSONDataFromFile() throws IOException {

        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();

        try {
            String jsonString = null;
            inputStream = getResources().openRawResource(R.raw.my_data);
            BufferedReader bufferedReader = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }

            while ((jsonString = bufferedReader.readLine()) != null) {
                builder.append(jsonString);
            }

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return new String(builder);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACT_LOC")) {
                double lat = intent.getDoubleExtra("latitude", 0f);
                double longitude = intent.getDoubleExtra("longitude", 0f);
                LatLng latLng = null;
                if (mMap != null) {
                    latLng = new LatLng(lat, longitude);

                    //latLngArrayList.add(latLng);
                    dbLocation.AddLnLg(lat, longitude);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(getString(R.string.position));
                    markerOptions.snippet(getCompleteAddressString(lat, longitude));
                    if (marker != null)
                        marker.setPosition(latLng);
                    else
                        marker = mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                }
                Toast.makeText(MainActivity.this, "Latitude is: " + lat + ", Longitude is " + longitude, Toast.LENGTH_LONG).show();


                for (int i = 0; i < dbLocation.getAllLocations().size(); i++) {
                    Log.w(TAG, "onReceive0---: " + dbLocation.getAllLocations().get(i));

                    if (!dbLocation.getAllLocations().get(i).equals(latLng)) {
                        Log.w(TAG, "onReceive: i = " + i + "------" + dbLocation.getAllLocations().get(i));
                        mMap.addMarker(new MarkerOptions()
                                .position(dbLocation.getAllLocations().get(i))
                                .title(getString(R.string.autre_position))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dbLocation.getAllLocations().get(i), 5));
                    }

                }

            }
        }



        /**
         * Methode permettant de recuperer l'adress à partir de ses coordonnées
         *
         * @param LATITUDE
         * @param LONGITUDE
         * @return strAdd
         */
        private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
            String strAdd = "";
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder();

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return strAdd;
        }


    }

}