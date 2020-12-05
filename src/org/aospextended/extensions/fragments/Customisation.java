/*
 * Copyright (C) 2018 AospExtended ROM Project
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

package org.aospextended.extensions.fragments;

import static android.os.UserHandle.USER_SYSTEM;
import static android.os.UserHandle.USER_CURRENT;

import android.app.ActivityManagerNative;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import com.android.internal.util.aospextended.AEXUtils;
import com.android.internal.util.aospextended.ThemeUtils;

import org.aospextended.extensions.preference.FontListPreference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Customisation extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Customisation";

    private static final String SYSTEM_THEME_STYLE = "android.theme.customization.theme_style";
    private static final String SYSTEM_ICON_STYLE = "android.theme.customization.icon_pack.android";
    private static final String SYSTEM_FONT_STYLE = "android.theme.customization.font";

    private static final String CUSTOM_CLOCK_FACE = Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE;
    private static final String DEFAULT_CLOCK = "com.android.keyguard.clock.DefaultClockController";

    private ListPreference mSystemThemeStyle;
    private ListPreference mIconPreference;
    private ListPreference mLockClockStyles;

    private FontListPreference mFontPreference;

    private Context mContext;
    private ThemeUtils mThemeUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.customisation);

        mContext = getActivity();
        mThemeUtils = new ThemeUtils(mContext);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

        mSystemThemeStyle = (ListPreference) screen.findPreference(SYSTEM_THEME_STYLE);
        mSystemThemeStyle.setOnPreferenceChangeListener(this);
        updateState(mSystemThemeStyle);

        mIconPreference = (ListPreference) screen.findPreference(SYSTEM_ICON_STYLE);
        mIconPreference.setOnPreferenceChangeListener(this);
        updateState(mIconPreference);

        mFontPreference = (FontListPreference) screen.findPreference(SYSTEM_FONT_STYLE);
        mFontPreference.setOnPreferenceChangeListener(this);
        updateState((ListPreference) mFontPreference);

        mLockClockStyles = (ListPreference) findPreference(CUSTOM_CLOCK_FACE);
        String mLockClockStylesValue = getLockScreenCustomClockFace();
        mLockClockStyles.setValue(mLockClockStylesValue);
        mLockClockStyles.setSummary(mLockClockStyles.getEntry());
        mLockClockStyles.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState(mSystemThemeStyle);
        updateState(mIconPreference);
        updateState((ListPreference) mFontPreference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSystemThemeStyle) {
            String value = (String) newValue;
            mThemeUtils.setThemeEnabled(value);
            return true;
        }
        if (preference == mIconPreference) {
            mThemeUtils.setOverlayEnabled(SYSTEM_ICON_STYLE, (String) newValue);
            return true;
        }
        if (preference == mFontPreference) {
            mThemeUtils.setOverlayEnabled(SYSTEM_FONT_STYLE, (String) newValue);
            return true;
        }
        if (preference == mLockClockStyles) {
            setLockScreenCustomClockFace((String) newValue);
            int index = mLockClockStyles.findIndexOfValue((String) newValue);
            mLockClockStyles.setSummary(mLockClockStyles.getEntries()[index]);
            return true;
        }
        return false;
    }

    public void updateState(ListPreference preference) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey()).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse("Default");

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey());
        List<String> labels = mThemeUtils.getLabels(preference.getKey());

        if (preference == mSystemThemeStyle) {
            pkgs = mThemeUtils.getThemePackages();
            labels = mThemeUtils.getThemeLabels();

            int mCurrentTheme = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SYSTEM_THEME_STYLE, 1, USER_SYSTEM);
            currentPackageName = pkgs.get(mCurrentTheme - 1);
        }

        preference.setEntries(labels.toArray(new String[labels.size()]));
        preference.setEntryValues(pkgs.toArray(new String[pkgs.size()]));
        preference.setValue("Default".equals(currentPackageName) ? pkgs.get(0) : currentPackageName);
        preference.setSummary("Default".equals(currentPackageName) ? "Default" : labels.get(pkgs.indexOf(currentPackageName)));
    }

    private String getLockScreenCustomClockFace() {
        String value = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                CUSTOM_CLOCK_FACE, USER_CURRENT);

        if (value == null || value.isEmpty()) value = DEFAULT_CLOCK;

        try {
            JSONObject json = new JSONObject(value);
            return json.getString("clock");
        } catch (JSONException ex) {
        }
        return value;
    }

    private void setLockScreenCustomClockFace(String value) {
        try {
            JSONObject json = new JSONObject();
            json.put("clock", value);
            Settings.Secure.putStringForUser(mContext.getContentResolver(), CUSTOM_CLOCK_FACE,
                    json.toString(), USER_CURRENT);
        } catch (JSONException ex) {
        }
    }
}
