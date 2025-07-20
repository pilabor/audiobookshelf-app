package com.audiobookshelf.app.player.mediaButtonHandler

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class KeyDownRepeatCountHandler(
  scope: CoroutineScope,
  playingStatusCallback: (playing:Boolean?) -> Boolean,
  stopCallback: () -> Unit,
  clickActions: MutableList<MediaButtonHandlerClickAction> = mutableListOf(),
  holdActions: MutableList<MediaButtonHandlerClickAction> = mutableListOf()
) : AbstractMediaButtonHandler(scope, playingStatusCallback, stopCallback, clickActions, holdActions) {

  val holdEndedDelay = 100.milliseconds

  var buttonHoldEndedJob: Job? = null
  var wasPlaying: Boolean = false


  override fun handleKeyEvent(keyEvent: KeyEvent?): Boolean {

    if(keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
      if(keyEvent == null) {
        log("handleKeyEvent null")
      } else {
        log("handleKeyEvent ${keyEventToString(keyEvent)}")
      }

      return false;
    }

    log("debounceKeyEvent: ${keyEventToString(keyEvent)}, clickCount=$clickCount, repeatCount=${keyEvent.repeatCount}")

    // this is device dependant
    // - longerDelay is less responsive but secure for all devices
    // - shortDelay is what we want, but most devices just don't support it

    val timerDelay = handlerDelay
    val clickPressed = keyEvent.repeatCount > 0

    // only increase the clickCount on non-clickPressed events
    if(!clickPressed) {
      updateClickCount(keyEvent)
    }

    // cancel all running jobs on a new click
    buttonReleasedJob?.cancel()
    buttonHoldEndedJob?.cancel()

    if(clickPressed) {
      // only handle first repeatedEvent
      if(keyEvent.repeatCount < 3) {
        wasPlaying = playingStatusCallback(null)// player.isPlaying
        executeHoldAction(clickCount)
      }
      buttonHoldEndedJob = scope.launch {
        log("clickPressedJob: scheduled")
        delay(holdEndedDelay)
        log("clickPressedJob: execute")
        clickCount = 0
        if(wasPlaying) {
          log("playerAction - holdEnded: play")
          playingStatusCallback(true)
          // player.play()
        } else {
          log("playerAction - holdEnded: pause")
          playingStatusCallback(true)
          // player.pause()
        }
      }
    } else {
      wasPlaying = playingStatusCallback(null) // player.isPlaying
      buttonReleasedJob = scope.launch {
        // delay(650);
        log("clickReleasedJob scheduled: delay=${timerDelay.inWholeMilliseconds}ms, clicks=$clickCount, hold=$clickPressed ==== ${
          keyEventToString(keyEvent)
        }"
        )
        delay(timerDelay)
        log("clickReleasedJob executed: delay=${timerDelay.inWholeMilliseconds}ms, clicks=$clickCount, hold=$clickPressed ==== ${
          keyEventToString(keyEvent)
        }")
        executeClickAction(clickCount)
        clickCount = 0
      }
    }

    return true
  }

}
