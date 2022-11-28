import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { HexagonView } from 'react-native-hexagon';

export default function App() {
  return (
    <View style={styles.container}>
      <HexagonView
        src="https://picsum.photos/200/300.jpg"
        borderColor="#FFC901"
        borderWidth={6}
        cornerRadius={6}
        width={32}
        height={32}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
