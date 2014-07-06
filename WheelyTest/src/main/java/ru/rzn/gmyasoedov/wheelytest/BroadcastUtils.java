package ru.rzn.gmyasoedov.wheelytest;

import android.content.ContextWrapper;
import android.content.Intent;

/**
 * Utils for send broad cast message
 */
public class BroadcastUtils {
    public static final String WHEELY_BROADCAST_ACTION = "ru.rzn.gmyasoedov.wheelytest.WHEELY_SERVICE";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final int STATUS_ERROR = 100;
    public static final int STATUS_DATA = 200;
    public static final int STATUS_CONNECTION_OPEN = 300;
    public static final int STATUS_CONNECTION_START = 400;
    public static final int STATUS_CONNECTION_DISCONNECT = 500;

    /**
     * send broadcast message
     * @param status code of status
     * @param data data for action
     * @param contextWrapper contextWrapper for send broadcast message
     */
    public static void sendBroadcast(int status, String data, ContextWrapper contextWrapper) {
        Intent dataIntent = new Intent(WHEELY_BROADCAST_ACTION);
        dataIntent.putExtra(STATUS, status);
        dataIntent.putExtra(DATA, data);
        contextWrapper.sendBroadcast(dataIntent);
    }
}
