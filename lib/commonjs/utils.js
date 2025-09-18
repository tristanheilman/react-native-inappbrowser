"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.RNInAppBrowser = void 0;
exports.authSessionIsNativelySupported = authSessionIsNativelySupported;
exports.closeAuthSessionPolyfillAsync = closeAuthSessionPolyfillAsync;
exports.isAndroid = void 0;
exports.openAuthSessionAsync = openAuthSessionAsync;
exports.openAuthSessionPolyfillAsync = openAuthSessionPolyfillAsync;
exports.openBrowserAsync = openBrowserAsync;
var _invariant = _interopRequireDefault(require("invariant"));
var _reactNative = require("react-native");
function _interopRequireDefault(e) { return e && e.__esModule ? e : { default: e }; }
/**
 * @format
 * 
 */

const RNInAppBrowser = exports.RNInAppBrowser = _reactNative.NativeModules.RNInAppBrowser;
let _redirectHandler;
let _linkingEventSubscription;
// If the initial AppState.currentState is null, we assume that the first call to
// AppState#change event is not actually triggered by a real change,
// is triggered instead by the bridge capturing the current state
// (https://reactnative.dev/docs/appstate#basic-usage)
let _isAppStateAvailable = _reactNative.AppState.currentState !== null;
function waitForRedirectAsync(returnUrl) {
  return new Promise(function (resolve) {
    _redirectHandler = event => {
      if (event.url && event.url.startsWith(returnUrl)) {
        resolve({
          url: event.url,
          type: "success"
        });
      }
    };
    _linkingEventSubscription = _reactNative.Linking.addEventListener("url", _redirectHandler);
  });
}

/**
 * Detect Android Activity `OnResume` event once
 */
function handleAppStateActiveOnce() {
  return new Promise(function (resolve) {
    let appStateEventSubscription;
    function handleAppStateChange(nextAppState) {
      if (!_isAppStateAvailable) {
        _isAppStateAvailable = true;
        return;
      }
      if (nextAppState === "active") {
        if (appStateEventSubscription && appStateEventSubscription.remove !== undefined) {
          appStateEventSubscription.remove();
        } else {
          // TODO: fix this to elegantly remove the listener
          // Only solution is to remove all listeners at the moment
          //AppState.removeEventListener("change", handleAppStateChange);
        }
        resolve();
      }
    }
    appStateEventSubscription = _reactNative.AppState.addEventListener("change", handleAppStateChange);
  });
}
async function checkResultAndReturnUrl(returnUrl, result) {
  if (_reactNative.Platform.OS === "android" && result.type !== "cancel") {
    try {
      await handleAppStateActiveOnce();
      const url = await _reactNative.Linking.getInitialURL();
      return url && url.startsWith(returnUrl) ? {
        url,
        type: "success"
      } : result;
    } catch {
      return result;
    }
  } else {
    return result;
  }
}
async function openBrowserAsync(url, options = {
  animated: true,
  modalEnabled: true,
  dismissButtonStyle: "close",
  readerMode: false,
  enableBarCollapsing: false
}) {
  return RNInAppBrowser.open({
    url: url,
    ...options,
    preferredBarTintColor: options.preferredBarTintColor && (0, _reactNative.processColor)(options.preferredBarTintColor),
    preferredControlTintColor: options.preferredControlTintColor && (0, _reactNative.processColor)(options.preferredControlTintColor)
  });
}
async function openAuthSessionAsync(url, redirectUrl, options = {
  ephemeralWebSession: false
}) {
  return RNInAppBrowser.openAuth(url, redirectUrl, options);
}
async function openAuthSessionPolyfillAsync(startUrl, returnUrl, options) {
  (0, _invariant.default)(!_redirectHandler, "InAppBrowser.openAuth is in a bad state. _redirectHandler is defined when it should not be.");
  try {
    return await Promise.race([openBrowserAsync(startUrl, options).then(function (result) {
      return checkResultAndReturnUrl(returnUrl, result);
    }), waitForRedirectAsync(returnUrl)]);
  } finally {
    closeAuthSessionPolyfillAsync();
    RNInAppBrowser.close();
  }
}
function closeAuthSessionPolyfillAsync() {
  if (_redirectHandler) {
    if (_linkingEventSubscription && _linkingEventSubscription.remove !== undefined) {
      _linkingEventSubscription.remove();
      _linkingEventSubscription = null;
    } else {
      // TODO: fix this to elegantly remove the listener
      // Only solution is to remove all listeners at the moment
      //Linking.removeEventListener("url", _redirectHandler);
    }
    _redirectHandler = null;
  }
}

/* iOS <= 10 and Android polyfill for SFAuthenticationSession flow */
function authSessionIsNativelySupported() {
  if (_reactNative.Platform.OS === "android") {
    return false;
  }
  const versionNumber = parseInt(_reactNative.Platform.Version, 10);
  return versionNumber >= 11;
}
const isAndroid = exports.isAndroid = _reactNative.Platform.OS === "android";
//# sourceMappingURL=utils.js.map