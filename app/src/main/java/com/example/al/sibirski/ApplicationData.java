package com.example.al.sibirski;

import java.net.CookieManager;

public class ApplicationData extends android.app.Application {
    private CookieManager cookieManager = new CookieManager();
    public void setCookieManager(CookieManager cookieManager) {this.cookieManager=cookieManager;}
    public CookieManager getCookieManager() {return cookieManager;}
}
