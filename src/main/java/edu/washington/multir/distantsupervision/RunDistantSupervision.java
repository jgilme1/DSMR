package edu.washington.multir.distantsupervision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import edu.washington.multir.database.DerbyDb;
import edu.washington.multir.knowledgebase.KB;

public class RunDistantSupervision {
	
	
	
	public static void main(String[] args) throws Exception{
		if(args.length != 4){
			throw new Exception("Must be 4 arguments");
		}
		
		String pathToEntityStringTsv = args[0];
		String pathToKBTsv = args[1];
		String pathToTargetRelations = args[2];
		String pathToCorpus = args[3];
		
		//create new Derby DB
		KB kb = new KB("KB;create=true","ENTITYTABLE","KBTABLE");
		
		//load entities into Derby DB
		File delimitedEntityFile = File.createTempFile("delimitedEntity",".tmp");
		delimitedEntityFile.deleteOnExit();
		addDelimiterToEntityFile(new File(pathToEntityStringTsv),delimitedEntityFile);
		kb.loadEntityTableFromTsv(delimitedEntityFile);
		//Map<String,Set<String>> entityToIDsMap = readInEntityMapFromTsv(new File(pathToEntityStringTsv));
		System.err.println("Loaded entites");
		
		
		//create temperorary file for filtered KB
		File filteredKBFile = File.createTempFile("filteredKB", ".tmp");
		filteredKBFile.deleteOnExit();
		
		//filter KBTsv File by target Relations
		filterKB(pathToKBTsv,pathToTargetRelations,filteredKBFile);
		System.err.println("Filtered KB");
		
		
		//load relations into Derby DB
		kb.loadRelationTableFromTsv(filteredKBFile);
		System.err.println("Loaded KB");
		
		//iterate over corpus
		File corpusDirectory = new File(pathToCorpus);
		if(!corpusDirectory.isDirectory()){
			throw new Exception("Corpus path should be a directory!");
		}
		
		DistantSupervisionAnnotator dsa = new DistantSupervisionAnnotator(kb);
		for(File f :corpusDirectory.listFiles()){
			if(f.isFile()){
				String fileText = FileUtils.readFileToString(f);
				//use StanfordCoreNLP to break file into sentences and do NER tagging
				//System.out.println(fileText.substring(0,10));
				List<String> documentLevelDistantSupervisionAnnotations = dsa.getDistantSupervisionAnnotations(fileText,f);
				
				//write annotations to file
				PrintWriter pw = new PrintWriter(f.getPath()+".ann");
				for(String dsAnnotation : documentLevelDistantSupervisionAnnotations){
					pw.write(dsAnnotation+"\n");
				}
				pw.close();
			}
		}		
	}
	
	
	public static void filterKB(String pathToKBFile, String pathToTargetRelationsFile, File tmpKBFile) throws IOException {
		Set<String> targetRelations = new HashSet<String>();
		File targetRelationFile = new File(pathToTargetRelationsFile);
		String targetRelationsString = FileUtils.readFileToString(targetRelationFile);
		String[] targetRelationLines = targetRelationsString.split("\n");
		for(String line : targetRelationLines){
			targetRelations.add(line.split("\\s+")[1]);
		}
		LineIterator li = FileUtils.lineIterator(new File(pathToKBFile));
		PrintWriter pw = new PrintWriter(tmpKBFile);
		int index =0;
		while(li.hasNext()){
			String line = li.nextLine();
			String[] lineValues = line.split("\t");
			String e1 = lineValues[0];
			String e2 = lineValues[1];
			String rel = lineValues[2];
			if(targetRelations.contains(rel)){
				pw.write(e1 +"\t" + e2 + "\t" + rel+"\n");
			}
			if(index % 1000 == 0){
				System.out.println(index + " lines processed");
			}
			index ++;
		}
		pw.close();
	}
	
	public static Map<String,Set<String>> readInEntityMapFromTsv(File tsvEntitymapFile) throws IOException{
		Map<String,Set<String>> kbEntityMap = new HashMap<String,Set<String>>();
		LineIterator li = FileUtils.lineIterator(tsvEntitymapFile);
		int index =0;
		while(li.hasNext()){
			String nextLine = li.nextLine();
			String[] lineValues = nextLine.split("\t");
			if(lineValues.length ==2){
				String kbId = lineValues[0];
				String entityName = lineValues[1];
				
				//update kbEntityMap
				if(kbEntityMap.containsKey(entityName)){
					kbEntityMap.get(entityName).add(kbId);
				}
				else{
					Set<String> setOfKBIds = new HashSet<String>();
					setOfKBIds.add(kbId);
					kbEntityMap.put(entityName, setOfKBIds);
				}
			}
			
			if(index % 1000000 == 0){
				System.out.println(index + " lines read");
			}
			index ++;
		}
		li.close();
		return kbEntityMap;
	}
	
	public static void addDelimiterToEntityFile(File originalEntityFile, File tempEntityFile) throws IOException{
		LineIterator li = FileUtils.lineIterator(originalEntityFile);
		PrintWriter pw = new PrintWriter(tempEntityFile);
		int index =0;
		while(li.hasNext()){
			String line = li.nextLine();
			String newLine = line.replaceAll("%", "%%").replaceAll("\t(.*)","\t%$1%\n");
			pw.write(newLine);
			if(index % 1000000 == 0){
				System.out.println(index + " lines read");
			}
			index ++;
		}		
		li.close();
		pw.close();
	}

}
