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

import com.android.internal.util.aospextended.ThemeUtils;

import com.android.settings.R;

import com.android.settingslib.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThemePreference extends Preference {
    private ThemeUtils mThemeUtils;
    private String mCategory;

    private List<String> mLabels;
    private List<String> mPkgs;
    private List<Integer> mColors;
    private List<Typeface> mFonts;
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
        mFonts = mThemeUtils.getFonts();
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

        LinearLayout layout = (LinearLayout) holder.findViewById(R.id.accent_layout);

        int width = getContext().getResources().getDimensionPixelSize(R.dimen.back_gesture_indicator_width);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.dashboard_tile_foreground_image_inset);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width, 1.0f);
        params.gravity = Gravity.FILL_HORIZONTAL;
        params.setMarginStart(margin);
        params.setMarginEnd(margin);

        for (String overlayPackage : mPkgs) {
            Button btn = new Button(getContext());
            if (mCategory.equals(ACCENT_KEY)) {
                btn.setBackgroundDrawable(mThemeUtils.createShapeDrawable("default"));
                btn.setBackgroundTintList(ColorStateList.valueOf(mColors.get(mPkgs.indexOf(overlayPackage))));
            } else if (mCategory.equals(FONT_KEY)) {
                btn.setBackgroundDrawable(mThemeUtils.createShapeDrawable("default"));
                btn.setFocusable(true);
                btn.requestFocus();
                btn.setText(mLabels.get(mPkgs.indexOf(overlayPackage)));
                btn.setTypeface(mFonts.get(mPkgs.indexOf(overlayPackage)));
                btn.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                btn.setSingleLine(true);
                btn.setMarqueeRepeatLimit(-1);
                btn.setSelected(true);
            } else if (mCategory.equals(ICON_SHAPE_KEY)) {
                btn.setBackgroundDrawable(mShapes.get(mPkgs.indexOf(overlayPackage)));
            }
            btn.setLayoutParams(params);
            layout.addView(btn);
            setButton(overlayPackage, btn);
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
            } else if (mCategory.equals(FONT_KEY)) {
                    button.setBackgroundTintList(ColorStateList.valueOf(color));
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
