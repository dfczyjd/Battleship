package gui;

import java.net.Socket;

public class ClientGameWindow {
    static void runGame(String myName, String partnerName, Socket socket) {
        System.out.println(String.format("Client %s is playing with server %s", myName, partnerName));
    }
}
