package com.example.al.sibirski;

import android.content.Context;

import java.net.CookieManager;
import java.net.HttpCookie;

public class UtilCookies {

    private static final String COOKIE_PASSPORT = "PASSPORTID";

    public static boolean isLoggedIn(CookieManager cookieManager) {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals(COOKIE_PASSPORT)) {
                if (cookie.getValue().length() > 5) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void saveCookies(Context context, CookieManager cookieManager) {
        ApplicationData app = (ApplicationData) context;
        app.setCookieManager(cookieManager);
    }

    public static CookieManager loadCookies(Context context) {
        ApplicationData app = (ApplicationData) context;
        return app.getCookieManager();
    }
}
