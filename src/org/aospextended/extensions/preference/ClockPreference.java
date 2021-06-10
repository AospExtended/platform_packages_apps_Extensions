/*
 * Copyright (C) 2018-2019 The Dirty Unicorns Project
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

package org.aospextended.extensions.preference;

import static android.os.UserHandle.USER_SYSTEM;

import android.content.Context;
import android.util.AttributeSet;
import android.content.res.Resources;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.preference.PreferenceViewHolder;
import android.view.ViewGroup.LayoutParams;

import androidx.preference.Preference;

import com.android.internal.util.aospextended.clock.ClockFace;
import com.android.internal.util.aospextended.ThemeUtils;

import com.android.settings.R;

import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;

import com.google.android.flexbox.FlexboxLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClockPreference extends Preference {
    private ThemeUtils mThemeUtils;
    private String mCategory;
    private final LayoutInflater mInflater;
    private List<ClockFace> mClocks;
    private boolean mp = false;

    public ClockPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!mp) {
            Picasso.setSingletonInstance(new Picasso.Builder(context).build());
            mp = true;
        }
        setLayoutResource(R.layout.flexbox);
        mThemeUtils = new ThemeUtils(context);
        mClocks = mThemeUtils.getClocks();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false); // disable parent clik
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

//        FlexboxLayout layout = (FlexboxLayout) holder.findViewById(R.id.flexboxlayout);
        FlexboxLayout layout = (FlexboxLayout) holder.itemView;
//        int width = getContext().getResources().getDimensionPixelSize(R.dimen.battery_meter_height);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.switchbar_subsettings_margin_end);
        //FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(width, width);
        //params.setMargins(margin, margin, margin, margin);

        for (ClockFace clock : mClocks) {
            View clockOptionView = LayoutInflater.from(getContext()).inflate(R.layout.clock_option, layout, false);
            View v = clockOptionView.findViewById(R.id.option_tile);
            ImageView iv = v.findViewById(R.id.clock_option_thumbnail);
            TextView tv = clockOptionView.findViewById(R.id.option_label);
            String title = clock.getTitle();
            String id = clock.getId();
            Uri thumbnail = clock.getThumbnailUri();
            Picasso.get()
                .load(thumbnail)
                .into(iv);
            tv.setText(title);
            FlexboxLayout.LayoutParams params = (FlexboxLayout.LayoutParams) clockOptionView.getLayoutParams();
            params.setMargins(margin, margin, margin, margin);
            clockOptionView.setLayoutParams(params);
//            params.setMargins(margin, margin, margin, margin);
            layout.addView(clockOptionView);
            setButton(clockOptionView, title, id);
        }
    }

    private void setButton(View view, String title, String id) {
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setActivated(!v.isActivated());
                    Settings.Secure.putString(getContext().getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE, id);
                    Toast.makeText(getContext(),
                        title, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
