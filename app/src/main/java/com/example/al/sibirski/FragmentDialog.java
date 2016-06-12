package com.example.al.sibirski;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

public class FragmentDialog extends DialogFragment {
    private static final String LOG_TAG = FragmentDialog.class.getSimpleName();

    public static final String BUNDLE_SUM = "bundleSum";
    public static final String FRAGMENT_TAG = "fragmentDialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*CharSequence[] list = new CharSequence[mSumList.size()];
        for (int i = 0; i < mSumList.size(); i++) {
            list[i] = mSumList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_sum_title).setItems(list, (dialog, which) -> {

        });
        builder.create();*/
        return null;
    }
}