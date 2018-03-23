package org.aospextended.extensions.aexstats.models;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.aospextended.extensions.aexstats.Constants;

/**
 * Created by ishubhamsingh on 25/9/17.
 */

public class StatsData {

    private String device;
    private String model;
    private String version;
    private String buildType;
    private String countryCode;
    private String buildDate;

    public String getDevice() {
        return SystemProperties.get(Constants.KEY_DEVICE);
    }

    public void setDevice(String device) {
        this.device = TextUtils.isEmpty(device) ? "unknown" : device;
    }

    public String getModel() {
        return SystemProperties.get(Constants.KEY_MODEL);
    }

    public void setModel(String model) {
        this.model = TextUtils.isEmpty(model) ? "unknown" : model;
    }

    public String getVersion() {
        return Constants.KEY_VERSION;
    }

    public void setVersion(String version) {
        this.version = TextUtils.isEmpty(version) ? "unknown" : version;
    }

    public String getBuildType() {
        return SystemProperties.get(Constants.KEY_BUILD_TYPE);
    }

    public void setBuildType(String buildType) {
        this.buildType = TextUtils.isEmpty(buildType) ? "unknown" : buildType;
    }

    public String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso();
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = TextUtils.isEmpty(countryCode) ? "unknown" : countryCode;
    }

    public String getBuildDate() {
        return SystemProperties.get(Constants.KEY_BUILD_DATE);
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = TextUtils.isEmpty(buildDate) ? "unknown" : buildDate;
    }
}