package edu.washington.multir.distantsupervision;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class FilterKBByRelations {
	
	
	public static void main(String[] args) throws IOException{
		String pathToKB = args[0];
		
		Set<String> targetRelations = new HashSet<String>();
		File targetRelationFile = new File("targetRelations.txt");
		String targetRelationsString = FileUtils.readFileToString(targetRelationFile);
		String[] targetRelationLines = targetRelationsString.split("\n");
		for(String line : targetRelationLines){
			targetRelations.add(line.split("\\s+")[1]);
		}
		LineIterator li = FileUtils.lineIterator(new File(pathToKB));
		PrintWriter pw = new PrintWriter("filteredKB.txt");
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
}
