/*
 * Copyright (C) 2019 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aospextended.extensions.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.aospextended.extensions.preference.PackageListAdapter;
import org.aospextended.extensions.preference.PackageListAdapter.PackageItem;

import android.widget.Switch;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.SwitchBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamingMode extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener, SwitchBar.OnSwitchChangeListener {

    private static final int DIALOG_GAMING_APPS = 1;
    private static final String GAMING_MODE_HW_KEYS = "gaming_mode_hw_keys_toggle";

    private SwitchPreference mHardwareKeysDisable;

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mGamingPrefList;
    private Preference mAddGamingPref;
    private PreferenceCategory mGaming;

    private Switch mSwitch;

    private String mGamingPackageList;
    private Map<String, Package> mGamingPackages;
    private Context mContext;

    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_APP_SWITCH = 0x10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get launch-able applications
        addPreferencesFromResource(R.xml.gaming_mode_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHardwareKeysDisable = (SwitchPreference) findPreference(GAMING_MODE_HW_KEYS);

        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());

        mGaming = (PreferenceCategory) findPreference("gamingmode_category");

        if (!hasHWkeys()) {
            mGaming.removePreference(mHardwareKeysDisable);
        }

        mGamingPrefList = (PreferenceGroup) findPreference("gamingmode_applications");
        mGamingPrefList.setOrderingAsAdded(false);

        mGamingPackages = new HashMap<String, Package>();

        mAddGamingPref = findPreference("add_gamingmode_packages");

        mAddGamingPref.setOnPreferenceClickListener(this);

        mContext = getActivity().getApplicationContext();

        SettingsObserver observer = new SettingsObserver(new Handler(Looper.getMainLooper()));
        observer.observe();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mSwitch = switchBar.getSwitch();
        boolean enabled = Settings.System.getInt(getActivity().getContentResolver(),
                       Settings.System.GAMING_MODE_ENABLED, 0) == 1;
        mSwitch.setChecked(enabled);
        switchBar.setSwitchBarText(R.string.switch_on_text, R.string.switch_off_text);
        switchBar.addOnSwitchChangeListener(this);
        switchBar.show();
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
            Settings.System.putInt(getActivity().getContentResolver(),
		            Settings.System.GAMING_MODE_ENABLED, isChecked ? 1 : 0);
        mGaming.setEnabled(isChecked);
        mGamingPrefList.setEnabled(isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomApplicationPrefs();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_GAMING_APPS) {
            return MetricsEvent.EXTENSIONS;
        }
        return 0;
    }

    /**
     * Utility classes and supporting methods
     */
    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        switch (id) {
            case DIALOG_GAMING_APPS:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mGamingPackages);
                        dialog.cancel();
                    }
                });
        }
        return dialog;
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.GAMING_MODE_ACTIVE), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(
                                   Settings.System.GAMING_MODE_ACTIVE))) {
                boolean enable = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.GAMING_MODE_ACTIVE, 0) == 1;
            }
        }
    }

    /**
     * Application class
     */
    private static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    };

    private boolean hasHWkeys() {
        final int deviceKeys = getContext().getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        return (hasHomeKey || hasBackKey || hasMenuKey || hasAppSwitchKey);
    }

    private void refreshCustomApplicationPrefs() {
        if (!parsePackageList()) {
            return;
        }

        // Add the Application Preferences
        if (mGamingPrefList != null) {
            mGamingPrefList.removeAll();

            for (Package pkg : mGamingPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mGamingPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }
            // Keep these at the top
            mAddGamingPref.setOrder(0);
            // Add 'add' options
            mGamingPrefList.addPreference(mAddGamingPref);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mAddGamingPref) {
            showDialog(DIALOG_GAMING_APPS);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (preference == mGamingPrefList.findPreference(preference.getKey())) {
                                removeApplicationPref(preference.getKey(), mGamingPackages);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            builder.show();
        }
        return true;
    }

    private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
        Package pkg = map.get(packageName);
        if (pkg == null) {
            pkg = new Package(packageName);
            map.put(packageName, pkg);
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private Preference createPreferenceFromInfo(Package pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                PackageManager.GET_META_DATA);
        Preference pref =
                new Preference(getActivity());

        pref.setKey(pkg.name);
        pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
        pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    private void removeApplicationPref(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private boolean parsePackageList() {
        boolean parsed = false;

        final String gamingModeString = Settings.System.getString(getContentResolver(),
                Settings.System.GAMING_MODE_VALUES);

        if (!TextUtils.equals(mGamingPackageList, gamingModeString)) {
            mGamingPackageList = gamingModeString;
            mGamingPackages.clear();
            parseAndAddToMap(gamingModeString, mGamingPackages);
            parsed = true;
        }

        return parsed;
    }

    private void parseAndAddToMap(String baseString, Map<String,Package> map) {
        if (baseString == null) {
            return;
        }

        final String[] array = TextUtils.split(baseString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
    }


    private void savePackageList(boolean preferencesUpdated, Map<String,Package> map) {
        String setting = map == mGamingPackages ? Settings.System.GAMING_MODE_VALUES : Settings.System.GAMING_MODE_DUMMY;

        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
            if (TextUtils.equals(setting, Settings.System.GAMING_MODE_VALUES)) {
                mGamingPackageList = value;
            }
        }
        Settings.System.putString(getContentResolver(),
                setting, value);
    }
}
