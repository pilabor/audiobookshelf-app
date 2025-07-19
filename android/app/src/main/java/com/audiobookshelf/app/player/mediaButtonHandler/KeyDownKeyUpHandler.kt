package com.audiobookshelf.app.player.mediaButtonHandler

import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


// todo:
// - configure click callbacks (1=playPause, 2=next, etc.)
// - configure stopAction (
class KeyDownKeyUpHandler(val scope: CoroutineScope): MediaButtonHandler {
  val handlerDelay = 1050.milliseconds

  var clickActions = mutableListOf<MediaButtonHandlerClickAction>()
  var holdActions = mutableListOf<MediaButtonHandlerClickAction>()

  var clickHandlerJob: Job? = null
  var clickCount = 0

  var firstRepeatCount = 0;

  fun log(message: String) {
    Log.d("KeyDownKeyUpHandler", message)
  }

  fun updateClickCount(keyEvent: KeyEvent): KeyCodeResult {
    when (keyEvent.keyCode) {
      KeyEvent.KEYCODE_HEADSETHOOK,
      KeyEvent.KEYCODE_MEDIA_PLAY,
      KeyEvent.KEYCODE_MEDIA_PAUSE,
      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
        clickCount++
        log("=== handleCallMediaButton: Headset Hook/Play/ Pause, clickCount=$clickCount")
      }

      KeyEvent.KEYCODE_MEDIA_NEXT -> {
        clickCount += 2
        log("=== handleCallMediaButton: Media Next, clickCount=$clickCount")
      }

      KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
        clickCount += 3
        log("=== handleCallMediaButton: Media Previous, clickCount=$clickCount")
      }
      KeyEvent.KEYCODE_MEDIA_STOP -> {
        log("=== handleCallMediaButton: Media Stop, clickCount=$clickCount")
        // playerNotificationService.closePlayback()
        // clickTimer.cancel()
        return KeyCodeResult.StopPlayback
      } else -> {
      log("=== KeyCode:${keyEvent.keyCode}, clickCount=$clickCount")
      return KeyCodeResult.NotHandled
    }
    }
    return KeyCodeResult.Default
  }

  override fun handleKeyEvent(keyEvent: KeyEvent?): Boolean {
    if(keyEvent == null) {
      log("handleKeyEvent: keyEvent is null")
      return false
    }
    val keyHold = keyEvent.action == KeyEvent.ACTION_DOWN

    // ignore first KEY_UP event after hold action has been executed
    if(!keyHold && firstRepeatCount > 0) {
      log("handleKeyEvent: !keyHold && firstRepeatCount > 0")
      firstRepeatCount = 0
      return true
    }
    // repeated events can be ignored because we only take into account KEY_DOWN and KEY_UP first appearances
    if(keyEvent.repeatCount != 0) {
      if(firstRepeatCount > 0) {
        log("handleKeyEvent: firstRepeatCount > 0 -> return true")
        return true
      }
      log("handleKeyEvent: repeatCount > 0")
    }
    firstRepeatCount = keyEvent.repeatCount


    if(!keyHold) {
      val keyCodeResult = updateClickCount(keyEvent)
      if (keyCodeResult == KeyCodeResult.NotHandled) {
        log("keyCodeResult == KeyCodeResult.NotHandled")
        return false;
      }
    }
    clickHandlerJob?.cancel()
    clickHandlerJob = scope.launch {
      log("clickHandlerJob scheduled")

      delay(handlerDelay)
      if(keyHold) {
        executeHoldAction(clickCount)
      } else {
        executeClickAction(clickCount)
      }
      clickCount = 0
    }
    return true
  }

  override fun addClickAction(clicks: Int, callback: () -> Unit) {
    clickActions.add(MediaButtonHandlerClickAction(clicks, callback))
  }
  override fun addHoldAction(clicksBeforeHold: Int, callback: () -> Unit) {
    holdActions.add(MediaButtonHandlerClickAction(clicksBeforeHold, callback))
  }

  fun executeHoldAction(clickCount: Int) {
    log("executeHoldAction: clickCount=$clickCount")
    val action = holdActions.find{it -> it.clicks == clickCount}
    if(action == null) {
      log("executeHoldAction: no action found")
    }
    action?.callback?.invoke()
  }

  fun executeClickAction(clickCount: Int) {
    log("executeClickAction: clickCount=$clickCount")

    val action = clickActions.find{it -> it.clicks == clickCount}
    if(action == null) {
      log("executeClickAction: no action found")
    }
    action?.callback?.invoke()
  }



  /*
private fun debounceKeyEvent(keyEvent: KeyEvent?): Boolean {
  // how does this work:
  // - every keyDown and keyUp triggers a scheduled handler
  // - another keyDown or keyUp cancels the scheduled handler and re-triggers it with new values
  // - the handler takes clickCount:int and clickPressed:bool (if held down)
  // - keyCodes increase the number of clicks (PlayPause+=1, Next+=2, Prev+=3)
  // - depending on the number of clicks, the playerNotificationService handles the configured action
  // problems:
  // - the logs show pretty accurate click / hold detection, but it does not really translate well in the player
  // - since the trigger is scheduled, it does run in a different thread
  // - this leads to strange behaviour - probably easy to fix, but I'm no kotlin native (Coroutines)
  // - probably after some actions the thread of the player is no longer accessible...
  if (keyEvent?.action == KeyEvent.ACTION_UP) {
    clickPressed = false
    // log("=== KeyEvent.ACTION_UP")

  } else if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
    // log("=== KeyEvent.ACTION_DOWN")

    if(clickPressed) {
      return false
    }
    clickPressed = true

    when (keyEvent.keyCode) {
      KeyEvent.KEYCODE_HEADSETHOOK,
      KeyEvent.KEYCODE_MEDIA_PLAY,
      KeyEvent.KEYCODE_MEDIA_PAUSE,
      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
        clickCount++
        log("=== handleCallMediaButton: Headset Hook/Play/ Pause, clickCount=$clickCount")
      }

      KeyEvent.KEYCODE_MEDIA_NEXT -> {
        clickCount += 2
        log("=== handleCallMediaButton: Media Next, clickCount=$clickCount")
      }

      KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
        clickCount += 3
        log("=== handleCallMediaButton: Media Previous, clickCount=$clickCount")
      }

      KeyEvent.KEYCODE_MEDIA_STOP -> {
        log("=== handleCallMediaButton: Media Stop, clickCount=$clickCount")
        playerNotificationService.closePlayback()
        clickTimer.cancel()
        return true
      } else -> {
      log("=== KeyCode:${keyEvent.keyCode}, clickCount=$clickCount")
      return false
    }
    }
  }

  if(clickTimerScheduled) {
    log("=== clickTimer cancelled ($clickTimerId): clicks=$clickCount, hold=$clickPressed =========")
    clickTimer.cancel()
    clickTimer = Timer()
  }

  clickTimer.schedule(650) {
    log("=== clickTimer executed ($clickTimerId): clicks=$clickCount, hold=$clickPressed =========")
    Handler(Looper.getMainLooper()).post {
      playerNotificationService.handleClicks(clickCount, clickPressed)
    }
    clickCount = 0
    clickTimerScheduled = false
  }
  clickTimerScheduled = true
  log("=== clickTimer scheduled ($clickTimerId): clicks=$clickCount, hold=$clickPressed =========")
  return true
}
  fun handleClicks(clicks: Int, clickPressed: Boolean) {
    stopSeeking = true
    launch {
      // the handlers should be configurlateinitable, defaults:
      // hold -> jumpBackward
      // click -> play / pause
      // click, hold -> fast forward
      // click, click -> next (chapter or track)
      // click, click, hold -> rewind
      // click, click, click -> previous (chapter or track)

      withContext(coroutineContext) {
        Log.d(tag, "=== handleClicks: count=$clicks,hold=$clickPressed")

        if (clickPressed) {
          lastStatePlaying = currentPlayer.isPlaying
          when (clicks) {
            1 -> {
              jumpBackward()
            }
            2 -> {
              Log.d(tag, "=== fastForward init, stopSeeking=$stopSeeking")

              stopSeeking = false
              val mainHandler = Handler(Looper.getMainLooper())
              mainHandler.post(object : Runnable {
                override fun run() {
                  Log.d(tag, "=== fastForward run, stopSeeking=$stopSeeking")
                  seekForward(10000 - seekPlayBufferTime)
                  play()
                  if(!stopSeeking) {
                    Log.d(tag, "=== fastForward recursion")
                    mainHandler.postDelayed(this, seekPlayBufferTime)
                  }
                }
              })
            }

            3 -> {
              stopSeeking = false
              val mainHandler = Handler(Looper.getMainLooper())
              mainHandler.post(object : Runnable {
                override fun run() {
                  seekBackward(10000 + seekPlayBufferTime)
                  play()
                  if(!stopSeeking) {
                    mainHandler.postDelayed(this, seekPlayBufferTime)
                  }
                }
              })
            }
          }
        } else {
          when (clicks) {
            0 -> {
              // switch from fastForward / rewind back to last playing state
              if (lastStatePlaying) {
                play()
              } else {
                pause()
              }
            }

            1 -> {
              playPause()
              /*
              if (currentPlayer.isPlaying) {
                pause()
              } else {
                play()
              }
               */
            }

            2 -> {
              // todo: implement "next chapter"
              // skipToNext()
              seekForward(300000)
            }

            3 -> {
              // todo: implement "previous chapter"
              // skipToPrevious()
              seekBackward(300000)
            }
          }
        }
      }
    }

   */

}
