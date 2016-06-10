package com.example.al.sibirski;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class FragmentLogin extends Fragment {
    private static final String LOG_TAG = FragmentLogin.class.getSimpleName();

    public static final String LOGIN_URL = "http://header.211.ru/";
    public static final String NAME_PARAM = "login";
    public static final String PASS_PARAM = "password";
    public static final String COOKIE_PASSPORT = "PASSPORTID";
    private static final String REMEMBER_LOGIN = "rememberLogin";

    public FragmentLogin() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        EditText editName = (EditText) rootView.findViewById(R.id.edit_name);
        editName.setText(retrieveLogin());
        EditText editPass = (EditText) rootView.findViewById(R.id.edit_pass);
        Button buttonLogin = (Button) rootView.findViewById(R.id.button_login);
        buttonLogin.setEnabled(false);

        buttonLogin.setOnClickListener(v -> {
            loginRequest(editName.getText().toString(), editPass.getText().toString());
        });

        Observable<TextViewTextChangeEvent> observableName = RxTextView.textChangeEvents(editName);
        Observable<TextViewTextChangeEvent> observablePass = RxTextView.textChangeEvents(editPass);

        Observable<Boolean> enableButton = Observable.combineLatest(
                observableName,
                observablePass,
                (name,pass)-> lengthOk(name.text().toString()) && lengthOk(pass.text().toString()));

        // lambda method reference
        enableButton.subscribe(buttonLogin::setEnabled);

        return rootView;
    }

    private String retrieveLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getString(REMEMBER_LOGIN, "");
    }

    private void rememberLogin(String login) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REMEMBER_LOGIN, login);
        editor.apply();
    }

    private boolean lengthOk(String string) {
        return string.length() > 0;
    }

    private void log(String text) {
        Log.i(LOG_TAG, text);
    }

    private void loginRequest(String name, String pass) {

        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                LOGIN_URL,
                (Response.Listener<String>) response -> checkLogin(manager, name),
                (Response.ErrorListener) error -> log("Volley query returned error!")
        ) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put(NAME_PARAM, name);
                params.put(PASS_PARAM, pass);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void checkLogin(CookieManager manager, String name) {
        for (HttpCookie cookie : manager.getCookieStore().getCookies()) {
            if (cookie.getName().equals(COOKIE_PASSPORT)) {
                if (cookie.getValue().length() > 5) {
                    proceedLogin(name);
                }
            }
        }
    }

    private void proceedLogin(String name) {
        rememberLogin(name);
        log("PROCEED");
//        Intent intent = new Intent();
    }

}
