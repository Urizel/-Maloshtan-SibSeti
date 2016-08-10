package com.example.al.sibirski;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class FragmentMain extends Fragment {
    private static final String LOG_TAG = FragmentMain.class.getSimpleName();

    public static final String MAIN_URL = "http://passport.211.ru/profile/";
    public static final String BALANCE_CLASS = "header-balance-button";
    public static final String PLAN_CLASS = "tariff-info";
    public static final String PLAN_TAG = "h2";
    private static final String COST_TAG = "strong";
    private static final char PLAN_LEFT_BRACE = '«';
    private static final char PLAN_RIGHT_BRACE = '»';
    private static final String PAYMENT_URI = "http://sibset.ru/oplata/";

    private TextView mTextBalance;
    private TextView mTextPlan;
    private TextView mTextCharge;

    private ProgressDialog mProgressDialog;
    private CookieManager mCookieManager;
    private Context mAppContext;

    public FragmentMain() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppContext = getActivity().getApplicationContext();
        mCookieManager = UtilCookies.loadCookies(mAppContext);
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
        Button buttonLoan = (Button) rootView.findViewById(R.id.button_open_loan);
        buttonLoan.setOnClickListener(v -> openLoanActivity());
        Button buttonPayment = (Button) rootView.findViewById(R.id.button_payment);
        buttonPayment.setOnClickListener(v -> goToPayment());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!UtilCookies.isLoggedIn(mCookieManager)) {
            showToast(getString(R.string.login_failed));
            goBack();
        }

        startProgressDialog();

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        logCookies();

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                MAIN_URL,
                this::loadingFinished,
                this::loadingError
        );

        queue.add(stringRequest);
    }

    private void loadingFinished(String response) {
        closeProgressDialog();

        String responseEncoded = null;
        try {
            responseEncoded = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"),"UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        // XXX Possible NPE
        Document doc = Jsoup.parse(responseEncoded);

        try {
            Elements balanceClass = doc.getElementsByClass(BALANCE_CLASS);
            String balance =  balanceClass.get(0).ownText();
            mTextBalance.setText(String.format(getContext().getString(R.string.balance),balance));

            Elements planClass = doc.getElementsByClass(PLAN_CLASS);
            Elements planTag = planClass.get(0).getElementsByTag(PLAN_TAG);
            String plan = planTag.get(0).ownText();
            plan = plan.substring(plan.indexOf(PLAN_LEFT_BRACE)+1);
            plan = plan.substring(0, plan.indexOf(PLAN_RIGHT_BRACE));
            mTextPlan.setText(plan);

            Elements costClass = doc.getElementsByClass(PLAN_CLASS);
            Elements costTag = costClass.get(0).getElementsByTag(COST_TAG);
            String cost = costTag.get(1).ownText();
            mTextCharge.setText(cost);
        }
        catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            showToast(getString(R.string.parsing_error));
            goBack();
        }
    }

    private void loadingError(VolleyError error) {
        closeProgressDialog();
        log(error.toString());
    }

    private void logCookies() {
        for (java.net.HttpCookie cookie : mCookieManager.getCookieStore().getCookies()) {
            log(cookie.toString());
        }
    }

    private void startProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle(R.string.please_wait);
        mProgressDialog.show();

    }

    private void closeProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void goToPayment() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(PAYMENT_URI));
        startActivity(i);
    }

    private void openLoanActivity() {
        openActivity(ActivityLoan.class);
    }

    private void openChargesActivity() {
        openActivity(ActivityDetail.class);
    }

    private void openActivity(Class whatActivity) {
        UtilCookies.saveCookies(mAppContext, mCookieManager);
        Intent intent = new Intent(getContext(), whatActivity);
        startActivity(intent);
    }

    private void goBack() {
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
