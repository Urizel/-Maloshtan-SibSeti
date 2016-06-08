package com.example.al.sibirski;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import rx.Observable;

public class FragmentLogin extends Fragment {
    private static final String LOG_TAG = FragmentLogin.class.getSimpleName();

    public FragmentLogin() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        EditText editName = (EditText) rootView.findViewById(R.id.edit_name);
        EditText editPass = (EditText) rootView.findViewById(R.id.edit_pass);
        Button buttonLogin = (Button) rootView.findViewById(R.id.button_login);
        buttonLogin.setEnabled(false);

        Observable<TextViewTextChangeEvent> observableName = RxTextView.textChangeEvents(editName);
        Observable<TextViewTextChangeEvent> observablePass = RxTextView.textChangeEvents(editPass);

        Observable<Boolean> enableButton = Observable.combineLatest(
                observableName,
                observablePass,
                (name,pass)-> lengthOk(name.text().toString()) && lengthOk(pass.text().toString()));

        enableButton.subscribe(enabled -> buttonLogin.setEnabled(enabled));

        return rootView;
    }

    private boolean lengthOk(String string) {
        return string.length() >= 3;
    }

    private void log(String text) {
        Log.i(LOG_TAG, text);
    }
}
