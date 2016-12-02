

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Allows the Host to send data using TCP port
 */
public class HostTcpSendPort {

	/**
	 * Sends a message to the Gateway
	 * 
	 * @param npduBytes
	 *            The bytes to send
	 */
	public static boolean sendToGatewayFromHost(byte[] npduBytes) {
		return send("192.168.1.101", 8890, npduBytes);
	}

	/**
	 * Send a message
	 * 
	 * @param host
	 *            The host to send the message to
	 * @param port
	 *            The port number to use
	 * @param npduBytes
	 *            The data to send
	 */
	public static boolean send(String host, int port, byte[] npduBytes) {
		boolean ret = true;
		try {
                        Socket skt = new Socket();
                        skt.connect(new InetSocketAddress(host, port), 5000); // Attempt to connect, and set a specific timeout

			PrintStream out = new PrintStream(skt.getOutputStream(), true);

			out.write(npduBytes);
			out.flush();

			out.close();
			skt.close();
		} catch (UnknownHostException uhe) {
			System.err.println("Location configured incorrectly: " + uhe.getMessage());
			ret = false;
		} catch (IOException ioe) {
			ret = false;
		}
		return ret;
	}
}
