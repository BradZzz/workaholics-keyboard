package com.snaps.workaholics_emojikeyboard;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.apptentive.android.sdk.Apptentive;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class ActivitySliderContainer extends FragmentActivity {

  private InputMethodManager mImm;
  private ViewPager mPager;
  private PagerAdapter mPagerAdapter;
  //private ArrayList<PagerMaterials> pager_slides;
  private MixpanelAPI mixpanel;
  private PagerStep step;
  private Context context;
  private SliderFragment[] fragment;
  private static String TAG = "ActivitySliderContainer";

  /*private void checkForUpdates() {
    UpdateManager.register(this, String.valueOf(this.getResources().getString(R.string.HOCKEYAPP_TOKEN)));
  }*/
  
  private void checkForCrashes() {
    CrashManager.register(this, String.valueOf(this.getResources().getString(R.string.HOCKEYAPP_TOKEN)));
  }

  @Override
  protected void onStart(){
      super.onStart();
      Apptentive.onStart(this);

      step.setDirty(true);

      /*if (determineSetupStepNumber() > 0) {
          Log.i(TAG,"Step Over 0");
          step.setDirty(true);
      } else {
          Log.i(TAG,"Step = 0");
          step.setDirty(false);
          step.setStep(0);
          mPager.setCurrentItem(0);
      }*/
  }

  @Override
  protected void onStop() {
    super.onStop();
    Apptentive.onStop(this);
  }

  @Override
  protected void onResume() {
  	super.onResume();
  	checkForCrashes();
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    UpdateManager.unregister();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_screen_slide);
 
      //LoginManager.register(this, String.valueOf(this.getResources().getString(R.string.HOCKEYAPP_TOKEN)), 
      //		String.valueOf(this.getResources().getString(R.string.HOCKEYAPP_SECRET)), LoginManager.LOGIN_MODE_EMAIL_PASSWORD, ActivitySliderContainer.class);
      //LoginManager.verifyLogin(this, getIntent());
      
	   	//checkForUpdates();  	 
      context = this;
      
      mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      
      mixpanel = MixpanelAPI.getInstance(this, getResources().getString(R.string.MIXPANEL_TOKEN));
      mixpanel.track("android_container_app_launched", null);
      
      step = new PagerStep();
      
      /*pager_slides = new ArrayList<PagerMaterials>();
      pager_slides.add(new PagerMaterials(R.string.slide_1,R.drawable.android_device_emojis, false));
      pager_slides.add(new PagerMaterials(R.string.slide_2,R.drawable.android_device_stickers, false));
      pager_slides.add(new PagerMaterials(R.string.slide_3,R.drawable.android_device_gif, false));
      pager_slides.add(new PagerMaterials(R.string.slide_4,R.drawable.keyboard_icon, true));*/

      fragment = new SliderFragment[]{
          new SliderFragment(context, new PagerMaterials(R.string.slide_1,R.drawable.android_device_emojis, false), step),
          new SliderFragment(context, new PagerMaterials(R.string.slide_2,R.drawable.android_device_stickers, false), step),
          new SliderFragment(context, new PagerMaterials(R.string.slide_3,R.drawable.android_device_gif, false), step),
          new SliderFragment(context, new PagerMaterials(R.string.slide_4,R.drawable.ic_launcher, true), step),
      };

      mPager = (ViewPager) findViewById(R.id.pager);
      mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
      mPager.setAdapter(mPagerAdapter);

      if (UncachedInputMethodManagerUtils.isThisImeCurrent(this, mImm)) {
          skipTutorial();
      }
  }

  public void skipTutorial (){
      step.setDirty(false);
      step.setStep(determineSetupStepNumber());
      mPager.setAdapter(mPagerAdapter);
      mPager.setCurrentItem(fragment.length-1);
  }

  public void setPosition (int index) {
      if (index >= 0 && index < mPager.getCurrentItem()) {
          mPager.setCurrentItem(index);
      }
  }

  private int determineSetupStepNumber() {
  	if (!UncachedInputMethodManagerUtils.isThisImeEnabled(this, mImm)) {
        Log.i(TAG,"Input Method Not Enabled!");
        return 0;
    } else if (!UncachedInputMethodManagerUtils.isThisImeCurrent(this, mImm)) {
        Log.i(TAG,"Input Method Not Default!");
        return 1;
    }
    Log.i(TAG,"Input Method Enabled and Default!");
    return 2;
  }
  
	@Override
  public void onWindowFocusChanged(final boolean hasFocus) {
    //super.onWindowFocusChanged(hasFocus);
    if (hasFocus && step.getDirty()) {
        if (determineSetupStepNumber() > 0) {
            skipTutorial();
        } else {
            step.setDirty(false);
            step.setStep(0);
            mPager.setCurrentItem(0);
        }
    }
  }
  
  @Override
  public void onBackPressed() {
      if (mPager.getCurrentItem() == 0) {
          super.onBackPressed();
      } else {
          mPager.setCurrentItem(mPager.getCurrentItem()-1);
      }
  }

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
      public ScreenSlidePagerAdapter(FragmentManager fm) {
          super(fm);
      }

      @Override
      public Fragment getItem(int position) {
        Log.i(TAG, "Position: " + position);
        return fragment[position];
      }

      @Override
      public int getCount() {
          return fragment.length;
      }

      @Override
      public void setPrimaryItem (ViewGroup container, int position, Object object) {
          super.setPrimaryItem(container,position, object);
          fragment[position].sendNotificationOnPosition(position);
      }
  }
}
