package org.aospextended.extensions.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class TelephonyUtils {

    private static final String TAG = TelephonyUtils.class.getSimpleName();

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isVoiceCapable();
    }
}