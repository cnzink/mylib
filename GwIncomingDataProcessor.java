
import java.io.IOException;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;




/**
 * Data Processor that handles incoming messages from the Gateway
 */
public class GwIncomingDataProcessor implements Runnable
{
    //Host host; //reference to the Host

    private boolean stop = false;
    private final ConcurrentLinkedQueue<byte[]> incomingBytes = new ConcurrentLinkedQueue<>();
    static ArrayList<DeviceInfoEntry> deviceInfoList; 
   // private byte[] currentCmd = new byte[] { 0x00, 0x00 };
   
    
    /**
     *
     * @param h Reference to the Host
     */
    public GwIncomingDataProcessor()
    {
      //  host = h;
    	deviceInfoList = new ArrayList<DeviceInfoEntry>(); 
    }

    /**
     * Used by TcpReceivePort to add messages into a buffer
     * @param data Message received from TCP port.
     */
    public void addNpduBytes(byte[] data) 
    {
        incomingBytes.add(data);
    }

	
    
	public void addDevicesFromTagList(byte[] tagList) {
        int numDevices = tagList.length / 6;
        byte[] deviceId = new byte[6];
        DeviceInfoEntry dev;
        int i = 0;                
             
        
        for (i = 0; i < deviceInfoList.size(); i++) {
	deviceInfoList.get(i).setActiveStatus(false);// set all device's status to inactive
        }

        for (i = 0; i < numDevices; i++) {
	for (int j = 6 * i; j < 6 * i + 6; j++) {
                deviceId[j - (6 * i)] = tagList[j]; // assigns tempId[0-5] = tagList[6n] through tagList[6n+6]
	}

                dev = new DeviceInfoEntry(deviceId);
                addToDeviceInfoList(dev); // Add or Update the device
               
        }		
        
}
	
	private void addToDeviceInfoList(DeviceInfoEntry device) {
		DeviceInfoEntry tempDevice;
		boolean exists = false;
		
		// Check if device already exists, if yes, update it.
		for (int i = 0; i < deviceInfoList.size(); i++) {
			tempDevice = deviceInfoList.get(i);
			if (device.equals(tempDevice)) {
				tempDevice.setActiveStatus(true);
				exists = true;
				break;
			}
		}
		
		// If device does not exist, add it.
		if (exists == false) {
			deviceInfoList.add(device);
		}
	}
	
	public byte[] getSelectedCmd() {
		return WebSocketTest.currentCmd;
	}
    
    @Override
    public void run()
    {
        while(!stop)
        {
            try
            {
                Thread.yield();
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
           
            
            byte[] nextMessage = incomingBytes.poll();
            if( nextMessage == null )
            {
                
            	continue;
                
            }
            else
            {
                try {
                	   	Handler(nextMessage);
                } catch (Exception ex) {
                    Logger.getLogger(GwIncomingDataProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Stops this data processor.
     */
    public void stop()
    {
        this.stop = true;
    }

	// Process commands that are received from the Gateway.
	void Handler(byte[] msg) throws IOException, Exception {
		HostQueryEntry input = new HostQueryEntry(msg);
		int APIindex = (int) input.getIndex();
		final byte[] tagList;
		byte[] deviceID;
		final byte[] dataValue;
		byte[] customCmdNum;
		final byte[] payload;
		byte[] timestampBytes;

		switch (APIindex) {
		case 5: // TagList(tag list)
			tagList = input.getTagList();
            //相关设备信息放入设备列表
			//WebSocketTest.broadcast("this is device taglist process");
			//System.out.println("dataprocess process is running");
			  if(tagList.length==0)
		        {
				     int i;
		        	 for (i = 0; i < deviceInfoList.size(); i++) {
		        			deviceInfoList.get(i).setActiveStatus(false);// set all device's status to inactive
		        		        }
		         }
			  else{	
						addDevicesFromTagList(tagList);
			  }
                        break;

		case 6: // Data(device tag, data value). For Read or Subscribe
			//如果是设备采集数据，则
			byte[] cmdNumBytes = getSelectedCmd();
			final int hostCurrentCmd = (cmdNumBytes[0] << 8) + cmdNumBytes[1]; 
			
			dataValue = input.getDataValue();
			if (dataValue.length == 0)
				break;
			deviceID = input.getDeviceId();
			timestampBytes = input.getTimestamp();

			//final String deviceIdAndTimestamp = getTimeFromBytes(timestampBytes) + " (" + Converter.ByteArrayToHexString(deviceID ).toUpperCase()+")";
			
   			              
		
					
			// Get the current burst cmd num in string format, add to deviceID and timestamp 
			String cmdNumStr = Converter.ByteArrayToHexString(getSelectedCmd());
			String strData=Converter.ByteArrayToHexString(dataValue);
			if (dataValue.length == 0) {
				strData=" data is null";
			}
			else {
				// If the command number is 1, and data can be interpreted as a real number,
				//  then update Host by displaying a real number and updating graph
				if (hostCurrentCmd == 1 && dataValue.length % 4 == 0) {
					double doubleData = Converter.bytesToRealNumber(dataValue);
					
					strData = Double.toString(doubleData);
					
					
				}
				else {
					// If the command number != 1, then just display the data as hex bytes
					
					strData = Converter.ByteArrayToHexString(dataValue).toUpperCase();
					
				}
			}
			
			String msgTime=getTimeFromBytes(timestampBytes);
			String msgDevID=Converter.ByteArrayToHexString(deviceID ).toUpperCase();
			
			//String info = "devData"+ "," +cmdNumStr + "," + deviceIdAndTimestamp+ "," +strData;
			String info = "devData"+ "," +msgTime+"," +msgDevID+ ","+cmdNumStr + "," +strData;
			
            WebSocketTest.broadcast(info);
			
			break;

		case 9: // Custom Cmd(device tag, cmd number, payload)
			// The gateway sends back the response to the custom cmd sent by the user, so display the command number + payload on the Host.

			// Get the command number
			customCmdNum = input.getCustomCmdNum(); //2 bytes
			final int cmdNumInteger = (customCmdNum[0] << 8) + customCmdNum[1]; // convert 2 byte command number into Integer

			// Get the payload (response code is the first byte in payload)
			payload = input.getPayload();
			final byte[] payload2 = new byte[payload.length - 1]; //holds the payload except for status byte
			System.arraycopy(payload, 1, payload2, 0, payload2.length); //remove status byte payload and copy the rest into payload2

			String custmoInfo;
			if (payload2.length != 0) {
					//host.getCmdDlg().displayResponse("Received Cmd " + Integer.toString(cmdNumInteger) + " Response:\n  Status: " + (int) payload[0] + "\n  Payload: 0x" + Converter.ByteArrayToHexString(payload2).toUpperCase());
		            custmoInfo = "cusCmdBack"+","+"Received Cmd " + Integer.toString(cmdNumInteger) + " Response:\n  Status: " + (int) payload[0] + "\n  Payload: 0x" + Converter.ByteArrayToHexString(payload2).toUpperCase();
				  				
	                WebSocketTest.broadcast(custmoInfo);
				
			        }
					else {
					//	host.getCmdDlg().displayResponse("Received Cmd " + Integer.toString(cmdNumInteger) + " Response:\n  Status: " + (int) payload[0]);
				    custmoInfo = "cusCmdBack"+","+"Received Cmd " + Integer.toString(cmdNumInteger) + " Response:\n  Status: " + (int) payload[0];
				    WebSocketTest.broadcast(custmoInfo);
					}
					
			break;

		default: // Do nothing
			break;
		}
	}

	// Convert timestamp from bytes to string
    public String getTimeFromBytes(byte[] time) {
        // Returns a string in this format: 
    	// yyyy/MM/dd hh:mm:ss a

    	int year = (time[0] * 100) + time[1];
    	int month = time[2];
    	int day = time[3];
    	int hour = time[4];
    	int minute = time[5];
    	int second = time[6];
    	int am = time[7]; //am = 0, pm = 1
    	
    	String output = "";
    	output += Integer.toString(year) + "/";
    	output += String.format("%02d", month) + "/";
    	output += String.format("%02d", day) + " ";
    	output += String.format("%02d", hour) + ":";
    	output += String.format("%02d", minute) + ":";
    	output += String.format("%02d", second) + " ";
    	
    	if(am == 0)
    		output += "AM";
    	else
    		output += "PM";
    	
    	return output;
    }
}