package com.example.al.sibirski;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;

public class FragmentMain extends Fragment {
    private static final String LOG_TAG = FragmentMain.class.getSimpleName();

    public static final String MAIN_URL = "http://header.211.ru/";

    private TextView mTextBalance;
    private TextView mTextPlan;
    private TextView mTextCharge;

    private CookieManager mCookieManager;
    private Context mAppContext;

    public FragmentMain() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppContext = getActivity().getApplicationContext();
        mCookieManager = UtilCookies.loadCookies(mAppContext);
        if (!UtilCookies.isLoggedIn(mCookieManager)) {
            loginFailed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mTextBalance = (TextView) rootView.findViewById(R.id.text_balance);
        mTextPlan = (TextView) rootView.findViewById(R.id.text_plan);
        mTextCharge = (TextView) rootView.findViewById(R.id.text_charge);

        Button buttonCharges = (Button) rootView.findViewById(R.id.button_charges);
        buttonCharges.setOnClickListener(v -> openChargesActivity());
        Button buttonLoan = (Button) rootView.findViewById(R.id.button_loan);
        buttonLoan.setOnClickListener(v -> openLoanActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                MAIN_URL,
                (Response.Listener<String>) this::populateInterface,
                (Response.ErrorListener) error -> log("Volley query in main returned error!")
        );

        queue.add(stringRequest);
    }

    private void populateInterface(String response) {
//        mTextCharge.setText(response);
    }


    private void openLoanActivity() {
    }

    private void openChargesActivity() {
    }

    private void loginFailed() {
        showToast(getString(R.string.login_failed));
        mCookieManager.getCookieStore().removeAll();
        getActivity().onBackPressed();
    }

    private void log(String text) {
        Log.i(LOG_TAG, text);
    }

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}
