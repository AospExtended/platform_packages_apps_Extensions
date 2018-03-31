/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.SummaryLoader;

import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;

import org.aospextended.extensions.aexstats.Constants;
import org.aospextended.extensions.aexstats.RequestInterface;
import org.aospextended.extensions.aexstats.models.ServerRequest;
import org.aospextended.extensions.aexstats.models.ServerResponse;
import org.aospextended.extensions.aexstats.models.StatsData;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PartsFragment extends SettingsPreferenceFragment implements   
       Preference.OnPreferenceChangeListener {

    private static final int MENU_HELP  = 0;
    private SharedPreferences pref;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extensions);
        setHasOptionsMenu(true);
        ContentResolver resolver = getActivity().getContentResolver();
        mCompositeDisposable = new CompositeDisposable();
        pref = getActivity().getSharedPreferences("aexStatsPrefs", Context.MODE_PRIVATE);
        if (!pref.getString(Constants.LAST_BUILD_DATE, "null").equals(SystemProperties.get(Constants.KEY_BUILD_DATE))
                || pref.getBoolean(Constants.IS_FIRST_LAUNCH, true)) {
            pushStats();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    }

    private void pushStats() {
        //Anonymous Stats

        if (!TextUtils.isEmpty(SystemProperties.get(Constants.KEY_DEVICE))) { //Push only if installed ROM is AEX
            RequestInterface requestInterface = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(RequestInterface.class);

            StatsData stats = new StatsData();
            stats.setDevice(stats.getDevice());
            stats.setModel(stats.getModel());
            stats.setVersion(stats.getVersion());
            stats.setBuildType(stats.getBuildType());
            stats.setCountryCode(stats.getCountryCode(getActivity()));
            stats.setBuildDate(stats.getBuildDate());
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.PUSH_OPERATION);
            request.setStats(stats);
            mCompositeDisposable.add(requestInterface.operation(request)
                    .observeOn(AndroidSchedulers.mainThread(),false,100)
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::handleResponse, this::handleError));
        } else {
            Log.d(Constants.TAG, "This ain't AEX!");
        }

    }

    private void handleResponse(ServerResponse resp) {

        if (resp.getResult().equals(Constants.SUCCESS)) {

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(Constants.IS_FIRST_LAUNCH, false);
            editor.putString(Constants.LAST_BUILD_DATE, SystemProperties.get(Constants.KEY_BUILD_DATE));
            editor.apply();
            Log.d(Constants.TAG, "push successful");

        } else {
            Log.d(Constants.TAG, resp.getMessage());
        }

    }

    private void handleError(Throwable error) {

        Log.d(Constants.TAG, error.toString());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EXTENSIONS;
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
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_HELP, 0, R.string.extensions_dialog_title)
                .setIcon(R.drawable.ic_extensions_info)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HELP:
                showDialogInner(MENU_HELP);
                Toast.makeText(getActivity(),
                (R.string.extensions_dialog_toast),
                Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case MENU_HELP:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.extensions_dialog_title)
                    .setMessage(R.string.extensions_dialog_message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                mSummaryLoader.setSummary(this, mContext.getString(R.string.build_tweaks_summary_title));
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

}

