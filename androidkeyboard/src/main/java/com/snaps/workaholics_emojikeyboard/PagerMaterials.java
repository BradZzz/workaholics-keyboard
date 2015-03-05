package com.snaps.workaholics_emojikeyboard;

import android.util.Log;

public class PagerMaterials {
	int text_id, drawable_id;
	boolean constrain_image;
	PagerMaterials (int text_id, int drawable_id, boolean constrain_image) {
		this.text_id = text_id;
		this.drawable_id = drawable_id;
		this.constrain_image = constrain_image;
	}
	public int getDrawable() {
		return drawable_id;
	}
	public int getText() {
		return text_id;
	}
	public boolean getConstrain() {
		return constrain_image;
	}
	public void print() {
		Log.i("PagerMaterials","Drawable: " + String.valueOf(drawable_id));
		Log.i("PagerMaterials","Text: " + String.valueOf(text_id));
		Log.i("PagerMaterials","Constrain: " + String.valueOf(constrain_image));
	}
}