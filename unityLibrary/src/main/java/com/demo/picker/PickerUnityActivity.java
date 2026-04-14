package com.demo.picker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.window.OnBackInvokedDispatcher;

import com.unity3d.player.UnityPlayerGameActivity;

public class PickerUnityActivity extends UnityPlayerGameActivity {
    private static final String TAG = "PickerUnityActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    this::finish
            );
        }
    }

    @Override
    protected String updateUnityCommandLineArguments(String cmdLine) {
        String args = cmdLine == null ? "" : cmdLine.trim();

        if (isEmulator() && !args.contains("-force-gles30")) {
            args = args.isEmpty() ? "-force-gles30" : args + " -force-gles30";
        }

        Log.i(TAG, "Unity args=" + args);
        return args;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PickerPlugin.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("sdk_gphone")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("goldfish")
                || "google_sdk".equals(Build.PRODUCT)
                || Build.PRODUCT.contains("sdk");
    }
}
