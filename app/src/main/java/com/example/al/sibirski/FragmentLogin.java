package com.example.al.sibirski;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class FragmentLogin extends Fragment {
    private static final String LOG_TAG = FragmentLogin.class.getSimpleName();

    public static final String LOGIN_URL = "http://header.211.ru/";
    public static final String NAME_PARAM = "login";
    public static final String PASS_PARAM = "password";
    private static final String REMEMBER_LOGIN = "rememberLogin";

    private EditText mEditName;
    private EditText mEditPass;

    private CookieManager mCookieManager;
    private Context mAppContext;

    public FragmentLogin() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppContext = getActivity().getApplicationContext();
        mCookieManager = UtilCookies.loadCookies(mAppContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mEditName = (EditText) rootView.findViewById(R.id.edit_name);
        mEditName.setText(retrieveLogin());
        mEditPass = (EditText) rootView.findViewById(R.id.edit_pass);
        Button buttonLogin = (Button) rootView.findViewById(R.id.button_login);
        buttonLogin.setEnabled(false);

        buttonLogin.setOnClickListener(v -> loginRequest());

        Observable<TextViewTextChangeEvent> observableName = RxTextView.textChangeEvents(mEditName);
        Observable<TextViewTextChangeEvent> observablePass = RxTextView.textChangeEvents(mEditPass);

        Observable<Boolean> enableButton = Observable.combineLatest(
                observableName,
                observablePass,
                (name,pass)-> lengthOk(name.text().toString()) && lengthOk(pass.text().toString()));

        // lambda method reference
        enableButton.subscribe(buttonLogin::setEnabled);

        return rootView;
    }

    private boolean lengthOk(String string) {
        return string.length() > 0;
    }

    private void loginRequest() {

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                LOGIN_URL,
                (Response.Listener<String>) response -> checkLogin(),
                (Response.ErrorListener) error -> log("Volley in login query returned error!")
        ) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put(NAME_PARAM, mEditName.getText().toString());
                params.put(PASS_PARAM, mEditPass.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void checkLogin() {
        if (UtilCookies.isLoggedIn(mCookieManager)){
            loginSuccess();
        }
        else {
            loginFail();
        }
    }

    private void loginSuccess() {
        rememberLogin();
        UtilCookies.saveCookies(mAppContext, mCookieManager);
        startActivity(new Intent(getActivity(), ActivityMain.class));
    }

    private void loginFail() {
        showToast(getString(R.string.login_failed));
        mCookieManager.getCookieStore().removeAll();
        mEditPass.setText("");
    }

    private String retrieveLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getString(REMEMBER_LOGIN, "");
    }

    private void rememberLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REMEMBER_LOGIN, mEditName.getText().toString());
        editor.apply();
    }

    private void log(String text) {
        Log.i(LOG_TAG, text);
    }

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}