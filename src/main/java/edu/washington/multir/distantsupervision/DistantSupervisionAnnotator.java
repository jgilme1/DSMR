package edu.washington.multir.distantsupervision;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.multir.knowledgebase.KB;
import edu.washington.multir.utils.StanfordNERAnnotator;

class DistantSupervisionAnnotator {
	
	
	public static void main(String[] args) throws SQLException, IOException{
		DistantSupervisionAnnotator dsa= new DistantSupervisionAnnotator(new KB("KB;create=true","ENTITYTABLE","KBTABLE"));
		File docFile = new File("/scratch2/code/multir-reimplementation/DSMR/toyCorpus/NYT_ENG_20060302.0210.LDC2007T07.sgm");
		List<String> annotations = dsa.getDistantSupervisionAnnotations(FileUtils.readFileToString(docFile), docFile);
		for(String anno : annotations){
			System.out.println(anno);
		}
	}
	
	private final KB kb;
	private final StanfordNERAnnotator nerAnnotator;
	
	DistantSupervisionAnnotator(KB kb){
		this.kb = kb;
		this.nerAnnotator = StanfordNERAnnotator.getInstance();
	}
	
	List<String> getDistantSupervisionAnnotations(String fileText, File docFile) throws SQLException{
		List<String> distantSupervisionAnnotations = new ArrayList<String>();
		Annotation doc = nerAnnotator.annotate(fileText);
		
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		//for each sentence check if it contains to entity mentions from the KB
		for(CoreMap sentence : sentences){
			List<String> sentenceLevelDistantSupervisionAnnotations = getSentenceLevelDistantSupervisionAnnotations(sentence, docFile);
			distantSupervisionAnnotations.addAll(sentenceLevelDistantSupervisionAnnotations);
		}
		return distantSupervisionAnnotations;
	}
	
	private List<String> getSentenceLevelDistantSupervisionAnnotations(CoreMap sentence, File docFile) throws SQLException{
		int startOffset = sentence.get(TokensAnnotation.class).get(0).beginPosition();
		String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);

		System.err.println("Starting annotating sentence at start offset " + startOffset + " in doc + " + docFile.getName());
		System.err.println("Sentence = " + sentenceText);
		
		List<String> distantSupervisionAnnotations = new ArrayList<String>();
		List<NamedEntity> namedEntities = collectNamedEntities(sentence);
		
		
		//Generate List<KnowledgeBaseEntity>
		List<List<KnowledgeBaseEntity>> knowledgeBaseEntities = new ArrayList<List<KnowledgeBaseEntity>>();
		for(NamedEntity ne : namedEntities){
			List<String>  associatedKBIds = kb.getAssociatedKBIdsFromName(ne.getName());
			System.err.println("kbIds for " + ne.getName());
			for(String kbId : associatedKBIds){
				System.err.println(kbId);
			}
			List<KnowledgeBaseEntity> namedEntityKnowledgeBaseEntities = new ArrayList<KnowledgeBaseEntity>();
			for(String kbID : associatedKBIds){
				namedEntityKnowledgeBaseEntities.add(new KnowledgeBaseEntity(ne,kbID));
			}
			knowledgeBaseEntities.add(namedEntityKnowledgeBaseEntities);
		}
		
		//Generate List<RelationInstance> for all KnowledgeBaseEntities that appear in KB with a relation
		List<RelationInstance> relationInstances = new ArrayList<RelationInstance>();
		for(int i = 0; i < (knowledgeBaseEntities.size() -1); i ++){
			for(int j = i+1; j < knowledgeBaseEntities.size(); j ++){
				List<KnowledgeBaseEntity> list1 = knowledgeBaseEntities.get(i);
				List<KnowledgeBaseEntity> list2 = knowledgeBaseEntities.get(j);
				
				//make SQL query for relations with entities in list1 as arg1 and get back a List of all the arg2s that cooccur
				//then check that list for items in list2

				List<String> list1Ids = new ArrayList<String>();
				List<String> list2Ids = new ArrayList<String>();
				
				for(KnowledgeBaseEntity e : list1){
					list1Ids.add(e.getKbId());
				}
				for(KnowledgeBaseEntity e : list2){
					list2Ids.add(e.getKbId());
				}

				List<String> x = kb.getListOfCandidateArgument2AndRels(list1Ids);
				List<String> y = kb.getListOfCandidateArgument1AndRels(list1Ids);
				
				for(String arg2AndRel : x){
					String[] values = arg2AndRel.split("\t");
					String arg2 = values[0];
					String rel = values[1];
					if(list2Ids.contains(arg2)){
						relationInstances.add(new RelationInstance());
					}
				}

				
				
				for(KnowledgeBaseEntity kbe1: list1){
					for(KnowledgeBaseEntity kbe2: list2){
						System.err.println(kbe1.getKbId() + "\t" + kbe2.getKbId());
						//if the two kbes partake in a relation in the KB then add that relation to the relationInstances
						List<String> e1ToE2Relations = kb.getAssociatedRelationsFromEntityPair(kbe1.getKbId(),kbe2.getKbId());
						List<String> e2ToE1Relations = kb.getAssociatedRelationsFromEntityPair(kbe2.getKbId(),kbe1.getKbId());
						System.err.println("Number of relations = " + e1ToE2Relations.size());
						System.err.println("Number of backwards realtions = " + e2ToE1Relations.size());
						for(String rel : e1ToE2Relations){
							//create new relationInstance
							relationInstances.add(new RelationInstance(kbe1,kbe2,rel));
						}
						for(String rel : e2ToE1Relations){
							relationInstances.add(new RelationInstance(kbe2,kbe1,rel));
						}
					}
				}
			}
		}
		
		
		//Convert List<RelationInstance> to Strings 
		for(RelationInstance ri: relationInstances){
			String riString = ri.toString();
			String distantSupervisionAnnotationString = riString + "\t" + sentenceText + "\t" + startOffset + "\t" + docFile.getPath();
			distantSupervisionAnnotations.add(distantSupervisionAnnotationString);
		}
		
		System.err.println("Finishing annotating sentence at start offset " + startOffset + " in doc + " + docFile.getName());
		System.err.println("There were " + relationInstances.size() + " relation instances for this sentence .");
		return distantSupervisionAnnotations;
	}

	private List<NamedEntity> collectNamedEntities(CoreMap sentence){
		
		int startIndex = sentence.get(TokensAnnotation.class).get(0).beginPosition();
		String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
		List<NamedEntity> namedEntities = new ArrayList<NamedEntity>();
    	List<CoreLabel> sentenceTokenList = new ArrayList<CoreLabel>();
    	List<CoreLabel> relevantTokens = new ArrayList<CoreLabel>();
    	int tokenIndex =0;
    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
    			String net = token.get(NamedEntityTagAnnotation.class);
    			token.setNER(net);
				token.setIndex(tokenIndex);
    			if( (net.equals("ORGANIZATION"))||
    				(net.equals("LOCATION")) ||
    				(net.equals("PERSON"))
    				){
    				System.err.println("NEW RELEVANT TOKEN: " + token.originalText() + " INDEX = " + token.index() + " NER = " + token.ner());
    				relevantTokens.add(token);
    			}
    		tokenIndex +=1 ;
    	}
    	
    	List<CoreLabel> namedEntityTokens = new ArrayList<CoreLabel>();
    	System.err.println("Processing tokens");
    	for(int i =0; i < relevantTokens.size(); i ++){
    		CoreLabel token = relevantTokens.get(i);
    		//append to tokensList if tokenIndices are neighbors, else create a new namedENtity
    		System.err.println("Token = "+ token.originalText());
    		
    		if(!namedEntityTokens.isEmpty()){
    			//if the next token follows the last one and has the same NER
    			CoreLabel lastToken = namedEntityTokens.get(namedEntityTokens.size()-1);
    			if( (lastToken.index() == (token.index() -1)) &&
    					(lastToken.ner().equals(token.ner())) ){
    				System.err.println("added to token sequence");
    				namedEntityTokens.add(token);
    			}
    			else{
    				//create new NamedEntity
    				System.err.println("added old seqyebce to named Entity List");
    				int begOffset = namedEntityTokens.get(0).beginPosition() - startIndex ;
    				int endOffset = namedEntityTokens.get(namedEntityTokens.size()-1).endPosition() - startIndex;
    				namedEntities.add(new NamedEntity(begOffset,endOffset,sentenceText.substring(begOffset,endOffset)));
    				//clear tokensList
    				namedEntityTokens = new ArrayList<CoreLabel>();
    				namedEntityTokens.add(token);
    			}
    		}
    		
    		else{
    			System.err.println("sequence was empty so started new sequence");
    			namedEntityTokens.add(token);
    		}
    	}
		if(!namedEntityTokens.isEmpty()){
			int begOffset = namedEntityTokens.get(0).beginPosition() - startIndex ;
			int endOffset = namedEntityTokens.get(namedEntityTokens.size()-1).endPosition() - startIndex;
			namedEntities.add(new NamedEntity(begOffset,endOffset,sentenceText.substring(begOffset,endOffset)));
			//clear tokensList
			namedEntityTokens = new ArrayList<CoreLabel>();
		}
		
		System.err.println("Number of named entities is" + namedEntities.size());
		for(NamedEntity ne : namedEntities){
			System.err.println(ne.getName());
		}
		
		return namedEntities;
	}

	
	
	
	private class NamedEntity{
		private int begOffset;
		private int endOffset;
		private String name;
		
		NamedEntity(int begOffset, int endOffset, String name){
			this.begOffset = begOffset;
			this.endOffset = endOffset;
			this.name = name;
		}
		
		String getName(){
			return name;
		}
		
		int getBegOffset(){
			return begOffset;
		}
	}
	
	private class KnowledgeBaseEntity{
		private NamedEntity namedEntity;
		private String kbId;
		
		KnowledgeBaseEntity(NamedEntity namedEntity, String kbId){
			this.namedEntity = namedEntity;
			this.kbId = kbId;
		}
		
		public String getKbId(){
			return kbId;
		}
		
		public NamedEntity getNamedEntity(){
			return namedEntity;
		}
	}
	
	private class RelationInstance{
		private KnowledgeBaseEntity e1;
		private KnowledgeBaseEntity e2;
		private String rel;
		
		RelationInstance(KnowledgeBaseEntity e1, KnowledgeBaseEntity e2, String rel){
			this.e1 = e1;
			this.e2 = e2;
			this.rel = rel;
		}
		
		String makeTabDelimitedString(){
			StringBuilder sb = new StringBuilder();
			sb.append(e1.getNamedEntity().getName());
			sb.append("\t");
			sb.append(e1.getNamedEntity().getBegOffset());
			sb.append("\t");
			sb.append(e2.getNamedEntity().getName());
			sb.append("\t");
			sb.append(e2.getNamedEntity().getBegOffset());
			sb.append("\t");
			sb.append(rel);
			return sb.toString();
		}
		

	}
}
