package com.snaps.workaholics_emojikeyboard;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class EmojiGestureDetector extends SimpleOnGestureListener implements OnTouchListener {
	
	private final GestureDetector gestureDetector;

  public EmojiGestureDetector (Context ctx){
      gestureDetector = new GestureDetector(ctx, new GestureListener());
  }
  
  public GestureDetector getGestureDetector(){
      return  gestureDetector;
  }

  private final class GestureListener extends SimpleOnGestureListener {

      private static final int SWIPE_THRESHOLD = 30;
      private static final int SWIPE_VELOCITY_THRESHOLD = 10;
      
      @Override
      public boolean onDown(MotionEvent e) {
      	Log.i("Gest","Down");
        return true;
      }
      
      @Override
      public boolean onDoubleTap(MotionEvent e){
      	Log.i("Gest","Double");
      	propogate();
      	return true;
      }
      
      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      	Log.i("Gest","Fling");
          try {
              float diffY = e2.getY() - e1.getY();
              float diffX = e2.getX() - e1.getX();
              if (Math.abs(diffX) > Math.abs(diffY)) {
                  if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                      if (diffX > 0) {
                        onSwipeRight();
                      } else {
                        onSwipeLeft();
                      }
                  }
              } else {
                  if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                      if (diffY > 0) {
                        onSwipeBottom();
                      } else {
                        onSwipeTop();
                      }
                  }
              }
          } catch (Exception exception) {
              exception.printStackTrace();
          }
          return false;
      }
  }

  public void onSwipeRight() {
  }

  public void onSwipeLeft() {
  }

  public void onSwipeTop() {
  }

  public void onSwipeBottom() {
  }
  
  public void onMove(float x_offset){
  }
  
  public void onDown(float x_offset){
  }
  
  public void notMoving(){
  }
  
  public void propogate(){	
  }

  public boolean onTouch(View v, MotionEvent event) {
  	Log.i("Gesture","Detected!");
  	switch(event.getAction()){
  		case MotionEvent.ACTION_DOWN :
  			Log.i("Gesture","OnDown!");
  			onDown(event.getX());
  			return true;
  		case MotionEvent.ACTION_MOVE :
  			Log.i("Gesture","OnMove!");
  			onMove(event.getX());
  			return true;
  		case MotionEvent.ACTION_UP :
  			notMoving();
  			return true;
  		default:
  			return true;
  	}
  }
}