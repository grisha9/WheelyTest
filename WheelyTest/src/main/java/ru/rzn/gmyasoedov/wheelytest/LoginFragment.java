package ru.rzn.gmyasoedov.wheelytest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

/**
 * Login fragment
 */
public class LoginFragment extends Fragment {
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    private EditText name;
    private EditText password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.layout_login, container, false);
        name = (EditText) view.findViewById(R.id.name);
        password = ((EditText) view.findViewById(R.id.password));
        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.login_action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.connect:
                if (isOnline()) {
                    //hide key board
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    Intent connectIntent = new Intent(getActivity(), WheelyService.class);
                    connectIntent.putExtra(NAME, name.getText().toString());
                    connectIntent.putExtra(PASSWORD, password.getText().toString());
                    try {
                        TimeUnit.MILLISECONDS.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.e("e", e.getMessage());
                    }
                    BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_CONNECTION_START, null, getActivity());
                    getActivity().startService(connectIntent);
                } else {
                    BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_ERROR,
                            getResources().getString(R.string.connect_error), getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
