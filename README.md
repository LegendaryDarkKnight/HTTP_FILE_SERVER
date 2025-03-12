# HTTP_FILE_SERVER

## Introduction
I have developed a web server that handles multiple incoming HTTP GET requests and responds with an HTTP response message, including the appropriate `Content-Type` header according to the standard HTTP protocol. This project was developed as part of the **CSE-322 (Computer Networking)** course at **BUET CSE**.

The project was built using **Java** and developed in **IntelliJ IDEA**, but it can also be run in other environments. 
- The **server** is implemented in Java.
- The **client** can be either a web browser or a Java-based client application.

To learn more about the project details and requirements, see the [Assignment_1](Assignment_1) file in the repository.

---

## Requirements
1. **Java SE Development Kit (JDK)**
    - This project was tested on **Java(TM) SE Runtime Environment (build 23.0.1+11-39)**
    - **Get the latest JDK here:** [JDK Download](https://www.oracle.com/java/technologies/downloads/)

---

## Installation & Usage
### 1. Clone the repository
```bash
git clone https://github.com/LegendaryDarkKnight/HTTP_FILE_SERVER.git
```

### 2. Navigate to the `src` directory and create a `root` directory
The `root` directory will be used as the root filesystem for the web server.
```bash
cd src
mkdir root
```

### 3. Compile and Start the Server
```bash
javac Server/ServerThread.java Server/Server.java
java Server.Server
```

### 4. Access the Server from a Browser
Open your web browser and visit:
```
http://localhost:5033
```

### 5. Start the Client (For Uploading Files)
Open another terminal instance and run the client application:
```bash
javac Client/ClientThread.java Client/Client.java
java Client.Client
```

### Notes:
- The **command-line client** can only upload files that are located in the `src` directory.
- The **web client** can browse, view, and download files but does not support uploading.

---

## License
This project is developed as part of an academic assignment and is free to use for learning purposes. Feel free to modify and experiment with it!

---

For any issues or contributions, feel free to submit an issue or pull request on the repository. 🚀