
// This class is used by the Host to control the subscribe status 
//  and connection status of each device 
public class DeviceInfoEntry {
	private final byte[] deviceId;
	private final byte[] burstCmd;
	private byte updateRateindex;
	private byte cmdIndex;
	private boolean subscribeStatus; // If the host is subscribed to this device or not
	private boolean active = false;  // Device's connection status to the Gateway

	public DeviceInfoEntry(byte[] id) {
		deviceId = new byte[6];
		System.arraycopy(id, 0, deviceId, 0, 6);
		burstCmd = new byte[] { 0x00, 0x01 };
		updateRateindex = 5; // Default update rate (4 seconds)
		subscribeStatus = false;
		active = true;
	}

	// Set the device ID
	public void setDeviceId(byte[] id) {
		System.arraycopy(id, 0, deviceId, 0, 6);
	}

	// Set the burst command number
	public void setBurstCmd(byte[] cmd) {
		System.arraycopy(cmd, 0, burstCmd, 0, 2);
	}

	// Get the burst command number
	public byte[] getBurstCmd() {
		return burstCmd;
	}

	//Set the update rate for a subscribe on this device
	public void setUpdateRate(byte index) {
		updateRateindex = index;
	}

	// Set the subscription status for this device
	public void setSubscribedStatus(boolean b) {
		subscribeStatus = b;
	}

	// Check if this device has an active subscription
	public boolean isSubscribed() {
		return subscribeStatus;
	}

	// Set the active status of this device
	public void setActiveStatus(boolean b) {
		active = b;
	}

	// Check if this device is actively joined to the Gateway
	public boolean isActive() {
		return active;
	}

	// Get this device's update rate
	public byte getUpdateRate() {
		return updateRateindex;
	}

	// Get command index
	public byte getCmdIndex() {
		return cmdIndex;
	}
        
	// Set command index
	public void setCmdIndex(byte i) {
		cmdIndex = i;
	}

	//Equals
	public boolean equals(DeviceInfoEntry dev) {
		for (int i = 0; i < 6; i++) {
			if (this.deviceId[i] != dev.deviceId[i]) // replace with Arrays.equals()
				return false;
		}
		return true;
	}

	// Get the device ID
	public byte[] getDeviceId() {
		return deviceId;
	}

	// Start a subscribe to a device
	public void subscribe() {
            if (isActive()) {		
		setSubscribedStatus(true);
		HostQueryEntry bytearray = new HostQueryEntry((byte) 2, deviceId, updateRateindex, burstCmd);
		HostTcpSendPort.sendToGatewayFromHost(bytearray.getByteArray());
            }
	}
        
        // Modify an existing subscription
        public void modifySubscribe() {
            HostQueryEntry output = new HostQueryEntry((byte) 11, deviceId, updateRateindex, burstCmd);
            HostTcpSendPort.sendToGatewayFromHost(output.getByteArray());
        }
        
	// Stop a subscribe from a device
	public void unsubscribe() {
		setSubscribedStatus(false);
		HostQueryEntry bytearray = new HostQueryEntry((byte) 3, deviceId);
		HostTcpSendPort.sendToGatewayFromHost(bytearray.getByteArray());
	}
}
