/**
 * @format
 * @flow strict-local
 */
import type { BrowserResult, AuthSessionResult, InAppBrowserOptions } from "./types";
export declare const RNInAppBrowser: any;
export declare function openBrowserAsync(url: string, options?: InAppBrowserOptions): Promise<BrowserResult>;
export declare function openAuthSessionAsync(url: string, redirectUrl: string, options?: InAppBrowserOptions): Promise<AuthSessionResult>;
export declare function openAuthSessionPolyfillAsync(startUrl: string, returnUrl: string, options?: InAppBrowserOptions): Promise<AuthSessionResult>;
export declare function closeAuthSessionPolyfillAsync(): void;
export declare function authSessionIsNativelySupported(): boolean;
export declare const isAndroid: boolean;
//# sourceMappingURL=utils.d.ts.map