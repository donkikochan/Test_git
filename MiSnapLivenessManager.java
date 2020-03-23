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
import com.miteksystems.facialcapture.science.api.params.FacialCaptureApi;
import com.miteksystems.facialcapture.workflow.FacialCaptureWorkflowActivity;
import com.miteksystems.facialcapture.workflow.params.FacialCaptureWorkflowParameters;
import com.miteksystems.misnap.misnapworkflow_UX2.MiSnapWorkflowActivity_UX2;
import com.miteksystems.misnap.misnapworkflow_UX2.params.WorkflowApi;
import com.miteksystems.misnap.params.CameraApi;
import com.miteksystems.misnap.params.MiSnapApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

public class MiSnapLivenessManager extends ReactContextBaseJavaModule {

        // TODO: Add your license key here
        private static final String LICENSE_KEY = "{\"signature\":\"iQnvsHPRTG5imxgga5Ujs5L71mdn+XMP5qBq6CZqLywlGcXKCOLXCq3GsjhiAbdLs2z2t89nBjb6GO4xn5fSl26xVhkgCY7jFSNCFUxN+ddRWQo+ThHZbDa5iC3bdXUgPqd93vsIjQDbNZs1QJXOUYhtEJIk2nJeit3NUQE37olv7J99TYRc\\/iDIGii8M641dVwb6BfROtqaHirZtJGzqJ6SQ3cKTMeWuWlACgAvW7+sO4lSRzgdLZK9HyedopODOCuEjIu0Q\\/jjSH\\/h+PN4yF7kKP68CERcin+d2\\/x67kSZ3OEzqoFpmim\\/Ilv77gdFWcoEe1UOhj6zKAuUpue\\/uw==\",\"organization\":\"Daon\",\"signed\":{\"features\":[\"ALL\"],\"expiry\":\"2020-09-28 00:00:00\",\"applicationIdentifier\":\"com.icarvision.idmobile\"},\"version\":\"2.1\"}";

        private Promise mPickerPromise;
        private static ReactApplicationContext reactContext;

        MiSnapLivenessManager(ReactApplicationContext context) {
            super(context);
            reactContext = context;
            // Add the listener for `onActivityResult`
            reactContext.addActivityEventListener(mActivityEventListener);
        }


    @Override
    public String getName() {
        return "MiSnapLivenessManager";
    }

    @ReactMethod
    public void captureFace(final Promise promise) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            promise.reject("E_ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist");
            return;
        }

        mPickerPromise = promise;


        JSONObject jjs = new JSONObject();
        try {
            // MiSnap-specific parameters
            jjs.put(CameraApi.MiSnapAllowScreenshots, 1);
            jjs.put(FacialCaptureApi.FacialCaptureLicenseKey, LICENSE_KEY);

            //KIKE PETA
            jjs.put(MiSnapApi.MiSnapImageQuality, 90); //estaba en 99 antes y petaba.
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jjsWorkflow = new JSONObject();
        try {
            // Optionally add in customizable runtime settings for the FacialCapture workflow.
            // NOTE: These don't go into the JOB_SETTINGS because they are for your app, not for core FacialCapture.
            jjsWorkflow.put(FacialCaptureWorkflowParameters.FACIALCAPTURE_WORKFLOW_MESSAGE_DELAY, 500);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            Intent intentFacialCapture = new Intent(currentActivity, FacialCaptureWorkflowActivity.class);
            intentFacialCapture.putExtra(MiSnapApi.JOB_SETTINGS, jjs.toString());
            intentFacialCapture.putExtra(FacialCaptureWorkflowParameters.EXTRA_WORKFLOW_PARAMETERS, jjsWorkflow.toString());
            currentActivity.startActivityForResult(intentFacialCapture, MiSnapApi.RESULT_PICTURE_CODE);

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
