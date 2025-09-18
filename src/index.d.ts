import type { TurboModule } from "react-native";
import { TurboModuleRegistry } from "react-native";

export interface BrowserResult {
  type: "cancel" | "dismiss";
}

declare module "react-native-inappbrowser-reborn" {
  export interface RedirectEvent {
    url: "string";
  }

  export interface RedirectResult {
    type: "success";
    url: string;
  }

  export type InAppBrowseriOSOptions = {
    dismissButtonStyle?: "done" | "close" | "cancel";
    preferredBarTintColor?: string;
    preferredControlTintColor?: string;
    readerMode?: boolean;
    animated?: boolean;
    modalPresentationStyle?:
      | "automatic"
      | "fullScreen"
      | "pageSheet"
      | "formSheet"
      | "currentContext"
      | "custom"
      | "overFullScreen"
      | "overCurrentContext"
      | "popover"
      | "none";
    modalTransitionStyle?:
      | "coverVertical"
      | "flipHorizontal"
      | "crossDissolve"
      | "partialCurl";
    modalEnabled?: boolean;
    enableBarCollapsing?: boolean;
    ephemeralWebSession?: boolean;
    formSheetPreferredContentSize?: { width: number; height: number };
  };

  export type InAppBrowserAndroidOptions = {
    showTitle?: boolean;
    toolbarColor?: string;
    secondaryToolbarColor?: string;
    navigationBarColor?: string;
    navigationBarDividerColor?: string;
    enableUrlBarHiding?: boolean;
    enableDefaultShare?: boolean;
    forceCloseOnRedirection?: boolean;
    animations?: {
      startEnter: string;
      startExit: string;
      endEnter: string;
      endExit: string;
    };
    headers?: { [key: string]: string };
    hasBackButton?: boolean;
    browserPackage?: string;
    showInRecents?: boolean;
    includeReferrer?: boolean;
  };

  export type InAppBrowserOptions =
    | InAppBrowserAndroidOptions
    | InAppBrowseriOSOptions;

  type AuthSessionResult = RedirectResult | BrowserResult;

  interface InAppBrowserClassMethods {
    open: (
      url: string,
      options?: InAppBrowserOptions
    ) => Promise<BrowserResult>;
    close: () => void;
    warmup: () => Promise<boolean>;
    mayLaunchUrl: (mostLikelyUrl: string, otherUrls: Array<string>) => void;
    openAuth: (
      url: string,
      redirectUrl: string,
      options?: InAppBrowserOptions
    ) => Promise<AuthSessionResult>;
    closeAuth: () => void;
    isAvailable: () => Promise<boolean>;
  }

  export const InAppBrowser: InAppBrowserClassMethods;

  export default InAppBrowser;
}

export interface RNInAppBrowserSpec extends TurboModule {
  openAuth(
    authURL: string,
    redirectURL: string,
    options: {
      ephemeralWebSession?: boolean;
    }
  ): Promise<{
    type: "success" | "cancel" | "dismiss";
    url?: string;
    description?: string;
    message?: string;
  }>;

  open(options: {
    url: string;
    dismissButtonStyle?: "done" | "close" | "cancel";
    preferredBarTintColor?: number;
    preferredControlTintColor?: number;
    modalPresentationStyle?: string;
    modalTransitionStyle?: string;
    formSheetPreferredContentSize?: {
      width: number;
      height: number;
    };
    readerMode?: boolean;
    enableBarCollapsing?: boolean;
    modalEnabled?: boolean;
    animated?: boolean;
  }): Promise<void>;

  close(): void;
  closeAuth(): void;
  isAvailable(): Promise<boolean>;
}

export interface AuthSessionResult {
  type: "success" | "cancel" | "dismiss";
  url?: string;
  description?: string;
  message?: string;
}

export interface InAppBrowserOptions {
  dismissButtonStyle?: "done" | "close" | "cancel";
  preferredBarTintColor?: string;
  preferredControlTintColor?: string;
  modalPresentationStyle?: string;
  modalTransitionStyle?: string;
  formSheetPreferredContentSize?: {
    width: number;
    height: number;
  };
  readerMode?: boolean;
  enableBarCollapsing?: boolean;
  modalEnabled?: boolean;
  animated?: boolean;
  ephemeralWebSession?: boolean;
}

export interface InAppBrowser {
  open(url: string, options?: InAppBrowserOptions): Promise<BrowserResult>;
  openAuth(
    url: string,
    redirectUrl: string,
    options?: InAppBrowserOptions
  ): Promise<AuthSessionResult>;
  close(): void;
  closeAuth(): void;
  isAvailable(): Promise<boolean>;
  warmup(): Promise<boolean>;
  mayLaunchUrl(mostLikelyUrl: string, otherUrls?: string[]): void;
}

declare const InAppBrowser: InAppBrowser;
export default InAppBrowser;
