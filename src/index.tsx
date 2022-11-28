import { requireNativeComponent, UIManager, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-hexagon' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type HexagonProps = {
  src: string;
  borderColor: string;
  borderWidth: number;
  cornerRadius: number;
  width: number;
  height: number;
};

const ComponentName = 'HexagonView';

export const HexagonView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<HexagonProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
