import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Enter multicast ip-address.");
            return;
        }
        Client client;
        try {
            client = new Client(args[0]);
        } catch (IOException e) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(200);
                client.sendMsg("Bye");
                client.closeSockets();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e.getMessage());
            }
        }));

        while (true) {
            try {
                client.sendMsg("Hey");
                client.recvMsg();
                Thread.sleep(3000);
            } catch (IOException | InterruptedException ignored) {
                break;
            }
        }

    }
}
