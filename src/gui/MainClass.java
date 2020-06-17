package gui;

public class MainClass {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please specify \"client\" or \"server\" option");
            return;
        }
        if (args[0].equals("Client"))
            ClientConnectionWindow.start(args);
        else if (args[0].equals("Server"))
            ServerConnectionWindow.start(args);
        else
            System.err.println("Please specify \"Client\" or \"Server\" option");
    }
}
