package com.example.godic.fitravel;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {
    private FragmentManager fragmentManger;
    private MapFragment mapFragment;
    private Geocoder geocoder;
    private Button button;
    private Button btn_save;
    private Button btn_load;
    private Button btn_clear;
    private Button btn_distance;
    private TextView tv_distance;
    //    private EditText editText;
    private GoogleMap mMap;
    private ArrayList<Marker> markers;
    //    private ArrayList<MarkerOptions>
    private static final int PLACE_PICKER_REQUEST = 1;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Handler mHandler;
    private double result_distance;
    private String url;
    private String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(MainActivity.this, "place.db", null, 1);
        db = dbHelper.getWritableDatabase();

        dbHelper.onCreate(db);
        markers = new ArrayList<Marker>();
        fragmentManger = getFragmentManager();
        mapFragment = (MapFragment) fragmentManger.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        button = (Button) findViewById(R.id.btn_searchLocation);
        btn_save = (Button) findViewById(R.id.btn_saveLocation);
        btn_load = (Button) findViewById(R.id.btn_loadLocation);
        btn_clear = (Button) findViewById(R.id.btn_clearLocation);
        btn_distance = (Button) findViewById(R.id.btn_searchDistance);
        tv_distance = (TextView) findViewById(R.id.distance);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                result = result +", "+ msg.obj.toString();
                tv_distance.setText(result);
            }
        };
        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
//                #Place API
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = intentBuilder.build(MainActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_save.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                db.delete("place", null, null);
//                 #store in DB
                for (int i = 0; i < markers.size(); i++) {
                    LatLng latLng = markers.get(i).getPosition();
                    ContentValues val = new ContentValues();
                    val.put("name", markers.get(i).getTitle().toString());
                    val.put("address", markers.get(i).getSnippet().toString());
                    val.put("latitude", latLng.latitude);
                    val.put("longitude", latLng.longitude);
                    val.put("state", 0);
                    db.insert("place", null, val);
                }
                Toast.makeText(MainActivity.this, "Save", 2).show();
            }
        });

        btn_load.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Cursor cursor = db.rawQuery("SELECT * FROM place WHERE state=0 ORDER BY idx ASC", null);
                cursor.moveToFirst();
                mMap.clear();
                markers = new ArrayList<Marker>();
                while (true) {
                    final LatLng latLng = new LatLng(cursor.getDouble(3), cursor.getDouble(4));
                    MarkerOptions markerOptions_result = new MarkerOptions();
                    markerOptions_result.title(cursor.getString(1).toString());
                    markerOptions_result.position(latLng);
                    markerOptions_result.snippet(cursor.getString(2).toString());
                    Marker marker = mMap.addMarker(markerOptions_result);
                    markers.add(marker);
                    if (cursor.isLast()) {
                        break;
                    }
                    cursor.moveToNext();
                }
                Toast.makeText(MainActivity.this, "Load", 2).show();
            }

        });

        btn_clear.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
//                for(int i = 0 ; i < markers.size(); i++){
//                    markers.get( i).remove();
//                }
                mMap.clear();
                markers = new ArrayList<Marker>();
                tv_distance.setText("");
                result = "";
                Toast.makeText(MainActivity.this, "Clear", 2).show();
            }
        });

        btn_distance.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), navi.class);
                double[] arr_lat = new double[markers.size()];
                double[] arr_lng = new double[markers.size()];
                for(int i = 0; i < markers.size(); i++){
                    arr_lat[i] = markers.get(i).getPosition().latitude;
                    arr_lng[i] = markers.get(i).getPosition().longitude;
                }
                intent.putExtra("lat", arr_lat);
                intent.putExtra("lng", arr_lng);
                intent.putExtra("max", markers.size());
                startActivity(intent);
//                for (int i = 1; i < markers.size(); i++) {
//                    url = "https://maps.googleapis.com/maps/api/distancematrix/json?mode=driving&units=imperial&origins=" + markers.get(i-1).getPosition().latitude + "," + markers.get(i-1).getPosition().longitude + "&destinations=" + markers.get(i).getPosition().latitude + "," + markers.get(i).getPosition().longitude + "&key=AIzaSyAJqObzYRJXitd5ukKtBcQtNyHKQXyCzOs";
//                    url = url.replace(" ", "+");
//                    Thread thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            StringBuilder stringBuilder = new StringBuilder();
//                            HttpPost httppost = new HttpPost(url);
//
//                            HttpClient client = new DefaultHttpClient();
//                            HttpResponse response = null;
//                            try {
//                                response = client.execute(httppost);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            HttpEntity entity = response.getEntity();
//                            InputStream stream = null;
//                            try {
//                                stream = entity.getContent();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            int b;
//                            try {
//                                while ((b = stream.read()) != -1) {
//                                    stringBuilder.append((char) b);
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            JSONObject jsonObject;
//                            try {
//                                Message msg = mHandler.obtainMessage();
//                                msg.what = 0;
//                                msg.arg1 = 1;
//
//
//                                jsonObject = new JSONObject(stringBuilder.toString());
//
//                                JSONArray rows = jsonObject.getJSONArray("rows");
//                                JSONObject elements = rows.getJSONObject(0);
//
//                                JSONArray distance = elements.getJSONArray("elements");
//                                JSONObject distance2 = distance.getJSONObject(0);
//                                JSONObject distance3 = distance2.getJSONObject("duration");
////
//
//                                msg.obj = distance3.getString("text");
////                            tv_distance.setText(dist.toString());
////                                msg.obj = rows.toString();
//                                mHandler.sendMessage(msg);
//                            } catch (JSONException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//
//                        }
//                    });
//                    try {
//                        thread.start();
//                        thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        LatLng latLng_default = new LatLng(37.3351874, -121.8832602);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng_default));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence place_name = place.getName();
            final CharSequence place_address = place.getAddress();

            LatLng latLng_result = place.getLatLng();
            MarkerOptions markerOptions_result = new MarkerOptions();
            markerOptions_result.title(place_name.toString());
            markerOptions_result.position(latLng_result);
            markerOptions_result.snippet(place_address.toString());
            Marker marker = mMap.addMarker(markerOptions_result);
            markers.add(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng_result, 15));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        for (int i = 0; i < markers.size(); i++) {
            if (markers.get(i).equals(marker)) {
                markers.remove(i);
                marker.remove();
                Toast.makeText(this, "Remove", 1).show();
            }
        }
    }

}
