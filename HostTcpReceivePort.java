
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * This TCP receive port puts all data that it receives into a buffer. A
 * GwIncomingDataProcessor processes the data.
 */
public class HostTcpReceivePort implements Runnable {

	private boolean stop = false;
	private boolean hasStopped = false;

	ServerSocket srvr;

	protected static final int READ_BYTE_LEN = 20;
	GwIncomingDataProcessor gwProcessor;
	
	
	//Session session;
	//String sessionid=null;

	
	/**
	 * 
	 * @param portNumber
	 *            Port number for ServerSocket to use
	 * @param gwProcessor
	 *            Data processor
	 * @throws IOException
	 */
	public HostTcpReceivePort(GwIncomingDataProcessor gwProcessor) {
		
		try{
		srvr = new ServerSocket(8891);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		//this.session=session;
		//sessionid=session.getId();
		this.gwProcessor=gwProcessor;
			
	}

	

	
		
	/**
	 * Receive data from port, add it to the buffer of the data processor
	 */
        @Override
	public void run() {

		try {
			while (!stop) {
				Thread.yield();
				Thread.sleep(5); // sleep is not necessary
				//broadcast("The number of connected cliets is"+WebSocketTest.connections.size());
				//String sessionnow=session.getId();
				
				//if(session.isOpen())
				//    {
				//    	 session.getBasicRemote().sendText("hahdfdfdsafdafdafdafdafdafdafda");
				//  	 }
				
				try {
					srvr.setSoTimeout(100);
					Socket skt = srvr.accept();
					InputStream in = skt.getInputStream();

					byte[] npduBytes = new byte[0];
					byte[] incomingBytes = new byte[READ_BYTE_LEN];

					int inputSize = -1;
					while ((inputSize = in.read(incomingBytes)) > -1) {
						byte[] newNpduBytes = new byte[npduBytes.length + inputSize];
						System.arraycopy(npduBytes, 0, newNpduBytes, 0, npduBytes.length);
						System.arraycopy(incomingBytes, 0, newNpduBytes, npduBytes.length, inputSize);
						npduBytes = newNpduBytes;
					}
					
					//String news=Converter.ByteArrayToHexString(npduBytes).toUpperCase();
				//	String news=Converter.ByteArrayToHexString(npduBytes);
				//	if(session.isOpen())			
				//	{
				//		session.getBasicRemote().sendText(news);
				//	}
					
                //    ByteBuffer buf = ByteBuffer.wrap(npduBytes);
                  //  if(session.isOpen())
                 //   {
                 //   	session.getBasicRemote().sendBinary(buf);
                 //   }
					
					//WebSocketTest.broadcast("receive a msg by receive process");
					//System.out.println("recv process is running");
					gwProcessor.addNpduBytes(npduBytes);
				
					skt.close();
				} catch (SocketTimeoutException ste) {
					// do nothing
					// want to check stop condition
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				srvr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			hasStopped = true;
		}
	}

	/**
         *
         */
	public void stop() {
		this.stop = true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasStopped() {
		return hasStopped;
	}
}
