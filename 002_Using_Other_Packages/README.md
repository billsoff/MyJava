To **download the Gson package** (Google's JSON library for Java), you have several options depending on how you're managing your Java project:

---

## ✅ 1. **Download Gson JAR directly (manual use)**

### 🔗 Official Download:

* Go to: [https://repo1.maven.org/maven2/com/google/code/gson/gson/](https://repo1.maven.org/maven2/com/google/code/gson/gson/)
* Choose a version (e.g., `2.10.1`)
* Download: [`gson-2.10.1.jar`](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)

### 📦 How to Use:

1. Save `gson-2.10.1.jar` into a folder (e.g., `lib/`)
2. In Eclipse: Right-click project → **Build Path** → **Add External JARs** → choose `gson-2.10.1.jar`
3. You can now import and use it:

   ```java
   import com.google.gson.Gson;
   ```

---

## ✅ 2. **Use Gson with Maven**

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.10.1</version>
</dependency>
```

---

## ✅ 3. **Use Gson with Gradle**

Add to `build.gradle`:

```groovy
dependencies {
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## ✅ 4. **Command Line Compile/Run (with gson.jar)**

```bash
javac -cp gson-2.10.1.jar MyApp.java
java -cp .:gson-2.10.1.jar MyApp
```

(Use `;` instead of `:` on Windows)

---

## Compile

```bash
javac --class-path lib\gson-2.10.1.jar -d out src\p002\Main.java
```

## Run

```shell
java --class-path "lib\gson-2.10.1.jar;out" p002.Main
```

---

## Package

```shell
jar --create --file target\p002.jar --main-class p002.Main -C out .
```

## Run

```shell
java --class-path "target/p002.jar;lib\gson-2.10.1.jar" p002.Main
```

---

## Package with manifest

```shell
jar --create --file target\p002.jar --manifest manifest.txt -C out .
copy lib target/ -Recurse
java -jar p002.jar
```
