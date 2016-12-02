
import java.io.IOException;
import java.util.Timer;       
public class SendTimerTask extends java.util.TimerTask{ 
	
    private boolean connectedToGateway = false; 
    private void readDeviceList() {
		byte[] pkt = new byte[2];
		pkt[0] = 10;
		pkt[1] = (byte) 1; // 1 == Read tag list
		if (HostTcpSendPort.sendToGatewayFromHost(pkt))
	            connectedToGateway = true;
		else
	            connectedToGateway = false;
		}
        
        boolean ifSendCon=false;    
        boolean ifSendNotCon=false;

	
	
    @Override 
    public void run() { 
       // TODO Auto-generated method stub 
       //System.out.println("start"); 
       readDeviceList();
       //send connection status infomation to client and avoid to send repeatly
       if(connectedToGateway == true && ifSendCon == false)
       {
           WebSocketTest.broadcast("conStatus"+","+"The gateway server  is connected ");
           ifSendCon = true;
           ifSendNotCon=false;
       }
       else if(connectedToGateway == false && ifSendNotCon == false)
       {
    	   WebSocketTest.broadcast("conStatus"+","+"The gateway server  is disconnected ");
    	   ifSendNotCon = true;
    	   ifSendCon=false;
       }
       
    } 
} 
