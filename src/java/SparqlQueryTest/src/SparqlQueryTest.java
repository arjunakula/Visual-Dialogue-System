/*
 * Copyright (c) 2012, Intelligent Automation Inc. 
 * All Rights Reserved.                                                       
 * Date:   3/30/2012
 * Author: Mun Wai Lee                                                           
 * E-Mail: mlee@i-a-i.com       
 *
 */ 

import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import TextParser.JenaEngine;

class SparqlQueryTest  {
	
	public SparqlQueryTest() throws Exception {
	
		// String rdf_model_for_sparql = "C:\\projects\\MSEE\\demo\\TextParserDemo\\data\\Data0712\\Indoor\\Text+JointParsing\\human_merged\\0.ex1.joint.rdf";
		// String rdf_model_for_sparql = "C:\\projects\\MSEE\\demo\\TextParserDemo\\data\\Data0712\\Outdoor\\Text+JointParsing\\human_merged\\0.ex1.joint.rdf";
		// String rdf_model_for_sparql = "C:\\projects\\MSEE\\demo\\TextParserDemo\\data\\Data0712\\Indoor\\Text+JointParsing\\human1\\seg10\\level0.joint.rdf";
		// String rdf_model_for_sparql = "C:\\svn\\msee\\trunk\\src\\java\\TextQueryEval\\data\\Data0712\\Indoor\\Text+JointParsing\\human1\\seg10\\level0.joint.rdf";
		// String rdf_model_for_sparql = "C:\\svn\\msee\\trunk\\data\\SIG_DATA2\\temp_test\\test_confidence.rdf";
		String rdf_model_for_sparql = "C:\\svn\\msee\\trunk\\data\\SIG_DATA2\\temp_test\\data_protege_20140305.rdf";
		
		
		
		// String sparql_query = "C:\\projects\\MSEE\\demo\\SparqlQueryTest\\data\\sparql_test_input.txt";
		// String sparql_query = "C:\\svn\\msee\\trunk\\data\\SIG_DATA2\\temp_test\\test_confidence.txt";
		String sparql_query = "C:\\svn\\msee\\trunk\\data\\SIG_DATA2\\temp_test\\test_protege_20140304.sparql.txt";
		
		String owl_file = "C:\\svn\\msee\\trunk\\src\\java\\EES-SUT\\QueryUnitTest\\model\\MSEE.owl";

		// make result root folder
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		String resultroot_folder = "result_" + dateFormat.format(date);
		File dir = new File(resultroot_folder);   
		dir.mkdir(); 
		
		String sparql_result = resultroot_folder + "\\sparql_result.txt";

		
		
		
		JenaEngine jenaEngine = new JenaEngine(rdf_model_for_sparql , owl_file, sparql_query, sparql_result);
	}

	// main function
	public static void main(String args[]) throws Exception 
	{
		SparqlQueryTest new_app = new SparqlQueryTest();
	}
}
