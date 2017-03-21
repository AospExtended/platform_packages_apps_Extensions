/*
 * Copyright (C) 2016 The Pure Nexus Project
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

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import org.aospextended.extensions.preference.SystemSettingSwitchPreference;
import com.android.internal.widget.LockPatternUtils;

import com.android.internal.util.jdcteam.PowerMenuConstants;
import static com.android.internal.util.jdcteam.PowerMenuConstants.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class PowerMenuSettings extends SettingsPreferenceFragment {

    private static final String ACTION_CATEGORY = "action_category";
    private static final String POWER_CATEGORY = "power_category";
    // power items

    private SwitchPreference mRebootPref;
    private SwitchPreference mScreenshotPref;
    private SwitchPreference mScreenRecordPref;
    private SwitchPreference mTorchPref;
    private SwitchPreference mAirplanePref;
    private SwitchPreference mUsersPref;
    private SwitchPreference mSettingsPref;
    private SwitchPreference mLockdownPref;
    private SwitchPreference mAssistPref;
    private SwitchPreference mVoicePref;
    private SwitchPreference mBugReportPref;
    private SwitchPreference mSilentPref;

    Context mContext;
    private ArrayList<String> mLocalUserConfig = new ArrayList<String>();
    private String[] mAvailableActions;
    private String[] mAllActions;

    private static final int MY_USER_ID = UserHandle.myUserId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.powermenu_settings);
        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
 
        final PreferenceCategory actionCategory =
                (PreferenceCategory) prefScreen.findPreference(ACTION_CATEGORY);
        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(POWER_CATEGORY);

        if (!lockPatternUtils.isSecure(MY_USER_ID)) {
            prefScreen.removePreference(powerCategory);
        }

		// power item
        mAvailableActions = getActivity().getResources().getStringArray(
                R.array.power_menu_actions_array);
        mAllActions = PowerMenuConstants.getAllActions();

        for (String action : mAllActions) {
            // Remove preferences not present in the overlay
            if (!isActionAllowed(action)) {
                actionCategory.removePreference(findPreference(action));
                continue;
            }

            if (action.equals(GLOBAL_ACTION_KEY_RESTART)) {
                mRebootPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_RESTART);
            } else if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                mScreenshotPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
            } else if (action.equals(GLOBAL_ACTION_KEY_SCREENRECORD)) {
                mScreenRecordPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SCREENRECORD);
            } else if (action.equals(GLOBAL_ACTION_KEY_TORCH)) {
                mTorchPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_TORCH);
            } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                mAirplanePref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
            } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                mUsersPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_USERS);
            } else if (action.equals(GLOBAL_ACTION_KEY_SETTINGS)) {
                mSettingsPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SETTINGS);
            } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                mLockdownPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
            } else if (action.equals(GLOBAL_ACTION_KEY_ASSIST)) {
                mAssistPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_ASSIST);
            } else if (action.equals(GLOBAL_ACTION_KEY_VOICEASSIST )) {
                mVoicePref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_VOICEASSIST );
            } else if (action.equals(GLOBAL_ACTION_KEY_BUGREPORT)) {
                mBugReportPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_BUGREPORT);
            } else if (action.equals(GLOBAL_ACTION_KEY_SILENT)) {
                mSilentPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SILENT);
            }
        }

        getUserConfig();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onStart() {
        super.onStart();

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory actionCategory =
                (PreferenceCategory) prefScreen.findPreference(ACTION_CATEGORY);

        if (mRebootPref != null) {
            mRebootPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_RESTART));
        }

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mScreenRecordPref != null) {
            mScreenRecordPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENRECORD));
        }

        if (mTorchPref != null) {
            if (!Utils.deviceSupportsFlashLight(getActivity())) {
                actionCategory.removePreference(findPreference(GLOBAL_ACTION_KEY_TORCH));
                mTorchPref = null;
            } else {
                mTorchPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_TORCH));
            }
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                actionCategory.removePreference(findPreference(GLOBAL_ACTION_KEY_USERS));
                mUsersPref = null;
            } else {
                List<UserInfo> users = ((UserManager) mContext.getSystemService(
                        Context.USER_SERVICE)).getUsers();
                boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mSettingsPref != null) {
            mSettingsPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SETTINGS));
        }

        if (mLockdownPref != null) {
            mLockdownPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_LOCKDOWN));
        }

        if (mAssistPref != null) {
            mAssistPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_ASSIST));
        }

        if (mVoicePref != null) {
            mVoicePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_VOICEASSIST));
        }

        if (mBugReportPref != null) {
            mBugReportPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_BUGREPORT));
        }

        if (mSilentPref != null) {
            mSilentPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SILENT));
        }

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean value;

        if (preference == mRebootPref) {
            value = mRebootPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_RESTART);

        } else if (preference == mScreenshotPref) {
            value = mScreenshotPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);

        } else if (preference == mScreenRecordPref) {
            value = mScreenRecordPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENRECORD);

        } else if (preference == mTorchPref) {
            value = mTorchPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_TORCH);

        } else if (preference == mAirplanePref) {
            value = mAirplanePref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);

        } else if (preference == mUsersPref) {
            value = mUsersPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);

        } else if (preference == mSettingsPref) {
            value = mSettingsPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SETTINGS);

        } else if (preference == mLockdownPref) {
            value = mLockdownPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);

        } else if (preference == mAssistPref) {
            value = mAssistPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_ASSIST);

        } else if (preference == mVoicePref) {
            value = mVoicePref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_VOICEASSIST);

        } else if (preference == mBugReportPref) {
            value = mBugReportPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_BUGREPORT);

        } else if (preference == mSilentPref) {
            value = mSilentPref.isChecked();
            updateUserConfig(value, GLOBAL_ACTION_KEY_SILENT);

        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    private boolean settingsArrayContains(String preference) {
        return mLocalUserConfig.contains(preference);
    }

    private boolean isActionAllowed(String action) {
        if (Arrays.asList(mAvailableActions).contains(action)) {
            return true;
        }
        return false;
    }

    private void updateUserConfig(boolean enabled, String action) {
        if (enabled) {
            if (!settingsArrayContains(action)) {
                mLocalUserConfig.add(action);
            }
        } else {
            if (settingsArrayContains(action)) {
                mLocalUserConfig.remove(action);
            }
        }
        saveUserConfig();
    }

    private void updatePreferences() {
        boolean bugreport = Settings.Secure.getInt(getContentResolver(),
            Settings.Secure.BUGREPORT_IN_POWER_MENU, 0) != 0;

        if (mBugReportPref != null) {
            mBugReportPref.setEnabled(bugreport);
            if (bugreport) {
                mBugReportPref.setSummary(null);
            } else {
                mBugReportPref.setSummary(R.string.power_menu_bug_report_disabled);
            }
        }
    }

    private void getUserConfig() {
        mLocalUserConfig.clear();
        String[] defaultActions;
        String savedActions = Settings.Global.getStringForUser(mContext.getContentResolver(),
            Settings.Global.POWER_MENU_ACTIONS, UserHandle.USER_CURRENT);

        if (savedActions == null) {
            defaultActions = mContext.getResources().getStringArray(
                    com.android.internal.R.array.config_globalActionsList);
            for (String action : defaultActions) {
                mLocalUserConfig.add(action);
            }
        } else {
            for (String action : savedActions.split("\\|")) {
                mLocalUserConfig.add(action);
            }
        }
    }

    private void saveUserConfig() {
        StringBuilder s = new StringBuilder();

        // TODO: Use DragSortListView
        ArrayList<String> setactions = new ArrayList<String>();
        for (String action : mAllActions) {
            if (settingsArrayContains(action) && isActionAllowed(action)) {
                setactions.add(action);
            } else {
                continue;
            }
        }

        for (int i = 0; i < setactions.size(); i++) {
            s.append(setactions.get(i).toString());
            if (i != setactions.size() - 1) {
                s.append("|");
            }
        }

        Settings.Global.putStringForUser(getContentResolver(),
            Settings.Global.POWER_MENU_ACTIONS, s.toString(), UserHandle.USER_CURRENT);
        updatePowerMenuDialog();
    }

    private void updatePowerMenuDialog() {
        Intent u = new Intent();
        u.setAction(Intent.UPDATE_POWER_MENU);
        mContext.sendBroadcastAsUser(u, UserHandle.ALL);
    }
}
