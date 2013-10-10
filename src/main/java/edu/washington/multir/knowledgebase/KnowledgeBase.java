package edu.washington.multir.knowledgebase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class KnowledgeBase {
	
	private Map<String,Set<String>> kbEntityMap;
	private Map<String[],Set<String>> kb;
	
	public KnowledgeBase(Map<String[],Set<String>> kb, Map<String,Set<String>> kbEntityMap){
		this.kb = kb;
		this.kbEntityMap = kbEntityMap;
	}
	
	public static KnowledgeBase buildKnowledgeBaseFromtsvFiles(File tsvEntityMapFile, File tsvKnowledgeBaseMapFile) throws IOException{
		return new KnowledgeBase(readInKBFromTsv(tsvKnowledgeBaseMapFile),readInEntityMapFromTsv(tsvEntityMapFile));
	}
	
	
	public static Map<String[],Set<String>> readInKBFromTsv(File tsvKnowledgeBaseMapFile ) throws IOException{
		Map<String[],Set<String>> kb = new HashMap<String[],Set<String>>();
		LineIterator li = FileUtils.lineIterator(tsvKnowledgeBaseMapFile);
		int i = 0;
		while(li.hasNext()){
			String nextLine = li.nextLine();
			String[] lineValues = nextLine.split("\t");
			if(lineValues.length ==3){
				String entity1 = lineValues[0];
				String entity2 = lineValues[1];
				String[] mapKey = {entity1,entity2};
				String rel = lineValues[2];
				
				//update kbEntityMap
				if(kb.containsKey(mapKey)){
					kb.get(mapKey).add(rel);
				}
				else{
					Set<String> setOfRelationNames = new HashSet<String>();
					setOfRelationNames.add(rel);
					kb.put(mapKey, setOfRelationNames);
				}
			}
			if( i  % 10000000 == 0){
				System.out.println(i + "lines read");
			}
			i++;
		}		
		
		return null;
	}
	public static Map<String,Set<String>> readInEntityMapFromTsv(File tsvEntitymapFile) throws IOException{
		Map<String,Set<String>> kbEntityMap = new HashMap<String,Set<String>>();
		LineIterator li = FileUtils.lineIterator(tsvEntitymapFile);
		while(li.hasNext()){
			String nextLine = li.nextLine();
			String[] lineValues = nextLine.split("\t");
			if(lineValues.length ==2){
				String kbId = lineValues[0];
				String entityName = lineValues[1];
				
				//update kbEntityMap
				if(kbEntityMap.containsKey(kbId)){
					kbEntityMap.get(kbId).add(entityName);
				}
				else{
					Set<String> setOfEntityNames = new HashSet<String>();
					setOfEntityNames.add(entityName);
					kbEntityMap.put(kbId, setOfEntityNames);
				}
			}
		}
		return kbEntityMap;
	}
	
}
