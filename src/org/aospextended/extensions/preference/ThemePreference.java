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

import static com.android.internal.util.aospextended.ThemeUtils.ACCENT_KEY;
import static com.android.internal.util.aospextended.ThemeUtils.FONT_KEY;
import static com.android.internal.util.aospextended.ThemeUtils.ICON_SHAPE_KEY;

import android.content.Context;
import android.util.AttributeSet;
import android.content.res.Resources;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.preference.PreferenceViewHolder;
import android.view.ViewGroup.LayoutParams;

import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.util.aospextended.ThemeUtils;

import com.android.settings.R;

import com.android.settingslib.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThemePreference extends Preference {
    private ThemeUtils mThemeUtils;
    private String mCategory;
    private RecyclerView mRecyclerView;
    private List<String> mLabels;
    private List<String> mPkgs;
    private List<Integer> mColors;
    private List<ShapeDrawable> mShapes;

    public ThemePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.theme_preference);
        setIconSpaceReserved(true);
        mThemeUtils = new ThemeUtils(context);
        mCategory = getKey();
        mLabels = mThemeUtils.getLabels(mCategory);
        mPkgs = mThemeUtils.getOverlayPackagesForCategory(mCategory);
        mColors = mThemeUtils.getColors();
        mShapes = mThemeUtils.getShapeDrawables();
        updateSummary();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false); // disable parent clik
        holder.setIsRecyclable(false);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        mRecyclerView = (RecyclerView) holder.itemView.findViewById(R.id.layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        CustomAdapter mCustomAdapter = new CustomAdapter(getContext());
        mRecyclerView.setAdapter(mCustomAdapter);
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        Context context;

        public CustomAdapter(Context context) {
            this.context = context;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_button, parent, false);
            CustomViewHolder vh = new CustomViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, final int position) {
            if (mCategory.equals(ACCENT_KEY)) {
                holder.button.setBackgroundDrawable(mThemeUtils.createShapeDrawable("default"));
                holder.button.setBackgroundTintList(ColorStateList.valueOf(mColors.get(position)));
            } else if (mCategory.equals(ICON_SHAPE_KEY)) {
                holder.button.setBackgroundDrawable(mShapes.get(position));
            }
            setButton(mPkgs.get(position), holder.button);
        }

        @Override
        public int getItemCount() {
            return mPkgs.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            Button button;
            public CustomViewHolder(View itemView) {
                super(itemView);
                button = (Button) itemView.findViewById(R.id.button);
            }
        }
    }

    private void setButton(final String overlayPackage, final Button button) {
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mThemeUtils.setOverlayEnabled(mCategory, overlayPackage);
                }
            });

            String currentPackageName = getEnabledOverlay();
            final boolean isDefault = "Default".equals(currentPackageName) && "android".equals(overlayPackage);
            final int color = ColorUtils.setAlphaComponent(
                     Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorAccent),
                     overlayPackage.equals(currentPackageName) || isDefault ? 170 : 75);

            if (mCategory.equals(ACCENT_KEY)) {
                    button.setText(overlayPackage.equals(currentPackageName) || isDefault ? "\u2713" : null);
            } else if (mCategory.equals(ICON_SHAPE_KEY)) {
                    button.setBackgroundTintList(ColorStateList.valueOf(color));
            }
        }
    }

    public void updateSummary() {
        String currentPackageName = getEnabledOverlay();
        setSummary("Default".equals(currentPackageName) ? "Default"
                : mLabels.get(mPkgs.indexOf(currentPackageName)));
    }

    public String getEnabledOverlay() {
        return mThemeUtils.getOverlayInfos(getKey()).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse("Default");
    }
}
