package com.snaps.workaholics_emojikeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LatinKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    private Context context;
    //private static final int[] excluded_keys = {-1,-5,300,32,10};
    private boolean switched_view;
  	private EmojiGestureDetector detector;
  	//private Handler handler;
    
    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        switched_view = false;
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        switched_view = false;
    }
    
    public void setHandler(Handler handler){
    	//this.handler = handler;
    	init_detector();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
    	detector.getGestureDetector().onTouchEvent(ev);
    	return super.dispatchTouchEvent(ev);   
    }
    
    private void init_detector() {
  		detector = new EmojiGestureDetector(context) {
 		   public void onSwipeLeft() {
		  	 //handler.sendEmptyMessage(10);
		   }
		   public void onSwipeRight() {
		  	 //handler.sendEmptyMessage(11);
		   }
		   public void onSwipeTop() {
		  	 //handler.sendEmptyMessage(12);
		   }
		   public void onSwipeBottom() {
		  	 //handler.sendEmptyMessage(13);
		   }
		   public void propogate(){
		  	 //handler.sendEmptyMessage(14);
		   }
  		};
  	}

    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }
    
    public void switch_view () {
    	switched_view = !switched_view;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
      //This draws the keyboard_background under the keyboard
      Drawable dr = (Drawable) context.getResources().getDrawable(R.drawable.keyboard_background);
      dr.setBounds(getKeyboard().EDGE_LEFT, getKeyboard().EDGE_TOP, getKeyboard().getMinWidth(), getKeyboard().getHeight());
      dr.draw(canvas);
      super.onDraw(canvas);
    }
}