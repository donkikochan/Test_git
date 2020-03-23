package com.mverify_reactnative_poc;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.WritableMap;
import com.miteksystems.misnap.misnapworkflow_UX2.MiSnapWorkflowActivity_UX2;
import com.miteksystems.misnap.misnapworkflow_UX2.params.WorkflowApi;
import com.miteksystems.misnap.params.CameraApi;
import com.miteksystems.misnap.params.MiSnapApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import java.util.HashMap;
import java.util.Map;

public class MiSnapManager extends ReactContextBaseJavaModule {

    private Promise mPickerPromise;
    private static ReactApplicationContext reactContext;

    MiSnapManager(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(mActivityEventListener);
    }


    @Override
    public String getName() {
        return "MiSnapManager";
    }

    @ReactMethod
    public void captureDoc(final Promise promise) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            promise.reject("E_ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist");
            return;
        }

        mPickerPromise = promise;

        JSONObject misnapParams = new JSONObject();
        try {
            misnapParams.put(MiSnapApi.MiSnapDocumentType, MiSnapApi.PARAMETER_DOCTYPE_ID_CARD_FRONT);

            // Here you can override optional API parameter defaults
            misnapParams.put(CameraApi.MiSnapAllowScreenshots, 1);
            // e.g. misnapParams.put(MiSnapApi.AppVersion, "1.0");
            // Workflow parameters are now put into the same JSONObject as MiSnap parameters
            misnapParams.put(WorkflowApi.MiSnapTrackGlare, WorkflowApi.TRACK_GLARE_ENABLED);
            //misnapParams.put(CameraApi.MiSnapFocusMode, CameraApi.PARAMETER_FOCUS_MODE_HYBRID_NEW);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            Intent intentMiSnap;
            intentMiSnap = new Intent(currentActivity, MiSnapWorkflowActivity_UX2.class);
            intentMiSnap.putExtra(MiSnapApi.JOB_SETTINGS, misnapParams.toString());
            currentActivity.startActivityForResult(intentMiSnap, MiSnapApi.RESULT_PICTURE_CODE);
        }catch(Exception e){
            mPickerPromise.reject("E_FAILED_TO_SHOW_PICKER", e);
            mPickerPromise = null;
        }

    }


    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == MiSnapApi.RESULT_PICTURE_CODE) {
                if (mPickerPromise != null && intent != null) {
                    Bundle extras = intent.getExtras();
                    String mibiData = extras.getString(MiSnapApi.RESULT_MIBI_DATA);
                    //Log.i("MainActivity", "MIBI: " + mibiData);

                    byte[] image = intent.getByteArrayExtra(MiSnapApi.RESULT_PICTURE_DATA);
                    String base64EncodedImg = Base64.encodeToString(image, Base64.NO_WRAP);

                    WritableMap newDict = Arguments.createMap();

                    newDict.putString("encodedImage",base64EncodedImg);
                    newDict.putString("MiSnapMIBIData",mibiData);

                    if (base64EncodedImg == null) {
                        mPickerPromise.reject("NO DATA FOUND", "No image data found");
                    } else {
                        mPickerPromise.resolve(newDict);
                    }
                    mPickerPromise = null;
                }
            }
        }
    };
}
