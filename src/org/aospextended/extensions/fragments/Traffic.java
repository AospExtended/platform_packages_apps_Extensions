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

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.aospextended.extensions.preference.CustomSeekBarPreference;
import org.aospextended.extensions.preference.SystemSettingSwitchPreference;

public class Traffic extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficType;
    private CustomSeekBarPreference mNetTrafficSize;
    private CustomSeekBarPreference mThreshold;
    private SystemSettingSwitchPreference mShowArrows;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.traffic);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        int sizeValue = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_FONT_SIZE, 26);
        mNetTrafficSize = (CustomSeekBarPreference) findPreference("network_traffic_font_size");
        mNetTrafficSize.setValue(sizeValue / 1);
        mNetTrafficSize.setOnPreferenceChangeListener(this);

        int type = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0, UserHandle.USER_CURRENT);
        mNetTrafficType = (ListPreference) findPreference("network_traffic_type");
        mNetTrafficType.setValue(String.valueOf(type));
        mNetTrafficType.setSummary(mNetTrafficType.getEntry());
        mNetTrafficType.setOnPreferenceChangeListener(this);

        int location = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT);
        mNetTrafficLocation = (ListPreference) findPreference("network_traffic_state");
        mNetTrafficLocation.setValue(String.valueOf(location));
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        int thresholdValue = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(thresholdValue);
        mThreshold.setOnPreferenceChangeListener(this);

        mShowArrows = (SystemSettingSwitchPreference) findPreference("network_traffic_arrow");
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
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE,
                    location, UserHandle.USER_CURRENT);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            mNetTrafficType.setEnabled(netTrafficEnabled());
            mShowArrows.setEnabled(netTrafficEnabled());
            mThreshold.setEnabled(netTrafficEnabled());
            mNetTrafficSize.setEnabled(netTrafficEnabled() && fontResizingAvailable());
            return true;
        } else if (preference == mNetTrafficType) {
            int typeValue = Integer.valueOf((String) objValue);
            int index = mNetTrafficType.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_TYPE,
                    typeValue, UserHandle.USER_CURRENT);
            mNetTrafficType.setSummary(mNetTrafficType.getEntries()[index]);
            mNetTrafficSize.setEnabled(fontResizingAvailable() && netTrafficEnabled());
            return true;
        } else if (preference == mThreshold) {
            int thresholdValue = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD,
                    thresholdValue, UserHandle.USER_CURRENT);
            return true;
        }  else if (preference == mNetTrafficSize) {
            int sizeValue = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_FONT_SIZE,
                    sizeValue, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private boolean netTrafficEnabled() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0) != 0;
    }

    private boolean fontResizingAvailable() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0) != 0;
    }
}
