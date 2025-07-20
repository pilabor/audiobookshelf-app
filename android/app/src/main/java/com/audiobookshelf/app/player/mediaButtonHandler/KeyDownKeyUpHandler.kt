package com.audiobookshelf.app.player.mediaButtonHandler

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KeyDownKeyUpHandler(
  scope: CoroutineScope,
  playingStatusCallback: (playing:Boolean?) -> Boolean,
  stopCallback: () -> Unit,
  clickActions: MutableList<MediaButtonHandlerClickAction> = mutableListOf(),
  holdActions: MutableList<MediaButtonHandlerClickAction> = mutableListOf()
) : AbstractMediaButtonHandler(scope, playingStatusCallback, stopCallback, clickActions, holdActions) {

  var firstRepeatCount = 0;

  override fun handleKeyEvent(keyEvent: KeyEvent?): Boolean {
    if(keyEvent == null) {
      log("handleKeyEvent: keyEvent is null")
      return false
    }
    // reset firstRepeatCount as soon as there is an event with repeatCount=0
    if(keyEvent.repeatCount == 0) {
      firstRepeatCount = 0
    }

    val keyHold = keyEvent.action == KeyEvent.ACTION_DOWN

    // ignore first KEY_UP event after hold action has been executed
    if(!keyHold && firstRepeatCount > 0) {
      log("handleKeyEvent: !keyHold && firstRepeatCount > 0")
      return true
    }
    // repeated events can be ignored because we only take into account KEY_DOWN and KEY_UP first appearances
    if(keyEvent.repeatCount != 0) {
      if(firstRepeatCount > 0) {
        // log("handleKeyEvent: firstRepeatCount > 0 -> return true")
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
    buttonReleasedJob?.cancel()
    buttonReleasedJob = scope.launch {
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

}
