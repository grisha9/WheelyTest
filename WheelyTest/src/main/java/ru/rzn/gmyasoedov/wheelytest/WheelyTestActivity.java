package ru.rzn.gmyasoedov.wheelytest;

import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity class
 */
public class WheelyTestActivity extends ActionBarActivity {
    private static final String TAG = WheelyTestActivity.class.getSimpleName();
    private static final String TAG_FRAGMENT = "tag-fragment";

    private FragmentManager fragmentManager;
    private BroadcastReceiver receiver;
    private Fragment fragment;
    private ProgressDialog dialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fragment == null) {
            fragment = new LoginFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, TAG_FRAGMENT).commit();
        }

        ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                WheelyService myService = ((WheelyService.WheelyBinder) binder).getService();
                if (myService.isConnected() && !(fragment instanceof WheelyMapFragment)) {
                    Log.e(TAG, "BIND");
                    fragment = new WheelyMapFragment();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment,
                            TAG_FRAGMENT).commit();
                }
                unbindService(this);
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "MainActivity onServiceDisconnected");
            }
        };
        Intent intent = new Intent(this, WheelyService.class);
        bindService(intent, serviceConnection, 0);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(BroadcastUtils.STATUS, 0);
                String data = intent.getStringExtra(BroadcastUtils.DATA);
                switch (status) {
                    case BroadcastUtils.STATUS_ERROR:
                        Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                        hideDialog();
                        break;
                    case BroadcastUtils.STATUS_DATA:
                        if (fragment instanceof WheelyMapFragment) {
                            ((WheelyMapFragment) fragment).addMarkersFromJSON(data);
                        }
                        break;
                    case BroadcastUtils.STATUS_CONNECTION_OPEN:
                        if (fragment instanceof LoginFragment) {
                            fragment = new WheelyMapFragment();
                            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment,
                                    TAG_FRAGMENT).commit();
                        }
                        hideDialog();
                        break;
                    case BroadcastUtils.STATUS_CONNECTION_DISCONNECT:
                        hideDialog();
                        if (fragment instanceof WheelyMapFragment) {
                            fragment = new LoginFragment();
                            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment,
                                    TAG_FRAGMENT).commit();
                        }
                        break;
                    case BroadcastUtils.STATUS_CONNECTION_START:
                        showDialog(context);
                        break;
                    default:
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BroadcastUtils.WHEELY_BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);


    }

    private void hideDialog() {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void showDialog(Context context) {
        try {
            dialog = ProgressDialog.show(context, null, getResources().getString(R.string.connecting));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
