package com.hermosaprogramacion.blog.saludmock.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hermosaprogramacion.blog.saludmock.data.api.model.Affiliate;

/**
 * Manejador de preferencias de la sesión del afiliado
 */
public class SessionPrefs {

    private static final String PREFS_NAME = "SALUDMOCK_PREFS";
    private static final String PREF_AFFILIATE_ID = "PREF_USER_ID";
    private static final String PREF_AFFILIATE_NAME = "PREF_AFFILIATE_NAME";
    private static final String PREF_AFFILIATE_ADDRESS = "PREF_AFFILIATE_ADDRESS";
    private static final String PREF_AFFILIATE_GENDER = "PREF_AFFILIATE_GENDER";
    private static final String PREF_AFFILIATE_TOKEN = "PREF_AFFILIATE_TOKEN";

    private final SharedPreferences mPrefs;

    private boolean mIsLoggedIn = false;

    private static SessionPrefs INSTANCE;

    public static SessionPrefs get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionPrefs(context);
        }
        return INSTANCE;
    }

    private SessionPrefs(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mIsLoggedIn = !TextUtils.isEmpty(mPrefs.getString(PREF_AFFILIATE_TOKEN, null));
    }

    public boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    public void saveAffiliate(Affiliate affiliate) {
        if (affiliate != null) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(PREF_AFFILIATE_ID, affiliate.getId());
            editor.putString(PREF_AFFILIATE_NAME, affiliate.getName());
            editor.putString(PREF_AFFILIATE_ADDRESS, affiliate.getAddress());
            editor.putString(PREF_AFFILIATE_GENDER, affiliate.getGender());
            editor.putString(PREF_AFFILIATE_TOKEN, affiliate.getToken());
            editor.apply();

            mIsLoggedIn = true;
        }
    }

    public String getToken(){
        return mPrefs.getString(PREF_AFFILIATE_TOKEN, null);
    }

    public void logOut(){
        mIsLoggedIn = false;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_AFFILIATE_ID, null);
        editor.putString(PREF_AFFILIATE_NAME, null);
        editor.putString(PREF_AFFILIATE_ADDRESS, null);
        editor.putString(PREF_AFFILIATE_GENDER, null);
        editor.putString(PREF_AFFILIATE_TOKEN, null);
        editor.apply();
    }
}
