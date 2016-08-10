package com.example.al.sibirski;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentLoan extends Fragment {
    private static final String LOG_TAG = FragmentLoan.class.getSimpleName();

    public static final String LOAN_URL = "http://passport.211.ru/cabinet/promised/";
    public static final String BALANCE_CLASS = "header-balance-button";

    public static final String DATE_ID = "popup-set-promise";
    private static final String DATE_TAG = "strong";
    public static final String SUM_CLASS = "promised-choose";
    private static final String SUM_TAG = "span";
    private static final String SUM_ATTR = "data-name";
    private static final String INPUT_TAG = "input";
    private static final String NAME_ATTR = "name";
    private static final String VALUE_ATTR = "value";
    public static final String SUCCESS_CLASS = "profile-notificaton";
    public static final String ALREADY_CLASS = "tariff-warning";

    public static final String TOKEN_PARAM = "token";
    public static final String CAPTCHA_PARAM = "captcha";
    public static final String SUM_PARAM = "sum";
    public static final String AGREE_PARAM = "agree";

    private TextView mTextBalance;
    private TextView mTextDate;
    private TextView mTextResponse;
    private Button mButtonChoose;
    private Button mButtonLoan;

    private ArrayList<String> mSumList = new ArrayList<>();
    private String mToken = null;
    private String mCaptcha = null;

    private ProgressDialog mProgressDialog;
    private CookieManager mCookieManager;
    private Context mAppContext;

    public FragmentLoan() {
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

        View rootView = inflater.inflate(R.layout.fragment_loan, container, false);

        mTextBalance = (TextView) rootView.findViewById(R.id.text_balance);
        mButtonLoan = (Button) rootView.findViewById(R.id.button_loan);
        mButtonLoan.setOnClickListener(v -> loanRequest());
        mButtonLoan.setEnabled(false);

        return rootView;
    }

    private void showSumDialog() {

        CharSequence[] items = new CharSequence[mSumList.size()];
        for (int i = 0; i < mSumList.size(); i++) {
            items[i] = mSumList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_sum_title);
        builder.setCancelable(true);
        builder.setItems(items, (dialog, item) -> returnItem(item));
        builder.show();
    }

    public void returnItem(int item) {
        mButtonChoose.setText(mSumList.get(item));
        mButtonLoan.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!UtilCookies.isLoggedIn(mCookieManager)) {
            showToast(getString(R.string.open_loan_failed));
            goBack();
        }

        startProgressDialog();

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        logCookies();

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                LOAN_URL,
                this::loadingFinished,
                this::loadingError
        );

        queue.add(stringRequest);
    }

    private void loanRequest() {

        startProgressDialog();

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                LOAN_URL,
                (Response.Listener<String>) this::requestFinished,
                (Response.ErrorListener) this::requestError
        ) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put(TOKEN_PARAM, mToken);
                params.put(CAPTCHA_PARAM, mCaptcha);
                params.put(SUM_PARAM, mButtonChoose.getText().toString());
                params.put(AGREE_PARAM, "1");
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

    private void loadingFinished(String response) {
        closeProgressDialog();

        String responseEncoded = null;
        try {
            responseEncoded = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"),"UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        Document doc = Jsoup.parse(responseEncoded);

        try {
            Elements balanceClass = doc.getElementsByClass(BALANCE_CLASS);
            String balance =  balanceClass.get(0).ownText();
            mTextBalance.setText(String.format(getContext().getString(R.string.balance),balance));


            if (doc.getElementsByClass(SUM_CLASS).size() > 0) {
                showLoanOptions(doc);
            }
            else {
                showLoanTaken(doc);
            }
        }
        catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            showToast(getString(R.string.parsing_error));
            goBack();
        }
    }

    private void showLoanOptions(Document doc) {

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View loanOptionsLayout = inflater.inflate(R.layout.loan_options, (ViewGroup) getActivity()
                .findViewById(R.id.loan_options_id));

        // XXX Scroll instead of list adapter
        ViewGroup rootView = (ViewGroup) getActivity().findViewById(R.id.scroll_container);

        rootView.addView(loanOptionsLayout);

        Elements sumClass = doc.getElementsByClass(SUM_CLASS);
        Elements sumTags = sumClass.get(0).getElementsByTag(SUM_TAG);
        for (Element sumElement : sumTags) {
            String sumAttr = sumElement.attr(SUM_ATTR);
            mSumList.add(sumAttr);
        }

        Elements inputTags = sumClass.get(0).getElementsByTag(INPUT_TAG);
        for (Element inputElement : inputTags) {
            String name = inputElement.attr(NAME_ATTR);
            String value = inputElement.attr(VALUE_ATTR);
            if (name.equals(CAPTCHA_PARAM)){
                mCaptcha = value;
            }
            else if (name.equals(TOKEN_PARAM)) {
                mToken = value;
            }
        }

        Element dateId = doc.getElementById(DATE_ID);
        Elements dateTag = dateId.getElementsByTag(DATE_TAG);
        String date =  dateTag.get(0).ownText();

        mTextDate = (TextView) loanOptionsLayout.findViewById(R.id.text_date);
        mTextDate.setText(date);
        mButtonChoose = (Button) loanOptionsLayout.findViewById(R.id.button_sum);
        mButtonChoose.setOnClickListener(v -> showSumDialog());
    }

    private void showLoanTaken(Document doc) {

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View loanOptionsLayout = inflater.inflate(R.layout.loan_already, (ViewGroup) getActivity()
                .findViewById(R.id.loan_already_id));

        ViewGroup rootView = (ViewGroup) getActivity().findViewById(R.id.scroll_container);

        rootView.addView(loanOptionsLayout);

        String text = "";
        Elements alreadyClass = doc.getElementsByClass(ALREADY_CLASS);
        for (Element line : alreadyClass) {
            text = text.concat(line.ownText());
        }

        mTextResponse = (TextView) loanOptionsLayout.findViewById(R.id.text_response);
        mTextResponse.setText(text);

    }

    private void requestFinished(String response) {
        closeProgressDialog();

        String responseEncoded = null;
        try {
            responseEncoded = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"),"UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        Document doc = Jsoup.parse(responseEncoded);

        Elements successClass = doc.getElementsByClass(SUCCESS_CLASS);

        if (successClass.size() > 0) {
            showToast(getString(R.string.open_loan_success));
            log(responseEncoded);
            goBack();
        }
    }

    private void requestError(VolleyError error) {
        closeProgressDialog();
        log(error.toString());
    }

    private void loadingError(VolleyError error) {
        closeProgressDialog();
        log(error.toString());
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

    private void logCookies() {
        for (java.net.HttpCookie cookie : mCookieManager.getCookieStore().getCookies()) {
            log(cookie.toString());
        }
    }

    private void goBack() {
        getActivity().onBackPressed();
    }

    private void log(String text) {
        Log.i(LOG_TAG, text);
    }

    // XXX boilerplate method
    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}
