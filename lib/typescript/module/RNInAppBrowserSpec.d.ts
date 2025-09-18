import type { TurboModule } from 'react-native';
export interface Spec extends TurboModule {
    openAuth(authURL: string, redirectURL: string, options: {
        ephemeralWebSession?: boolean;
    }): Promise<{
        type: 'success' | 'cancel' | 'dismiss';
        url?: string;
        description?: string;
        message?: string;
    }>;
    open(options?: {
        url: string;
        dismissButtonStyle?: 'done' | 'close' | 'cancel';
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
declare const _default: Spec;
export default _default;
//# sourceMappingURL=RNInAppBrowserSpec.d.ts.map