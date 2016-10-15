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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

public class Notifications extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String FORCE_EXPANDED_NOTIFICATIONS = "force_expanded_notifications";
    private static final String DISABLE_IMMERSIVE_MESSAGE = "disable_immersive_message";
    private static final String FLASHLIGHT_NOTIFICATION = "flashlight_notification";

    private SwitchPreference mFlashlightNotification;
    private SwitchPreference mForceExpanded;
    private SwitchPreference mDisableIM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notifications);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mFlashlightNotification = (SwitchPreference) findPreference(FLASHLIGHT_NOTIFICATION);
        mFlashlightNotification.setOnPreferenceChangeListener(this);
        if (!Utils.deviceSupportsFlashLight(getActivity())) {
            prefScreen.removePreference(mFlashlightNotification);
        } else {
        mFlashlightNotification.setChecked((Settings.System.getInt(resolver,
                Settings.System.FLASHLIGHT_NOTIFICATION, 0) == 1));
        }

        mForceExpanded = (SwitchPreference) findPreference(FORCE_EXPANDED_NOTIFICATIONS);
        mForceExpanded.setOnPreferenceChangeListener(this);
        int ForceExpanded = Settings.System.getInt(getContentResolver(),
                FORCE_EXPANDED_NOTIFICATIONS, 0);
        mForceExpanded.setChecked(ForceExpanded != 0);

        mDisableIM = (SwitchPreference) findPreference(DISABLE_IMMERSIVE_MESSAGE);
        mDisableIM.setOnPreferenceChangeListener(this);
        int DisableIM = Settings.System.getInt(getContentResolver(),
                DISABLE_IMMERSIVE_MESSAGE, 0);
        mDisableIM.setChecked(DisableIM != 0);

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
            if (preference == mForceExpanded) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), FORCE_EXPANDED_NOTIFICATIONS,
                    value ? 1 : 0);
            return true;
          } else if (preference == mDisableIM) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), DISABLE_IMMERSIVE_MESSAGE,
                    value ? 1 : 0);
            return true;
          } else if  (preference == mFlashlightNotification) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                   Settings.System.FLASHLIGHT_NOTIFICATION, checked ? 1:0);
            return true;
        }
        return false;
    }
}
