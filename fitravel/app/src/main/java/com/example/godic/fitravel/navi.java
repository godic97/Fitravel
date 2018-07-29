package com.example.godic.fitravel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.maps.*;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;


import org.joda.time.DateTime;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.maps.DistanceMatrixApi.getDistanceMatrix;


public class navi extends AppCompatActivity implements OnMapReadyCallback {

    private static final int overview = 0;
    double[] lat;
    double[] lng;
    ArrayList<LatLng> arr;
//    TextView tv;
//    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        arr = new ArrayList<LatLng>();
        Intent previntent = getIntent();
        lat = previntent.getDoubleArrayExtra("lat");
        lng = previntent.getDoubleArrayExtra("lng");
        int max = previntent.getIntExtra("max",0);
//        tv = (TextView)findViewById(R.id.tv);
        for(int i = 0; i < max; i++){
            arr.add(new LatLng(lat[i], lng[i]));
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
    }



    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey("AIzaSyDPWIQshTjTbE9B1o7C4ksZD6AaELDL0dk")
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private DirectionsResult getDirectionsDetails(LatLng origin, LatLng destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin.latitude + ","+origin.longitude)
                    .destination(destination.latitude + "," +destination.longitude)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap, int i) {
        if(i == 1) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].startLocation.lat, results.routes[0].legs[0].startLocation.lng)).title("("+ i +")" + results.routes[0].legs[0].startAddress));
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].endLocation.lat, results.routes[0].legs[0].endLocation.lng)).title("("+ (i+1)+")" +results.routes[0].legs[0].endAddress).snippet(getEndLocationTitle(results)));
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable +
                " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
         //mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }

    public void onMapReady(GoogleMap googleMap) {
        setupGoogleMapScreenSettings(googleMap);
        for(int i = 1; i < arr.size(); i++) {
            DirectionsResult results = getDirectionsDetails(arr.get(i - 1), arr.get(i), TravelMode.DRIVING);
            int j = 0;
            while(results == null){
                j++;
                if(j > 3000){
                    break;
                }
            }
            if (results != null) {
                addPolyline(results, googleMap);
                positionCamera(results.routes[overview], googleMap);
                addMarkersToMap(results, googleMap, i);
            }
            else {
//                googleMap.addMarker(new MarkerOptions().position(arr.get(i - 1)));
//                googleMap.addMarker(new MarkerOptions().position(arr.get(i)));
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arr.get(i), 12));
            }
        }
    }



}
