package com.example.al.sibirski;

import java.net.CookieManager;

// XXX Code style and naming
public class ApplicationCookies extends android.app.Application {
    private CookieManager cookieManager = new CookieManager();
    public void setCookieManager(CookieManager cookieManager) {this.cookieManager=cookieManager;}
    public CookieManager getCookieManager() {return cookieManager;}
}
