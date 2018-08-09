/*
 * Copyright (C) 2017 AospExtended ROM
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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import org.aospextended.extensions.preference.CustomSeekBarPreference;
import org.aospextended.extensions.preference.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.utils.du.ActionConstants;
import com.android.internal.utils.du.Config;
import com.android.internal.utils.du.DUActionUtils;
import com.android.internal.utils.du.Config.ButtonConfig;
import com.android.settings.R;

public class NavbarSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String KEY_GESTURE_NAVIGATION = "use_bottom_gesture_navigation";
    private static final String KEY_NAVBAR_MODE = "navbar_mode";
    private static final String KEY_DEFAULT_NAVBAR_SETTINGS = "default_settings";
    private static final String KEY_FLING_NAVBAR_SETTINGS = "fling_settings";
    private static final String KEY_CATEGORY_NAVIGATION_INTERFACE = "category_navbar_interface";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_SMARTBAR_SETTINGS = "smartbar_settings";
    private static final String KEY_NAVIGATION_HEIGHT_PORT = "navbar_height_portrait";
    private static final String KEY_NAVIGATION_HEIGHT_LAND = "navbar_height_landscape";
    private static final String KEY_NAVIGATION_WIDTH = "navbar_width";
    private static final String KEY_PULSE_SETTINGS = "pulse_settings";

    private SwitchPreference mNavbarVisibility;
    private SystemSettingSwitchPreference mGestureNavigation;
    private ListPreference mNavbarMode;
    private PreferenceScreen mFlingSettings;
    private PreferenceCategory mNavInterface;
    private PreferenceCategory mNavGeneral;
    private PreferenceScreen mSmartbarSettings;
    private Preference mDefaultSettings;
    private CustomSeekBarPreference mBarHeightPort;
    private CustomSeekBarPreference mBarHeightLand;
    private CustomSeekBarPreference mBarWidth;
    private PreferenceScreen mPulseSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navbar_settings);

        mNavInterface = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_INTERFACE);
        mGestureNavigation = (SystemSettingSwitchPreference) findPreference(KEY_GESTURE_NAVIGATION);
        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);
        mNavbarMode = (ListPreference) findPreference(KEY_NAVBAR_MODE);
        mDefaultSettings = (Preference) findPreference(KEY_DEFAULT_NAVBAR_SETTINGS);
        mFlingSettings = (PreferenceScreen) findPreference(KEY_FLING_NAVBAR_SETTINGS);
        mSmartbarSettings = (PreferenceScreen) findPreference(KEY_SMARTBAR_SETTINGS);
        mPulseSettings = (PreferenceScreen) findPreference(KEY_PULSE_SETTINGS);

        boolean showing = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_VISIBLE,
                DUActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);
        mNavbarVisibility.setOnPreferenceChangeListener(this);

        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);

        updateBarModeSettings(mode);
        mNavbarMode.setOnPreferenceChangeListener(this);

        boolean gestures = Settings.System.getInt(getContentResolver(),
                Settings.System.USE_BOTTOM_GESTURE_NAVIGATION, 0) == 1;
        updateGestures(gestures);
        mGestureNavigation.setOnPreferenceChangeListener(this);

        int size = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_HEIGHT, 100, UserHandle.USER_CURRENT);
        mBarHeightPort = (CustomSeekBarPreference) findPreference(KEY_NAVIGATION_HEIGHT_PORT);
        mBarHeightPort.setValue(size);
        mBarHeightPort.setOnPreferenceChangeListener(this);

        final boolean canMove = DUActionUtils.navigationBarCanMove();
        if (canMove) {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_HEIGHT_LAND));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, 100, UserHandle.USER_CURRENT);
            mBarWidth = (CustomSeekBarPreference) findPreference(KEY_NAVIGATION_WIDTH);
            mBarWidth.setValue(size);
            mBarWidth.setOnPreferenceChangeListener(this);
        } else {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_WIDTH));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, 100, UserHandle.USER_CURRENT);
            mBarHeightLand = (CustomSeekBarPreference) findPreference(KEY_NAVIGATION_HEIGHT_LAND);
            mBarHeightLand.setValue(size);
            mBarHeightLand.setOnPreferenceChangeListener(this);
        }
    }

    private void updateBarModeSettings(int mode) {
        mNavbarMode.setValue(String.valueOf(mode));
        switch (mode) {
            case 0:
                mDefaultSettings.setEnabled(true);
                mDefaultSettings.setSelectable(true);
                mSmartbarSettings.setEnabled(false);
                mSmartbarSettings.setSelectable(false);
                mFlingSettings.setEnabled(false);
                mFlingSettings.setSelectable(false);
                mPulseSettings.setEnabled(false);
                mPulseSettings.setSelectable(false);
                break;
            case 1:
                mDefaultSettings.setEnabled(false);
                mDefaultSettings.setSelectable(false);
                mSmartbarSettings.setEnabled(true);
                mSmartbarSettings.setSelectable(true);
                mFlingSettings.setEnabled(false);
                mFlingSettings.setSelectable(false);
                mPulseSettings.setEnabled(true);
                mPulseSettings.setSelectable(true);
                break;
            case 2:
                mDefaultSettings.setEnabled(false);
                mDefaultSettings.setSelectable(false);
                mSmartbarSettings.setEnabled(false);
                mSmartbarSettings.setSelectable(false);
                mFlingSettings.setEnabled(true);
                mFlingSettings.setSelectable(true);
                mPulseSettings.setEnabled(true);
                mPulseSettings.setSelectable(true);
                break;
        }
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
        mGestureNavigation.setChecked(!showing);
        mGestureNavigation.setEnabled(!showing);
        mNavInterface.setEnabled(mNavbarVisibility.isChecked());
        mNavGeneral.setEnabled(mNavbarVisibility.isChecked());
    }

    private void updateGestures(boolean showing) {
        mGestureNavigation.setChecked(showing);
        mNavbarVisibility.setChecked(!showing);
        mNavbarVisibility.setEnabled(!showing);
        mNavInterface.setEnabled(mNavbarVisibility.isChecked());
        mNavGeneral.setEnabled(mNavbarVisibility.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarMode)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_MODE, mode);
            updateBarModeSettings(mode);
            return true;
        } else if (preference.equals(mNavbarVisibility)) {
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 1 : 0);
            Settings.System.putInt(getContentResolver(), Settings.System.USE_BOTTOM_GESTURE_NAVIGATION,
                    showing ? 0 : 1);
            updateBarVisibleAndUpdatePrefs(showing);
            return true;
        } else if (preference == mGestureNavigation) {
            boolean showing = ((Boolean)newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.USE_BOTTOM_GESTURE_NAVIGATION,
                    showing ? 1 : 0);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 0 : 1);
            updateGestures(showing);
            return true;
        } else if (preference == mBarHeightPort) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarHeightLand) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarWidth) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, val, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EXTENSIONS;
    }
}
