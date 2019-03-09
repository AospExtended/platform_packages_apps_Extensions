/*
 * Copyright (C) 2018-2019 crDroid Android Project
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
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import org.aospextended.extensions.preference.CustomSeekBarPreference;

import org.aospextended.extensions.R;

public class VolumePanel extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "VolumePanel";

    private static final String KEY_NOTIFICATION = "audio_panel_view_notification";
    private static final String KEY_POSITION = "audio_panel_view_position";
    private static final String KEY_VIEW_TIMEOUT = "audio_panel_view_timeout";

    private SwitchPreference mNotification;
    private SwitchPreference mPosition;
    private CustomSeekBarPreference mViewTimout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.volume_panel);

        boolean isNotificationLinked = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.VOLUME_LINK_NOTIFICATION, 1, UserHandle.USER_CURRENT) != 0;

        if (isNotificationLinked) {
            mNotification = (SwitchPreference) findPreference(KEY_NOTIFICATION);
            mNotification.setSummary(R.string.audio_panel_view_notification_disabled);
            mNotification.setEnabled(false);
        }

        boolean isAudioPanelOnLeft = Settings.System.getIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_POSITION, isAudioPanelOnLeftSide(getActivity()) ? 1 : 0,
                UserHandle.USER_CURRENT) != 0;

        mPosition = (SwitchPreference) findPreference(KEY_POSITION);
        mPosition.setChecked(isAudioPanelOnLeft);

        mViewTimout = (CustomSeekBarPreference) findPreference(KEY_VIEW_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                Settings.System.AUDIO_PANEL_VIEW_TIMEOUT, 3);
        mViewTimout.setValue(currentTimeout);
        mViewTimout.setOnPreferenceChangeListener(this);

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.audio_panel_view_info);
    }

    private static boolean isAudioPanelOnLeftSide(Context context) {
        try {
            Context con = context.createPackageContext("com.android.systemui", 0);
            int id = con.getResources().getIdentifier("config_audioPanelOnLeftSide",
                    "bool", "com.android.systemui");
            return con.getResources().getBoolean(id);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mViewTimout) {
            int viewTimeout = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AUDIO_PANEL_VIEW_TIMEOUT, viewTimeout);
        } else {
            return false;
        }
        return true;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_NOTIFICATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_ALARM, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_VOICE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_BT_SCO, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_POSITION, isAudioPanelOnLeftSide(mContext) ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUDIO_PANEL_VIEW_TIMEOUT, 3, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.VOLUME_LINK_NOTIFICATION, 1, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EXTENSIONS;
    }
}
