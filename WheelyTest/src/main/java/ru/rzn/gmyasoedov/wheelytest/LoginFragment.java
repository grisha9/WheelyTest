package ru.rzn.gmyasoedov.wheelytest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Login fragment
 */
public class LoginFragment extends Fragment {
    public static final String PASSWORD = "password";
    public static final String NAME = "name";

    private String name;
    private String password;

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
        EditText nameEditText = ((EditText) view.findViewById(R.id.name));
        nameEditText.setText(name);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                name = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        EditText passwordEditText = ((EditText) view.findViewById(R.id.password));
        passwordEditText.setText(password);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                password = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                //hide key board
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                Intent connectIntent = new Intent(getActivity(), WheelyService.class);
                connectIntent.putExtra(NAME, name);
                connectIntent.putExtra(PASSWORD, password);

                BroadcastUtils.sendBroadcast(BroadcastUtils.STATUS_CONNECTION_START, null, getActivity());
                getActivity().startService(connectIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
