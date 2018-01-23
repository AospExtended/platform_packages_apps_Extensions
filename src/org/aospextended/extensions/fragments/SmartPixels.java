/*
 * Copyright (C) 2018 CarbonROM
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.SwitchBar;
import org.aospextended.extensions.preference.SystemSettingSwitchPreference;

public class SmartPixels extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, SwitchBar.OnSwitchChangeListener {
    private static final String TAG = "SmartPixels";

    private static final String ON_POWER_SAVE = "smart_pixels_on_power_save";

    private SystemSettingSwitchPreference mSmartPixelsOnPowerSave;

    ContentResolver resolver;

    private Switch mSwitch;

    private PreferenceCategory pc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.smart_pixels);

        resolver = getActivity().getContentResolver();

        pc = (PreferenceCategory) findPreference("smart_pixels_category");

        mSmartPixelsOnPowerSave = (SystemSettingSwitchPreference) findPreference(ON_POWER_SAVE);

        updateDependency();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mSwitch = switchBar.getSwitch();
        boolean enabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                        Settings.System.SMART_PIXELS_ENABLE,
                        0, UserHandle.USER_CURRENT) == 1;
        mSwitch.setChecked(enabled);
        pc.setEnabled(enabled);
        switchBar.setSwitchBarText(R.string.switch_on_text, R.string.switch_off_text);
        switchBar.addOnSwitchChangeListener(this);
        switchBar.show();
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Settings.System.putIntForUser(getActivity().getContentResolver(),
                  Settings.System.SMART_PIXELS_ENABLE,
                  isChecked ? 1 : 0, UserHandle.USER_CURRENT);
        pc.setEnabled(isChecked);
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
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        updateDependency();
        return true;
    }

    private void updateDependency() {
        boolean mUseOnPowerSave = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                0, UserHandle.USER_CURRENT) == 1);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        if (pm.isPowerSaveMode() && mUseOnPowerSave) {
            mSmartPixelsOnPowerSave.setEnabled(false);
        } else {
            mSmartPixelsOnPowerSave.setEnabled(true);
        }
    }

}
