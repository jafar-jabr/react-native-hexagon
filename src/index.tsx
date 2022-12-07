import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  StyleSheet,
} from 'react-native';
import * as React from 'react';

const LINKING_ERROR =
  `The package 'react-native-hexagon' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type HexagonProps = {
  source: Record<any, any>;
  style: ViewStyle;
};

const ComponentName = 'HexagonImageView';

export const HexagonImage =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<HexagonProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const HexagonView = ({ style, source }: HexagonProps) => {
  const borderWidth = source.borderWidth;
  const { width, height } = StyleSheet.flatten([style]);
  React.useEffect(() => {
    if (typeof width !== 'number' || typeof height !== 'number') {
      throw new Error(
        'react-native-hexagon style height and width must be numbers'
      );
    }
    if (borderWidth && typeof borderWidth !== 'number') {
      throw new Error(
        'react-native-hexagon border width value must be numbers'
      );
    }
  }, [borderWidth, height, width]);

  return <HexagonImage style={style} source={source} />;
};
