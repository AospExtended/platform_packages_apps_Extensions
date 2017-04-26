/*
 * Copyright (C) 2016 AospExtended ROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aospextended.extensions;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import org.aospextended.extensions.preference.CustomSeekBarPreference;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.Utils;
import com.android.internal.util.du.DuUtils;
import android.hardware.fingerprint.FingerprintManager;
import org.aospextended.extensions.preference.SystemSettingSwitchPreference;

public class LockscreenUI extends SettingsPreferenceFragment implements OnPreferenceChangeListener {


    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String LOCKSCREEN_CHARGING = "lockscreen_battery_info";
    private static final String PREF_HIDE_WEATHER =
            "weather_hide_panel";
    private static final String PREF_NUMBER_OF_NOTIFICATIONS =
            "weather_number_of_notifications";
    private static final String FP_CAT = "lockscreen_ui_gestures_category";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String KEY_OMNIJAWS = "omnijaws";

    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";

    private ListPreference mLockClockFonts;
    private SwitchPreference mLockscreenCharging;
    private SystemSettingSwitchPreference mFpKeystore;
    private ListPreference mWeatherIconPack;
    private ListPreference mHideWeather;
    private CustomSeekBarPreference mNumberOfNotifications;
    private SystemSettingSwitchPreference mFingerprintVib;
    private FingerprintManager mFingerprintManager;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.lockscreen_ui);

        mResolver = getActivity().getContentResolver();
        PreferenceScreen prefs = getPreferenceScreen();
        Resources resources = getResources();

        PreferenceCategory fingerprintCategory = (PreferenceCategory) findPreference(FP_CAT);

        mHideWeather =
                (ListPreference) findPreference(PREF_HIDE_WEATHER);
        int hideWeather = Settings.System.getInt(mResolver,
               Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
        mHideWeather.setValue(String.valueOf(hideWeather));
        mHideWeather.setOnPreferenceChangeListener(this);

        mNumberOfNotifications =
                (CustomSeekBarPreference) findPreference(PREF_NUMBER_OF_NOTIFICATIONS);
        int numberOfNotifications = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 4);
        mNumberOfNotifications.setValue(numberOfNotifications);
        mNumberOfNotifications.setOnPreferenceChangeListener(this);
        if (!isOmniJawsServiceInstalled())
            getPreferenceScreen().removePreference(findPreference(KEY_OMNIJAWS));

        updatePreference();
        initweather();

        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.LOCK_CLOCK_FONTS, 4)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        mLockscreenCharging = (SwitchPreference) findPreference(LOCKSCREEN_CHARGING);
        if (!resources.getBoolean(R.bool.showCharging)) {
            prefScreen.removePreference(mLockscreenCharging);
        } else {
        mLockscreenCharging.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY_INFO, 0) == 1));
        mLockscreenCharging.setOnPreferenceChangeListener(this);
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        mFpKeystore = (SystemSettingSwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        if (!mFingerprintManager.isHardwareDetected()){
            fingerprintCategory.removePreference(mFingerprintVib);
            fingerprintCategory.removePreference(mFpKeystore);
        } else {
        mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
        mFingerprintVib.setOnPreferenceChangeListener(this);

        mFpKeystore.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FP_UNLOCK_KEYSTORE, 0) == 1));
        mFpKeystore.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreference();
    }

    private void updatePreference() {
        int hideWeather = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
        if (hideWeather == 0) {
            mNumberOfNotifications.setEnabled(false);
            mHideWeather.setSummary(R.string.weather_hide_panel_auto_summary);
        } else if (hideWeather == 1) {
            mNumberOfNotifications.setEnabled(true);
            mHideWeather.setSummary(R.string.weather_hide_panel_custom_summary);
        } else {
            mNumberOfNotifications.setEnabled(false);
            mHideWeather.setSummary(R.string.weather_hide_panel_never_summary);
        }
      }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockFonts) {
            Settings.System.putInt(resolver, Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mHideWeather) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mHideWeather.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
            updatePreference();
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mFpKeystore) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FP_UNLOCK_KEYSTORE, value ? 1 : 0);
            return true;
        } else if (preference == mNumberOfNotifications) {
            int numberOfNotifications = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS,
            numberOfNotifications);
            return true;
        } else if (preference == mLockscreenCharging) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY_INFO, value ? 1 : 0);
            return true;
        } else if (preference == mWeatherIconPack) {
                String value = (String) newValue;
                Settings.System.putString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
                int valueIndex = mWeatherIconPack.findIndexOfValue(value);
                mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
               return true;
        }
        return false;
    }

    private boolean isOmniJawsServiceInstalled() {
         return DuUtils.isPackageInstalled(getActivity(), WEATHER_SERVICE_PACKAGE);
     }

    public void initweather() {
        String settingsJaws = Settings.System.getString(getContentResolver(),
             Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
         if (settingsJaws == null) {
             settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
         }
         mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

         List<String> entriesJaws = new ArrayList<String>();
         List<String> valuesJaws = new ArrayList<String>();
         getAvailableWeatherIconPacks(entriesJaws, valuesJaws);
         mWeatherIconPack.setEntries(entriesJaws.toArray(new String[entriesJaws.size()]));
         mWeatherIconPack.setEntryValues(valuesJaws.toArray(new String[valuesJaws.size()]));

         int valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
         if (valueJawsIndex == -1) {
             // no longer found
             settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
             Settings.System.putString(getContentResolver(),
                     Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingsJaws);
             valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
         }
         mWeatherIconPack.setValueIndex(valueJawsIndex >= 0 ? valueJawsIndex : 0);
         mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
         mWeatherIconPack.setOnPreferenceChangeListener(this);
    }

     private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
         Intent i = new Intent();
         PackageManager packageManager = getPackageManager();
         i.setAction("org.omnirom.WeatherIconPack");
         for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
             String packageName = r.activityInfo.packageName;
             Log.d("maxwen", packageName);
             if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                 values.add(0, r.activityInfo.name);
             } else {
                 values.add(r.activityInfo.name);
             }
             String label = r.activityInfo.loadLabel(getPackageManager()).toString();
             if (label == null) {
                 label = r.activityInfo.packageName;
             }
             if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                 entries.add(0, label);
             } else {
                 entries.add(label);
             }
         }
         i = new Intent(Intent.ACTION_MAIN);
         i.addCategory(CHRONUS_ICON_PACK_INTENT);
         for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
             String packageName = r.activityInfo.packageName;
             values.add(packageName + ".weather");
             String label = r.activityInfo.loadLabel(getPackageManager()).toString();
             if (label == null) {
                 label = r.activityInfo.packageName;
             }
             entries.add(label);
         }
     }

     private boolean isOmniJawsEnabled() {
         final Uri SETTINGS_URI
             = Uri.parse("content://org.omnirom.omnijaws.provider/settings");

         final String[] SETTINGS_PROJECTION = new String[] {
             "enabled"
         };

         final Cursor c = getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                 null, null, null);
         if (c != null) {
             int count = c.getCount();
             if (count == 1) {
                 c.moveToPosition(0);
                 boolean enabled = c.getInt(0) == 1;
                 return enabled;
             }
         }
         return true;
     }

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
           pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
           installed = true;
        } catch (PackageManager.NameNotFoundException e) {
           installed = false;
        }
        return installed;
    }
}
