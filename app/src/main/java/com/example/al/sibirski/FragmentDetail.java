package com.example.al.sibirski;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class FragmentDetail extends Fragment {
    private static final String LOG_TAG = FragmentDetail.class.getSimpleName();

    private static final String DETAILS_URL = "http://passport.211.ru/cabinet";
    public static final String BALANCE_CLASS = "header-balance-button";
    private static final String TABLE_CLASS = "balance-history";
    private static final String TR_TAG = "tr";
    private static final String TD_TAG = "td";
    private static final String PLUS_CLASS = "bh-sum-positive";

    private TextView mTextBalance;
    private AdapterDetails mAdapterDetails = null;

    private ProgressDialog mProgressDialog;
    private CookieManager mCookieManager;
    private Context mAppContext;

    public FragmentDetail() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppContext = getActivity().getApplicationContext();
        mCookieManager = UtilCookies.loadCookies(mAppContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTextBalance = (TextView) rootView.findViewById(R.id.text_balance);

        mAdapterDetails = new AdapterDetails(getContext(), R.layout.list_item);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_details);
        listView.setAdapter(mAdapterDetails);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!UtilCookies.isLoggedIn(mCookieManager)) {
            showToast(getString(R.string.open_details_failed));
            goBack();
        }

        startProgressDialog();

        CookieHandler.setDefault(mCookieManager);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        logCookies();

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                DETAILS_URL,
                this::loadingFinished,
                this::loadingError
        );

        queue.add(stringRequest);
    }

    private void loadingError(VolleyError error) {
        closeProgressDialog();
        log(error.toString());
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

        Elements balanceClass = doc.getElementsByClass(BALANCE_CLASS);
        String balance =  balanceClass.get(0).ownText();
        mTextBalance.setText(String.format(getContext().getString(R.string.balance),balance));

        Elements tableClass = doc.getElementsByClass(TABLE_CLASS);
        Elements trTag = tableClass.get(0).getElementsByTag(TR_TAG);
        for (int i = 1; i < trTag.size(); i++) {
            Elements tdTag = trTag.get(i).getElementsByTag(TD_TAG);
            String date = tdTag.get(0).ownText();
            String text = tdTag.get(1).ownText();
            String sum = tdTag.get(2).text();
            Elements plusClass = tdTag.get(2).getElementsByClass(PLUS_CLASS);
            boolean plus = (plusClass.size() > 0);
            ContainerPayment newElement = new ContainerPayment(date, plus, sum, text);
            mAdapterDetails.add(newElement);
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

    private void showToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}
