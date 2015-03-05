package com.snaps.workaholics_emojikeyboard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Services {
	private Context context;
	private static String TAG = "Services";
	private ArrayList<EmojiStore> small, large, gifs;
	private Handler offline_callback;
	private String share_text, emoji_fetch_url;
	
	Services (Context context, Handler offline_callback) {
		this.context = context;
		this.small = new ArrayList<EmojiStore>();
		this.large = new ArrayList<EmojiStore>();
		this.gifs = new ArrayList<EmojiStore>();
		this.offline_callback = offline_callback;
		share_text = context.getResources().getString(R.string.default_share);
 		emoji_fetch_url = context.getResources().getString(R.string.default_url);
	}
	
 	public void saveRecord(String path, MixpanelAPI mixpanel){
		try {
		 	JSONObject props = new JSONObject();
			if (path.contains("keyboardGIFStickers")) {
				props.put("Category", "GIF");
			} else if (path.contains("keyboardLargeStickers")) {
				props.put("Category", "Large");
			} else if (path.contains("keyboardSmallStickers")) {
				props.put("Category", "Small");
			}
			props.put("Sharing To", pullTopActivity());
			props.put("File", path.replace("keyboardGIFStickers-", "").replace("keyboardLargeStickers-", "").replace("keyboardSmallStickers-", ""));
			mixpanel.track("android_keyboard_image_copy", props);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
	
 	public static boolean isConnected() {
 		return isOnline();
 	}
 	
	//Check that the user is connected to data/wifi
	private boolean checkOnlineState() {
    ConnectivityManager CManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo NInfo = CManager.getActiveNetworkInfo();
    if (NInfo != null && NInfo.isConnectedOrConnecting()) {
        return true;
    }
    return false;
	}
	
	//Check that the user can access the internet from their data/wifi
	private static Boolean isOnline() {
    try {
        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
        int returnVal = p1.waitFor();
        boolean reachable = (returnVal==0);
        return reachable;
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return false;
	}
 	
	public String pullTopActivity() {
    final PackageManager pm = context.getPackageManager();
    //Get the Activity Manager Object
    ActivityManager aManager = 
    (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
    //Get the list of running Applications
    List<ActivityManager.RunningAppProcessInfo> rapInfoList = aManager.getRunningAppProcesses();
    //Iterate all running apps to get their details
    for (ActivityManager.RunningAppProcessInfo rapInfo : rapInfoList) {
      //error getting package name for this process so move on
      if (rapInfo.pkgList.length == 0)
        continue; 
      try {
        PackageInfo pkgInfo = pm.getPackageInfo(rapInfo.pkgList[0], PackageManager.GET_ACTIVITIES);
        Log.i("Package",pkgInfo.packageName);
        //If the process is not a system process, return the app name as being on top
        //These are the processes this keyboard uses. We don't care about piping data to them
        if(!(pkgInfo.packageName.equals("com.android.systemui") 
        		|| pkgInfo.packageName.equals("com.android.providers.telephony")
        		|| pkgInfo.packageName.equals("com.android.inputmethod.latin")
        		|| pkgInfo.packageName.equals("com.android.smspush")
        		|| pkgInfo.packageName.equals("android")
        		|| pkgInfo.packageName.equals(context.getPackageName())
        		|| pkgInfo.packageName.equals("com.android.quicksearchbox")
        		|| pkgInfo.packageName.equals("com.android.musicfx")
        		|| pkgInfo.packageName.equals("com.android.defcontainer")
        		|| pkgInfo.packageName.equals("com.android.providers.applications")
        		|| pkgInfo.packageName.equals("com.noshufou.android.su")
        		|| pkgInfo.packageName.equals("com.svox.pico")
        		|| pkgInfo.packageName.equals("com.android.voicedialer")
        		|| pkgInfo.packageName.equals("com.android.keychain"))){
        	return pkgInfo.packageName;
        }
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
        Log.d("", "NameNotFoundException :" + rapInfo.pkgList[0]);
      }
    }
    return null;
  }
	public void fetchAssets () {
		scanAssets fetch = new scanAssets(context);
		fetch.execute();
	}
	
	public void fetchOnline () {
		fetchPics fetch = new fetchPics(emoji_fetch_url);
		fetch.execute();
	}
	
 	private class fetchPics extends AsyncTask<String, Void, String> {
 		
 		String url = "";
 		
 		fetchPics (String url) {
 			this.url = url;
 		}
 		
 		private void DownloadTheShit(String location, JSONObject result_object, int callback_message, String[] file_list){
 			JSONArray gif_stickers;
 			try {
 				final ArrayList<String> image_adapter = new ArrayList<String>();
 				gif_stickers = new JSONArray(result_object.get(location).toString());
 				for (int i = 0; i < gif_stickers.length(); i++) {
 					if(!gif_stickers.isNull(i) && gif_stickers.getJSONObject(i).get("image") != null){
 						Log.i("PopUpActivity", "position: " + i + " value: " + gif_stickers.getJSONObject(i).get("image"));
 						image_adapter.add(gif_stickers.getJSONObject(i).get("image").toString());
 					}
 				}
 				downloadMultipleImageTask task = new downloadMultipleImageTask(location, callback_message, file_list);
 				task.execute((String[]) image_adapter.toArray(new String[image_adapter.size()]));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		@Override
 		protected String doInBackground(String... arg0) {
 			try {
 				return new ClassHttpTasks().network_get_task(url);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return "error";
 			}
 		}
 		
 		@Override
     protected void onPostExecute(String response) {
 			Log.i("PopupActivity","Response: " + response);
 			try {
 				//Im guessing this call is expensive. I'm only doing it once
 				String[] file_list = context.fileList();
 				JSONObject result_object = new JSONObject(response);
 				DownloadTheShit("keyboardSmallStickers",result_object, 1, file_list);
 				DownloadTheShit("keyboardLargeStickers",result_object, 2, file_list);
 				DownloadTheShit("keyboardGIFStickers",result_object, 3, file_list);
 				if (result_object.has("keyboardShareText")) {
					share_text = result_object.getString("keyboardAndroidShareText");
					offline_callback.sendEmptyMessage(4);
				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
	
//Make sure that the network is active and working...
  private class downloadMultipleImageTask extends AsyncTask<String, Integer, Void> {
    
    Bitmap downloadedImage;
    URL url;
    String parent_folder = "";
    ArrayList<EmojiStore> valid_locations;
    ArrayList<String> current_files;
    int callback_message;
    
  	
    downloadMultipleImageTask(String parent_folder, int callback_message, String[] file_list){
  		this.parent_folder = parent_folder;
  		this.callback_message = callback_message;
  		valid_locations = new ArrayList<EmojiStore>();
  		current_files = new ArrayList<String>(Arrays.asList(file_list));
  	}
  	
    @Override
    protected void onPreExecute() {}
    
    @Override
    protected Void doInBackground(String... args) {
  		for(String arg : args){
	    	try {
					url = new URL(arg);
					String path = parent_folder + "-" + url.getPath().substring(url.getPath().lastIndexOf('/') + 1, url.getPath().length());
					if (current_files.contains(path)) {
						Log.i(TAG,"File at: " + path + " already exists!");
						valid_locations.add(new EmojiStore(parent_folder, null, path));
					} else {
						Log.i(TAG,"New file at: " + path + ". Creating!");
						downloadedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						//String path = parent_folder + "-" + url.getPath().replace("/", "");
						try {
							FileOutputStream fos = context.openFileOutput(path, Context.MODE_WORLD_READABLE);
							if (parent_folder.equals("keyboardGIFStickers")) {
								BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
								byte[] buffer = new byte[1024];               
                                int bytesRead;
                                while ((bytesRead = bis.read(buffer)) != -1)
                                {
                                  fos.write(buffer, 0, bytesRead);
                                }
                                fos.close();
                                valid_locations.add(new EmojiStore(parent_folder, null, path));
							} else {
                                downloadedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();
                                valid_locations.add(new EmojiStore(parent_folder, null, path));
							}
						} catch (Exception e) {
							Log.e(TAG,e.toString());
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					Log.i(TAG,e.toString());
					e.printStackTrace();
				}
			}
	    return null;
	  }
    
    @Override
    protected void onPostExecute(Void v) {
    	//This puts all the stickers in the right place
    	Log.i(TAG,"Parent Folder: " + parent_folder);
    	Log.i(TAG,"Callback: " + String.valueOf(callback_message));
    	if (parent_folder.equals("keyboardGIFStickers")) {
    		gifs = valid_locations;
    		for (EmojiStore path : gifs) {
	  			Log.i(TAG,"Gif Path: " + path.getPath());
	  		}
    	}
    	if (parent_folder.equals("keyboardSmallStickers")) {
    		small = valid_locations;
    		for (EmojiStore path : small) {
	  			Log.i(TAG,"Small Path: " + path.getPath());
	  		}
    	}
			if (parent_folder.equals("keyboardLargeStickers")) {
				large = valid_locations;
				for (EmojiStore path : large) {
	  			Log.i(TAG,"Large Path: " + path.getPath());
	  		}
			}
    	offline_callback.sendEmptyMessage(callback_message);
    }
  }
 	
	public ArrayList<EmojiStore> getSmall(){
		return small;
	}
	
	public ArrayList<EmojiStore> getLarge(){
		return large;
	}
	
	public ArrayList<EmojiStore> getGif(){
		return gifs;
	}
	
	public String getShareText(){
		return share_text;
	}
	
	private class scanAssets extends AsyncTask<Void, Void, Void> {

 		Context context;
 		
 		public scanAssets(Context context){
 			this.context = context;
 		}

 		private String readFile (String file_name) {
 			String line = "";
 			InputStream inputStream = null;
 			BufferedReader br = null;
 			StringBuilder sb = null;
 			
			try {
				inputStream = context.getAssets().open(file_name);
				br = new BufferedReader(new InputStreamReader(inputStream));
				 
				sb = new StringBuilder();
		 
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			Log.i(TAG,"Data: " + sb.toString());
			return sb.toString();
 		}
 		
 		public void copyDirectoryOneLocationToAnotherLocation(InputStream in, String targetLocation) throws IOException {
	
      FileOutputStream out = context.openFileOutput(targetLocation, Context.MODE_WORLD_READABLE);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
      }
      in.close();
      out.close();

 		}
 		
 		private void transferList (ArrayList<EmojiStore> bucket, String category, JSONObject jsonBrand, JSONObject jsonMapping) {
 			JSONArray jsonList;
			try {
				jsonList = new JSONArray(jsonBrand.get(category).toString());
	 			for (int i = 0; i < jsonList.length(); i++) {
					if(!jsonList.isNull(i) && jsonList.getJSONObject(i).get("image") != null){
						Log.i(TAG, "position: " + i + " value: " + jsonList.getJSONObject(i).get("image").toString());
						Log.i(TAG, "maps to: " + jsonMapping.get(jsonList.getJSONObject(i).get("image").toString()).toString());
						try {
							
							URL url = new URL(jsonList.getJSONObject(i).get("image").toString());
			 				//String path = url.getPath().replace("/", "");
			 				String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1, url.getPath().length());
							
							copyDirectoryOneLocationToAnotherLocation(
									context.getAssets().open("files/" + jsonMapping.get(jsonList.getJSONObject(i).get("image").toString()).toString()), 
									category + "-" + path);
							Log.i(TAG,"Adding to system: " + category + "-" + path);
							bucket.add(new EmojiStore(category, null, category + "-" + path));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e(TAG,e.toString());
							e.printStackTrace();
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
 		
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			Log.i("ServiceEmojiKeyboard","Scanning Assets...");
			try {
				for (String file : context.getAssets().list("files")) {
					Log.i("ServiceEmojiKeyboard","File: " + file);
				}
				//Brand has all the asset categories
				try {
					JSONObject brands = new JSONObject(readFile("files/brand.json"));
					JSONObject brand_assets = new JSONObject(readFile("files/brand-assets.json"));
					small.clear();
					large.clear();
					gifs.clear();
					transferList(small, "keyboardSmallStickers", brands, brand_assets);
					transferList(large, "keyboardLargeStickers", brands, brand_assets);
					transferList(gifs, "keyboardGIFStickers", brands, brand_assets);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
    protected void onPostExecute(Void response) {
			offline_callback.sendEmptyMessage(0);
		}
 		
 	}
}
