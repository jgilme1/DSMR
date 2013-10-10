package edu.washington.multir.knowledgebase;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.sun.corba.se.pept.transport.Connection;

import edu.washington.multir.database.DerbyDb;

public class KB extends DerbyDb{
	private KB(String url){
		super(url);
	}
	
	void createTable(String tableCreationString) throws SQLException{
		connection.prepareStatement(tableCreationString).execute();
	}
	
	void deleteTable(String tableName) throws SQLException{
		connection.prepareStatement("DROP TABLE " + tableName).execute();
	}
	
	public static KB loadKBFromTsv(String url, File tsvFile) throws SQLException{
		//connect to KB database, creating it if it does not exist
		KB kb  = new KB(url+";create=true");

		String tableName = "KBTABLE";
		
		String tableCreationString = "CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))";
		//if table already exists then delete it
		try{
		  kb.createTable(tableCreationString);
		}
		catch(java.sql.SQLException e){
	         String theError = e.getSQLState();
	         if (theError.equals("X0Y32")) {
	        	 kb.deleteTable(tableName);
	        	 kb.createTable(tableCreationString);
	         }
	         else{
	        	 System.out.println(theError);
	        	 throw e;
	         }
		}
		
		//import Data from tsv file		
		PreparedStatement loadTable = kb.connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
		loadTable.setString(1, null);
		loadTable.setString(2,tableName);
		loadTable.setString(3,tsvFile.getPath());
		loadTable.setString(4, "\t");
		loadTable.setString(5,"\"");
		loadTable.setString(6,null);
		loadTable.setInt(7, 0);
		loadTable.execute();
		return kb;
	}
	
	
	public static KB loadKBFromTsvWithRelationFilter(String url, File tsvFile, Set<String> targetRelations) throws SQLException, IOException{
		//connect to KB database, creating it if it does not exist
		KB kb  = new KB(url+";create=true");

		String tableName = "KBTABLE";
		
		String tableCreationString = "CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))";
		//if table already exists then delete it
		try{
		  kb.createTable(tableCreationString);
		}
		catch(java.sql.SQLException e){
	         String theError = e.getSQLState();
	         if (theError.equals("X0Y32")) {
	        	 kb.deleteTable(tableName);
	        	 kb.createTable(tableCreationString);
	         }
	         else{
	        	 System.out.println(theError);
	        	 throw e;
	         }
		}
		
		
		LineIterator li = FileUtils.lineIterator(tsvFile);
		int index =0;
		while(li.hasNext()){
			String line = li.nextLine();
			String[] lineValues = line.split("\t");
			String e1 = lineValues[0];
			String e2 = lineValues[1];
			String rel = lineValues[2];
			if(targetRelations.contains(rel)){
				PreparedStatement loadLine = kb.connection.prepareStatement("INSERT INTO "+ tableName + " VALUES ('"+e1+"','"+e2+"','"+rel+"')");
				loadLine.execute();
			}
			
			if(index % 1000 == 0){
				System.out.println(index + " lines processed");
			}
			index ++;
		}
		return kb;
	}
	
//	public static KB loadKBFromTsv(String url, File tsvFile) throws SQLException{
//		KB kb  = new KB(url+";create=true");
//	    ResultSet tableResultSet = kb.connection.getMetaData().getTables(null, null, null, null);
//		PreparedStatement makeTable = kb.connection.prepareStatement("CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))");
//		try{
//		  makeTable.execute();
//		}
//		catch(java.sql.SQLException e){
//			if(e.getErrorCode() == -1){
//				kb.connection.prepareStatement("DROP TABLE KBTABLE").execute();
//				kb.cleanUp();
//				kb.startConnection(url);
//				kb.connection.prepareStatement("CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))").execute();
//			}
//			else{
//				throw e;
//			}
//		}
//		
//		PreparedStatement loadTable = kb.connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
//		loadTable.setString(1, null);
//		loadTable.setString(2,"KBTABLE");
//		loadTable.setString(3,tsvFile.getPath());
//		loadTable.setString(4, "\t");
//		loadTable.setString(5,"\"");
//		loadTable.setString(6,null);
//		loadTable.setInt(7, 0);
//		loadTable.execute();
//		return kb;
//	}
	
	public static KB loadKBFromDerby(String url){
		return new KB(url);
	}
	
	
	public void printRelsForEntities(String e1, String e2) throws SQLException{
		System.out.println("E1: "+ e1);
		System.out.println("E2: "+ e2);
		
//		e1 = e1.replaceAll("\\/", "\\\\\\/");
//		e2 = e2.replaceAll("\\/", "\\\\\\/");
		
		System.out.println("E1: "+ e1);
		System.out.println("E2: "+ e2);
		
		ResultSet results = connection.prepareStatement("SELECT * FROM KBTABLE WHERE E1='"+e1+"' AND E2='" + e2+"'").executeQuery();
		while(results.next()){
			String rel = results.getString(3);
			System.out.println(e1 +"\t" + e2 + "\t" + rel);
		}
		results.close();
	}
	
	
}
