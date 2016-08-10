package com.example.al.sibirski;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentDialer {
    public static void callProvider(Context context) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        // XXX hardcoded number
        intent.setData(Uri.parse("tel:88002500211"));
        context.startActivity(intent);
    }
}
