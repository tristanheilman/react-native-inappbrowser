package com.proyecto26.inappbrowser

import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = RNInAppBrowserModuleImpl.NAME)
class RNInAppBrowserModule(reactContext: ReactApplicationContext) :
    NativeRNInAppBrowserSpec(reactContext),
    LifecycleEventListener {

    private val inAppBrowserImpl = RNInAppBrowserModuleImpl()

    init {
        reactApplicationContext.addLifecycleEventListener(this)
    }

    override fun invalidate() {
        reactApplicationContext.removeLifecycleEventListener(this)
    }

    override fun getName(): String {
        return RNInAppBrowserModuleImpl.NAME
    }

    override fun onHostResume() {
        reactApplicationContext.currentActivity?.let { activity ->
            inAppBrowserImpl.onStart(activity)
        }
    }

    override fun onHostPause() {}

    override fun onHostDestroy() {}

    override fun open(url: String, options: ReadableMap, promise: Promise) {
        inAppBrowserImpl.open(reactApplicationContext, options, promise)
    }

    override fun close() {
        // noop on Android since the modal is closed by deep-link
    }

    override fun addListener(eventName: String) {
        // iOS only
    }

    override fun removeListeners(count: Double) {
        // iOS only
    }
} 