

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import java.util.Timer; 
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
@ServerEndpoint("/websocket")
public class WebSocketTest {
	
    static final String GUEST_PREFIX = "Guest";
    static final AtomicInteger connectionIds = new AtomicInteger(0);
    static final Set<WebSocketTest> connections =
            new CopyOnWriteArraySet<WebSocketTest>();
    String burstMode;
	String burstCmd;
	String upRate;   
	private DeviceInfoEntry deviceInfoEntry;
	
	static byte[] currentCmd = new byte[] { 0x00, 0x00 };
    

    final String nickname;
    
	Session session;
	ServerSocket srvr=null;
	static Timer timer;
	boolean conIsClosed=true;
	HostTcpReceivePort hostReceiver;
	GwIncomingDataProcessor gwDataProcessor;
	
    public WebSocketTest() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
        session=this.session;
       
    }
    
	
		
 
  @OnMessage
  public void onMessage(String message, Session session)
    throws IOException, InterruptedException {
   
	  if(message.equals("requestDeviceList"))
	  {
		  getDeviceList();
	  }
		  
	  else if(message.startsWith("burstMod"))
	  {	  
		  
		  String[] msgArray = message.split(",");
		 // System.out.println(msgArray[0]);
		 // System.out.println(msgArray[1]);
		 // System.out.println(msgArray[2]);
		//  System.out.println(msgArray[3]);
		  
		  
		   if("burstMod".equals(msgArray[0])&&msgArray.length==5)
	   	   {   
		     
		    	// System.out.println(msgArray[4]);
			     String devID;
		    	 burstMode=msgArray[1];
		 		 burstCmd=msgArray[2];
		 		 upRate=msgArray[3];
		 		 devID=msgArray[4];
	             onBurstModeOk(devID);
		  }
	  }
	  
	  else if(message.startsWith("customCmd"))
	  {
         String revCmdPayLoad=message;	  
         //System.out.println(revCmdPayLoad);
         cusCmdProcess(revCmdPayLoad);
	  }
          
          //
   }         
     
	
     // session.getBasicRemote().sendText("This is the server message"+nickname+":"+Integer.toString(connections.size()));
	 // session.getBasicRemote().sendText("This is the server message");
  
   
  @OnOpen
  public void onOpen(Session session) {
	  
	   	    
	    this.session = session;
	    connections.add(this);
	    
     
   	  if(connections.size()==1)
	  { 
   		  
   		  timer = new Timer();
	      timer.schedule(new SendTimerTask(), 1000, 30000);
	      
	      gwDataProcessor = new GwIncomingDataProcessor( );
	  	  Thread gwDataProcessorThread = new Thread(gwDataProcessor);
	  	  gwDataProcessorThread.start();
     
          hostReceiver = new HostTcpReceivePort(gwDataProcessor);
 	      Thread receiverThread = new Thread(hostReceiver);
 	      receiverThread.start();
	  }
   	  
   	 System.out.println("websocket server has beed connected by client successfully");
  }
 
  @OnClose
  public void onClose() {
    //System.out.println("Connection closed");
      connections.remove(this);
	  
      if(connections.size()==0)
	  {
		  if(timer!=null)
	      {
    	     timer.cancel();
          }
	 
	   
	     if (hostReceiver != null) {
		     	hostReceiver.stop();
		  }
	     
	     if (gwDataProcessor != null) {
				gwDataProcessor.stop();
			}
	     
	  
	   
		int sizeOfDvList;
		sizeOfDvList=GwIncomingDataProcessor.deviceInfoList.size();
		
		DeviceInfoEntry tempDevice;
		
		
		if(!GwIncomingDataProcessor.deviceInfoList.isEmpty()){
			      
		   for (int i = 0; i < sizeOfDvList; i++) {
			   
			   	   
			   tempDevice=GwIncomingDataProcessor.deviceInfoList.get(i);
			 
			   if (tempDevice.isSubscribed())
	            {
	                // Stop the burst message
				   tempDevice.unsubscribe();
	            }
			   
		   }
		 }
      
	  }
	  
	  	  	  
	  System.out.println("the websocket server is closed");
	  
  }
  
	static void broadcast(String msg) {
        for (WebSocketTest client : WebSocketTest.connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {

            	WebSocketTest.connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
  
	void getDeviceList(){
		
		String deviceListInfo;
		int sizeOfDvList;
		sizeOfDvList=GwIncomingDataProcessor.deviceInfoList.size();
		
		
		if(!GwIncomingDataProcessor.deviceInfoList.isEmpty()){
			      
		   for (int i = 0; i < sizeOfDvList; i++) {
			   
			   String deviceId;
			   String burstCmd;
			   String updateRateindex;
			   String subscribeStatus; 
			   String active;
			   DeviceInfoEntry tempDevice;
			   int order;
			   
			   deviceListInfo="devInfo,";
			   tempDevice=GwIncomingDataProcessor.deviceInfoList.get(i);
			   deviceId=Converter.ByteArrayToHexString(tempDevice.getDeviceId()).toUpperCase();
			   burstCmd=Converter.ByteArrayToHexString(tempDevice.getBurstCmd());
			   	   
			   updateRateindex=Converter.ByteToHexString(tempDevice.getUpdateRate());
			   if(tempDevice.isSubscribed())
			   {
			      subscribeStatus="Subscribed";
			    }else{
			      subscribeStatus="Unsubscribed";
			    }
			   
			   if(tempDevice.isActive())
			   {
			      active="active";
			    }else{
			      active="inactive";
			    }
			     
			   order=i+1;			   
			   //发送设备列表
			   deviceListInfo+=sizeOfDvList+","+order+","+deviceId+","+burstCmd+","+updateRateindex+","+subscribeStatus+","+active;
			   
			   if(session.isOpen())
			   {
				   try {
					session.getBasicRemote().sendText(deviceListInfo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   }
		   }
			   
	   }
		
		
	}

	
	private void onBurstModeOk(String devID){
        boolean configChanged = false;
        byte burstCmdNumSelection = getCmdSelection(burstCmd);
        byte rateSelected = getRateSelection(upRate);
        boolean burstModeOn = getBurstMode(burstMode);
        deviceInfoEntry=getDevInfo(devID);
        
        configChanged = (burstModeOn != deviceInfoEntry.isSubscribed()) || (rateSelected != deviceInfoEntry.getUpdateRate()) || (burstCmdNumSelection != deviceInfoEntry.getCmdIndex());
        if (configChanged)
        {
            deviceInfoEntry.setUpdateRate(rateSelected);
            deviceInfoEntry.setCmdIndex(burstCmdNumSelection);

            // Determine the burst command number                   
            if (burstCmdNumSelection == 0)
            {
                deviceInfoEntry.setBurstCmd(new byte[] {0x00, 0x01});   
                System.arraycopy(deviceInfoEntry.getBurstCmd(), 0, currentCmd, 0, 2);
            }
            else if (burstCmdNumSelection == 1)
            {
                deviceInfoEntry.setBurstCmd(new byte[] {0x00, (byte) 0xaa});
                System.arraycopy(deviceInfoEntry.getBurstCmd(), 0, currentCmd, 0, 2);
            }

            // Start or modify a burst message
            if (burstModeOn)
            {            
                if (deviceInfoEntry.isSubscribed()) // if already subscribed, modify the subscription
                {
                    //deviceInfoEntry.unsubscribe();
                    deviceInfoEntry.modifySubscribe();
                }
                else
                {
                    // Start a new subscription
                    deviceInfoEntry.subscribe();
                }
            }
            else if (deviceInfoEntry.isSubscribed())
            {
                // Stop the burst message
                deviceInfoEntry.unsubscribe();
            }
        }
                
    }
	
	  public byte getCmdSelection(String burstcmd) {
	        byte ret = 0;
	        if(burstcmd.equals("cmd1"))
	            ret = (byte) 0;
	        if(burstcmd.equals("cmd170"))
	            ret = (byte) 1;
	        return ret;
	    }   
	
	  private byte getRateSelection(String uprate) {
			byte ret = 0;

			  ret=(byte)Integer.parseInt(uprate);
		      
			return ret;        
		    }
	  
	  private boolean getBurstMode(String burstmode) {
		  if(burstmode.equals("on"))
			{
				return true;
			}
		  else{
			   return false;
		  }
		    
	  }
	  
	  private DeviceInfoEntry getDevInfo(String devid){
		  
		 DeviceInfoEntry selectedDev=null;
		 int devListSize=GwIncomingDataProcessor.deviceInfoList.size();
	     byte[] temDevID;
	     for(int i=0;i<devListSize;i++)
	     {
	    	 
	    	 temDevID=GwIncomingDataProcessor.deviceInfoList.get(i).getDeviceId();
	    	 if(devid.equals(Converter.ByteArrayToHexString(temDevID).toUpperCase()))
	    	 {
	    		 selectedDev=GwIncomingDataProcessor.deviceInfoList.get(i);
	    		 	    		 
	    	 }
	    	 
	     }
	    	 return selectedDev;
    
		  
	  }
	  
	  
	  
	  
	  public byte[] extractBytesFromTextArea(String msg) {
			String strOriginal = msg;
			String str = "";

			//for empty payload, return empty array
			if (strOriginal.equals("")) {
				return new byte[0];
			}

			
			//remove "0x" if user typed it
			if (strOriginal.length() >= 2) {
				if ((strOriginal.substring(0, 2)).equals("0x")) {
					strOriginal = strOriginal.substring(2);
				}
			}

			//remove any special characters from input, such as '\n'
			for (int i = 0; i < strOriginal.length(); i++) {
				char temp = strOriginal.charAt(i);
				if (Character.isLetter(temp) || Character.isDigit(temp)) {
					str = str + temp; //add this character to input string
				}
			}

			//convert hex string to bytes
			byte[] tempData = null;
			try {
				tempData = Converter.HexStringToByte(str); //will truncate incomplete(odd number of) bytes (must have 2 hex chars per byte)
			}
			catch (Exception e) {
				//message("Invalid Input: " + str + ".");
				String sendMsg;
				sendMsg="payLoadCheck"+","+"Invalid Input: " + str + ".";
			
				 try {
					 session.getBasicRemote().sendText(sendMsg);
					 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
					 }
								
				return null; //do not sent any data to gateway.
			}
			
			//warn user if they input odd number of hex chars
			if (str.length() % 2 != 0) {
				//message("Warning: Uneven hex string, truncating last hex charactater\n");
				String sendMsg;
				sendMsg="payLoadCheck"+","+"Warning: Uneven hex string, truncating last hex charactater\n";
				try {
					 session.getBasicRemote().sendText(sendMsg);
					 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
					 }
			//	broadcast(sendMsg);

			}

			int length = tempData.length; //may be any number of bytes
			//restrict # of bytes to 72 or less
			if (length > 88) {
				length = 88;
				//message("Warning: Truncating input to 72 bytes\n");
				String sendMsg;
				sendMsg="payLoadCheck"+","+"Warning: Truncating input to 72 bytes\n";
				try {
					 session.getBasicRemote().sendText(sendMsg);
					 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
					 }
			}

			//return up to 88 bytes parsed from text area.
			byte[] payload = new byte[length];
			System.arraycopy(tempData, 0, payload, 0, length); //keep only the first 88 bytes in data
			return payload;
		}
	  
	
	  public void cusCmdProcess(String revCmdPayLoad) {
	        byte[] customCmdNum = new byte[2];
			byte[] customCmdPayload;
			
			
			String[] msgArray = revCmdPayLoad.split(",");
					// Convert command number to 2 bytes
			int number=Integer.parseInt(msgArray[1]);
			
			byte[] temp = Converter.IntToByteArray(number); // return 4 byte array
			customCmdNum[0] = temp[2];
			customCmdNum[1] = temp[3];

			// Get the user input payload
			customCmdPayload = extractBytesFromTextArea(msgArray[2]);
	                if (customCmdPayload == null) {
				return; // null payload indicates an error occured.
			}
			// Update output 
			String sendMsg;
	        if (customCmdPayload.length > 0) {
				sendMsg="cusCmdBack"+","+ "Sending Cmd " + number+ "\nPayload: 0x" + Converter.ByteArrayToHexString(customCmdPayload).toUpperCase() + "\n" ;
				try {
					 session.getBasicRemote().sendText(sendMsg);
					 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
					 }
									
			//	textAreaHartCommandLog.append("Sending Cmd " + number);   //
			//	textAreaHartCommandLog.append("\nPayload: 0x" + Converter.ByteArrayToHexString(customCmdPayload).toUpperCase() + "\n");
			}
			else {
			//	textAreaHartCommandLog.append("Sending Cmd " + number + ": \n");
				sendMsg="cusCmdBack"+","+"Sending Cmd " + number + ": \n";
				try {
					 session.getBasicRemote().sendText(sendMsg);
					 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
					 }
							
			}
			
			byte[] deviceId= Converter.HexStringToByte(msgArray[3]) ;
			
			// Send command + payload to Gateway
			HostQueryEntry byteArray = new HostQueryEntry((byte) 8, deviceId, customCmdNum, customCmdPayload);
			HostTcpSendPort.sendToGatewayFromHost(byteArray.getByteArray());
	    }
	  
	  
	  
	  
	  
	  
  /*
  public void ontest() {
	
	  while(conIsClosed)
	  {
		  try{
		  Thread.sleep(5000);
          this.session.getBasicRemote().sendText("This is the last server message");
		  }
		  catch (Exception e) {
				e.printStackTrace();
	     }
      }
  }
  */
}