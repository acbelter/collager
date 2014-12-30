package com.acbelter.collager.ui;

import com.google.android.gms.ads.AdRequest;

class CollagerAdRequestGenerator {
    private static final boolean DEBUG = false;

    static AdRequest generate() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (DEBUG) {
            builder.addTestDevice("889FDAC4AACBAFB86227BA341B7DBA5C");
            builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        builder.addKeyword("instagram")
                .addKeyword("photo")
                .addKeyword("image")
                .addKeyword("people")
                .addKeyword("facebook")
                .addKeyword("social");
        return builder.build();
    }
}