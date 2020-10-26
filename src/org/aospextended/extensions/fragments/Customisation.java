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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Customisation extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Customisation";

    private static final String SYSTEM_ICON_STYLE = "android.theme.customization.icon_pack.android";

    private ListPreference mAccentPreference;
    private ListPreference mFontPreference;
    private ListPreference mIconShapePreference;
    private ListPreference mIconPreference;

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

        mAccentPreference = (ListPreference) screen.findPreference(ACCENT_KEY);
        mAccentPreference.setOnPreferenceChangeListener(this);
        updateState(mAccentPreference);

        mFontPreference = (ListPreference) screen.findPreference(ACCENT_KEY);
        mFontPreference.setOnPreferenceChangeListener(this);
        updateState(mFontPreference);

        mIconShapePreference = (ListPreference) screen.findPreference(ACCENT_KEY);
        mIconShapePreference.setOnPreferenceChangeListener(this);
        updateState(mIconShapePreference);

        mIconPreference = (ListPreference) screen.findPreference(SYSTEM_ICON_STYLE);
        mIconPreference.setOnPreferenceChangeListener(this);
        updateState(mIconPreference);
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
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
            mThemeUtils.setOverlayEnabled(preference.getKey(), (String) newValue);
            return true;
    }

    public void updateState(ListPreference preference) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey()).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse("Default");

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey());
        List<String> labels = mThemeUtils.getLabels(preference.getKey());

        preference.setEntries(labels.toArray(new String[labels.size()]));
        preference.setEntryValues(pkgs.toArray(new String[pkgs.size()]));
        preference.setValue("Default".equals(currentPackageName) ? pkgs.get(0) : currentPackageName);
        preference.setSummary("Default".equals(currentPackageName) ? "Default" : labels.get(pkgs.indexOf(currentPackageName)));
    }
}
