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
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aospextended.extensions.preference.CustomSeekBarPreference;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

public class MiscExtensions extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";

    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.misc_extensions);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mContext = getContext();
        Resources res = null;
        try {
            res = mContext.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        float displayDensity = getResources().getDisplayMetrics().density;

        // Rounded Corner Radius
        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int cornerRadius = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE, (int) (res.getDimension(resourceIdRadius)/displayDensity));
        mCornerRadius.setValue(cornerRadius / 1);
        mCornerRadius.setOnPreferenceChangeListener(this);
         // Rounded Content Padding
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null, null);
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int contentPadding = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, (int) (res.getDimension(resourceIdPadding)/displayDensity));
        mContentPadding.setValue(contentPadding / 1);
        mContentPadding.setOnPreferenceChangeListener(this);

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
        if (preference == mCornerRadius) {
            int value = (Integer) objValue;
            Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE, value * 1);
        } else if (preference == mContentPadding) {
            int value = (Integer) objValue;
            Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, value * 1);
        }
        return true;
    }
}
