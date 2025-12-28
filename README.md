# RT-CHAT Application

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)

**RT-CHAT** is a robust, multi-threaded real-time chat application built with Java Swing and Socket programming. It features a modern, responsive interface with support for both private one-on-one messaging and global server-wide announcements.

## 🚀 Features

*   **🎨 Modern UI**: Sleek interface with a toggleable **Dark/Light Mode**.
*   **🔒 Private Messaging**: Secure, direct messaging between users.
*   **📢 Global Announcements**: A dedicated channel for broadcasting messages to all connected users.
*   **🔔 Unread Counters**: Visual notification counters for missed messages.
*   **😊 Emoji Support**: Integrated emoji picker for expressive communication.
*   **👤 Dynamic Avatars**: Auto-generated user avatars with initials.
*   **⚡ Real-time Updates**: Instant message delivery and status updates (Join/Leave).
*   **🕒 Chat History Retention**: New users receive the last 50 messages upon joining.
*   **💤 Offline Status**: Users who leave are marked as "(Offline)" and can still be messaged or viewed.

## 🛠️ Tech Stack

*   **Language**: Java (JDK 8+)
*   **GUI Framework**: Swing & AWT
*   **Networking**: Java Sockets (TCP/IP)
*   **Architecture**: Client-Server Model (Multi-threaded)

## 📂 Project Structure

```
rt-chat/
├── src/
│   └── chat/
│       ├── ChatServer.java    # Central server managing connections
│       ├── ChatClient.java    # Client UI and Logic (Single Optimized File)
│       └── ClientHandler.java # Thread handler for individual clients
└── README.md                  # Project Documentation
```

## ⚙️ Installation & Setup

### Prerequisites
*   Java Development Kit (JDK) 8 or higher installed.

### Steps
1.  **Clone the Repository**
    ```bash
    git clone https://github.com/talibk90/RT-CHAT.git
    cd RT-CHAT
    ```

2.  **Compile the Code**
    ```bash
    javac -d bin -sourcepath src src/chat/*.java
    ```

## 🖥️ Usage

1.  **Start the Server** (Run this first)
    ```bash
    java -cp bin chat.ChatServer
    ```

2.  **Start a Client** (Open a new terminal for each user)
    ```bash
    java -cp bin chat.ChatClient
    ```

3.  **Connect**:
    *   Enter your **Display Name**.
    *   Enter **Server IP** (default: `localhost`).
    *   Enter **Port** (default: `12345`).

## 👤 Author
**TALIB KHAN**
