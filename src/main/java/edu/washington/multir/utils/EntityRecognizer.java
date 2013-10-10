package edu.washington.multir.utils;

public class EntityRecognizer {

	
	
	public EntityRecognizer(EntityRecognitionAlgorithm alg){
		if(alg == EntityRecognitionAlgorithm.StanfordNER){
			new StanfordNEREntityRecognizer();
		}
	}
	
	
	
	
	class StanfordNEREntityRecognizer {
		
		public StanfordNEREntityRecognizer(){
			
		}
	}
}

