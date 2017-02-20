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

import org.aospextended.extensions.preference.CustomSeekBarPreference;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

public class LockscreenUI extends SettingsPreferenceFragment implements OnPreferenceChangeListener {


    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String LOCKSCREEN_CHARGING = "lockscreen_battery_info";
    private static final String PREF_CONDITION_ICON =
            "weather_condition_icon";
    private static final String PREF_HIDE_WEATHER =
            "weather_hide_panel";
    private static final String PREF_NUMBER_OF_NOTIFICATIONS =
            "weather_number_of_notifications";
    private static final String KEY_LOCK_CLOCK =
            "lock_clock";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";

    private static final int MONOCHROME_ICON = 0;

    private ListPreference mLockClockFonts;
    private SwitchPreference mLockscreenCharging;
    private SwitchPreference mFpKeystore;
    private ListPreference mConditionIcon;
    private ListPreference mHideWeather;
    private CustomSeekBarPreference mNumberOfNotifications;

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

        // Remove the lock clock preference if its not installed
        if (!isPackageInstalled("com.cyanogenmod.lockclock")) {
            removePreference(KEY_LOCK_CLOCK);
        }

        mConditionIcon =
                (ListPreference) findPreference(PREF_CONDITION_ICON);
        int conditionIcon = Settings.System.getInt(mResolver,
               Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, MONOCHROME_ICON);
        mConditionIcon.setValue(String.valueOf(conditionIcon));
        mConditionIcon.setSummary(mConditionIcon.getEntry());
        mConditionIcon.setOnPreferenceChangeListener(this);

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

        updatePreference();

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

        mFpKeystore = (SwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        mFpKeystore.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FP_UNLOCK_KEYSTORE, 0) == 1));
        mFpKeystore.setOnPreferenceChangeListener(this);

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
          } else if (preference == mConditionIcon) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mConditionIcon.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, intValue);
            mConditionIcon.setSummary(mConditionIcon.getEntries()[index]);
            return true;
        } else if (preference == mHideWeather) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mHideWeather.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
            updatePreference();
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
        }
        return false;
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
