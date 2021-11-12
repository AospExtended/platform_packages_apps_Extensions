
/*
 * Copyright (C) 2017 AospExtended ROM Project
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

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.aospextended.AEXUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aospextended.support.preference.SecureSettingSwitchPreference;

@SearchIndexable
public class MiscExtensions extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_STATUS_BAR_LOGO = "status_bar_logo";
    private static final String LOCATION_DEVICE_CONFIG = "location_indicators_enabled";
    private static final String CAMERA_DEVICE_CONFIG = "camera_indicators_enabled";
    private static final String LOCATION_INDICATOR = "enable_location_privacy_indicator";
    private static final String CAMERA_INDICATOR = "enable_camera_privacy_indicator";

    private static final String COMBINED_SIGNAL_ICONS = "combined_status_bar_signal_icons";

    private SwitchPreference mShowAexLogo;
    private SwitchPreference mEnableCombinedSignalIcons;
    private SecureSettingSwitchPreference mLocationIndicator;
    private SecureSettingSwitchPreference mCamIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.misc_extensions);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mShowAexLogo = (SwitchPreference) findPreference(KEY_STATUS_BAR_LOGO);
        mShowAexLogo.setChecked((Settings.System.getInt(getContentResolver(),
             Settings.System.STATUS_BAR_LOGO, 0) == 1));
        mShowAexLogo.setOnPreferenceChangeListener(this);

        mEnableCombinedSignalIcons = (SwitchPreference) findPreference(COMBINED_SIGNAL_ICONS);
        mEnableCombinedSignalIcons.setChecked((Settings.Secure.getInt(getContentResolver(),
             Settings.Secure.SHOW_COMBINED_STATUS_BAR_SIGNAL_ICONS, 0) == 1));
        mEnableCombinedSignalIcons.setOnPreferenceChangeListener(this);

        mLocationIndicator = (SecureSettingSwitchPreference) findPreference(LOCATION_INDICATOR);
        boolean locIndicator = DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_PRIVACY,
                LOCATION_DEVICE_CONFIG, false);
        mLocationIndicator.setDefaultValue(locIndicator);
        mLocationIndicator.setChecked(Settings.Secure.getInt(resolver,
                LOCATION_INDICATOR, locIndicator ? 1 : 0) == 1);

        mCamIndicator = (SecureSettingSwitchPreference) findPreference(CAMERA_INDICATOR);
        boolean camIndicator = DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_PRIVACY,
                CAMERA_DEVICE_CONFIG, false);
        mCamIndicator.setDefaultValue(camIndicator);
        mCamIndicator.setChecked(Settings.Secure.getInt(resolver,
                CAMERA_INDICATOR, camIndicator ? 1 : 0) == 1);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if  (preference == mShowAexLogo) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, value ? 1 : 0);
            return true;
        } else if  (preference == mEnableCombinedSignalIcons) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SHOW_COMBINED_STATUS_BAR_SIGNAL_ICONS, value ? 1 : 0);
            AEXUtils.showSystemUiRestartDialog(getActivity());
            return true;
        }
        return false;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.misc_extensions;
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
