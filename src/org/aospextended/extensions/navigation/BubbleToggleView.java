package org.aospextended.extensions.navigation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.Utils;

import org.aospextended.extensions.navigation.ViewUtils;

/**
 * BubbleToggleView
 *
 */
public class BubbleToggleView extends RelativeLayout {

    private static final String TAG = "BNI_View";
    private static final int DEFAULT_ANIM_DURATION = 300;
    private int mLayoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR;

    private BubbleToggleItem bubbleToggleItem;

    private boolean isActive = false;

    private ImageView iconView;
    private TextView titleView;

    private int animationDuration;
    private boolean showShapeAlways;

    private float maxTitleWidth;
    private float measuredTitleWidth;

    /**
     * Constructors
     */
    public BubbleToggleView(Context context) {
        super(context);
        init(context, null);
    }

    public BubbleToggleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BubbleToggleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        int viewCompatLayoutDirection = layoutDirection == View.LAYOUT_DIRECTION_RTL ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR;
        if (viewCompatLayoutDirection != mLayoutDirection) {
            mLayoutDirection = viewCompatLayoutDirection;
        }

        if (isRtl())
            setGravity(Gravity.START);
        else setGravity(Gravity.CENTER);
    }

    private boolean isRtl() {
        return mLayoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mLayoutDirection);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        mLayoutDirection = ss.mLayoutDirection;
        super.onRestoreInstanceState(ss.mViewPagerSavedState);
    }


    public static class SavedState implements Parcelable {

        private final Parcelable mViewPagerSavedState;
        private final int mLayoutDirection;

        private SavedState(Parcelable viewPagerSavedState, int layoutDirection) {
            mViewPagerSavedState = viewPagerSavedState;
            mLayoutDirection = layoutDirection;
        }

        private SavedState(Parcel in, ClassLoader loader) {
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            mViewPagerSavedState = in.readParcelable(loader);
            mLayoutDirection = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(mViewPagerSavedState, flags);
            out.writeInt(mLayoutDirection);
        }

        // The `CREATOR` field is used to create the parcelable from a parcel, even though it is never referenced directly.
        public static final Parcelable.ClassLoaderCreator<SavedState> CREATOR
                = new Parcelable.ClassLoaderCreator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BubbleToggleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /////////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////////

    /**
     * Initialize
     *
     * @param context current context
     * @param attrs   custom attributes
     */
    private void init(Context context, @Nullable AttributeSet attrs) {
        //initialize default component
        String title = "Title";
        Drawable icon = null;
        Drawable shape = null;
        int shapeColor = Integer.MIN_VALUE;
        int colorActive = ViewUtils.getThemeAccentColor(context);
        int titleColorActive = colorActive;
        int colorInactive = ContextCompat.getColor(context, R.color.default_inactive_color);
        float titleSize = context.getResources().getDimension(R.dimen.default_nav_item_text_size);
        maxTitleWidth = context.getResources().getDimension(R.dimen.default_nav_item_title_max_width);
        float iconWidth = context.getResources().getDimension(R.dimen.default_icon_size);
        float iconHeight = context.getResources().getDimension(R.dimen.default_icon_size);
        int internalPadding = (int) context.getResources().getDimension(R.dimen.default_nav_item_padding);
        int titlePadding = (int) context.getResources().getDimension(R.dimen.default_nav_item_text_padding);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleToggleView, 0, 0);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    icon = ta.getDrawable(R.styleable.BubbleToggleView_extensions_icon);
                else
                icon = AppCompatResources.getDrawable(getContext(), ta.getResourceId(R.styleable.BubbleToggleView_extensions_icon, R.drawable.default_icon));
                iconWidth = ta.getDimension(R.styleable.BubbleToggleView_extensions_iconWidth, iconWidth);
                iconHeight = ta.getDimension(R.styleable.BubbleToggleView_extensions_iconHeight, iconHeight);
                shape = ta.getDrawable(R.styleable.BubbleToggleView_extensions_shape);
                shapeColor = ta.getColor(R.styleable.BubbleToggleView_extensions_shapeColor, shapeColor);
                showShapeAlways = ta.getBoolean(R.styleable.BubbleToggleView_extensions_showShapeAlways, false);
                title = ta.getString(R.styleable.BubbleToggleView_extensions_title);
                titleSize = ta.getDimension(R.styleable.BubbleToggleView_extensions_titleSize, titleSize);
                colorActive = ta.getColor(R.styleable.BubbleToggleView_extensions_colorActive, colorActive);
                titleColorActive = ta.getColor(R.styleable.BubbleToggleView_extensions_titleColorActive, colorActive);
                colorInactive = ta.getColor(R.styleable.BubbleToggleView_extensions_colorInactive, colorInactive);
                isActive = ta.getBoolean(R.styleable.BubbleToggleView_extensions_active, false);
                animationDuration = ta.getInteger(R.styleable.BubbleToggleView_extensions_duration, DEFAULT_ANIM_DURATION);
                internalPadding = (int) ta.getDimension(R.styleable.BubbleToggleView_extensions_padding, internalPadding);
                titlePadding = (int) ta.getDimension(R.styleable.BubbleToggleView_extensions_titlePadding, titlePadding);
            } finally {
                ta.recycle();
            }
        }

        //set the default icon
        if (icon == null)
            icon = ContextCompat.getDrawable(context, R.drawable.default_icon);

        //set the default shape
        if (shape == null)
            shape = ContextCompat.getDrawable(context, R.drawable.transition_background_drawable);

        //create a default bubble item
        bubbleToggleItem = new BubbleToggleItem();
        bubbleToggleItem.setIcon(icon);
        bubbleToggleItem.setShape(shape);
        bubbleToggleItem.setTitle(title);
        bubbleToggleItem.setTitleSize(titleSize);
        bubbleToggleItem.setTitlePadding(titlePadding);
        bubbleToggleItem.setShapeColor(shapeColor);
        bubbleToggleItem.setColorActive(colorActive);
        bubbleToggleItem.setTitleColorActive(titleColorActive);
        bubbleToggleItem.setColorInactive(colorInactive);
        bubbleToggleItem.setIconWidth(iconWidth);
        bubbleToggleItem.setIconHeight(iconHeight);
        bubbleToggleItem.setInternalPadding(internalPadding);

        //set the gravity
        if (isRtl())
            setGravity(Gravity.START);
        else setGravity(Gravity.CENTER);

        //set the internal padding
        setPadding(
                bubbleToggleItem.getInternalPadding(),
                bubbleToggleItem.getInternalPadding(),
                bubbleToggleItem.getInternalPadding(),
                bubbleToggleItem.getInternalPadding());
        post(new Runnable() {
            @Override
            public void run() {
                //make sure the padding is added
                setPadding(
                        bubbleToggleItem.getInternalPadding(),
                        bubbleToggleItem.getInternalPadding(),
                        bubbleToggleItem.getInternalPadding(),
                        bubbleToggleItem.getInternalPadding());
            }
        });

        createBubbleItemView(context);
        setInitialState(isActive);
    }

    /**
     * Create the components of the bubble item view {@link #iconView} and {@link #titleView}
     *
     * @param context current context
     */
    private void createBubbleItemView(Context context) {

        //create the nav icon
        iconView = new ImageView(context);
        iconView.setId(ViewCompat.generateViewId());
        LayoutParams lpIcon = new LayoutParams((int) bubbleToggleItem.getIconWidth(), (int) bubbleToggleItem.getIconHeight());
        lpIcon.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        iconView.setLayoutParams(lpIcon);
        iconView.setImageDrawable(bubbleToggleItem.getIcon());

        //create the nav title
        titleView = new TextView(context);
        LayoutParams lpTitle = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpTitle.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            lpTitle.addRule(RelativeLayout.END_OF, iconView.getId());
        else
            lpTitle.addRule(RelativeLayout.RIGHT_OF, iconView.getId());
        titleView.setLayoutParams(lpTitle);
        titleView.setSingleLine(true);
        titleView.setTextColor(bubbleToggleItem.getTitleColorActive());
        titleView.setText(bubbleToggleItem.getTitle());
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleToggleItem.getTitleSize());
        //get the current measured title width
        titleView.setVisibility(VISIBLE);
        //update the margin of the text view
        titleView.setPadding(bubbleToggleItem.getTitlePadding(), 0, bubbleToggleItem.getTitlePadding(), 0);
        //measure the content width
        titleView.measure(0, 0);       //must call measure!
        measuredTitleWidth = titleView.getMeasuredWidth();  //get width
        //limit measured width, based on the max width
        if (measuredTitleWidth > maxTitleWidth)
            measuredTitleWidth = maxTitleWidth;

        //change the visibility
        titleView.setVisibility(GONE);

        addView(iconView);
        addView(titleView);

        //set the initial state
        setInitialState(isActive);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    ////////////////////////////////

    /**
     * Updates the Initial State
     *
     * @param isActive current state
     */
    public void setInitialState(boolean isActive) {
        //set the background
        setBackground(bubbleToggleItem.getShape());

        if (isActive) {
            ViewUtils.updateDrawableColor(iconView.getDrawable(), bubbleToggleItem.getColorActive());
            this.isActive = true;
            titleView.setVisibility(VISIBLE);
            if (getBackground() instanceof TransitionDrawable) {
                TransitionDrawable trans = (TransitionDrawable) getBackground();
                trans.startTransition(0);
            } else {
                if (!showShapeAlways && bubbleToggleItem.getShapeColor() != Integer.MIN_VALUE)
                    ViewUtils.updateDrawableColor(bubbleToggleItem.getShape(), bubbleToggleItem.getShapeColor());
            }
        } else {
            ViewUtils.updateDrawableColor(iconView.getDrawable(), bubbleToggleItem.getColorInactive());
            this.isActive = false;
            titleView.setVisibility(GONE);
            if (!showShapeAlways) {
                if (!(getBackground() instanceof TransitionDrawable)) {
                    setBackground(null);
                } else {
                    TransitionDrawable trans = (TransitionDrawable) getBackground();
                    trans.resetTransition();
                }
            }
        }
    }

    /**
     * Toggles between Active and Inactive state
     */
    public void toggle() {
        if (!isActive)
            activate();
        else
            deactivate();
    }

    /**
     * Set Active state
     */
    public void activate() {
        ViewUtils.updateDrawableColor(iconView.getDrawable(), bubbleToggleItem.getColorActive());
        isActive = true;
        titleView.setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                titleView.setWidth((int) (measuredTitleWidth * value));
                //end of animation
                if (value >= 1.0f) {
                    //do something
                }
            }
        });
        animator.start();

        if (getBackground() instanceof TransitionDrawable) {
            TransitionDrawable trans = (TransitionDrawable) getBackground();
            trans.startTransition(animationDuration);
        } else {
            //if not showing Shape Always and valid shape color present, use that as tint
            if (!showShapeAlways && bubbleToggleItem.getShapeColor() != Integer.MIN_VALUE)
                ViewUtils.updateDrawableColor(bubbleToggleItem.getShape(), bubbleToggleItem.getShapeColor());
            setBackground(bubbleToggleItem.getShape());
        }
    }

    /**
     * Set Inactive State
     */
    public void deactivate() {
        ViewUtils.updateDrawableColor(iconView.getDrawable(), bubbleToggleItem.getColorInactive());
        isActive = false;
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                titleView.setWidth((int) (measuredTitleWidth * value));
                //end of animation
                if (value <= 0.0f)
                    titleView.setVisibility(GONE);
            }
        });
        animator.start();

        if (getBackground() instanceof TransitionDrawable) {
            TransitionDrawable trans = (TransitionDrawable) getBackground();
            trans.reverseTransition(animationDuration);
        } else {
            if (!showShapeAlways) setBackground(null);
        }
    }

    /**
     * Get the current state of the view
     *
     * @return the current state
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the {@link Typeface} of the {@link #titleView}
     *
     * @param typeface to be used
     */
    public void setTitleTypeface(Typeface typeface) {
        titleView.setTypeface(typeface);
    }

    /**
     * Updates the measurements and fits the view
     *
     * @param maxWidth in pixels
     */
    public void updateMeasurements(int maxWidth) {
        int marginLeft = 0, marginRight = 0;
        ViewGroup.LayoutParams titleViewLayoutParams = titleView.getLayoutParams();
        if (titleViewLayoutParams instanceof LayoutParams) {
            marginLeft = ((LayoutParams) titleViewLayoutParams).rightMargin;
            marginRight = ((LayoutParams) titleViewLayoutParams).leftMargin;
        }

        int newTitleWidth = maxWidth
                - (getPaddingRight() + getPaddingLeft())
                - (marginLeft + marginRight)
                - ((int) bubbleToggleItem.getIconWidth())
                + titleView.getPaddingRight() + titleView.getPaddingLeft();

        //if the new calculate title width is less than current one, update the titleView specs
        if (newTitleWidth > 0 && newTitleWidth < measuredTitleWidth) {
            measuredTitleWidth = titleView.getMeasuredWidth();
        }
    }
}
