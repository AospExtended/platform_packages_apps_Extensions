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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import androidx.preference.PreferenceViewHolder;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;

import com.bumptech.glide.Glide;

import com.android.internal.util.aospextended.clock.ClockFace;
import com.android.internal.util.aospextended.ThemeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.json.JSONObject;
import org.json.JSONException;

public class FODAnimation extends SettingsPreferenceFragment {

    private RecyclerView mRecyclerView;
    private String mPkg;
    private AnimationDrawable animation;

    private String[] mAnims = {
        "fod_miui_normal_recognizing_anim",
        "fod_miui_aod_recognizing_anim",
        "fod_miui_aurora_recognizing_anim",
        "fod_miui_aurora_cas_recognizing_anim",
        "fod_miui_light_recognizing_anim",
        "fod_miui_pop_recognizing_anim",
        "fod_miui_pulse_recognizing_anim",
        "fod_miui_pulse_recognizing_white_anim",
        "fod_miui_rhythm_recognizing_anim",
        "fod_miui_star_cas_recognizing_anim",
        "fod_op_cosmos_recognizing_anim",
        "fod_op_energy_recognizing_anim",
        "fod_op_mclaren_recognizing_anim",
        "fod_op_ripple_recognizing_anim",
        "fod_op_scanning_recognizing_anim",
        "fod_op_stripe_recognizing_anim",
        "fod_op_wave_recognizing_anim",
        "fod_pureview_dna_recognizing_anim",
        "fod_pureview_future_recognizing_anim",
        "fod_pureview_halo_ring_recognizing_anim",
        "fod_pureview_molecular_recognizing_anim",
        "fod_rog_fusion_recognizing_anim",
        "fod_rog_pulsar_recognizing_anim",
        "fod_rog_supernova_recognizing_anim",
    };

    private String[] mAnimPreviews = {
        "gxzw_normal_recognizing_anim_20",
        "gxzw_aod_recognizing_anim_20",
        "gxzw_aurora_recognizing_anim_30",
        "gxzw_aurora_cas_recognizing_anim_30",
        "gxzw_light_recognizing_anim_20",
        "gxzw_pop_recognizing_anim_10",
        "gxzw_pulse_recognizing_anim_15",
        "gxzw_pulse_recognizing_anim_white_18",
        "gxzw_rhythm_recognizing_anim_15",
        "gxzw_star_cas_recognizing_anim_30",
        "fod_op_cosmos_anim_60",
        "fod_op_energy_anim_35",
        "fod_op_mclaren_anim_45",
        "fod_op_ripple_anim_30",
        "fod_op_scanning_anim_18",
        "fod_op_stripe_anim_18",
        "fod_op_wave_anim_25",
        "fod_pureview_dna_anim_25",
        "fod_pureview_future_anim_35",
        "fod_pureview_halo_ring_anim_35",
        "fod_pureview_molecular_anim_30",
        "asus_fod_anim_1_10",
        "asus_fod_anim_2_35",
        "asus_fod_anim_3_25",
    };

    private String[] mTitles = {
        "MIUI default",
        "MIUI AOD",
        "Aurora (K30 Pro)",
        "Aurora (Mi 10 Ultra)",
        "Light",
        "Pop",
        "Pulse",
        "Pulse (White)" ,
        "Rhythm",
        "Starlight",
        "Cosmos",
        "Energy",
        "McLaren",
        "Ripple",
        "Scanning (Cyberpunk 2077)",
        "Stripe",
        "Wave",
        "DNA",
        "Future",
        "Halo ring" ,
        "Molecular",
        "Fusion",
        "Pulsar",
        "Supernova",
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.fod_recog_animation_effect_title);

        mPkg = getActivity().getResources().getString(com.android.internal.R.string.config_fodAnimationPackage);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.item_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.layout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        FODAnimAdapter mFODAnimAdapter = new FODAnimAdapter(getActivity());
        mRecyclerView.setAdapter(mFODAnimAdapter);

        return view;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public class FODAnimAdapter extends RecyclerView.Adapter<FODAnimAdapter.FODAnimViewHolder> {
        Context context;
        String mSelectedAnim;
        String mAppliedAnim;

        public FODAnimAdapter(Context context) {
            this.context = context;
        }

        @Override
        public FODAnimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
            FODAnimViewHolder vh = new FODAnimViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(FODAnimViewHolder holder, final int position) {
            String animName = mAnims[position];

            Glide.with(holder.image.getContext())
                    .load("")
                    .placeholder(getDrawable(holder.image.getContext(), mAnimPreviews[position]))
                    .into(holder.image);

            holder.name.setText(mTitles[position]);

            if (position == Settings.System.getInt(context.getContentResolver(),
                Settings.System.FOD_ANIM, 0)) {
                mAppliedAnim = animName;
                if (mSelectedAnim == null) {
                    mSelectedAnim = animName;
                }
            }

            holder.itemView.setActivated(animName == mSelectedAnim);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateActivatedStatus(mSelectedAnim, false);
                    updateActivatedStatus(animName, true);
                    mSelectedAnim = animName;
                    holder.image.setBackgroundDrawable(getDrawable(v.getContext(), mAnims[position]));
                    animation = (AnimationDrawable) holder.image.getBackground();
                    animation.setOneShot(true);
                    animation.start();
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.FOD_ANIM, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAnims.length;
        }

        public class FODAnimViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;
            public FODAnimViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.option_label);
                image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
            }
        }

        private void updateActivatedStatus(String anim, boolean isActivated) {
            int index = Arrays.asList(mAnims).indexOf(anim);
            if (index < 0) {
                return;
            }
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null && holder.itemView != null) {
                holder.itemView.setActivated(isActivated);
            }
        }
    }

    public Drawable getDrawable(Context context, String drawableName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pm.getResourcesForApplication(mPkg);
            return res.getDrawable(res.getIdentifier(drawableName, "drawable", mPkg));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
