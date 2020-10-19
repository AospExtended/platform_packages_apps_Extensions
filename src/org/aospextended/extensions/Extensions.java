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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.aospextended.extensions.aexstats.Constants;
import org.aospextended.extensions.aexstats.RequestInterface;
import org.aospextended.extensions.aexstats.models.ServerRequest;
import org.aospextended.extensions.aexstats.models.ServerResponse;
import org.aospextended.extensions.aexstats.models.StatsData;
import org.aospextended.extensions.categories.Lockscreen;
import org.aospextended.extensions.categories.NavigationAndRecents;
import org.aospextended.extensions.categories.NotificationsPanel;
import org.aospextended.extensions.categories.StatusBar;
import org.aospextended.extensions.categories.System;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Extensions extends SettingsPreferenceFragment implements   
       Preference.OnPreferenceChangeListener {

    private static final int MENU_HELP  = 0;
    private SharedPreferences pref;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Resources res = getResources();
        Window win = getActivity().getWindow();

        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        win.setNavigationBarColor(res.getColor(R.color.bottombar_bg));
        win.setNavigationBarDividerColor(res.getColor(R.color.bottombar_bg));

        View view = inflater.inflate(R.layout.layout_extensions, container, false);

        final BottomNavigationView bottomNavigation = (BottomNavigationView) view.findViewById(R.id.bottom_navigation);

    bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
	  public boolean onNavigationItemSelected(MenuItem item) {

             if (item.getItemId() == bottomNavigation.getSelectedItemId()) {

               return false;

             } else {

        if (item.getItemId() == R.id.status_bar_category) {
                switchFrag(new StatusBar());
        } else if (item.getItemId() == R.id.notifications_panel_category) {
                switchFrag(new NotificationsPanel());
        } else if (item.getItemId() == R.id.navigation_and_recents_category) {
                switchFrag(new NavigationAndRecents());
        } else if (item.getItemId() == R.id.lockscreen_category) {
                switchFrag(new Lockscreen());
        } else if (item.getItemId() == R.id.system_category) {
                switchFrag(new System());
        }
        return true;
        }
    }
    });
        

        setHasOptionsMenu(true);
        bottomNavigation.setSelectedItemId(R.id.status_bar_category);
        switchFrag(new StatusBar());
        bottomNavigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        return view;
    }

    private void switchFrag(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.fragment_frame, fragment).commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().setTitle(R.string.extensions_title);
        ContentResolver resolver = getActivity().getContentResolver();
        mCompositeDisposable = new CompositeDisposable();
        pref = getActivity().getSharedPreferences("aexStatsPrefs", Context.MODE_PRIVATE);
        if (!pref.getString(Constants.LAST_EXTENDED_FINGERPRINT, "null").equals(Build.EXTENDED_FINGERPRINT)
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
            stats.setBuildName(stats.getBuildName());
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
            editor.putString(Constants.LAST_EXTENDED_FINGERPRINT, Build.EXTENDED_FINGERPRINT);
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
}

