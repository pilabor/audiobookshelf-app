package com.audiobookshelf.app.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    String intentAction = intent.getAction();
    if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
      KeyEvent event = (KeyEvent)
        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

      if (event == null) {
        return;
      }

      int keycode = event.getKeyCode();
      int action = event.getAction();
      long eventtime = event.getEventTime();

      var keyCodeName = "";
      var keyActionName = "";
      switch(keycode) {
        case KeyEvent.KEYCODE_HEADSETHOOK:
          keyCodeName = "KEYCODE_HEADSETHOOK";
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          keyCodeName = "KEYCODE_MEDIA_PLAY";
          break;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          keyCodeName = "KEYCODE_MEDIA_PAUSE";
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
          keyCodeName = "KEYCODE_MEDIA_PLAY_PAUSE";
          break;
        case KeyEvent.KEYCODE_MEDIA_NEXT:
          keyCodeName = "KEYCODE_MEDIA_NEXT";
          break;
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
          keyCodeName = "KEYCODE_MEDIA_PREVIOUS";
          break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
          keyCodeName = "KEYCODE_MEDIA_STOP";
          break;
        default:
          keyCodeName = "KEYCODE_UNKNOWN";
          break;
      }
      if(action == KeyEvent.ACTION_DOWN) {
        keyActionName = "ACTION_DOWN";
      } else if(action == KeyEvent.ACTION_UP) {
        keyActionName = "ACTION_DOWN";
      } else if(action == KeyEvent.ACTION_MULTIPLE) {
        keyActionName = "ACTION_MULTIPLE";
      } else {
        keyActionName = "ACTION_UNKNOWN";
      }

    Log.d("MediaButtonReceiver", "custom-onMediaButtonEvent (MediaButtonReceiver): keyAction: "+keyActionName + ", keyCode: " + keyCodeName);
/*
      if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
        if (action == KeyEvent.ACTION_DOWN) {
          // Start your app here!

          // ...

          if (isOrderedBroadcast()) {
            abortBroadcast();
          }
        }
      }

 */
    }
  }
}
