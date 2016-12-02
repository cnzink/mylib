



import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.SwingUtilities;
import java.util.Timer; 
import java.nio.ByteBuffer;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
@ServerEndpoint("/simplewebsocket")
public class SimpleWebSocket {
	

		
 
  @OnMessage
  public void onMessage(String message, Session session)
    throws IOException, InterruptedException {
        
    session.getBasicRemote().sendText("This is the first server message"+ message);
       
  }
   
  @OnOpen
  public void onOpen() {
    System.out.println("websocket simple ex Client connected");
   
	    
  }
 
  @OnClose
  public void onClose() {
    //System.out.println("websocket simple ex Connection closed");
  }
  
}