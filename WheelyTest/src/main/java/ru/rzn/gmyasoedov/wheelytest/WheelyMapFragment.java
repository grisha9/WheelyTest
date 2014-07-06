package ru.rzn.gmyasoedov.wheelytest;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Map fragment
 * show pin from JSON
 */
public class WheelyMapFragment extends Fragment {
    private static final String TAG = WheelyMapFragment.class.getSimpleName();
    private static final int MAP_PADDING = 50;
    private static final int MAP_TIMEOUT = 300;
    private SupportMapFragment mapFragment;
    private List<MarkerOptions> optionses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        optionses = new ArrayList<MarkerOptions>();
        setHasOptionsMenu(true);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        // check location provider
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_ERROR,
                    getResources().getString(R.string.location_error),
                    getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.layout_map, container, false);
        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map_container, mapFragment);
        fragmentTransaction.commit();
        //show pin if its exist. need deelay for map rendering
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    TimeUnit.MILLISECONDS.sleep(MAP_TIMEOUT);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mapFragment.getMap() != null) {
                    mapFragment.getMap().setMyLocationEnabled(true);
                    addMarkers(optionses, mapFragment.getMap());
                }
            }
        }.execute();
        return view;
    }

    /**
     * add pin on map
     * @param jsonString json string from server
     */
    public void addMarkersFromJSON(String jsonString) {
        GoogleMap googleMap = mapFragment.getMap();
        if (googleMap != null) {
            optionses = getDataFromJSON(jsonString);
            addMarkers(optionses, googleMap);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.map_error), Toast.LENGTH_LONG).show();
        }
    }

    private void addMarkers(List<MarkerOptions> markerOptionses, GoogleMap googleMap) {
        googleMap.clear();
        if (markerOptionses.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions options : markerOptionses) {
            googleMap.addMarker(options);
            builder.include(options.getPosition());
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), MAP_PADDING));
    }

    /**
     * parse JSON from server to Marjer options
     * @param jsonString json string
     * @return list of MarkerOptions
     */
    private List<MarkerOptions> getDataFromJSON(String jsonString) {
        List<MarkerOptions> list = new ArrayList<MarkerOptions>();
        try {
            JSONArray json = new JSONArray(jsonString);
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = json.getJSONObject(i);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(object.getDouble(WheelyService.JSON_LAT),
                                object.getDouble(WheelyService.JSON_LON)))
                        .title(object.getString(WheelyService.JSON_ID));
                list.add(markerOptions);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return list;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.disconnect:
                BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_CONNECTION_DISCONNECT, null, getActivity());
                getActivity().stopService(new Intent(getActivity(), WheelyService.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
