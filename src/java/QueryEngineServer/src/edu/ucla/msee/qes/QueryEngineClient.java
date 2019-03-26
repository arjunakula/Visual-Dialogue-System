package edu.ucla.msee.qes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import msee.Config;
import msee.common.QueryAnswer;
import msee.common.SOCDescriptor;
import msee.service.QueryService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class QueryEngineClient {

	private static final String host = Config.getString(Config.CONFIG_KEY_QUERY_HOST);
	private static final int port = Config.getInt(Config.CONFIG_KEY_QUERY_PORT);
	
	private TTransport transport;
	private QueryService.Client client;
	
	public QueryEngineClient() {
		transport = new TFramedTransport(new TSocket(host, port));
		client = new QueryService.Client(new TBinaryProtocol(transport));
	}
	
	public void saveSOCKnowledge(SOCDescriptor soc, String socXML, String parseGraph) throws TException {
		transport.open();
		client.prepareSOC(soc.getId());
		transport.close();
	}
	
	public void resetStoryline(String storylineId) throws TException {
		transport.open();
		client.prepareStoryline(storylineId);
		transport.close();
	}
	
	public QueryAnswer query(String queryXML) throws TException {
		transport.open();
		QueryAnswer answer = client.query(queryXML);
		transport.close();
		return answer;
	}
	
	public static void main(String[] args) throws TTransportException {
		try {
			// TODO: Use the client object to test. For example:
			QueryEngineClient client = new QueryEngineClient();
			
			// Parking 10/18
			
//			SOCDescriptor socDescriptor = new SOCDescriptor();
//			socDescriptor.setId("soc-sig-parking-lot-2013-09-28-testing-MC");			
//			
//			client.saveSOCKnowledge( socDescriptor, "", "" );
//
//			socDescriptor = new SOCDescriptor();
//			socDescriptor.setId("soc-PrattGarden-2014-09-20-Testing-Fashion");			
//			
//			client.saveSOCKnowledge( socDescriptor, "", "" );
//			
//			socDescriptor = new SOCDescriptor();
//			socDescriptor.setId("soc-Schiciano-2014-02-22-Testing-Evacuation");			
//			
//			client.saveSOCKnowledge( socDescriptor, "", "" );
			
			SOCDescriptor socDescriptor = new SOCDescriptor();
			socDescriptor.setId("soc-PrattGarden-2014-09-20-Testing-Sport");
			//socDescriptor.setId("soc-PrattGarden-2014-09-20-Testing-Sport_iai31");
			
			if (true)
			{
				client.saveSOCKnowledge( socDescriptor, "", "" );
				
				client.resetStoryline("storyline");
	
				// QueryAnswer answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query.xml" ) );
				// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query_2_person_time.xml" ) );
				// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query_2_person_loc.xml" ) );
				// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query_2_person.xml" ) );
				// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query.xml" ) );
				QueryAnswer answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query.xml" ) );
				
				
				System.out.println( "Answer:\n" + answer );
			} else
			{
				// test text parser 
				
				// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01\\query.xml" ) );
				
				
				for (int i =0; i < 10; i++)
				{
					// QueryAnswer answer = client.query( getFileContents( "D:\\svn\\msee\\trunk\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-text\\storyline-01\\queries\\query-01\\query.xml" ) );
					QueryAnswer answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-text\\storyline-01\\queries\\query-01\\query.xml" ) );
					System.out.println( "Answer:\n" + answer );
				}
				
			}
		
			
			
			/*
			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01a\\query.xml" ) );
			
			System.out.println( "Answer:\n" + answer );
			
			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-01b\\query.xml" ) );
			
			System.out.println( "Answer:\n" + answer );
			
			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase3DistributableTestingData\\3_soc-PrattGarden-2014-09-20-Testing-Sport\\test-IAI-nonpolar\\storyline-01\\queries\\query-02\\query.xml" ) );
			
			System.out.println( "Answer:\n" + answer );
			*/ 
			if ( true ) {
				return;
			}
			/*
			// Office
			
			socDescriptor = new SOCDescriptor();
			socDescriptor.setId("soc-sig-office-2013-09-04-testing");			
			
			client.saveSOCKnowledge( socDescriptor, "", "" );

			client.resetStoryline("storyline-confroom-meeting-sfq");

			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase2DistributableTestingData\\soc-sig-office-2013-09-04-testing\\storylines_darpa_2\\storyline-confroom-meeting-sfq\\queries\\query-01\\query.xml" ) );
			
			System.out.println( "Answer:\n" + answer );
			
			
			// Parking Lot
			
			socDescriptor = new SOCDescriptor();
			socDescriptor.setId("soc-sig-parking-lot-2013-09-28-testing");			
			
			client.saveSOCKnowledge( socDescriptor, "", "" );

			client.resetStoryline("storyline-car-parts-SFQ");

			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase2DistributableTestingData\\soc-sig-parking-lot-2013-09-28-testing\\storylines_darpa_2\\storyline-car-parts-SFQ\\queries\\query-3\\query.xml" ) );

			System.out.println( "Answer:\n" + answer );
			
			// Garden
			
			socDescriptor = new SOCDescriptor();
			socDescriptor.setId("soc-pratt-garden-2013-10-12-testing");			
			
			client.saveSOCKnowledge( socDescriptor, "", "" );

			client.resetStoryline("storyline-HC3-SFQ");

			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase2DistributableTestingData\\soc-pratt-garden-2013-10-12-testing\\storylines_darpa_2\\storyline-HC3-SFQ\\queries\\query-7\\query.xml" ) );

			System.out.println( "Answer:\n" + answer );
			
//			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase2DistributableTestingData\\soc-pratt-garden-2013-10-12-testing\\storylines_nl\\storyline-2\\queries\\query-11\\query.xml") );
//			
//			System.out.println( "Answer:\n" + answer );
			
			// Back to Office
			
			socDescriptor = new SOCDescriptor();
			socDescriptor.setId("soc-sig-office-2013-09-04-testing");			
			
			client.saveSOCKnowledge( socDescriptor, "", "" );

			client.resetStoryline("storyline-items-left-alone-recovered");

			answer = client.query( getFileContents( "C:\\MSEE_2\\msee\\data\\SIG_DATA2\\Phase2DistributableTestingData\\soc-sig-office-2013-09-04-testing\\storylines_darpa_2\\storyline-items-left-alone-recovered\\queries\\query-2a\\query.xml" ) );
			
			System.out.println( "Answer:\n" + answer );
			*/ 
			
		} catch (TException e) {
			e.printStackTrace();
		}
	}
	
	private static String getFileContents( String path ) {
		StringBuilder contents = new StringBuilder();

		try {
			BufferedReader input =  new BufferedReader(new FileReader(path));
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			throw new RuntimeException(ex);
		}
		
		return contents.toString();
	}
}

