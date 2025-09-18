/**
 * InAppBrowser for React Native
 * https://github.com/proyecto26/react-native-inappbrowser
 *
 * @format
 * @flow strict-local
 */
import type { BrowserResult, AuthSessionResult, InAppBrowserOptions } from "./types";
/**
 * Opens the url with Safari in a modal on iOS using [`SFSafariViewController`](https://developer.apple.com/documentation/safariservices/sfsafariviewcontroller),
 * or Chrome in a new [custom tab](https://developer.chrome.com/multidevice/android/customtabs) on Android.
 *
 * @param url The url to open in the web browser.
 * @param options A dictionary of key-value pairs.
 *
 * @return The promise behaves differently based on the platform:
 * - If the user closed the web browser, the Promise resolves with `{ type: 'cancel' }`.
 * - If the browser is closed using [`close`](#webbrowserdismissbrowser), the Promise resolves with `{ type: 'dismiss' }`.
 */
declare function open(url: string, options?: InAppBrowserOptions): Promise<BrowserResult>;
/**
 * # On iOS:
 * Opens the url with Safari in a modal using `ASWebAuthenticationSession`. The user will be asked
 * whether to allow the app to authenticate using the given url.
 *
 * # On Android:
 * This will be done using a "custom Chrome tabs" browser and [activityResumedEvent](https://docs.nativescript.org/api-reference/classes/androidapplication#activityresumedevent),
 *
 * @param url The url to open in the web browser. This should be a login page.
 * @param redirectUrl _Optional_ - The url to deep link back into your app.
 * @param options _Optional_ - An object extending the InAppBrowser Options.
 *
 * @return
 * - If the user does not permit the application to authenticate with the given url, the Promise fulfills with `{ type: 'cancel' }` object.
 * - If the user closed the web browser, the Promise fulfills with `{ type: 'cancel' }` object.
 * - If the browser is closed using `dismissBrowser`, the Promise fulfills with `{ type: 'dismiss' }` object.
 */
declare function openAuth(url: string, redirectUrl: string, options?: InAppBrowserOptions): Promise<AuthSessionResult>;
/**
 * Dismisses the presented web browser.
 */
declare function close(): void;
/**
 * Warm up the browser process.
 * Allows the browser application to pre-initialize itself in the background.
 * Significantly speeds up URL opening in the browser.
 * This is asynchronous and can be called several times.
 *
 * @platform android
 */
declare function warmup(): Promise<boolean>;
/**
 * Tells the browser of a likely future navigation to a URL.
 * The most likely URL has to be specified first.
 * Optionally, a list of other likely URLs can be provided.
 * They are treated as less likely than the first one, and have to be sorted in decreasing priority order.
 * These additional URLs may be ignored.
 *
 * @param mostLikelyUrl Most likely URL, may be null if otherUrls is provided.
 * @param otherUrls Other likely destinations, sorted in decreasing likelihood order.
 *
 * @platform android
 */
declare function mayLaunchUrl(mostLikelyUrl: string, otherUrls?: Array<string>): void;
/**
 * Dismisses the current authentication session
 */
declare function closeAuth(): void;
/**
 * Detect if the device supports this plugin.
 */
declare function isAvailable(): Promise<boolean>;
export declare const InAppBrowser: {
    open: typeof open;
    openAuth: typeof openAuth;
    close: typeof close;
    closeAuth: typeof closeAuth;
    isAvailable: typeof isAvailable;
    warmup: typeof warmup;
    mayLaunchUrl: typeof mayLaunchUrl;
};
export default InAppBrowser;
//# sourceMappingURL=index.d.ts.map