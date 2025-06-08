### Compile

At root:

```shell
javac -d out src/p001/Main.java
```

---

### Run main class

At root:
```shell
java --claa-path out p001.Main "Sweet"
```

---

### Package

At root:

```shell
jar --create --file lib/p001.jar --main-class p001.Main -C out .
```

Run package:
```shell
java -jar lib/p001.jar "Sweet"
```

---
