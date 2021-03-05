/*
 * Copyright (C) 2021 AospExtended ROM Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aospextended.support.colorpicker.ColorPickerPreference;

@SearchIndexable
public class MonetEngine extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private String MONET_ENGINE_COLOR_OVERRIDE = "monet_engine_color_override";

    private Context mContext;

    private ColorPickerPreference mMonetColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.monet_settings);

        mContext = getActivity();

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

        mMonetColor = (ColorPickerPreference) screen.findPreference(MONET_ENGINE_COLOR_OVERRIDE);
        int intColor = Settings.Secure.getInt(resolver, MONET_ENGINE_COLOR_OVERRIDE, Color.WHITE);
        String hexColor = String.format("#%08x", (0xffffff & intColor));
        mMonetColor.setNewPreviewColor(intColor);
        mMonetColor.setSummary(hexColor);
        mMonetColor.setOnPreferenceChangeListener(this);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mMonetColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                .parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                MONET_ENGINE_COLOR_OVERRIDE, intHex);
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
                sir.xmlResId = R.xml.monet_settings;
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
