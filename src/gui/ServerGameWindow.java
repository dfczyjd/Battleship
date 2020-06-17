package gui;

import java.net.Socket;

public class ServerGameWindow {
    static void runGame(String myName, String partnerName, Socket socket) {
        System.out.println(String.format("Server %s is playing with client %s", myName, partnerName));
    }
}
