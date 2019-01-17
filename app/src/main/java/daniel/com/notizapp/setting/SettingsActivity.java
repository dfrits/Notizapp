package daniel.com.notizapp.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import daniel.com.notizapp.R;
import daniel.com.notizapp.core.SplashActivity;
import daniel.com.notizapp.util.Constants;

/**
 * Created by Tristan on 26.09.2016.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String activity = getIntent().getStringExtra("Activity");

        if (activity != null && activity.equals("Main")) {
            if (!SplashActivity.hasExternalPath()) {
                Toast.makeText(context, R.string.no_external_storage_found, Toast.LENGTH_LONG).show();
            }
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment_Main())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment_Notice())
                    .commit();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    /**
     * Settings ausgehend von der MainActivity.
     * Alle Settings verfügbar.
     */
    public static class SettingsFragment_Main extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_general);

            // Kann SD-Karte nicht verwendet werden, ist diese Einstellung nicht verwendbar
            if (!SplashActivity.hasExternalPath()) {
                Preference preference = findPreference(Constants.FOLDER_DEST_SETTING_KEY);
                preference.setEnabled(false);
            }
        }
    }

    /**
     * Settings ausgehend von der NotizActivity.
     * Pfad nicht änderbar.
     */
    public static class SettingsFragment_Notice extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference_general);
            Preference preference = findPreference(Constants.FOLDER_DEST_SETTING_KEY);
            preference.setEnabled(false);
        }
    }
}
