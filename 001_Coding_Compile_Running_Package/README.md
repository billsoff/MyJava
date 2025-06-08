## 📘 What Is a **Compilation Unit** in Java?

In Java, a **compilation unit** is **one `.java` source file** — the **smallest unit** of code that the Java compiler (`javac`) compiles at a time.

---

### ✅ Official Definition (Java Language Specification):

> A **compilation unit** is a file containing Java source code — typically a single `.java` file, which can declare:
>
> * One **public class or interface** (whose name matches the file name)
> * Any number of **package-private classes/interfaces**
> * A **package declaration** (optional)
> * **Import statements**
> * **Top-level type declarations** (classes, interfaces, enums)

---

### 🔍 Example: One Compilation Unit

File: `MyApp.java`

```java
package com.example;

import java.util.*;

public class MyApp {
    public static void main(String[] args) {
        System.out.println("Hello!");
    }
}

class Helper { }  // package-private class
```

This `.java` file is **one compilation unit**, even though it contains multiple classes.

---

## 🧩 How the Compiler Sees Compilation Units

When you run:

```bash
javac MyApp.java
```

* `javac` compiles `MyApp.java` as **one compilation unit**
* It generates `.class` files for each class: `MyApp.class`, `Helper.class`
* Access rules (like package-private access) apply **within this unit**

---

## ❗ Why It Matters

Java **enforces access control** based on **compilation units**:

| Class Type                            | Visible to...                                           |
| ------------------------------------- | ------------------------------------------------------- |
| `public` class                        | Any class with access to the package or JAR             |
| `package-private` class (no modifier) | Only classes in the **same package + compilation unit** |

So if two developers write code in `package com.example`, but in different JARs or source trees, package-private classes **aren’t shared** — because they come from **different compilation units**.

---

## ✅ Summary

| Term               | Meaning                                |
| ------------------ | -------------------------------------- |
| Compilation Unit   | A single `.java` source file           |
| Access within unit | Can access all package-private classes |
| Across units       | Package-private access doesn't apply   |

---

Would you like a visual diagram showing how compilation units, packages, and access work together?
