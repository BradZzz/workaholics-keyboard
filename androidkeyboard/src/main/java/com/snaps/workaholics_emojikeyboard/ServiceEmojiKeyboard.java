package com.snaps.workaholics_emojikeyboard;

import java.util.ArrayList;
import java.util.List;

import net.hockeyapp.android.CrashManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseAnalytics;

/*
 * TODO: All this shit needs to be put into a service after the keyboardview is overwritten
 */

public class ServiceEmojiKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
	
  private GridView gridView;
	private Context self;
	private AdapterEmoji emojis, stickers, gifs;
	private ProgressBar progressBar;
	//private String emoji_fetch_url;
	private float scale;
	private final static int column_count = 3;
	private int current_column;
	private EmojiGestureDetector detector;
	private View layout;
	private static String TAG = "ServiceEmojiKeyboard";
	
	//private static final boolean DEBUG = false;
  private static final int emojiButton = 300;
  private MixpanelAPI mixpanel;
  
  /**
   * This boolean indicates the optional example code for performing
   * processing of hard keys in addition to regular text generation
   * from on-screen interaction.  It would be used for input methods that
   * perform language translations (such as converting text entered on 
   * a QWERTY keyboard to Chinese), but may not be used for input methods
   * that are primarily intended to be used for on-screen text entry.
   */
  static final boolean PROCESS_HARD_KEYS = true;
  
  private LatinKeyboardView mInputView;
  //private CandidateView mCandidateView;
  private CompletionInfo[] mCompletions;
  
  private StringBuilder mComposing = new StringBuilder();
  private boolean mPredictionOn;
  private boolean mCompletionOn;
  private int mLastDisplayWidth;
  private boolean mCapsLock;
  private long mLastShiftTime;
  private long mMetaState;
  
  private Keyboard mSymbolsKeyboard;
  private Keyboard mSymbolsShiftedKeyboard;
  private Keyboard mQwertyKeyboard;
  
  private String mWordSeparators;
	private LinearLayout emoji, nav;
	private String share_text;
	private boolean sending_image;
	private Services keyboardService;
	
	Handler clickRedundancyHandler = new Handler() {
	  @Override
	  public void handleMessage(Message msg) {
	  	if (!sending_image){
		  	sending_image = true;
		  	Log.i("Click!","String: " + gridView.getAdapter().getItem(msg.what));
		  	EmojiStore store = ((EmojiStore) gridView.getAdapter().getItem(msg.what));
		  	String streaming_item = store.getAssetPath() != null ? store.getAssetPath() : store.getPath();
		  	//Check if the string is a gif url for now...
				/*if ( streaming_item.substring(streaming_item.lastIndexOf('.')).toLowerCase().equals(".gif") && !streaming_item.contains("keyboardGIFStickers")) {
					Log.i("ServiceEmojiKeyboard","Download1");
					new downloadSingleImageTask().execute(streaming_item);
				} else {*/
				Log.i("ServiceEmojiKeyboard","Download3");
				sendImage(streaming_item);
				//}
	  	}
	  }
  };
	
  Handler offlineCallback = new Handler() {
	  @Override
	  public void handleMessage(Message msg) {
	  	switch(msg.what){
	  		//The user is offline. Everything is loaded at once
		  	case 0:
		  		//This means that the user is offline and needs to load the cached assets
		  		emojis = new AdapterEmoji(self, keyboardService.getSmall(),clickRedundancyHandler);
		  		stickers = new AdapterEmoji(self, keyboardService.getLarge(),clickRedundancyHandler);
		  		gifs = new AdapterEmoji(self, keyboardService.getGif(),clickRedundancyHandler);
		  		//initializeButtons();
		  		//initializeButtons(1);
		  		//initializeButtons(2);
		  		gridView.setAdapter(emojis);
		  		gridView.setNumColumns(6);
		  		gridView.setOnItemClickListener(new OnItemClickListener() {
	 					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	 						if (!sending_image){
	 					  	sending_image = true;
		 						EmojiStore store = ((EmojiStore) gridView.getAdapter().getItem(position));
		 				  	String streaming_item = store.getAssetPath() != null ? store.getAssetPath() : store.getPath();
	 							sendImage(streaming_item);
	 						}
	 					}
	 				});
		  		gridView.setVisibility(View.VISIBLE);
		     	progressBar.setVisibility(View.GONE);
		  		break;
		  	//The user is online. Load in bundles
		  	case 1:
		  		//Small
		  		//for (EmojiStore path : keyboardService.getSmall()) {
		  		//	Log.i(TAG,"Small Path: " + path.getPath());
		  		//}
		  		emojis = new AdapterEmoji(self, keyboardService.getSmall(),clickRedundancyHandler);
		  		
		  		if (current_column == msg.what-1) {
			  		gridView.setVisibility(View.VISIBLE);
			     	progressBar.setVisibility(View.GONE);
			     	gridView.setNumColumns(6);
			     	set_current_column(current_column);
		  		}
		     	
		     	//gridView.setColumnWidth(dps_to_pix(SMALL_WIDTH));
		  		break;
		  	case 2:
		  		//Large
		  		//for (EmojiStore path : keyboardService.getLarge()) {
		  		//	Log.i(TAG,"Large Path: " + path.getPath());
		  		//}
		  		stickers = new AdapterEmoji(self, keyboardService.getLarge(),clickRedundancyHandler);
		  		//initializeButtons(1);
		  		if (current_column == msg.what-1) {
			  		gridView.setVisibility(View.VISIBLE);
			     	progressBar.setVisibility(View.GONE);
			     	set_current_column(current_column);
		  		}
		  		break;
		  	case 3:
		  		//Gifs
		  		//for (EmojiStore path : keyboardService.getGif()) {
		  		//	Log.i(TAG,"Gif Path: " + path.getPath());
		  		//}
		  		gifs = new AdapterEmoji(self, keyboardService.getGif(),clickRedundancyHandler);
		  		//initializeButtons(2);
		  		if (current_column == msg.what-1) {
			  		gridView.setVisibility(View.VISIBLE);
			     	progressBar.setVisibility(View.GONE);
			     	set_current_column(current_column);
		  		}
		  		break;
		  	case 4:
		  		//ShareString
		  		share_text = keyboardService.getShareText();
		  		gridView.setOnItemClickListener(new OnItemClickListener() {
	 					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	 						if (!sending_image){
	 					  	sending_image = true;
		 						EmojiStore store = ((EmojiStore) gridView.getAdapter().getItem(position));
		 				  	String streaming_item = store.getAssetPath() != null ? store.getAssetPath() : store.getPath();
	 							sendImage(streaming_item);
	 						}
	 					}
	 				});
		  		break;
	  	}
	  }
  };

  private void checkForCrashes() {
    CrashManager.register(this, String.valueOf(this.getResources().getString(R.string.HOCKEYAPP_TOKEN)));
  }
  
	@Override
  public void onInitializeInterface() {
      // Get InputMethodManager object.
  }
	
	@Override
  public View onCreateInputView() {
      // Create IME layout.
 			self = this;
 			
 			mixpanel = MixpanelAPI.getInstance(this, getResources().getString(R.string.MIXPANEL_TOKEN));
 			
 			ParseAnalytics.trackAppOpenedInBackground(new Intent(this, InputMethodService.class));
 			
      layout = getLayoutInflater().inflate(R.layout.emoji_keyboard, null);
      
      progressBar = (ProgressBar) layout.findViewById(R.id.progressbar);
   		
   		scale = this.getApplicationContext().getResources().getDisplayMetrics().density;
   		current_column = 0;
   		
   		//So the images only load once on startup
   		emojis = null;
   		stickers = null;
   		gifs = null;
   		
 			keyboardService = new Services(self, offlineCallback);
 			//keyboardService.scanAssets();
   		
   		mInputView = (LatinKeyboardView) layout.findViewById(R.id.keyboard);
	   	nav = (LinearLayout) layout.findViewById(R.id.emoji);
	   	emoji = (LinearLayout) layout.findViewById(R.id.gesture_layout);
	   	 
	   	makeKeyboards();
	   	mInputView.setHandler(clickRedundancyHandler);
	   	mInputView.setOnKeyboardActionListener(this);
	   	mInputView.setKeyboard(mQwertyKeyboard);
   		
   		final Button emoji_keyboard = (Button) layout.findViewById(R.id.emoji_keyboard);
   		emoji_keyboard.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {

          	 emoji.setVisibility(View.GONE);
          	 nav.setVisibility(View.GONE);
          	 mInputView.setVisibility(View.VISIBLE);
          	 
           }
       });
   		
   		final Button emoji_share = (Button) layout.findViewById(R.id.emoji_share);
   		emoji_share.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
          	 ExtractedText txt= getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(),0);
	           	if (txt != null && !String.valueOf(txt.text).contains(share_text)) {
	           		mixpanel.track("android_keyboard_inline_share", null);
	           		getCurrentInputConnection().commitText(share_text, share_text.length());
	           		//mixpanel.track("android_keyboard_inline_share", null);
	           	} else {
	           		Log.i("Service Keyboard","Already shared!");
	           	}
           }
       });
   		
   		gridView = (GridView) layout.findViewById(R.id.gridView1);
   		
   		init_detector();
   		emoji.setOnTouchListener(detector);
   		share_text = keyboardService.getShareText();

   		if (Services.isConnected()) {
   			notifyToast(self,"Loading Assets");
   			keyboardService.fetchOnline();
   		} else {
   			//Use the cached assets if we can't download them now...
   			notifyToast(self,"Keyboard requires internet access, please connect and try again");
   			keyboardService.fetchAssets();
   		}
   		
   		initializeButtons();
      return layout;
  }
	
   @Override
   public void onFinishInputView(boolean finishingInput) {
       super.onFinishInputView(finishingInput);
   }
   
   @Override
   public void onWindowShown() {
       // Reset IME
  	 super.onWindowShown();
     sending_image = false;
     mixpanel.track("android_keyboard_launched", null);
  	 Log.i(TAG,"Window Shown");
  	 checkForCrashes();
   }

   @Override
   public void hideWindow()
   {
  	 Log.i(TAG,"hideWindow");
  	 super.hideWindow();
   }

   private void makeKeyboards() {
     if (mQwertyKeyboard != null) {
         // Configuration changes can happen after the keyboard gets recreated,
         // so we need to be able to re-build the keyboards if the available
         // space has changed.
         int displayWidth = getMaxWidth();
         if (displayWidth == mLastDisplayWidth) return;
         mLastDisplayWidth = displayWidth;
     }
     mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
     mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
     mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
 }
 	
 	private void init_detector() {
 		detector = new EmojiGestureDetector(self) {
 		   public void onSwipeTop() {
 		   }
 		   public void onSwipeRight() {
 		  	 set_current_column(current_column-1);
 		   }
 		   public void onSwipeLeft() {
 		  	 set_current_column(current_column+1);
 		   }
 		   public void onSwipeBottom() {
 		   }
 		   public void onDown(float x_offset){
 		   } 
 		   public void onMove(float x_offset){   
 		   }
 		   public void notMoving(){
 		   }
 		};
 		
 		gridView.setOnTouchListener(new GridView.OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent ev) {
				// TODO Auto-generated method stub
				detector.getGestureDetector().onTouchEvent(ev);
				return false;
			}
		});
 		
 	}
 	
 	private void sendImage(String path){
 		 Uri screenshotUri = Uri.parse("file://" + self.getFilesDir().getAbsolutePath() + "/" + path);
	   String topApp = keyboardService.pullTopActivity();
	   keyboardService.saveRecord(path, mixpanel);
	   //Keep this log here. If the top is wrong, it's good to know about it
	   Log.i("Keying","stacktop: " + topApp);
	   Intent sendIntent = new Intent(Intent.ACTION_SEND); 
	   sendIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
	   sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   sendIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   sendIntent.setType("image/*");
	   
	   // Check.
	   final PackageManager pm = getPackageManager();
	   final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);
	   
	   for (ResolveInfo info : resolveInfos) {
	  	 Log.i("ServiceEmojiKeyboard","Good Process: " + info.activityInfo.packageName);
	     //Make sure that the top application can take emoji input in the first place. If not, stop the process
	     //It would be weird if another activity was inflated when a gif was shared
	     if (topApp.equals(info.activityInfo.packageName)) {
	       sendIntent.setPackage(info.activityInfo.packageName);
	       startActivity(sendIntent);
	       break;
	     }
	   }
	   sending_image = false;
	   //sendIntent.setPackage(topApp);
	   //startActivity(sendIntent);
 	}
 	
 	public void notifyToast(final Context context, final String message)
 	{
 	    Handler handler = new Handler(Looper.getMainLooper());
 	    handler.post(
 	      new Runnable()
 	      {
           @Override
           public void run()
           {
             Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
           }
 	      }
 	    );
 	}
 	
 	public int dps_to_pix (int dps) {
 		return (int) (dps * scale + 0.5f);
 	}
 	
 	public void set_current_column (int current_index) {
 		this.current_column = current_index;
 		if (this.current_column > column_count - 1) {
 			this.current_column = column_count - 1;
 		} else if (this.current_column < 0) {
 			this.current_column = 0;
 		}
 		Log.i("Emoji","CurrentColumn: " + this.current_column);
 		switch (this.current_column) {
 			case 0:
 				final RadioButton emoji_button = (RadioButton) layout.findViewById(R.id.emoji_small);
 				emoji_button.setChecked(true);
 				if (emojis != null) {
 					gridView.setVisibility(View.VISIBLE);
		     	progressBar.setVisibility(View.GONE);
 	 				gridView.setNumColumns(6);
 	       	gridView.invalidateViews();
 	       	emojis.notifyDataSetChanged();
 	       	gridView.setAdapter(emojis);
 				} else {
 					//this.current_column = column_placeholder;
 					gridView.setVisibility(View.GONE);
		     	progressBar.setVisibility(View.VISIBLE);
 				}
 				break;
 			case 1:
 				final RadioButton stickers_button = (RadioButton) layout.findViewById(R.id.emoji_large);
 				stickers_button.setChecked(true);
 				if (stickers != null) {
 					gridView.setVisibility(View.VISIBLE);
		     	progressBar.setVisibility(View.GONE);
	 				gridView.setNumColumns(5);
	       	gridView.invalidateViews();
	       	stickers.notifyDataSetChanged();
	       	gridView.setAdapter(stickers);
 				} else {
 					//this.current_column = column_placeholder;
 					gridView.setVisibility(View.GONE);
		     	progressBar.setVisibility(View.VISIBLE);
 				}
 				break;
 			case 2:
 				final RadioButton gifs_button = (RadioButton) layout.findViewById(R.id.emoji_gif);
 	  		gifs_button.setChecked(true);
 				if (gifs != null) {
 					gridView.setVisibility(View.VISIBLE);
		     	progressBar.setVisibility(View.GONE);
	 	  		gridView.setNumColumns(2);
	 				gridView.invalidateViews();
	       	gifs.notifyDataSetChanged();
	       	gridView.setAdapter(gifs);
 				} else {
 					//this.current_column = column_placeholder;
 					gridView.setVisibility(View.GONE);
		     	progressBar.setVisibility(View.VISIBLE);
 				}
 				break;
 		}
 	}
   
   private void initializeButtons(){
  	 //switch(which_button) {
  	 //case 0:
   	 	final Button emoji_button = (Button) layout.findViewById(R.id.emoji_small);
   		emoji_button.setEnabled(true);
   		emoji_button.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
           	set_current_column(0);
           }
       });
   	 //	 break;
  	 //case 1:
   		final Button stickers_button = (Button) layout.findViewById(R.id.emoji_large);
   		stickers_button.setEnabled(true);
   		stickers_button.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
           	set_current_column(1);
           }
       });
  	//	 break;
  	// case 2:
  		 final Button gifs_button = (Button) layout.findViewById(R.id.emoji_gif);
	  		gifs_button.setEnabled(true);
	  		gifs_button.setOnClickListener(new View.OnClickListener() {
	          public void onClick(View v) {
	          	set_current_column(2);
	          }
	      });
  	//	 break;
  	// }
   }

   /**
    * This is the main point where we do our initialization of the input method
    * to begin operating on an application.  At this point we have been
    * bound to the client, and are now receiving all of the detailed information
    * about the target of our edits.
    */
   @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
       super.onStartInputView(attribute, restarting);
       
       // Reset our state.  We want to do this even if restarting, because
       // the underlying state of the text editor could have changed in any way.
       mComposing.setLength(0);
       updateCandidates();
       
       if (!restarting) {
           // Clear shift states.
           mMetaState = 0;
       }
       
       mPredictionOn = false;
       mCompletionOn = false;
       mCompletions = null;
       Keyboard keyboard;
       
       // We are now going to initialize our state based on the type of
       // text being edited.
       switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
           case EditorInfo.TYPE_CLASS_NUMBER:
           case EditorInfo.TYPE_CLASS_DATETIME:
               // Numbers and dates default to the symbols keyboard, with
               // no extra features.
               keyboard = mSymbolsKeyboard;
               break;
               
           case EditorInfo.TYPE_CLASS_PHONE:
               // Phones will also default to the symbols keyboard, though
               // often you will want to have a dedicated phone keyboard.
               keyboard = mSymbolsKeyboard;
               break;
               
           case EditorInfo.TYPE_CLASS_TEXT:
               // This is general text editing.  We will default to the
               // normal alphabetic keyboard, and assume that we should
               // be doing predictive text (showing candidates as the
               // user types).
               keyboard = mQwertyKeyboard;
               mPredictionOn = true;
               
               // We now look for a few special variations of text that will
               // modify our behavior.
               int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
               if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) {
                   // Do not display predictions / what the user is typing
                   // when they are entering a password.
                   mPredictionOn = false;
               }
               
               if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                       || variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                   // Our predictions are not useful for e-mail addresses
                   // or URIs.
                   mPredictionOn = false;
               }
               
               if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                   // If this is an auto-complete text view, then our predictions
                   // will not be shown and instead we will allow the editor
                   // to supply their own.  We only show the editor's
                   // candidates when in fullscreen mode, otherwise relying
                   // own it displaying its own UI.
                   mPredictionOn = false;
                   mCompletionOn = isFullscreenMode();
               }
               
               // We also want to look at the current state of the editor
               // to decide whether our alphabetic keyboard should start out
               // shifted.
               updateShiftKeyState(attribute);
               break;
               
           default:
               // For all unknown input types, default to the alphabetic
               // keyboard with no special features.
               keyboard = mQwertyKeyboard;
       }
       
       // Apply the selected keyboard to the input view.
       if (mInputView != null) {
           mInputView.setKeyboard(keyboard);
           mInputView.closing();
       }
   }
   
   /**
    * This is called when the user is done editing a field.  We can use
    * this to reset our state.
    */
   @Override public void onFinishInput() {
       super.onFinishInput();
       
       // Clear current composing text and candidates.
       mComposing.setLength(0);
       updateCandidates();
       
       // We only hide the candidates window when finishing input on
       // a particular editor, to avoid popping the underlying application
       // up and down if the user is entering text into the bottom of
       // its window.
       setCandidatesViewShown(false);
       
       Log.i("ServiceEmojiKeyboard","OnFinishInput");
       if (mInputView != null) {
         mInputView.closing();
       }
       //super.hideWindow();
   }
   
   /**
    * Deal with the editor reporting movement of its cursor.
    */
   @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
           int newSelStart, int newSelEnd,
           int candidatesStart, int candidatesEnd) {
       
       // If the current selection in the text view changes, we should
       // clear whatever candidate text we have.
       if (mComposing.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
           mComposing.setLength(0);
           updateCandidates();
           InputConnection ic = getCurrentInputConnection();
           if (ic != null) {
               ic.finishComposingText();
           }
       }
   }

   /**
    * This tells us about completions that the editor has determined based
    * on the current text in it.  We want to use this in fullscreen mode
    * to show the completions ourself, since the editor can not be seen
    * in that situation.
    */
   @Override public void onDisplayCompletions(CompletionInfo[] completions) {
       if (mCompletionOn) {
           mCompletions = completions;
           if (completions == null) {
               setSuggestions(null, false, false);
               return;
           }
           
           List<String> stringList = new ArrayList<String>();
           for (int i=0; i<(completions != null ? completions.length : 0); i++) {
               CompletionInfo ci = completions[i];
               if (ci != null) stringList.add(ci.getText().toString());
           }
           setSuggestions(stringList, true, true);
       }
   }
   
   /**
    * This translates incoming hard key events in to edit operations on an
    * InputConnection.  It is only needed when using the
    * PROCESS_HARD_KEYS option.
    */
   private boolean translateKeyDown(int keyCode, KeyEvent event) {
       mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState, keyCode, event);
       int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
       mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
       InputConnection ic = getCurrentInputConnection();
       if (c == 0 || ic == null) {
           return false;
       }
       
       boolean dead = false;

       if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
           dead = true;
           c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
       }
       
       if (mComposing.length() > 0) {
           char accent = mComposing.charAt(mComposing.length() -1 );
           int composed = KeyEvent.getDeadChar(accent, c);

           if (composed != 0) {
               c = composed;
               mComposing.setLength(mComposing.length()-1);
           }
       }
       
       onKey(c, null);
       
       return true;
   }
   
   /**
    * Use this to monitor key events being delivered to the application.
    * We get first crack at them, and can either resume them or let them
    * continue to the app.
    */
   @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
       switch (keyCode) {
           case KeyEvent.KEYCODE_BACK:
               // The InputMethodService already takes care of the back
               // key for us, to dismiss the input method if it is shown.
               // However, our keyboard could be showing a pop-up window
               // that back should dismiss, so we first allow it to do that.
               if (event.getRepeatCount() == 0 && mInputView != null) {
                   if (mInputView.handleBack()) {
                       return true;
                   }
               }
               break;
               
           case KeyEvent.KEYCODE_DEL:
               // Special handling of the delete key: if we currently are
               // composing text for the user, we want to modify that instead
               // of let the application to the delete itself.
               if (mComposing.length() > 0) {
                   onKey(Keyboard.KEYCODE_DELETE, null);
                   return true;
               }
               break;
               
           default:
               // For all other keys, if we want to do transformations on
               // text being entered with a hard keyboard, we need to process
               // it and do the appropriate action.
               if (PROCESS_HARD_KEYS) {
                   if (mPredictionOn && translateKeyDown(keyCode, event)) {
                       return true;
                   }
               }
       }
       
       return super.onKeyDown(keyCode, event);
   }

   /**
    * Use this to monitor key events being delivered to the application.
    * We get first crack at them, and can either resume them or let them
    * continue to the app.
    */
   @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
       // If we want to do transformations on text being entered with a hard
       // keyboard, we need to process the up events to update the meta key
       // state we are tracking.
       if (PROCESS_HARD_KEYS) {
           if (mPredictionOn) {
               mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                       keyCode, event);
           }
       }
       
       return super.onKeyUp(keyCode, event);
   }

   /**
    * Helper function to commit any text being composed in to the editor.
    */
   private void commitTyped(InputConnection inputConnection) {
       if (mComposing.length() > 0) {
           inputConnection.commitText(mComposing, mComposing.length());
           mComposing.setLength(0);
           updateCandidates();
       }
   }

   /**
    * Helper to update the shift state of our keyboard based on the initial
    * editor state.
    */
   private void updateShiftKeyState(EditorInfo attr) {
       if (attr != null && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
           int caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
           mInputView.setShifted(mCapsLock || caps != 0);
       }
   }
   
   /**
    * Helper to determine if a given character code is alphabetic.
    */
   private boolean isAlphabet(int code) {
       if (Character.isLetter(code)) {
           return true;
       } else {
           return false;
       }
   }
   
   /**
    * Helper to send a key down / key up pair to the current editor.
    */
   private void keyDownUp(int keyEventCode) {
       getCurrentInputConnection().sendKeyEvent(
               new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
       getCurrentInputConnection().sendKeyEvent(
               new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
   }
   
   /**
    * Helper to send a character to the editor as raw key events.
    */
   private void sendKey(int keyCode) {
       switch (keyCode) {
           case '\n':
               keyDownUp(KeyEvent.KEYCODE_ENTER);
               break;
           default:
               if (keyCode >= '0' && keyCode <= '9') {
                   keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
               } else {
                   getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
               }
               break;
       }
   }

   // 			ParseAnalytics.trackAppOpenedInBackground(self.getIntent());
   private void playClick(int keyCode){
     AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
     switch(keyCode){
     case 32: 
         am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
         break;
     case Keyboard.KEYCODE_DONE:
     case 10: 
         am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
         break;
     case Keyboard.KEYCODE_DELETE:
         am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
         break;              
     default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
     }       
   }
   
   // Implementation of KeyboardViewListener
   public void onKey(int primaryCode, int[] keyCodes) {
   	playClick(primaryCode);
     if (isWordSeparator(primaryCode)) {
       // Handle separator
       if (mComposing.length() > 0) {
           commitTyped(getCurrentInputConnection());
       }
       sendKey(primaryCode);
       updateShiftKeyState(getCurrentInputEditorInfo());
     } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
       handleBackspace();
     } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
       handleShift();
     } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
       handleClose();
       return;
     } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
         // Show a menu or somethin'
     } else if (	primaryCode == emojiButton ) {
    	 nav.setVisibility(View.VISIBLE);
       emoji.setVisibility(View.VISIBLE);
       mInputView.setVisibility(View.GONE);
     } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
       Keyboard current = mInputView.getKeyboard();
       if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
           current = mQwertyKeyboard;
       } else {
           current = mSymbolsKeyboard;
       }
       mInputView.setKeyboard(current);
       if (current == mSymbolsKeyboard) {
         current.setShifted(false);
       }
     } else {
         handleCharacter(primaryCode, keyCodes);
     }
   }

   /**
    * Update the list of available candidates from the current composing
    * text.  This will need to be filled in by however you are determining
    * candidates.
    */
   private void updateCandidates() {
       if (!mCompletionOn) {
           if (mComposing.length() > 0) {
               ArrayList<String> list = new ArrayList<String>();
               list.add(mComposing.toString());
               setSuggestions(list, true, true);
           } else {
               setSuggestions(null, false, false);
           }
       }
   }
   
   public void setSuggestions(List<String> suggestions, boolean completions, boolean typedWordValid) {
       /*if (mCandidateView != null) {
           mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
           if (suggestions != null && suggestions.size() > 0) {
               setCandidatesViewShown(true);
           } else if (isFullscreenMode()) {
               setCandidatesViewShown(true);
           }
       }*/
   }
   
   private void handleBackspace() {
       final int length = mComposing.length();
       if (length > 1) {
           mComposing.delete(length - 1, length);
           getCurrentInputConnection().setComposingText(mComposing, mComposing.length());
           updateCandidates();
       } else if (length > 0) {
           mComposing.setLength(0);
           getCurrentInputConnection().commitText("", 0);
           updateCandidates();
       } else {
           keyDownUp(KeyEvent.KEYCODE_DEL);
       }
       updateShiftKeyState(getCurrentInputEditorInfo());
   }

   private void handleShift() {
       if (mInputView == null) {
           return;
       }
       
       Keyboard currentKeyboard = mInputView.getKeyboard();
       if (mQwertyKeyboard == currentKeyboard) {
           // Alphabet keyboard
           checkToggleCapsLock();
           mInputView.setShifted(mCapsLock || !mInputView.isShifted());
       } else if (currentKeyboard == mSymbolsKeyboard) {
           mSymbolsKeyboard.setShifted(true);
           mInputView.setKeyboard(mSymbolsShiftedKeyboard);
           mSymbolsShiftedKeyboard.setShifted(true);
       } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
           mSymbolsShiftedKeyboard.setShifted(false);
           mInputView.setKeyboard(mSymbolsKeyboard);
           mSymbolsKeyboard.setShifted(false);
       }
   }
   
   private void handleCharacter(int primaryCode, int[] keyCodes) {
       if (isInputViewShown()) {
         if (mInputView.isShifted()) {
             primaryCode = Character.toUpperCase(primaryCode);
         }
       }
       try {
       		JSONObject props = new JSONObject();
					props.put("Letter", String.valueOf((char) primaryCode));
	        mixpanel.track("android_keyboard_text_insert", props);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
       updateShiftKeyState(getCurrentInputEditorInfo());
   }

   private void handleClose() {
       commitTyped(getCurrentInputConnection());
       //dismissSoftInput(0);
       mInputView.closing();
   }

   private void checkToggleCapsLock() {
       long now = System.currentTimeMillis();
       if (mLastShiftTime + 800 > now) {
           mCapsLock = !mCapsLock;
           mLastShiftTime = 0;
       } else {
           mLastShiftTime = now;
       }
   }
   
   private String getWordSeparators() {
       return mWordSeparators;
   }
   
   public boolean isWordSeparator(int code) {
       String separators = getWordSeparators();
       return separators != null ? separators.contains(String.valueOf((char)code)) : false;
   }

   public void pickDefaultCandidate() {
       pickSuggestionManually(0);
   }
   
   public void pickSuggestionManually(int index) {
       if (mCompletionOn && mCompletions != null && index >= 0
               && index < mCompletions.length) {
           CompletionInfo ci = mCompletions[index];
           getCurrentInputConnection().commitCompletion(ci);
           /*if (mCandidateView != null) {
               mCandidateView.clear();
           }*/
           updateShiftKeyState(getCurrentInputEditorInfo());
       } else if (mComposing.length() > 0) {
           // If we were generating candidate suggestions for the current
           // text, we would commit one of them here.  But for this sample,
           // we will just commit the current text.
           commitTyped(getCurrentInputConnection());
       }
   }
   
   public void swipeRight() {
       if (mCompletionOn) {
           pickDefaultCandidate();
       }
   }
   
   public void swipeLeft() {
       handleBackspace();
   }

   public void swipeDown() {
       handleClose();
   }

   public void swipeUp() {
   }
   
   public void onPress(int primaryCode) {
   }
   
   public void onRelease(int primaryCode) {
   }

		@Override
		public void onText(CharSequence arg0) {
			// TODO Auto-generated method stub
			
		}
   
}
