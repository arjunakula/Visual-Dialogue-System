package edu.ucla.msee.online;
import java.util.ArrayList;
import java.util.List;

import msee.Config;
import msee.common.*;
import msee.online.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class OnlineComputingClient {

	private static final String host = Config.getString(Config.CONFIG_KEY_ONLINE_HOST);
	private static final int port = Config.getInt(Config.CONFIG_KEY_ONLINE_PORT);
	private static final boolean bVerboseThis = false;
	
	private TTransport transport;
	private OnlineComputingService.Client client;
	
	public OnlineComputingClient() {
		
		System.out.println("DEBUG OnlineComputingClient OnlineComputingClient  host " + host);
		System.out.println("DEBUG OnlineComputingClient OnlineComputingClient  port " + port);
		
		transport = new TFramedTransport(new TSocket(host, port));
		client = new OnlineComputingService.Client(new TBinaryProtocol(transport));
	}
	
	/**
	 * Initialize the remote Online Computation Server to get the knowledge set loaded.
	 * By doing this, we will be able to find the boundingboxes by object ids.
	 * @param knowledgeSetId To be determined.
	 * @throws OnlineComputingException
	 * @throws TException
	 */
	public void Initialize(String knowledgeSetId)
			throws OnlineComputingException, TException
	{
		if (this.bVerboseThis)
		{
			System.out.println("DEBUG OnlineComputingClient Initialize  host " + host);
			System.out.println("DEBUG OnlineComputingClient Initialize  port " + port);
		}
		
		transport.open();
		try {
			client.Initialize(knowledgeSetId);
		}
		finally {
			transport.close();
		}
	}
	
	/**
	 * Send a unary online computation request.
	 * @param requestType Specify which unary relation to request.
	 * @param objectId The object Id of the object to be queried.
	 * @param location The location in the query.
	 * @param timeInterval The time interval in the query.
	 * @return A list of time stamps when the requested relation is true.
	 * @throws OnlineComputingException
	 * @throws TException
	 */
	public List<String> OnlineRequestUnary(OnlineRequestType requestType,
			String objectId,
			List<CartesianMetricPoint> location, TimeInterval timeInterval)
			throws OnlineComputingException, TException {
		transport.open();
		List<String> trueTimeStamps = null;
		try {
			trueTimeStamps = client.OnlineRequestUnary(requestType, objectId, location, timeInterval);
		}
		finally {
			transport.close();
		}
		return trueTimeStamps;
	}
	
	public List<String> OnlineRequestBinary(OnlineRequestType requestType,
			String objectId1, String objectId2,
			List<CartesianMetricPoint>location, TimeInterval timeInterval)
			throws OnlineComputingException, TException {
		transport.open();
		List<String> trueTimeStamps = null;
		try {
			trueTimeStamps = client.OnlineRequestBinary(requestType, objectId1, objectId2, location, timeInterval);
		}
		finally {
			transport.close();
		}
		return trueTimeStamps;
	}
	
	public List<String> OnlineRequestTrinary(OnlineRequestType requestType,
			String objectId1, String objectId2, String objectId3,
			List<CartesianMetricPoint> location, TimeInterval timeInterval)
			throws OnlineComputingException, TException {
		transport.open();
		List<String> trueTimeStamps = null;
		try {
			trueTimeStamps = client.OnlineRequestTrinary(requestType, objectId1, objectId2, objectId3, location, timeInterval);
		}
		finally {
			transport.close();
		}
		return trueTimeStamps;
	}
	
	public static void main(String[] args) {
		try {
			
			// This is a example about how to use OnlineComputingClient.
			
			// Initialize the online computing every time you have a new scene.
			// By initialize it, we can find the bounding boxes and all other 2D parsing results
			// by the object id. 
			OnlineComputingClient online = new OnlineComputingClient();
			online.Initialize("soc-sig-office-2013-09-04-training");
			
			// Location: Polygon of the interested region. 
			List<CartesianMetricPoint> location = new ArrayList<CartesianMetricPoint>();
			CartesianMetricPoint pt1 = new CartesianMetricPoint();
			pt1.x = 1.05;
			pt1.y = 4.08;
			location.add(pt1);
			
			// Time interval. Use the string representation in the query.
			TimeInterval time = new TimeInterval();
			time.start_time = "2013-01-03T00:00:00.003Z";
			time.end_time = "2013-01-03T00:00:12.012Z";
			
			System.out.println( "About to call:" );
			
			// Make the online request with object ids, location, and time. 
			List<String> trueTimeStamps = online.OnlineRequestBinary(OnlineRequestType.ONLINE_BIN_PASSING,
					"object1_id", "object2_id",
					location, time);
			
			System.out.println( "Time Stamps:" );
			System.out.println(trueTimeStamps);
			
			
		} catch (OnlineComputingException e){
			System.out.println(e.message);
		}
		catch (TException e) {
			e.printStackTrace();
		}
	    
	}
}

