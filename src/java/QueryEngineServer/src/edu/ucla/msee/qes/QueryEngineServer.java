package edu.ucla.msee.qes;
import msee.service.QueryService;
import msee.Config;
import msee.Server;

import org.apache.thrift.transport.TTransportException;

public class QueryEngineServer{
	
	
	/**
	 * This is the main entrance of the server.
	 * @param args
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		String config_path = "";
		if ( args.length > 0 ) {
			config_path = args[0];
		}
		
		QueryService.Processor processor = new QueryService.Processor(new QueryEngineImpl(config_path));

		try {
			Server<QueryService.Processor> server = new Server<QueryService.Processor>(
					"query-engine", Config.CONFIG_KEY_QUERY_PORT, processor);
			server.run();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}