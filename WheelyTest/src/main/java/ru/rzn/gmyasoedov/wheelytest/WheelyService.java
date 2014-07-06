package ru.rzn.gmyasoedov.wheelytest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.tavendo.autobahn.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service for getting point from server
 */
public class WheelyService extends Service {
    public static final String JSON_LAT = "lat";
    public static final String JSON_LON = "lon";
    public static final String JSON_ID = "id";
    private static final String TAG = WheelyService.class.getSimpleName();
    private static final String WS_URI = "ws://mini-mdt.wheely.com";
    private static final int ERROR_403 = 403;
    private static final int TIMEOUT = 30000;
    private static final int MIN_TIME = 30000;
    private static final int MIN_DISTANCE = 100;
    private WebSocket connection;
    private String name;
    private String password;
    private LocationManager locationManager;
    private Binder binder;
    private boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        connection = new WebSocketConnection();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        binder = new WheelyBinder();
        isRunning = true;
        Log.e(TAG, "CREAT");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "startId " + startId);
        //only first call run connection
        if (startId == 1) {
            name = intent.getStringExtra(LoginFragment.NAME);
            password = intent.getStringExtra(LoginFragment.PASSWORD);
            new WheelyWebSocketConnectionHandler();
            Log.e(TAG, "START");
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "DESTROY");
        super.onDestroy();
        isRunning = false;
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    private String getWsUri() {
        return WS_URI + "?username=" + name + "&password=" + password;
    }

    /**
     * web socket connection handler class
     */
    private class WheelyWebSocketConnectionHandler extends WebSocketConnectionHandler {
        private WebSocketOptions options;
        private LocalLocationListener localLocationListener;

        public WheelyWebSocketConnectionHandler() {
            options = new WebSocketOptions();
            options.setSocketReceiveTimeout(TIMEOUT);
            localLocationListener = new LocalLocationListener();
            startConnect();
        }

        @Override
        public void onOpen() {
            BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_CONNECTION_OPEN, null, WheelyService.this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME, MIN_DISTANCE, localLocationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME, MIN_DISTANCE, localLocationListener);
        }

        @Override
        public void onTextMessage(String payload) {
            BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_DATA, payload, WheelyService.this);
        }

        @Override
        public void onClose(int code, Bundle reason) {
            locationManager.removeUpdates(localLocationListener);
            if (ERROR_403 == reason.getInt(WebSocketConnection.EXTRA_STATUS_CODE)) {
                BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_ERROR,
                        getResources().getString(R.string.access_error), WheelyService.this);
                stopSelf();
            } else {
                startConnect();
            }
        }

        private void startConnect() {
            try {
                if (isRunning) {
                    if (connection.isConnected()) {
                        connection.disconnect();
                    }
                    connection.connect(getWsUri(), this, options);
                }
            } catch (WebSocketException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private class LocalLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            sendLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                sendLocation(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        private void sendLocation(Location location) {
            JSONObject object = new JSONObject();
            try {
                object.put(JSON_LAT, location.getLatitude());
                object.put(JSON_LON, location.getLongitude());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            connection.sendTextMessage(object.toString());
        }
    }

    class WheelyBinder extends Binder {
        public WheelyService getService() {
            return WheelyService.this;
        }
    }
}
