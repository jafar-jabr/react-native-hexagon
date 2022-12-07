import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { HexagonView } from 'react-native-hexagon';

export default function App() {
  const imgSource = {
    uri: 'https://picsum.photos/200/300.jpg',
    borderColor: '#d3d363',
    borderWidth: 5,
    cornerRadius: 16,
  };
  return (
    <View style={styles.container}>
      <HexagonView style={styles.avatar} source={imgSource} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatar: {
    height: 232,
    width: 232,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
