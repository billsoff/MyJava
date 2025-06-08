Here's a **minimal Java web application example** that:

* Uses **no build tools** (no Maven, no Gradle)
* Uses **only Java SE and Servlet API**
* Is manually structured and packaged into a **WAR** file
* Runs on a servlet container like **Apache Tomcat**

---

## ✅ 1. Project Structure

```
MyWebApp/
├── WEB-INF/
│   ├── web.xml
│   └── classes/
│       └── com/example/
│           └── HelloServlet.class
└── index.html
```

---

## ✅ 2. Java Source Code (`HelloServlet.java`)

Save as `MyWebApp/src/com/example/HelloServlet.java`:

```java
package com.example;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello, World!");
    }
}
```

---

## ✅ 3. Deployment Descriptor (`WEB-INF/web.xml`)

```xml
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         version="5.0">
  <servlet>
    <servlet-name>HelloServlet</servlet-name>
    <servlet-class>com.example.HelloServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>HelloServlet</servlet-name>
    <url-pattern>/hello</url-pattern>
  </servlet-mapping>
</web-app>
```

---

## ✅ 4. Compile Servlet Class

Make sure you have the `servlet-api.jar` (from Tomcat or Jetty). Example:

```bash
javac -cp path/to/servlet-api.jar -d MyWebApp/WEB-INF/classes MyWebApp/src/com/example/HelloServlet.java
```

---

## ✅ 5. Package the WAR File

From the directory containing `MyWebApp/`:

```bash
jar -cvf MyWebApp.war -C MyWebApp .
```

This creates `MyWebApp.war`.

---

## ✅ 6. Deploy to Tomcat

* Copy `MyWebApp.war` to `tomcat/webapps/`
* Start Tomcat
* Open in browser: `http://localhost:8080/MyWebApp/hello`

---

## ✅ 7. Optional: Add `index.html`

```html
<!-- MyWebApp/index.html -->
<!DOCTYPE html>
<html><body>
<h1>Welcome</h1>
<a href="hello">Say Hello</a>
</body></html>
```

---

## 🧪 Summary

This example shows how to:

* Manually structure a Java web app
* Use servlet API with no Maven/Gradle
* Compile and package using `javac` + `jar`
* Deploy to Tomcat via `.war`

---

You can download `servlet-api.jar` (or its newer version `jakarta.servlet-api.jar`) from these **official sources**, depending on which version you need:

---

## ✅ **Jakarta EE (Modern Servlet API)**

Starting from **Jakarta EE 9**, the package name changed from `javax.servlet` to `jakarta.servlet`.

### 🔹 Latest (Jakarta Servlet API 6.x / EE 10)

* **Group ID**: `jakarta.servlet`
* **Artifact ID**: `jakarta.servlet-api`
* **Version**: `6.0.0` (or latest)
* **Download URL**:
  [https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/](https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/)

➡️ Direct JAR download:
[https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar](https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar)

---

## ✅ **Legacy Servlet API (Java EE / javax.servlet)**

### 🔹 Servlet 4.0 (for Java EE 8 / Tomcat 9)

* **Group ID**: `javax.servlet`
* **Artifact ID**: `javax.servlet-api`
* **Version**: `4.0.1`
* **Download URL**:
  [https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/](https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/)

➡️ Direct JAR download:
[https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar](https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar)

---

## ✅ Which One Should You Use?

| Servlet Version | Package Name      | Java EE / Jakarta EE | Use With   |
| --------------- | ----------------- | -------------------- | ---------- |
| 6.0             | `jakarta.servlet` | Jakarta EE 10+       | Tomcat 10+ |
| 4.0             | `javax.servlet`   | Java EE 8  

---

## Tomcat

Here’s how to **download**, **start**, and **stop Apache Tomcat** manually (without any build tools), step by step.

---

## ✅ 1. Download Apache Tomcat

### 🔗 Official Site:

Go to:
👉 [https://tomcat.apache.org/download-10.cgi](https://tomcat.apache.org/download-10.cgi)

### 📦 Recommended Version:

Choose **Tomcat 10** or **Tomcat 9** (based on your Servlet API version):

| Tomcat Version | Servlet API | Package Name        |
| -------------- | ----------- | ------------------- |
| Tomcat 10+     | Servlet 5/6 | `jakarta.servlet.*` |
| Tomcat 9       | Servlet 4   | `javax.servlet.*`   |

### 🧱 Example (Tomcat 10.1):

Direct download (Linux/macOS/Windows ZIP):
[https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.24/bin/apache-tomcat-10.1.24.zip](https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.24/bin/apache-tomcat-10.1.24.zip)

---

## ✅ 2. Extract Tomcat

```bash
unzip apache-tomcat-10.1.24.zip
cd apache-tomcat-10.1.24
```

---

## ✅ 3. Start Tomcat

Make the scripts executable (Linux/macOS):

```bash
chmod +x bin/*.sh
```

### ▶️ Start on Linux/macOS:

```bash
./bin/startup.sh
```

### ▶️ Start on Windows (CMD):

```cmd
bin\startup.bat
```

---

## ✅ 4. Stop Tomcat

### ⏹ Stop on Linux/macOS:

```bash
./bin/shutdown.sh
```

### ⏹ Stop on Windows:

```cmd
bin\shutdown.bat
```

---

## ✅ 5. Test in Browser

After starting Tomcat, open:

```
http://localhost:8080/
```

You should see the Tomcat welcome page.

---

## ✅ 6. Deploy Your App

* Copy your `.war` file to the `webapps/` folder:

```bash
cp MyWebApp.war apache-tomcat-10.1.24/webapps/
```

Tomcat auto-deploys WARs on startup or while running.

Access it:

```
http://localhost:8080/MyWebApp/
```

---

## Build and deply

- build

```shell
javac --class-path lib\jakarta.servlet-api-6.0.0.jar -d WEB-INF/classes src/p003/*.java

copy WEB-INF target/WEB-INF -Recurse
copy index.html target

jar --create --file web/p003.war -C target .

copy web/p003.war "$Env:TOMCAT/webapps/"
```
