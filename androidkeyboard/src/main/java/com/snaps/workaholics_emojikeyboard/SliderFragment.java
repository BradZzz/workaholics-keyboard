package com.snaps.workaholics_emojikeyboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

public class SliderFragment extends Fragment {
	
	int text_id, drawable_id;
	boolean constrain;
	private float scale;
	private PagerStep step;
	private static String TAG = "SliderFragment";
	private ViewGroup rootView;
	private LinearLayout container_drawable;
	private Context context;
    private ImageView image_element;
    private boolean toggle;

	//PagerMaterials
	public SliderFragment(Context context, PagerMaterials materials, PagerStep step) {
		this.step = step;
		this.text_id = materials.getText();
		this.drawable_id = materials.getDrawable();
		this.constrain = materials.getConstrain();
		this.context = context;
		if (getActivity() != null) {
			scale = getActivity().getResources().getDisplayMetrics().density;
		} else {
			scale = 1;
		}
		materials.print();
	}

	public int dps_to_pix (int dps) {
		return (int) (dps * scale + 0.5f);
	}
	
	public void inflatePopup (String message) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		dialog.setContentView(R.layout.popup_layout);
		TextView text = (TextView) dialog.findViewById(R.id.popup_text);
		text.setText(message);
		Button dialogButton = (Button) dialog.findViewById(R.id.popup_ok);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	/*@Override
	public void onResume() {
		super.onResume();
		if(image_element != null) {
			Log.i(TAG,"ImageView Drawable: " + String.valueOf((Integer)image_element.getTag()));
			Log.i(TAG,"Search Drawable: " + String.valueOf(R.drawable.keyboard_icon));
			if ((Integer) image_element.getTag() == R.drawable.keyboard_icon) {
                if (step.getStep() == 0) {
                    inflatePopup(String.valueOf(context.getText(R.string.toast_1)));
                } else if (step.getStep() == 1) {
                    inflatePopup(String.valueOf(context.getText(R.string.toast_2)));
                } else {
                    inflatePopup(String.valueOf(context.getText(R.string.toast_3)));
                }
			}
		}
	}*/

  public void sendNotificationOnPosition(int position){
      if (position == 3 && toggle) {
          toggle = false;
          if (step.getStep() == 0) {
              inflatePopup(String.valueOf(context.getText(R.string.toast_1)));
          } else if (step.getStep() == 1) {
              inflatePopup(String.valueOf(context.getText(R.string.toast_2)));
          } else {
              inflatePopup(String.valueOf(context.getText(R.string.toast_3)));
          }
      }
  }


  public void createFragmentView(LayoutInflater inflater){

      boolean last_view = this.constrain;
      boolean alt_view = step.getStep() > 1 && last_view;

      if (alt_view) {
          inflater.inflate(R.layout.container_share, rootView, true);
      } else {
          inflater.inflate(R.layout.container_default, rootView, true);
      }

      TextView text_element = (TextView) rootView.findViewById(R.id.container_text);
      text_element.setText(text_id);
      container_drawable = (LinearLayout) rootView.findViewById(R.id.container_drawable);
      image_element = (ImageView) rootView.findViewById(R.id.keyboard_selector);
      image_element.setImageResource(drawable_id);
      image_element.setTag(drawable_id);
      toggle = true;
      if (last_view) {
          Log.i(TAG,"Constrain is met!");
          LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
          if(alt_view) {
              text_element.setVisibility(View.GONE);
              layoutParams.setMargins(dps_to_pix(90), dps_to_pix(120), dps_to_pix(90), 0);
              container_drawable.setLayoutParams(layoutParams);
              setShareButtons();
          } else {
              text_element.setPadding(dps_to_pix(20), dps_to_pix(350), dps_to_pix(20), 0);
              image_element.getLayoutParams().height = dps_to_pix(350);
              image_element.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
              image_element.requestLayout();
              layoutParams.setMargins(dps_to_pix(80), dps_to_pix(40), dps_to_pix(80), 0);
              container_drawable.setLayoutParams(layoutParams);
          }
          image_element.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
              InputMethodManager imeManager;
              switch (step.getStep()) {
                  case 0:
                      final Intent intent = new Intent();
                      intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                      intent.addCategory(Intent.CATEGORY_DEFAULT);
                      startActivity(intent);
                      step.setDirty(true);
                      break;
                  case 1:
                      imeManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                      imeManager.showInputMethodPicker();
                      step.setDirty(true);
                      break;
                  default:
                      inflatePopup(String.valueOf(context.getText(R.string.toast_3)));
                      break;
              }
              }
          });
      }
  }

  public void setShareButtons() {

      //Drawable myIcon = getResources().getDrawable(R.drawable.your_image);
      //myIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
      //((ImageView)findViewById(R.id.view_to_change)).setImageDrawable(myIcon);

      ImageView image_1 = (ImageView) rootView.findViewById(R.id.container_image_1);
      ImageView image_2 = (ImageView) rootView.findViewById(R.id.container_image_2);
      ImageView image_3 = (ImageView) rootView.findViewById(R.id.container_image_3);
      ImageView image_4 = (ImageView) rootView.findViewById(R.id.container_image_4);

      Drawable drawable_1 = image_1.getDrawable();
      Drawable drawable_2 = image_2.getDrawable();
      Drawable drawable_3 = image_3.getDrawable();
      Drawable drawable_4 = image_4.getDrawable();

      int overlay = context.getResources().getColor(R.color.keyboard_preview);

      drawable_1.setColorFilter(overlay, PorterDuff.Mode.SRC_ATOP);
      drawable_2.setColorFilter(overlay, PorterDuff.Mode.SRC_ATOP);
      drawable_3.setColorFilter(overlay, PorterDuff.Mode.SRC_ATOP);
      drawable_4.setColorFilter(overlay, PorterDuff.Mode.SRC_ATOP);

      image_1.setImageDrawable(drawable_1);
      image_2.setImageDrawable(drawable_2);
      image_3.setImageDrawable(drawable_3);
      image_4.setImageDrawable(drawable_4);

      LinearLayout container_1 = (LinearLayout) rootView.findViewById(R.id.container_1);
      container_1.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              ActivitySliderContainer sliderContainer = (ActivitySliderContainer) context;
              sliderContainer.setPosition(0);
          }
      });

      LinearLayout container_2 = (LinearLayout) rootView.findViewById(R.id.container_2);
      container_2.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
              Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
              try {
                  startActivity(goToMarket);
              } catch (ActivityNotFoundException e) {
                  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
              }
          }
      });

      LinearLayout container_3 = (LinearLayout) rootView.findViewById(R.id.container_3);
      container_3.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Apptentive.showMessageCenter((Activity) context);
          }
      });

      LinearLayout container_4 = (LinearLayout) rootView.findViewById(R.id.container_4);
      container_4.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getResources().getString(R.string.privacy_policy)));
          startActivity(browserIntent);
          }
      });

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
      createFragmentView(inflater);
      return rootView;
  }
}