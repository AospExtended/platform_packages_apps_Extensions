/*
 * Copyright (C) 2019 AospExtended ROM Project
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
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import android.widget.Switch;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.SwitchBar;

public class Visualizer extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, SwitchBar.OnSwitchChangeListener {

    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";

    private SwitchPreference mAutoColor;
    private SwitchPreference mLavaLamp;

    private Switch mSwitch;

    private PreferenceCategory pc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.visualizer_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        pc = (PreferenceCategory) findPreference("lockscreen_solid_lines_category");

        boolean mLavaLampEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;

        mAutoColor = (SwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setEnabled(!mLavaLampEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mSwitch = switchBar.getSwitch();
        boolean enabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                       Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) ==1;
        mSwitch.setChecked(enabled);
        mAutoColor.setEnabled(enabled);
        mLavaLamp.setEnabled(enabled);
        pc.setEnabled(enabled);
        switchBar.setSwitchBarText(R.string.switch_on_text, R.string.switch_off_text);
        switchBar.addOnSwitchChangeListener(this);
        switchBar.show();
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
		            Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, isChecked ? 1 : 0);
        mAutoColor.setEnabled(isChecked);
        mLavaLamp.setEnabled(isChecked);
        pc.setEnabled(isChecked);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLavaLamp) {
            boolean mLavaLampEnabled = (Boolean) newValue;
            if (mLavaLampEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            mAutoColor.setEnabled(!mLavaLampEnabled);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }
}
