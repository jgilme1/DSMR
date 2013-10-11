package edu.washington.multir.distantsupervision;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.washington.multir.knowledgebase.KB;
import edu.washington.multir.knowledgebase.KnowledgeBase;
import edu.washington.multir.utils.DatabaseType;
import edu.washington.multir.utils.EntityRecognitionAlgorithm;

public class DistantSupervision {

	private File corpus;
	private EntityRecognitionAlgorithm alg;
	private DatabaseType dbType;
	private List<String> targetRelationList;
	
	
	public DistantSupervision(File corpus, EntityRecognitionAlgorithm alg, DatabaseType dbType, List<String> targetRelationList){
		this.corpus = corpus;
		this.alg = alg;
		this.dbType = dbType;
		this.targetRelationList = targetRelationList;
	}
	
	public void findRelevantSentences(){
		
	}

	public static void main(String[] args) throws IOException, InterruptedException, SQLException{
		
		
		
		
		//useOldTable(args);
		//createNewTable(args);
		//createNewTableWithSpecifiedRelations(args);
//		String pathToEntityMap = args[0];
//		String pathToKB = args[1];
//		String portNumber = args[2];
//		
//		long startTime = System.currentTimeMillis();
//		KB kb = KB.loadKBFromTsv("localhost:"+portNumber+"/KB", new File(pathToKB));
//		//KnowledgeBase kb = KnowledgeBase.buildKnowledgeBaseFromtsvFiles(new File(pathToEntityMap),new File(pathToKB));
//	    long endTime = System.currentTimeMillis();
//
//				
//		System.out.println("KB Loaded in "+ (endTime - startTime) + "milliseconds");
	}
	
//	public static void useOldTable(String[] args) throws SQLException{
//		String pathToEntityMap = args[0];
//		String pathToKB = args[1];
//		
//		KB kb =KB.loadKBFromDerby("KB");
//		long startTime = System.currentTimeMillis();
//		kb.printRelsForEntities("/m/07mdnv","/m/09c7w0");
//		kb.printRelsForEntities("/m/077g53","/m/0f8l9c");
//	    long endTime = System.currentTimeMillis();
//		System.out.println("Query completed in"+ (endTime - startTime) + "milliseconds");
//
//
//		kb.cleanUp();
//	}
//	
//	public static void createNewTable(String[] args) throws SQLException{
//		String pathToEntityMap = args[0];
//		String pathToKB = args[1];
//		
//		long startTime = System.currentTimeMillis();
//		KB kb = KB.loadKBFromTsv("KB", new File(pathToKB));
//		//KnowledgeBase kb = KnowledgeBase.buildKnowledgeBaseFromtsvFiles(new File(pathToEntityMap),new File(pathToKB));
//	    long endTime = System.currentTimeMillis();
//		
//	    kb.cleanUp();
//		System.out.println("KB Loaded in "+ (endTime - startTime) + "milliseconds");
//	}
//	
//	public static void createNewTableWithSpecifiedRelations(String[] args) throws SQLException, IOException{
//		String pathToEntityMap = args[0];
//		String pathToKB = args[1];
//		
//		Set<String> targetRelations = new HashSet<String>();
//		File targetRelationFile = new File("targetRelations.txt");
//		String targetRelationsString = FileUtils.readFileToString(targetRelationFile);
//		String[] targetRelationLines = targetRelationsString.split("\n");
//		for(String line : targetRelationLines){
//			targetRelations.add(line.split("\\s+")[1]);
//		}
//				
//		long startTime = System.currentTimeMillis();
//		KB kb = KB.loadKBFromTsvWithRelationFilter("KB", new File(pathToKB),targetRelations);
//		//KnowledgeBase kb = KnowledgeBase.buildKnowledgeBaseFromtsvFiles(new File(pathToEntityMap),new File(pathToKB));
//	    long endTime = System.currentTimeMillis();
//		
//	    kb.cleanUp();
//		System.out.println("KB Loaded in "+ (endTime - startTime) + "milliseconds");
//		
//	}
	
	
}
