/*
 * Copyright (C) 2018 AospExtended ROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aospextended.extensions.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import org.aospextended.extensions.preference.CustomSeekBarPreference;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class BatteryBar extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener, Indexable  {

    private static final String PREF_BATT_BAR = "statusbar_battery_bar_list";
    private static final String PREF_BATT_BAR_NO_NAVBAR = "statusbar_battery_bar_no_navbar_list";
    private static final String PREF_BATT_BAR_STYLE = "statusbar_battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "statusbar_battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "statusbar_battery_bar_charging_color";
    private static final String PREF_BATT_BAR_BATTERY_LOW_COLOR = "statusbar_battery_bar_battery_low_color";
    private static final String PREF_BATT_BAR_WIDTH = "statusbar_battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "statusbar_battery_bar_animate";
    private static final String PREF_BATT_USE_CHARGING_COLOR = "statusbar_battery_bar_enable_charging_color";
    private static final String PREF_BATT_BLEND_COLOR = "statusbar_battery_bar_blend_color";
    private static final String PREF_BATT_BLEND_COLOR_REVERSE = "statusbar_battery_bar_blend_color_reverse";

    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarNoNavbar;
    private ListPreference mBatteryBarStyle;
    private CustomSeekBarPreference mBatteryBarThickness;
    private SwitchPreference mBatteryBarChargingAnimation;
    private SwitchPreference mBatteryBarUseChargingColor;
    private SwitchPreference mBatteryBarBlendColor;
    private SwitchPreference mBatteryBarBlendColorReverse;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.battery_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        mBatteryBar = (ListPreference) prefSet.findPreference(PREF_BATT_BAR);
        mBatteryBar.setValue((Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBar.setSummary(mBatteryBar.getEntry());
        mBatteryBar.setOnPreferenceChangeListener(this);

        mBatteryBarNoNavbar = (ListPreference) prefSet.findPreference(PREF_BATT_BAR_NO_NAVBAR);
        mBatteryBarNoNavbar.setValue((Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBarNoNavbar.setSummary(mBatteryBarNoNavbar.getEntry());
        mBatteryBarNoNavbar.setOnPreferenceChangeListener(this);

        mBatteryBarStyle = (ListPreference) prefSet.findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
            Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());
        mBatteryBarStyle.setOnPreferenceChangeListener(this);

        mBatteryBarColor = (ColorPickerPreference) prefSet.findPreference(PREF_BATT_BAR_COLOR);
        intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_COLOR, Color.WHITE);
        hexColor = String.format("#%08x", (0xffffff & intColor));
        mBatteryBarColor.setNewPreviewColor(intColor);
        mBatteryBarColor.setSummary(hexColor);
        mBatteryBarColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingColor = (ColorPickerPreference) prefSet.findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, Color.WHITE);
        hexColor = String.format("#%08x", (0xffffff & intColor));
        mBatteryBarChargingColor.setNewPreviewColor(intColor);
        mBatteryBarChargingColor.setSummary(hexColor);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColor = (ColorPickerPreference) prefSet.findPreference(PREF_BATT_BAR_BATTERY_LOW_COLOR);
        intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, Color.WHITE);
        hexColor = String.format("#%08x", (0xffffff & intColor));
        mBatteryBarBatteryLowColor.setNewPreviewColor(intColor);
        mBatteryBarBatteryLowColor.setSummary(hexColor);
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingAnimation = (SwitchPreference) prefSet.findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
            Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);
        mBatteryBarChargingAnimation.setOnPreferenceChangeListener(this);

        mBatteryBarThickness = (CustomSeekBarPreference) prefSet.findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setValue(Settings.System.getInt(resolver,
            Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1));
        mBatteryBarThickness.setOnPreferenceChangeListener(this);

        mBatteryBarUseChargingColor = (SwitchPreference) findPreference(PREF_BATT_USE_CHARGING_COLOR);
        mBatteryBarBlendColor = (SwitchPreference) findPreference(PREF_BATT_BLEND_COLOR);
        mBatteryBarBlendColorReverse = (SwitchPreference) findPreference(PREF_BATT_BLEND_COLOR_REVERSE);

/*
        boolean hasNavBarByDefault = getResources().getBoolean(
            com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.Secure.getInt(resolver,
            Settings.Secure.NAVIGATION_BAR_VISIBLE, hasNavBarByDefault ? 1 : 0) == 1;
        boolean batteryBarSupported = Settings.Secure.getInt(resolver,
            Settings.Secure.NAVIGATION_BAR_MODE, 0) == 0;
        if (!enableNavigationBar || !batteryBarSupported) {
*/
            prefSet.removePreference(mBatteryBar);
/*
        } else {
            prefSet.removePreference(mBatteryBarNoNavbar);
        }
*/

        updateBatteryBarOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                .parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                .parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                .parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
        } else if (preference == mBatteryBarNoNavbar) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarNoNavbar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR, val);
            mBatteryBarNoNavbar.setSummary(mBatteryBarNoNavbar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val =  (Integer) newValue;
            Settings.System.putInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
            return true;
        } else if (preference == mBatteryBarChargingAnimation) {
            int val = ((Boolean) newValue) ? 1 : 0;
            Settings.System.putInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, val);
            return true;
        }
        return false;
    }

    private void updateBatteryBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.STATUSBAR_BATTERY_BAR, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
            mBatteryBarChargingColor.setEnabled(false);
            mBatteryBarBatteryLowColor.setEnabled(false);
            mBatteryBarUseChargingColor.setEnabled(false);
            mBatteryBarBlendColor.setEnabled(false);
            mBatteryBarBlendColorReverse.setEnabled(false);
        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
            mBatteryBarChargingColor.setEnabled(true);
            mBatteryBarBatteryLowColor.setEnabled(true);
            mBatteryBarUseChargingColor.setEnabled(true);
            mBatteryBarBlendColor.setEnabled(true);
            mBatteryBarBlendColorReverse.setEnabled(true);
        }
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EXTENSIONS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                 @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                     final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.battery_bar;
                    result.add(sir);
                    return result;
                }
                 @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
