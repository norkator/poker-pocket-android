package com.nitramite.pokerpocket;

import android.os.Bundle;

public class Preferences extends com.fnp.materialpreferences.PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferenceFragment(new MyPreferenceFragment());
    }

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment {
        @Override
        public int addPreferencesFromResource() {
            return R.xml.preferences;
        }
    }

}