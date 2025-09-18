package com.proyecto26.inappbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.greenrobot.eventbus.EventBus

/**
 * Manages the custom chrome tabs intent by detecting when it is dismissed by the user and allowing
 * to close it programmatically when needed.
 */
class ChromeTabsManagerActivity : Activity() {
    companion object {
        const val KEY_BROWSER_INTENT = "browserIntent"
        const val BROWSER_RESULT_TYPE = "browserResultType"
        const val DEFAULT_RESULT_TYPE = "dismiss"

        fun createStartIntent(context: Context, authIntent: Intent): Intent {
            return createBaseIntent(context).apply {
                putExtra(KEY_BROWSER_INTENT, authIntent)
            }
        }

        fun createDismissIntent(context: Context): Intent {
            return createBaseIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        private fun createBaseIntent(context: Context): Intent {
            return Intent(context, ChromeTabsManagerActivity::class.java)
        }
    }

    private var mOpened = false
    private var resultType: String = DEFAULT_RESULT_TYPE
    private var isError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            // This activity gets opened in 2 different ways. If the extra KEY_BROWSER_INTENT is present we
            // start that intent and if it is not it means this activity was started with FLAG_ACTIVITY_CLEAR_TOP
            // in order to close the intent that was started previously so we just close this.
            if (intent.hasExtra(KEY_BROWSER_INTENT) &&
                (savedInstanceState == null || savedInstanceState.getString(BROWSER_RESULT_TYPE) == null)
            ) {
                val browserIntent = intent.getParcelableExtra<Intent>(KEY_BROWSER_INTENT)
                browserIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(browserIntent)
                resultType = DEFAULT_RESULT_TYPE
            } else {
                finish()
            }
        } catch (e: Exception) {
            isError = true
            EventBus.getDefault().post(ChromeTabsDismissedEvent("Unable to open url.", resultType, isError))
            finish()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()

        // onResume will get called twice, the first time when the activity is created and a second
        // time if the user closes the chrome tabs activity. Knowing this we can detect if the user
        // dismissed the activity and send an event accordingly.
        if (!mOpened) {
            mOpened = true
        } else {
            resultType = "cancel"
            finish()
        }
    }

    override fun onDestroy() {
        when (resultType) {
            "cancel" -> EventBus.getDefault().post(
                ChromeTabsDismissedEvent("chrome tabs activity closed", resultType, isError)
            )
            else -> EventBus.getDefault().post(
                ChromeTabsDismissedEvent("chrome tabs activity destroyed", DEFAULT_RESULT_TYPE, isError)
            )
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        resultType = savedInstanceState.getString(BROWSER_RESULT_TYPE) ?: DEFAULT_RESULT_TYPE
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(BROWSER_RESULT_TYPE, DEFAULT_RESULT_TYPE)
        super.onSaveInstanceState(savedInstanceState)
    }
} 