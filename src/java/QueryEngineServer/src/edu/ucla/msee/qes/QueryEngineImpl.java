package edu.ucla.msee.qes;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import msee.common.QueryAnswer;
import msee.common.SOCDescriptor;
import msee.service.QueryService;

import org.apache.thrift.TException;

import TextParser.SparqlGenerator;
import TextParser.TPLib;

import com.iai.msee.qei.MSEEQueryEngineInterface;
import com.iai.msee.qei.Utilities;

public class QueryEngineImpl implements QueryService.Iface {
	
	// This is the path for the directory that contains the *.config files.
	private String config_path = "";
	
	private MSEEQueryEngineInterface mqei;
	
	private String currentSocId;

	private String currentStorylineId;
	
	// The path prefix that will be utilized in resolving relative paths (as described below).
	// It will also be utilized to resolve all relative paths that are specified within the XML elements of soc_description_file.
	private String path_prefix;
	
	// The relative path to the parent directory of the directory that contains the SOC data.
	private String parent_directory;
	
	// The relative path to the *.rdf file(s) describing the SOC.
	private String joint_parse_graph_directory;
	
	// The relative path to the *.xml file containing space-time data for the SOC.
	private String soc_description_file;
	
	// The absolute path to the file containing the MSEE ontology.
	private String owl_ontology_file;
	
	// The absolute path to the file containing query engine configuration information (e.g., parameters, view subsets) for the SOC.
	private String configuration_file;
	
	// The absolute path to the directory where query state information will be saved.
	// Each state file (in this directory) will have the following name: <soc id>_<storyline id>.obj.
	// CAUTION: The system may delete all files with the extension *.obj that are contained in it.
	private String persistent_names_directory;
	
	// Whether remote online functions should be called.  If this value is false (by default), no attempt will be made to contact the online
	// computation server, and each such function will return false.
	private String call_remote_online_functions;
	private boolean call_remote_online_functions_boolean = false;
	
	// Parameters for handling natural language queries; see the setSOC() method in MSEEQueryEngineInterface for their meaning.
	private String translation_directory;
	private String text_parser_grammar_file;
	private String text_parser_mapping_file;
	private String stanford_parser_grammar_file;
	private String graphdb_file; 
	private String str_scene_start_time = "2014-09-20T22:20:00.000Z";
	
	// If reload_persistent_state is true, then persistent names will be loaded from the persistent state file, for the first storyline of the SOC only
	// (this option can be used to recover from a crash that occurred in the middle of a storyline).
	// Otherwise, the contents of the persistent state file will be cleared for every storyline of an SOC, including the first.
	private boolean reload_persistent_state = false;
	private boolean firstStoryline = true;
	
	private FileWriter summaryWriter;
	
	public static final boolean WRITE_TO_FILE = true;
	
	
	public static final String QUERY_TEMPLATE = "resources\\query_template.xml";
	
	Hashtable<String,String> m_map_object_id_short_to_long = new Hashtable<String,String>();
	Hashtable<String,String> m_map_object_id_long_to_short = new Hashtable<String,String>();
	int m_object_short_id = 0;
	
		
	public QueryEngineImpl( String config ) {		
		mqei = new MSEEQueryEngineInterface();
		config_path = config;
	}
	
	private void readConfigFile( String socId ) {
		
		Map<String,String> paths = Utilities.getConfiguration(config_path + socId + ".config");
		
		path_prefix = paths.get("path_prefix");
		parent_directory = paths.get("parent_directory");
		joint_parse_graph_directory = paths.get("joint_parse_graph_directory");
		soc_description_file = paths.get("soc_description_file");
		owl_ontology_file = paths.get("owl_ontology_file");
		configuration_file = paths.get("configuration_file");
		persistent_names_directory = paths.get("persistent_names_directory");
		call_remote_online_functions = paths.get("call_remote_online_functions");
		
		if ( path_prefix == null || parent_directory == null || owl_ontology_file == null || joint_parse_graph_directory == null || soc_description_file == null || configuration_file == null ) {
			throw new RuntimeException( "One or more paths is not properly specified in qe_conf.config." );
		}
		
		if ( call_remote_online_functions != null && call_remote_online_functions.equals("true") ) {
			call_remote_online_functions_boolean = true;
		}
		
		translation_directory = paths.get("translation_directory");
		text_parser_grammar_file = paths.get("text_parser_grammar_file");
		text_parser_mapping_file = paths.get("text_parser_mapping_file");
		stanford_parser_grammar_file = paths.get("stanford_parser_grammar_file");

		graphdb_file = paths.get("graphdb_file");
		str_scene_start_time = paths.get("scene_start_time");
		
		
		
		String rps = paths.get("reload_persistent_state");
		
		if ( rps != null && rps.equals("true") ) {
			reload_persistent_state = true;
		}
		
		if ( WRITE_TO_FILE && persistent_names_directory != null && summaryWriter == null ) { // Only do this once (note that summaryWriter will never become null again).
			try {
				
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

				Date date = new Date();
				String file_suffix = dateFormat.format(date);
				
				summaryWriter = new FileWriter( persistent_names_directory + File.separator + "summary_ " + file_suffix + ".txt" );
			}
			catch ( IOException e ) {
				System.err.println(e);
			}
		}
	}
	
	// Important: If soc_id == null, then this method will delete all query engine state files.  It will also reset the current SOC id and the current storyline id.
	// If this option (soc_id == null) is used, no other queries should be made until either saveSOCKnowledge is called with soc != null, or resetStoryline is called,
	// or the system is restarted.
	// Note: if soc_id is equal to the current SOC id, then this method has no effect.
	@Override
	public void prepareSOC(String sod_id)
			throws TException {
		if ( sod_id == null ) {
			Utilities.deleteAllFiles(persistent_names_directory, ".obj");
						
			currentSocId = null;
			currentStorylineId = null;
			
			return;
		}
		
		
		if ( currentSocId != null && currentSocId.equals( sod_id ) ) {
			return;
		}
		
		readConfigFile( sod_id );
		
		currentSocId = sod_id;
		currentStorylineId = null;
		
		firstStoryline = true;
		
		String parent_directory_absolute =  path_prefix  + File.separator + parent_directory;
		String soc_description_file_absolute = path_prefix + File.separator + soc_description_file;
		String joint_parse_graph_directory_absolute = path_prefix + File.separator + joint_parse_graph_directory;
		
		System.err.println("DEBUG  saveSOCKnowledge: soc.getId() : " + sod_id );
		System.err.println("DEBUG  saveSOCKnowledge: parent_directory: " + parent_directory);
		
		mqei.setSOC( call_remote_online_functions_boolean, parent_directory_absolute, owl_ontology_file, joint_parse_graph_directory_absolute, soc_description_file_absolute, configuration_file,
				translation_directory, 
				text_parser_grammar_file, text_parser_mapping_file, stanford_parser_grammar_file, 
				graphdb_file,
				str_scene_start_time);
		
		
		printToFile( "SOC:\t" + sod_id, true );
	}

	// Note: if either persistent_names_directory or storylineId is null, then state will not be stored in a file.
	// Note: if storylineId is equal to the current storyline id (and they are not null), then this method has no effect.
	
	@Override
	public void prepareStoryline(String storylineId) throws TException {
		boolean clear_persistent_names = !firstStoryline || !reload_persistent_state;
		firstStoryline = false;
		
		if ( currentStorylineId != null && currentStorylineId.equals( storylineId ) ) {
			return;
		}
		currentStorylineId = storylineId;
		
		String persistent_names_file = null;
		
		if ( persistent_names_directory != null && currentStorylineId != null ) {
			persistent_names_file = persistent_names_directory + File.separator + currentSocId + "_" + currentStorylineId + ".obj";
		}		
		
		mqei.resetState(persistent_names_file, clear_persistent_names);
		
		printToFile( "Storyline:\t" + storylineId, true );
	}
	
	// Note: the query can also be in SPARQL format.
	@Override
	public QueryAnswer query(String queryXML) throws TException {
		String queryId = Utilities.extractQueryId(queryXML);
		
		StringBuffer xml = new StringBuffer();

		StringBuffer answer_string = new StringBuffer();
		StringBuffer answer_details = new StringBuffer();
		StringBuffer error_msg = new StringBuffer();
		
		// temporary file to hold the sparql query. it is read by the cypher translator
		String SPARQL_query_file = "sparql_tmp_.rq.txt";
		
		// testing 
		 //queryXML = "Who is walking?";
		 // queryXML = "What is an animal?";
		// queryXML = "Who is a male?";
		// queryXML = "Who has a hat?";
		
		
		 int index_xml = queryXML.indexOf("xml version");
		 if (index_xml==-1)
		 {
			 queryXML = SetUpNaturalLanguageQuery(queryXML);
		 }
		
		
		
		Boolean result = mqei.query(queryXML, SPARQL_query_file, xml, answer_string, answer_details, error_msg, true, true, false, true);
		
		if (false)
		{ // debug
			System.out.println("DEBUG xml: " + xml.toString());
			System.out.println("DEBUG answer_string: " + answer_string.toString());
			System.out.println("DEBUG setAnswer_details: " + answer_details.toString());
		}
		
		
		QueryAnswer answer = new QueryAnswer();
		answer.setId("answer-" + queryId);
		answer.setQuery_id(queryId);
		answer.setAnswer( result != null && result == true );
		answer.setAnswer_xml(xml.toString());
		answer.setAnswer_str(answer_string.toString());
		answer.setAnswer_details(answer_details.toString());
		answer.setError_msg(error_msg.toString());
		
		/*
    7: string answer_str;     // The answer string for non-polar questions. It may be a sentence or a list of objects.
    8: string answer_details; // The intermediate results (for visualization, or multiple returned objects). We will work out the format together slightly later. 
    9: bool is_error;         // Indicate whether query engine encountered any error.
    10: string error_msg;     // Give the error message in case of error.
}		 */
		
		// TODO: Should scene descriptive text also be considered polar?
		if ( (queryXML.indexOf("</Query>") >= 0 || queryXML.indexOf("</NLQuery>") >= 0) 
				//&& queryXML.indexOf("</NaturalLanguage>") < 0 )
			    && queryXML.indexOf("</NLQueryStatement>") < 0 )
		{
			answer.setIs_polar(true);
		}
		else {
			answer.setIs_polar(false);
		}
		
		String confidence = Utilities.extractContents(xml.toString(), "<confidence>", "</confidence>", true);
		
		if ( confidence != null ) {
			try {
				answer.setConfidence(Double.parseDouble(confidence));
			}
			catch ( NumberFormatException e ) {
				System.err.println( "Warning: could not parse the confidence field " + confidence + "." );
			}
		}
		
		// TODO: Should we also call methods such as setIdIsSet, etc.?
				
		printToFile( "Query:\t" + queryId + "\t" + result, true );
		printToFile( queryXML + "\n", true );
		printToFile( answer.toString(), true );
		
		return answer;
	}
	
	private String SetUpNaturalLanguageQuery(String queryText) {
		SparqlGenerator.m_object_long_id_toreplacewith="";
    	SparqlGenerator.m_query_scenetime_start="";
    	SparqlGenerator.m_query_scenetime_end="";
    	
    	
    	
    	    /*	
    	//String test_scenetime_s = "15:00:00";
    	//String test_scenetime_e = "15:15:00";
        
    	String test_scenetime_s = timeStartText;
    	String test_scenetime_e = timeEndText;
    	
    	if ((test_scenetime_s.length()>0) && (test_scenetime_e.length() ==0))
    	{
    		test_scenetime_e = test_scenetime_s;
    	}
    	
    	SparqlGenerator.m_query_scenetime_start = GetSceneDateTimeStr(test_scenetime_s);
    	SparqlGenerator.m_query_scenetime_end  = GetSceneDateTimeStr(test_scenetime_e);
    	
    	System.err.println("DEBUG  RunQuery SparqlGenerator.m_query_scenetime_start " + SparqlGenerator.m_query_scenetime_start);
    	System.err.println("DEBUG  RunQuery SparqlGenerator.m_query_scenetime_end " + SparqlGenerator.m_query_scenetime_end);
    	*/ 
    	
    	/*
    	if (queryText.startsWith("Where"))
    	{  
    		queryText = queryText.replace("Where", "When");
    	} else if (queryText.startsWith("where"))
    	{  
    		queryText = queryText.replace("where", "When");
    	}
    	*/
    	
        int index_object_id = queryText.indexOf("object_");     
        
        if (index_object_id!=-1)
        {
        	int index_end = queryText.indexOf(" ",index_object_id);
        	if (index_end != -1)
        	{
        		String object_short_id = queryText.substring(index_object_id,  index_end);
        		System.err.println("DEBUG object_short_id:" +object_short_id+"."  );
        		
        		
        		String object_long_id = this.m_map_object_id_short_to_long.get(object_short_id);        		
        		
        		if (object_long_id!= null)
        		{
        			// replace query
        			queryText = queryText.substring(0,index_object_id) + " object " + queryText.substring(index_end);
        			
        			System.err.println("DEBUG  RunQuery queryText " + queryText);
        			
        			SparqlGenerator.m_object_long_id_toreplacewith   = object_long_id;		        			
        			
        		}            
        	}
        }      
        
        
      	String xmlQuery = embedQuery(queryText);
		
		return xmlQuery;
	}
	

    private String embedQuery( String query ) {
    	String template = TPLib.GetFileContents(QUERY_TEMPLATE);
    	
    	/*
    	// Include all location definitions, if this is the first query in the SOC, so that they can be referred to in subsequent queries.
    	if ( newSOC ) {
        	
    		/*String locations = TPLib.GetFileContents(LOCATIONS_PATH + (String)socsComboBox.getSelectedItem() + ".xml");
        	if ( locations != null && !locations.equals("na") ) { // If the file exists.
        		locations = Utilities.extractContents( locations, "<locations>", "</locations>", true );
        		if ( locations != null ) { // If the locations node exists.
        			locations = locations.trim();
        			if ( !locations.equals("") ) { // If locations are defined within the node.
                		template = template.replace( "all_locations", locations );
        			}
        			else {
        				template = Utilities.removeContents( template, "<locations>", "</locations>", true );
        			}
        		}
        		else {
        			template = Utilities.removeContents( template, "<locations>", "</locations>", true );
        		}
        	}
        	else {
        		template = Utilities.removeContents( template, "<locations>", "</locations>", true );
        	}
        	
        	newSOC = false;
    	}
    	else {
    		template = Utilities.removeContents( template, "<locations>", "</locations>", true );
    	}
    	*/
    	
    	template = Utilities.removeContents( template, "<locations>", "</locations>", true );
    	
    	query = Utilities.embedInCDATA(query);
    	
    	template = template.replace("natural_language", query);
    	
    	return template;
    }

	// TODO: If the argument is not a XML string, then embed it inside a XML query.
	@Override
	public QueryAnswer queryNLP(String arg0) throws TException {
		return query(arg0);
	}

	private void printToFile( String str, boolean alsoPrintToConsole ) {
		if ( alsoPrintToConsole ) {
			System.out.println(str);
		}
		
		if ( summaryWriter == null ) {
			return;
		}
		
		try {
			summaryWriter.write(str + "\n");
			summaryWriter.flush();
		}
		catch ( IOException e ) {
			System.err.println(e);
		}
	}
	
	/**
	 * Returns the path that contains the intermediate and final translations of a natural language query.
	 * @return
	 */
	public String getTranslationDirectory( ) {
		return translation_directory;
	}
	
	public static void main(String[] args){
		// You can test your implementation locally from here.
	}
}
