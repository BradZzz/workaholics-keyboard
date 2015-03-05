package com.snaps.workaholics_emojikeyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

public class AdapterEmoji extends BaseAdapter {
	private Context self;
	private final ArrayList<EmojiStore> mobileValues;
	private ImageView imageView;
	private Handler handler;
	private static String TAG = "Adapter Emoji";
	//private static boolean online;
 
	public AdapterEmoji(Context context, ArrayList<EmojiStore> mobileValues, Handler handler) {
		Log.i("AdapterEmoji","Init");
		this.handler = handler;
		this.self = context;
		this.mobileValues = mobileValues;
		//online = Services.isConnected();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView = null;
        String mobile = mobileValues.get(position).getAssetPath() != null ? mobileValues.get(position).getAssetPath() : mobileValues.get(position).getPath();

        if (convertView == null || convertView.getTag() != mobile) {
            LayoutInflater inflater = (LayoutInflater) self.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Log.i(TAG, "Path: " + mobile);
            if (mobile.contains("keyboardSmallStickers")) {
                gridView = inflater.inflate(R.layout.grid_object_small, parent, false);
                imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
                File imgFile = self.getFileStreamPath(mobile);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                } else {
                    Log.i("Emoji Adapter", "Error finding system resource: " + mobile);
                }
            } else if (mobile.contains("keyboardLargeStickers")) {
                gridView = inflater.inflate(R.layout.grid_object_large, parent, false);
                imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
                File imgFile = self.getFileStreamPath(mobile);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                } else {
                    Log.i(TAG, "Error finding system resource: " + mobile);
                }
            } else {
                gridView = inflater.inflate(R.layout.grid_object_gifs, parent, false);
                imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
                Log.i(TAG, "file://" + self.getFilesDir().getAbsolutePath() + "/" + mobile);
                //TODO: This is a hack to get over gifs not being rendered locally without internet. Need to fix when Ion updates it's library
                //if (!online) {
                //Log.i(TAG,"Not Connected");
                Ion.with(imageView).load("file://" + self.getFilesDir().getAbsolutePath() + "/" + mobile);
                //} else {
                //	Log.i(TAG,"Connected!");
                //	Ion.with(imageView).load(self.getResources().getString(R.string.default_asset_base_url)
                //			+ mobile.replace("keyboardGIFStickers-", "").replace("keyboardLargeStickers-", "").replace("keyboardSmallStickers-", ""));
                //}
            }

            Animation anim = AnimationUtils.loadAnimation(self.getApplicationContext(), R.anim.fly_in_from_center);
            anim.setDuration(600 + 1 * getRandom(300));
            imageView.setAnimation(anim);
            imageView.bringToFront();
            anim.start();
            gridView.setTag(mobile);
        } else {
            gridView = (View) convertView;
        }

		return gridView;
	}
	
	private int getRandom(int range) {
		Random rand = new Random(); 
		return rand.nextInt(range) + 1;
	}
	
	@Override
	public int getCount() {
		Log.i("AdapterEmoji","Count");
		Log.i("AdapterEmoji","Count: " + mobileValues.size());
		return mobileValues.size();
	}
 
	@Override
	public Object getItem(int position) {
		Log.i("AdapterEmoji","GetItem");
		Log.i("AdapterEmoji","GetItem: " + position);
		return mobileValues.get(position);
	}
 
	@Override
	public long getItemId(int position) {
		//The inputmethod fucks up the onclick listener for the items have to catch here...
		Log.i("AdapterEmoji","GetId");
		Log.i("AdapterEmoji","GetId: " + position);
		handler.sendEmptyMessage(position);
		return 0;
	}
 
}