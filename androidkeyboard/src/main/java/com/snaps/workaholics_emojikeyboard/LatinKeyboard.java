package com.snaps.workaholics_emojikeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;

public class LatinKeyboard extends Keyboard {

		//private Context context;
		private float scale;
		//private int height;
	
    public LatinKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        scale = context.getResources().getDisplayMetrics().density;
        //this.context = context;
    }

    public LatinKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        scale = context.getResources().getDisplayMetrics().density;
        //this.context = context;
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }
    
    public int dps_to_pix (int dps) {
  		return (int) (dps * scale + 0.5f);
  	}
    
    /*@Override
    public int getHeight() {
      return dps_to_pix(this.height);
    }*/
    
    public void changeKeyHeight(double height_modifier)
    {
       int height = 0;
       for(Keyboard.Key key : getKeys()) {
          key.height *= height_modifier;
          key.y *= height_modifier;
          height = key.height;
       }
       setKeyHeight(height);
       getNearestKeys(0, 0); //somehow adding this fixed a weird bug where bottom row keys could not be pressed if keyboard height is too tall.. from the Keyboard source code seems like calling this will recalculate some values used in keypress detection calculation
    }
    
    static class LatinKey extends Key {
        
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }
        
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }

}