package com.audiobookshelf.app.player.mediaButtonHandler

import android.view.KeyEvent

interface MediaButtonHandler {
  fun handleKeyEvent(keyEvent: KeyEvent?): Boolean
  fun addClickAction(clicks: Int, callback: () -> Unit)
  fun addHoldAction(clicksBeforeHold: Int, callback: () -> Unit)
}
