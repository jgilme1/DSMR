package edu.washington.multir.knowledgebase;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.sun.corba.se.pept.transport.Connection;

import edu.washington.multir.database.DerbyDb;

public class KB extends DerbyDb{
	
	private final String entityTableName;
	private final String kbTableName;
	
	public KB(String url, String entityTableName, String kbTableName){
		super(url);
		this.entityTableName = entityTableName;
		this.kbTableName = kbTableName;
	}
	
	void createTable(String tableCreationString) throws SQLException{
		connection.prepareStatement(tableCreationString).execute();
	}
	
	void deleteTable(String tableName) throws SQLException{
		connection.prepareStatement("DROP TABLE " + tableName).execute();
	}
	
	public void loadRelationTableFromTsv(File tsvFile) throws SQLException{
		String tableCreationString = "CREATE TABLE " + kbTableName + " (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))";
		//if table already exists then delete it
		try{
		  createTable(tableCreationString);
		}
		catch(java.sql.SQLException e){
	         String theError = e.getSQLState();
	         if (theError.equals("X0Y32")) {
	        	 deleteTable(kbTableName);
	        	 createTable(tableCreationString);
	         }
	         else{
	        	 System.out.println(theError);
	        	 throw e;
	         }
		}
		
		//import Data from tsv file		
		PreparedStatement loadTable = connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
		loadTable.setString(1, null);
		loadTable.setString(2,kbTableName);
		loadTable.setString(3,tsvFile.getPath());
		loadTable.setString(4, "\t");
		loadTable.setString(5,null);
		loadTable.setString(6,null);
		loadTable.setInt(7, 0);
		loadTable.execute();
	}
	
	public void loadEntityTableFromTsv(File tsvFile) throws SQLException{
		String tableCreationString = "CREATE TABLE "+entityTableName+" (e VARCHAR(3200), name VARCHAR(32000))";
		
		try{
			createTable(tableCreationString);
		}
		catch (java.sql.SQLException e){
	         String theError = e.getSQLState();
	         if (theError.equals("X0Y32")) {
	        	 deleteTable(entityTableName);
	        	 createTable(tableCreationString);
	         }
	         else{
	        	 System.out.println(theError);
	        	 throw e;
	         }
		}
		
		// load in data
		PreparedStatement loadTable = connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
		loadTable.setString(1, null);
		loadTable.setString(2,entityTableName);
		loadTable.setString(3,tsvFile.getPath());
		loadTable.setString(4, "\t");
		loadTable.setString(5,"%");
		loadTable.setString(6,null);
		loadTable.setInt(7, 0);
		loadTable.execute();
	}
	
//	public static KB loadKBFromTsv(String url, File tsvFile) throws SQLException{
//		//connect to KB database, creating it if it does not exist
//		KB kb  = new KB(url+";create=true");
//
//		String tableName = "KBTABLE";
//		
//		String tableCreationString = "CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))";
//		//if table already exists then delete it
//		try{
//		  kb.createTable(tableCreationString);
//		}
//		catch(java.sql.SQLException e){
//	         String theError = e.getSQLState();
//	         if (theError.equals("X0Y32")) {
//	        	 kb.deleteTable(tableName);
//	        	 kb.createTable(tableCreationString);
//	         }
//	         else{
//	        	 System.out.println(theError);
//	        	 throw e;
//	         }
//		}
//		
//		//import Data from tsv file		
//		PreparedStatement loadTable = kb.connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
//		loadTable.setString(1, null);
//		loadTable.setString(2,tableName);
//		loadTable.setString(3,tsvFile.getPath());
//		loadTable.setString(4, "\t");
//		loadTable.setString(5,"\"");
//		loadTable.setString(6,null);
//		loadTable.setInt(7, 0);
//		loadTable.execute();
//		return kb;
//	}
//	
//	
//	public static KB loadKBFromTsvWithRelationFilter(String url, File tsvFile, Set<String> targetRelations) throws SQLException, IOException{
//		//connect to KB database, creating it if it does not exist
//		KB kb  = new KB(url+";create=true");
//
//		String tableName = "KBTABLE";
//		
//		String tableCreationString = "CREATE TABLE KBTABLE (e1 VARCHAR(32000), e2 VARCHAR(32000), rel VARCHAR(32000))";
//		//if table already exists then delete it
//		try{
//		  kb.createTable(tableCreationString);
//		}
//		catch(java.sql.SQLException e){
//	         String theError = e.getSQLState();
//	         if (theError.equals("X0Y32")) {
//	        	 kb.deleteTable(tableName);
//	        	 kb.createTable(tableCreationString);
//	         }
//	         else{
//	        	 System.out.println(theError);
//	        	 throw e;
//	         }
//		}
//		
//		
//		LineIterator li = FileUtils.lineIterator(tsvFile);
//		int index =0;
//		while(li.hasNext()){
//			String line = li.nextLine();
//			String[] lineValues = line.split("\t");
//			String e1 = lineValues[0];
//			String e2 = lineValues[1];
//			String rel = lineValues[2];
//			if(targetRelations.contains(rel)){
//				PreparedStatement loadLine = kb.connection.prepareStatement("INSERT INTO "+ tableName + " VALUES ('"+e1+"','"+e2+"','"+rel+"')");
//				loadLine.execute();
//			}
//			
//			if(index % 1000 == 0){
//				System.out.println(index + " lines processed");
//			}
//			index ++;
//		}
//		return kb;
//	}
	
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
	
//	public static KB loadKBFromDerby(String url){
//		return new KB(url);
//	}
	
	
	public void printRelsForEntities(String e1, String e2) throws SQLException{		
		ResultSet results = connection.prepareStatement("SELECT * FROM KBTABLE WHERE E1='"+e1+"' AND E2='" + e2+"'").executeQuery();
		while(results.next()){
			String rel = results.getString(3);
			System.out.println(e1 +"\t" + e2 + "\t" + rel);
		}
		results.close();
	}
	
	
	
	public List<String> getAssociatedKBIdsFromName(String name) throws SQLException{
		List<String> associatedKBIds = new ArrayList<String>();
		String queryString = "SELECT * FROM " + entityTableName + " WHERE NAME='" +name +"'";
		ResultSet results = connection.prepareStatement(queryString).executeQuery();
		while(results.next()){
			String kbId = results.getString(1);
			associatedKBIds.add(kbId);
		}
		results.close();
		return associatedKBIds;
	}
	
	public List<String> getAssociatedRelationsFromEntityPair(String e1Id, String e2Id) throws SQLException{
		List<String> associatedRelations = new ArrayList<String>();
		String queryString = "SELECT * FROM " + kbTableName + " WHERE E1='" +e1Id+ "' AND E2='" + e2Id + "'";
		ResultSet results = connection.prepareStatement(queryString).executeQuery();
		while(results.next()){
			String rel = results.getString(3);
			associatedRelations.add(rel);
		}
		results.close();
		return associatedRelations;
	}
	
	public void printIDsforName(String name) throws SQLException{
		ResultSet results = connection.prepareStatement("SELECT * FROM " + entityTableName +" WHERE NAME='" + name +"'").executeQuery();
		while(results.next()){
			String id = results.getString(1);
			System.out.println(id);
		}
		results.close();
	}
	
	public static void main(String[] args) throws SQLException{
		KB kb = new KB("KB;create=true","ENTITYTABLE","KBTABLE");
		//kb.printIDsforName("Minsk");
		kb.printRelsForEntities("/m/0163v","/m/0dlxj");
	}
	
	public List<String> getListOfCandidateArgument2AndRels(List<String> ids) throws SQLException{
		List<String> arg2IdAndRelations = new ArrayList<String>();
		StringBuilder replacementStringBuilder = new StringBuilder();
		for(String id: ids){
			replacementStringBuilder.append("E1='"+id+"'");
			replacementStringBuilder.append(" OR ");
		}
		String replacementString =replacementStringBuilder.substring(0, replacementStringBuilder.length()-4);
		String queryString = "SELECT * FROM " + kbTableName + " WHERE " + replacementString;
		ResultSet results = connection.prepareStatement(queryString).executeQuery();
		while(results.next()){
			String rel = results.getString(3);
			String arg2 = results.getString(2);
			arg2IdAndRelations.add(arg2 + "\t" + rel);
		}
		results.close();
		return arg2IdAndRelations;
	}
	
	public List<String> getListOfCandidateArgument1AndRels(List<String> ids) throws SQLException{
		List<String> arg1IdAndRelations = new ArrayList<String>();
		StringBuilder replacementStringBuilder = new StringBuilder();
		for(String id: ids){
			replacementStringBuilder.append("E2='"+id+"'");
			replacementStringBuilder.append(" OR ");
		}
		String replacementString =replacementStringBuilder.substring(0, replacementStringBuilder.length()-4);
		String queryString = "SELECT * FROM " + kbTableName + " WHERE " + replacementString;
		ResultSet results = connection.prepareStatement(queryString).executeQuery();
		while(results.next()){
			String rel = results.getString(3);
			String arg1 = results.getString(1);
			arg1IdAndRelations.add(arg1 + "\t" + rel);
		}
		results.close();
		return arg1IdAndRelations;
	}
}
