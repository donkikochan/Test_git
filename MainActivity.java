package com.mverify_reactnative_poc;

import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "mVerify_ReactNative_POC";
  }

  public void hola(){
  	print("hola");
  }
}
