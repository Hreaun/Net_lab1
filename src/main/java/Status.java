import java.net.DatagramPacket;
import java.net.InetAddress;

public class Status {
    private final InetAddress address;
    private final int port;
    private final Long time;

    public Status(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.time = System.currentTimeMillis();
    }

    boolean equals(DatagramPacket packet) {
        return packet.getAddress().toString().equals(this.address.toString()) && packet.getPort() == this.port;
    }

    public Long getTime() {
        return time;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
