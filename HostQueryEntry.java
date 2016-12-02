
import java.util.Arrays;

/**
 * Each HostQueryEntry holds one request/response (e.g ReadTagList, Subscribe)
 * between the Host and the Gateway.
 */
public class HostQueryEntry {

	private byte[] result; //holds final byte array
	//this are individual values of the byte array
	private byte protocol = 10; //10 for simple data access, 11 for control in gateway
	private byte index; //API index
	private byte[] deviceTag = new byte[6];
	private byte updateRate;
	private byte[] dataValue;
	private byte[] tagList;
	private byte[] burstCmdNum = new byte[2]; //2 byte number
	private byte[] customCmdNum = new byte[2]; //used for sending a custom cmd to device
	private byte[] payload; //payload data for a custom cmd
	private byte[] timestamp;

	//Constructor to obtain individual values, input is a byte array.

	/**
	 * Create HostQueryEntry using a byte array
	 * 
	 * @param input
	 *            The byte array that will be parsed into corresponding
	 *            HostQueryEntry data
	 */
	public HostQueryEntry(byte[] input) {
		result = new byte[input.length];
		System.arraycopy(input, 0, result, 0, input.length); //copy input array into result:

		int payloadLength;

		//obtain individual data values from the byte array:
		if (result[0] == protocol) //if first byte is simple data access (1), then continue. Otherwise, do nothing.
		{
			index = result[1]; //get the API index
			switch ((int) index) //determine which values to get based on API index.
			{
			case 1: //ReadTagList()
				break;

			case 2: //Subscribe(deviceTag, updateRate, burstCmdNum)
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				updateRate = result[8];
				burstCmdNum[0] = result[9];
				burstCmdNum[1] = result[10];
				break;

			case 3: //Unsubscribe(deviceTag)
			case 4: //Read(deviceTag)
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				break;

			case 5: //TagList(tag list) - copies a variable amount of deviceTags into byte array.
				tagList = new byte[result.length - 2];
				System.arraycopy(result, 2, tagList, 0, tagList.length); //get device tag, skip first two bytes
				break;

			case 6: //GW->Host. Data(deviceTag, dataValue): read data from device.
				// Assumption: timestamp is always 8 bytes.
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				dataValue = new byte[result.length - (8 + 8)]; //determine the number of bytes for dataValue. 8 + 8 = first 2 bytes + 6 bytes for device tag + 8 timestamp
				System.arraycopy(result, 8, dataValue, 0, dataValue.length); //get data value
				this.timestamp = new byte[8];
				System.arraycopy(result, 8 + dataValue.length, this.timestamp, 0, this.timestamp.length);
				break;

			case 7: //Write(deviceTag, dataValue):  write a data value to the device. From host to gateway
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				dataValue = new byte[result.length - 8]; //determine the number of bytes for dataValue. 8 = first 2 bytes + 6 bytes for device tag
				System.arraycopy(result, 8, dataValue, 0, dataValue.length); //get data value
				break;

			case 8: //Custom Cmd Request(device tag, customCmdNum, payload):
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				customCmdNum[0] = result[8]; //byte 8 and 9 are the cmd number
				customCmdNum[1] = result[9];
				payloadLength = result.length - 10;
				if (payloadLength == 0) {
					payload = new byte[0];
				} else {
					payload = new byte[payloadLength]; //byte 10 and beyond is payload.
					System.arraycopy(result, 10, payload, 0, payloadLength); //copy bytes into payload
				}
				break;

			case 9: //Custom Cmd Response(device tag, customCmdNum, payload, timestamp):
				// Assumption: timestamp is always 8 bytes.
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				customCmdNum[0] = result[8]; //byte 8 and 9 are the cmd number
				customCmdNum[1] = result[9];
				payloadLength = result.length - (10 + 8); // cmdNum(2) + device tag (6) + timestamp(8)
				if (payloadLength == 0) {
					payload = new byte[0];
				} else {
					payload = new byte[payloadLength];
					System.arraycopy(result, 10, payload, 0, payloadLength); //copy bytes into payload
				}

				this.timestamp = new byte[8];
				System.arraycopy(result, 10 + payloadLength, timestamp, 0, timestamp.length);

				break;
                            
                        case 11: // Modify current subscription
				System.arraycopy(result, 2, deviceTag, 0, deviceTag.length); //get device tag, skip first two bytes
				updateRate = result[8];
				burstCmdNum[0] = result[9];
				burstCmdNum[1] = result[10];
				break;
			}
		}
	}

	//Constructors to create a single byte array, inputs are individual values.

	/**
	 * Create HostQueryEntry for ReadTagList
	 * 
	 * @param index
	 *            API index
	 */
	public HostQueryEntry(byte index) //index is the API index for a function
	{
		//ReadTagList() - 2 bytes
		result = new byte[2];
		result[0] = protocol; //simple data access
		result[1] = index; //readTagList's index = 1

		this.index = index;
	}

	/**
	 * Create HostQueryEntry for Subscribe
	 * 
	 * @param index
	 *            API index
	 * @param deviceTag
	 *            The device tag
	 * @param updateRate
	 *            The periodic update rate for the subscribe
	 * @param burstCmdNum
	 *            Choose between Cmd1 or Cmd170
	 */
	public HostQueryEntry(byte index, byte[] deviceTag, byte updateRate, byte[] burstCmdNum) {
		//Subscribe(device tag, update rate, burstCmdNum) - 11 bytes
		result = new byte[11];
		result[0] = protocol; //simple data access
		result[1] = index; // Subscribe's index = 2
		System.arraycopy(deviceTag, 0, result, 2, deviceTag.length); //copy deviceTag into result[2] through result[7]
		result[8] = updateRate;
		result[9] = burstCmdNum[0];
		result[10] = burstCmdNum[1];

		this.index = index;
		this.updateRate = updateRate;
		System.arraycopy(deviceTag, 0, this.deviceTag, 0, this.deviceTag.length);
		this.burstCmdNum[0] = burstCmdNum[0];
		this.burstCmdNum[1] = burstCmdNum[1];
	}

	/**
	 * Create HostQueryEntry for Read, Unsubscribe, or TagList
	 * 
	 * @param index
	 *            API index
	 * @param deviceTags
	 *            May one 1 or more device tags, depending on the desired
	 *            function
	 */
	public HostQueryEntry(byte index, byte[] deviceTags) {
		//deviceTags may be one or multiple deviceTags.

		//Read(device tag) - 8 bytes
		//Unsubscribe(device tag) - 8 bytes
		//TagList(tag list) - 2 + 6n bytes. n = # of tags
		result = new byte[deviceTags.length + 2];
		result[0] = protocol; //simple data access
		result[1] = index; //Unsubscribe's index = 3, Read's index = 4, TagList's index = 5
		System.arraycopy(deviceTags, 0, result, 2, deviceTags.length); //copy 1 or more deviceTags into result[2] through result[2+6n]

		this.index = index;
		if (index == 3 || index == 4) //Read and Unsubscribe
		{
			System.arraycopy(deviceTags, 0, this.deviceTag, 0, deviceTag.length); //get single device tag
		} else //index == 5, TagList
		{
			tagList = new byte[deviceTags.length];
			System.arraycopy(deviceTags, 0, tagList, 0, deviceTags.length); //get all device tags
		}
	}

	/**
	 * Create HostQueryEntry for Data or CustomCmdRequest
	 * 
	 * @param index
	 *            API index
	 * @param deviceTag
	 *            The device tag
	 * @param bytes1
	 *            The value that is read from device, or custom cmd number
	 * @param bytes2
	 *            8-byte timestamp, or payload
	 */
	public HostQueryEntry(byte index, byte[] deviceTag, byte[] bytes1, byte[] bytes2) {
		if (index == 6)
			constructorForData(index, deviceTag, bytes1, bytes2);

		if (index == 8)
			constructorForCustomCmdRequest(index, deviceTag, bytes1, bytes2);
	}

	/**
	 * Create HostQueryEntry for Write
	 * 
	 * @param index
	 *            API index
	 * @param deviceTag
	 *            The device tag
	 * @param dataValue
	 *            The value that is written to device
	 */
	public HostQueryEntry(byte index, byte[] deviceTag, byte[] dataValue) {
		//Write(device tag, data value)
		result = new byte[8 + dataValue.length]; //8 = simple data access (1) + index (1) + deviceTag (6)
		result[0] = protocol; //simple data access
		result[1] = index; // Write's index = 7
		System.arraycopy(deviceTag, 0, result, 2, deviceTag.length); //copy deviceTag into result[2] through result[7]
		System.arraycopy(dataValue, 0, result, 8, dataValue.length); //copy dataValue into result[8] through result[X], X depends on length of data

		this.index = index;
		System.arraycopy(deviceTag, 0, this.deviceTag, 0, deviceTag.length);
		this.dataValue = new byte[dataValue.length];
		System.arraycopy(dataValue, 0, this.dataValue, 0, dataValue.length);
	}

	/**
	 * Create HostQueryEntry for Custom Command Response
	 * 
	 * @param index
	 *            API index
	 * @param deviceTag
	 *            The device tag
	 * @param customCmdNum
	 *            The command number of the custom command
	 * @param payload
	 *            Payload of the custom command
	 */
	public HostQueryEntry(byte index, byte[] deviceTag, byte[] customCmdNum, byte[] payload, byte[] timestamp) {
		result = new byte[10 + payload.length + timestamp.length];
		result[0] = protocol; //simple data access
		result[1] = index; //custom cmd response's index = 9
		System.arraycopy(deviceTag, 0, result, 2, deviceTag.length);
		result[8] = customCmdNum[0];
		result[9] = customCmdNum[1];
		System.arraycopy(payload, 0, result, 10, payload.length);
		System.arraycopy(timestamp, 0, result, 10 + payload.length, timestamp.length);

		this.index = index;
		System.arraycopy(deviceTag, 0, this.deviceTag, 0, deviceTag.length);
		this.customCmdNum[0] = customCmdNum[0];
		this.customCmdNum[1] = customCmdNum[1];
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
		this.timestamp = new byte[timestamp.length];
		System.arraycopy(timestamp, 0, this.timestamp, 0, timestamp.length);
	}

	//Methods:
	private void constructorForData(byte index, byte[] deviceTag, byte[] dataValue, byte[] timestamp) {
		//Data(device tag, data value)
		result = new byte[8 + dataValue.length + timestamp.length]; //= simple data access (1) + index (1) + deviceTag (6) + dataValue.length + timestamp.length
		result[0] = protocol; //simple data access
		result[1] = index; //Data's index = 6,
		System.arraycopy(deviceTag, 0, result, 2, deviceTag.length); //copy deviceTag into result[2] through result[7]
		System.arraycopy(dataValue, 0, result, 8, dataValue.length); //copy dataValue into result[8] through result[X], X depends on length of data
		System.arraycopy(timestamp, 0, result, 8 + dataValue.length, timestamp.length);

		this.index = index;
		System.arraycopy(deviceTag, 0, this.deviceTag, 0, deviceTag.length);
		this.dataValue = new byte[dataValue.length];
		System.arraycopy(dataValue, 0, this.dataValue, 0, dataValue.length);
		this.timestamp = new byte[timestamp.length];
		System.arraycopy(timestamp, 0, this.timestamp, 0, timestamp.length);
	}

	private void constructorForCustomCmdRequest(byte index, byte[] deviceTag, byte[] customCmdNum, byte[] payload) {
		//Custom Command(device tag, custom cmd num, payload)
		result = new byte[10 + payload.length];
		result[0] = protocol; //simple data access
		result[1] = index; //custom cmd's index = 8
		System.arraycopy(deviceTag, 0, result, 2, deviceTag.length);
		result[8] = customCmdNum[0];
		result[9] = customCmdNum[1];
		System.arraycopy(payload, 0, result, 10, payload.length);

		this.index = index;
		System.arraycopy(deviceTag, 0, this.deviceTag, 0, deviceTag.length);
		this.customCmdNum[0] = customCmdNum[0];
		this.customCmdNum[1] = customCmdNum[1];
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
	}

	/**
	 * Currently supports protocol 1 (HostQueryEntry) communication with
	 * Gateway.
	 * 
	 * @return Protocol number
	 */
	public byte getProtocol() {
		return this.protocol;
	}

	/**
	 * Set the protocol number in this HostQueryEntry
	 * 
	 * @param p
	 *            Protocol number
	 */
	public void setProtocol(byte p) {
		this.protocol = p;
		this.result[0] = p;
	}

	/**
	 * Check if this HostQueryEntry is equal to another HostQueryEntry
	 * 
	 * @param other
	 *            Other HostQueryEntry
	 * @return true if equal, otherwise false
	 */
	public boolean equals(HostQueryEntry other) {
		byte[] temp = other.getByteArray();

		return Arrays.equals(this.result, temp);
	}

	public String toString() {
		String str = new String();
		for (int i = 0; i < result.length; i++) {
			str = str + Byte.toString(result[i]);
			if (i < result.length - 1) {
				str = str + "__";
			}
		}
		return str;
	}

	/**
	 * Get the API index in this HostQueryEntry
	 * 
	 * @return API index
	 */
	public byte getIndex() {
		return index;
	}

	/**
	 * Get the device tag used by this HostQueryEntry
	 * 
	 * @return Device tag
	 */
	public byte[] getDeviceId() {
		return deviceTag;
	}

	/**
	 * Get the update rate in this HostQueryEntry
	 * 
	 * @return Update rate
	 */
	public byte getUpdateRate() {
		return updateRate;
	}

	/**
	 * Get the data value in this HostQueryEntry
	 * 
	 * @return Data
	 */
	public byte[] getDataValue() {
		return dataValue;
	}

	/**
	 * Get the timestamp in this HostQueryEntry
	 * 
	 * @return Data
	 */
	public byte[] getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the tag list in this HostQueryEntry
	 * 
	 * @return Tag list
	 */
	public byte[] getTagList() {
		return tagList;
	}

	/**
	 * Get the burst command number in this HostQueryEntry (1 or 170)
	 * 
	 * @return burst command number
	 */
	public byte[] getBurstCmdNum() {
		return burstCmdNum;
	}

	/**
	 * Get the HART command number in this HostQueryEntry
	 * 
	 * @return HART command number
	 */
	public byte[] getCustomCmdNum() {
		return customCmdNum;
	}

	/**
	 * Get the payload in this HostQueryEntry
	 * 
	 * @return Payload
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * Get the byte array in this HostQueryEntry (the entire representation of
	 * the HostQueryEntry in bytes)
	 * 
	 * @return Byte array
	 */
	public byte[] getByteArray() {
		return result;
	}

}
