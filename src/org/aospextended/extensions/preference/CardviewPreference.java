/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.provider.Settings;
import android.widget.ImageView;
import com.android.settings.R;

public class CardviewPreference extends Preference {

    private final View.OnClickListener mClickListener = v -> performClick(v);

    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;

    private ImageView arrow_icon;

    private int mIconStyle;
    private int mNormalColor;
    private int mAccentColor;

    public CardviewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);

        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        setLayoutResource(R.layout.preference_cardview);
        updateTheme();
    }

    public CardviewPreference(Context context, View view) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);

        arrow_icon = (ImageView) holder.findViewById(R.id.arrow_icon);


        mIconStyle = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.THEMING_SETTINGS_DASHBOARD_ICONS, 0);

        if (arrow_icon != null) {
            Drawable arrow = arrow_icon.getDrawable();
            arrow.setTint(mIconStyle == 2 ? mNormalColor : mAccentColor);
        }
        updateTheme();
    }

    public void updateTheme() {
        int[] attrs = new int[] {
            android.R.attr.colorControlNormal,
            android.R.attr.colorAccent,
        };
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
        mNormalColor = ta.getColor(0, 0xff808080);
        mAccentColor = ta.getColor(1, 0xff808080);
        ta.recycle();

        mIconStyle = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.THEMING_SETTINGS_DASHBOARD_ICONS, 0);

        if (arrow_icon != null) {
            Drawable arrow = arrow_icon.getDrawable();
            arrow.setTint(mIconStyle == 2 ? mNormalColor : mAccentColor);
        }

        Drawable icon = getIcon();
        if (icon != null) {
            if (icon instanceof LayerDrawable) {
                LayerDrawable lIcon = (LayerDrawable) icon;
                if (lIcon.getNumberOfLayers() == 2) {
                    Drawable fg = lIcon.getDrawable(1);
                    Drawable bg = lIcon.getDrawable(0);
                    // Clear tints from previous calls
                    bg.setTintList(null);
                    fg.setTintList(null);
                    int bgc = ((ShapeDrawable) bg).getPaint().getColor();
                    switch (mIconStyle) {
                        case 1:
                            bg.setTint(mAccentColor);
                            break;
                        case 2:
                            fg.setTint(mNormalColor);
                            bg.setTint(0);
                            break;
                        case 3:
                            fg.setTint(mAccentColor);
                            bg.setTint(0);
                            break;
                        case 4:
                            fg.setTint(mAccentColor);
                            bg.setTint(ColorUtils.setAlphaComponent(mAccentColor, 51));
                            break;
                        case 5:
                            fg.setTint(bgc);
                            bg.setTint(ColorUtils.setAlphaComponent(bgc, 51));
                    }
                }
            }
        }
    }
}
