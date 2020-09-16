import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private final long TTL = 100_000L; // TTL should be > than sleep time.
    private final MulticastSocket socketIn;
    private final DatagramSocket socketOut;

    private final InetAddress group;
    private byte[] buf = new byte[256];
    private final ArrayList<Status> copies;

    public DatagramSocket getSocketOut() {
        return socketOut;
    }

    public Client(String group) throws IOException {
        copies = new ArrayList<>();
        socketIn = new MulticastSocket(4444);
        socketOut = new DatagramSocket();

        this.group = InetAddress.getByName(group);
        socketIn.joinGroup(this.group);
    }

    private void printCopies() {
        System.out.println("Alive copies:");
        copies.forEach(e -> System.out.println(e.getAddress() + "   " + e.getPort()));
    }

    void recvMsg() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                socketIn.setSoTimeout(2000);
                socketIn.receive(packet);
            } catch (SocketTimeoutException ignored) {
                return;
            }

            String received = new String(packet.getData(), 0, packet.getLength());
            boolean removed = copies.removeIf(e -> System.currentTimeMillis() - e.getTime() > TTL
                    || ("Bye".equals(received) && e.equals(packet)));

            boolean newCopy = true;
            if (packet.getAddress() != null && !"Bye".equals(received)) {
                for (int i = 0; i < copies.size(); i++) {
                    if (copies.get(i).equals(packet)) {
                        copies.set(i, new Status(packet.getAddress(), packet.getPort()));
                        newCopy = false;
                    }
                }
                if (newCopy) {
                    copies.add(new Status(packet.getAddress(), packet.getPort()));
                }
            }
            if (newCopy || removed) {
                while (true) {
                    try {
                        socketIn.setSoTimeout(1000);
                        socketIn.receive(packet);
                        copies.removeIf(e -> System.currentTimeMillis() - e.getTime() > TTL
                                || ("Bye".equals(new String(packet.getData(), 0, packet.getLength()))
                                && e.equals(packet)));
                    } catch (SocketTimeoutException ignored) {
                        printCopies();
                        break;
                    }
                }
            }
        }
    }

    void sendMsg(String message) throws IOException {
        buf = message.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, socketIn.getLocalPort());
        socketOut.send(packet);

        boolean changed;
        changed = copies.removeIf(e -> System.currentTimeMillis() - e.getTime() > TTL);

        if (changed) {
            printCopies();
        }

    }
}
