package com.proyecto26.inappbrowser

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.text.TextUtils
import androidx.browser.customtabs.*
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.facebook.react.bridge.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.regex.Pattern

class RNInAppBrowserModuleImpl {
    companion object {
        const val NAME = "RNInAppBrowser"
        private const val ERROR_CODE = "InAppBrowser"
        private const val ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService"
        private const val CHROME_PACKAGE_STABLE = "com.android.chrome"
        private const val CHROME_PACKAGE_BETA = "com.chrome.beta"
        private const val CHROME_PACKAGE_DEV = "com.chrome.dev"
        private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    }

    private var mOpenBrowserPromise: Promise? = null
    private var isLightTheme: Boolean = false
    private var currentActivity: Activity? = null
    private var customTabsClient: CustomTabsClient? = null
    private val animationIdentifierPattern = Pattern.compile("^.+:.+/")

    fun open(reactContext: ReactApplicationContext, options: ReadableMap, promise: Promise) {
        currentActivity = reactContext.currentActivity
        if (mOpenBrowserPromise != null) {
            val result = Arguments.createMap().apply {
                putString("type", "cancel")
            }
            mOpenBrowserPromise?.resolve(result)
            mOpenBrowserPromise = null
            return
        }
        mOpenBrowserPromise = promise

        if (currentActivity == null) {
            mOpenBrowserPromise?.reject(ERROR_CODE, "No activity")
            mOpenBrowserPromise = null
            return
        }

        val url = options.getString("url")
        if (url == null) {
            mOpenBrowserPromise?.reject(ERROR_CODE, "No URL provided")
            mOpenBrowserPromise = null
            return
        }
        val builder = CustomTabsIntent.Builder()
        isLightTheme = false
        val toolbarColor = setColor(builder, options, "toolbarColor", "setToolbarColor", "toolbar")
        if (toolbarColor != null) {
            isLightTheme = toolbarIsLight(toolbarColor)
        }
        setColor(builder, options, "secondaryToolbarColor", "setSecondaryToolbarColor", "secondary toolbar")
        setColor(builder, options, "navigationBarColor", "setNavigationBarColor", "navigation bar")
        setColor(builder, options, "navigationBarDividerColor", "setNavigationBarDividerColor", "navigation bar divider")

        if (options.hasKey("enableDefaultShare") && options.getBoolean("enableDefaultShare")) {
            builder.addDefaultShareMenuItem()
        }

        if (options.hasKey("animations")) {
            val animations = options.getMap("animations")
            applyAnimation(reactContext, builder, animations)
        }

        if (options.hasKey("hasBackButton") && options.getBoolean("hasBackButton")) {
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                reactContext.resources,
                if (isLightTheme) R.drawable.ic_arrow_back_black else R.drawable.ic_arrow_back_white
            ))
        }

        val customTabsIntent = builder.build()
        val intent = customTabsIntent.intent

        // Add ephemeral web session support
        if (options.hasKey("ephemeralWebSession") && options.getBoolean("ephemeralWebSession")) {
            val packageName = if (options.hasKey("browserPackage")) {
                options.getString("browserPackage")
            } else {
                getDefaultBrowser(currentActivity!!)
            }
            
            if (!TextUtils.isEmpty(packageName) && 
                CustomTabsClient.isEphemeralBrowsingSupported(currentActivity!!, packageName)) {
                builder.setEphemeralBrowsingEnabled(true)
            }
        }

        if (options.hasKey("headers")) {
            val headers = Bundle()
            val readableMap = options.getMap("headers")
            if (readableMap != null) {
                val iterator = readableMap.keySetIterator()
                while (iterator.hasNextKey()) {
                    val key = iterator.nextKey()
                    if (readableMap.getType(key) == ReadableType.String) {
                        headers.putString(key, readableMap.getString(key))
                    }
                }
                intent.putExtra(Browser.EXTRA_HEADERS, headers)
            }
        }

        if (options.hasKey("forceCloseOnRedirection") && options.getBoolean("forceCloseOnRedirection")) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!options.hasKey("showInRecents") || !options.getBoolean("showInRecents")) {
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        intent.putExtra(CustomTabsIntent.EXTRA_ENABLE_URLBAR_HIDING, 
            options.hasKey("enableUrlBarHiding") && options.getBoolean("enableUrlBarHiding"))

        try {
            val packageName = if (options.hasKey("browserPackage")) {
                options.getString("browserPackage")
            } else {
                getDefaultBrowser(currentActivity!!)
            }
            if (!TextUtils.isEmpty(packageName)) {
                intent.setPackage(packageName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        registerEventBus()

        intent.setData(Uri.parse(url))
        if (options.hasKey("showTitle")) {
            builder.setShowTitle(options.getBoolean("showTitle"))
        } else {
            intent.putExtra(CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE, CustomTabsIntent.NO_TITLE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && 
            options.hasKey("includeReferrer") && 
            options.getBoolean("includeReferrer")) {
            intent.putExtra(Intent.EXTRA_REFERRER,
                Uri.parse("android-app://${reactContext.applicationContext.packageName}"))
        }

        currentActivity?.startActivity(
            ChromeTabsManagerActivity.createStartIntent(currentActivity!!, intent),
            customTabsIntent.startAnimationBundle
        )
    }

    fun close() {
        if (mOpenBrowserPromise == null) return

        if (currentActivity == null) {
            mOpenBrowserPromise?.reject(ERROR_CODE, "No activity")
            mOpenBrowserPromise = null
            return
        }

        unRegisterEventBus()

        val result = Arguments.createMap().apply {
            putString("type", "dismiss")
        }
        mOpenBrowserPromise?.resolve(result)
        mOpenBrowserPromise = null

        currentActivity?.startActivity(ChromeTabsManagerActivity.createDismissIntent(currentActivity!!))
    }

    fun isAvailable(context: Context, promise: Promise) {
        val resolveInfos = getPreferredPackages(context)
        promise.resolve(!(resolveInfos == null || resolveInfos.isEmpty()))
    }

    @Subscribe
    fun onEvent(event: ChromeTabsDismissedEvent) {
        unRegisterEventBus()

        if (mOpenBrowserPromise == null) return

        if (event.isError) {
            mOpenBrowserPromise?.reject(ERROR_CODE, event.message)
        } else {
            val result = Arguments.createMap().apply {
                putString("type", event.resultType)
                putString("message", event.message)
            }
            mOpenBrowserPromise?.resolve(result)
        }
        mOpenBrowserPromise = null
    }

    private fun setColor(builder: CustomTabsIntent.Builder, options: ReadableMap, key: String, method: String, colorName: String): Int? {
        var color: Int? = null
        try {
            if (options.hasKey(key)) {
                val colorString = options.getString(key)
                color = Color.parseColor(colorString)
                val findMethod = builder.javaClass.getDeclaredMethod(method, Int::class.java)
                findMethod.invoke(builder, color)
            }
        } catch (e: Exception) {
            if (e is IllegalArgumentException) {
                throw JSApplicationIllegalArgumentException(
                    "Invalid $colorName color '${options.getString(key)}': ${e.message}")
            }
        }
        return color
    }

    private fun applyAnimation(context: Context, builder: CustomTabsIntent.Builder, animations: ReadableMap?) {
        if (animations == null) return

        val startEnterAnimationId = if (animations.hasKey("startEnter"))
            resolveAnimationIdentifierIfNeeded(context, animations.getString("startEnter"))
        else -1
        val startExitAnimationId = if (animations.hasKey("startExit"))
            resolveAnimationIdentifierIfNeeded(context, animations.getString("startExit"))
        else -1
        val endEnterAnimationId = if (animations.hasKey("endEnter"))
            resolveAnimationIdentifierIfNeeded(context, animations.getString("endEnter"))
        else -1
        val endExitAnimationId = if (animations.hasKey("endExit"))
            resolveAnimationIdentifierIfNeeded(context, animations.getString("endExit"))
        else -1

        if (startEnterAnimationId != -1 && startExitAnimationId != -1) {
            builder.setStartAnimations(context, startEnterAnimationId, startExitAnimationId)
        }

        if (endEnterAnimationId != -1 && endExitAnimationId != -1) {
            builder.setExitAnimations(context, endEnterAnimationId, endExitAnimationId)
        }
    }

    private fun resolveAnimationIdentifierIfNeeded(context: Context, identifier: String?): Int {
        if (identifier == null) return -1
        return if (animationIdentifierPattern.matcher(identifier).find()) {
            context.resources.getIdentifier(identifier, null, null)
        } else {
            context.resources.getIdentifier(identifier, "anim", context.packageName)
        }
    }

    private fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun toolbarIsLight(themeColor: Int): Boolean {
        val red = themeColor.red
        val green = themeColor.green
        val blue = themeColor.blue
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return luminance > 0.5
    }

    private fun getPreferredPackages(context: Context): List<ResolveInfo> {
        val serviceIntent = Intent(ACTION_CUSTOM_TABS_CONNECTION)
        return context.packageManager.queryIntentServices(serviceIntent, 0)
    }

    private fun getDefaultBrowser(context: Context): String? {
        val resolveInfos = getPreferredPackages(context)
        var packageName = CustomTabsClient.getPackageName(context,
            listOf(CHROME_PACKAGE_STABLE, CHROME_PACKAGE_BETA, CHROME_PACKAGE_DEV, LOCAL_PACKAGE))
        if (packageName == null && resolveInfos.isNotEmpty()) {
            packageName = resolveInfos[0].serviceInfo.packageName
        }
        return packageName
    }

    fun onStart(activity: Activity) {
        val applicationContext = activity.applicationContext
        val connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                customTabsClient = client
                if (!customTabsClient?.warmup(0L)!!) {
                    System.err.println("Couldn't warmup custom tabs client")
                }
                applicationContext.unbindService(this)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                customTabsClient = null
            }
        }

        val packageName = getDefaultBrowser(applicationContext)
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(applicationContext, packageName, connection)
        } else {
            System.err.println("No browser supported to bind custom tab service")
        }
    }

    fun warmup(promise: Promise) {
        promise.resolve(customTabsClient?.warmup(0L) ?: false)
    }

    fun mayLaunchUrl(mostLikelyUrl: String, otherUrls: ReadableArray) {
        customTabsClient?.let { client ->
            val customTabsSession = client.newSession(object : CustomTabsCallback() {})
            if (customTabsSession != null) {
                val otherUrlBundles = ArrayList<Bundle>(otherUrls.size())

                for (i in 0 until otherUrls.size()) {
                    val link = otherUrls.getString(i)
                    if (link != null) {
                        val bundle = Bundle().apply {
                            putParcelable(CustomTabsService.KEY_URL, Uri.parse(link))
                        }
                        otherUrlBundles.add(bundle)
                    }
                }

                customTabsSession.mayLaunchUrl(Uri.parse(mostLikelyUrl), null, otherUrlBundles)
            }
        }
    }
} 