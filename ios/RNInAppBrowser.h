#import <React/RCTEventEmitter.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNInAppBrowserSpec/RNInAppBrowserSpec.h>

@interface RNInAppBrowser : RCTEventEmitter <NativeRNInAppBrowserSpec>
@end

#else

#import <React/RCTBridgeModule.h>

@interface RNInAppBrowser : RCTEventEmitter <RCTBridgeModule>
@end

#endif