# react-native-hexagon

hexagon view

## Installation

```sh
npm install react-native-hexagon
```

## Usage

```js
import { HexagonView } from 'react-native-hexagon';

// ...

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

```

![image](https://firebasestorage.googleapis.com/v0/b/jj-tech.appspot.com/o/react-native-hexagon.png?alt=media&token=1e4ba250-e54a-42bd-9b1a-ad658444f31e
)


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
