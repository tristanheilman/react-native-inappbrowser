import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  // Methods
  openAuth(
    authURL: string,
    redirectURL: string,
    options: {
      ephemeralWebSession?: boolean;
    }
  ): Promise<{
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

export default TurboModuleRegistry.getEnforcing<Spec>('RNInAppBrowser'); 