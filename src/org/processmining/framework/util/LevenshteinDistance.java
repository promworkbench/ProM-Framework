package org.processmining.framework.util;

import java.util.List;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class LevenshteinDistance{
	int encodingLength;
	float[][] distanceMatrix;
	
	public LevenshteinDistance(int encodingLength, List<String> charStreamList){
		this.encodingLength = encodingLength;
		int noTraces = charStreamList.size();
		distanceMatrix = new float[noTraces][noTraces];
		computeLevenshteinDistanceLinearSpace(charStreamList);
	}
	
	public LevenshteinDistance(){
		this.encodingLength = 1;
	}
	
	protected void computeLevenshteinDistanceLinearSpace(List<String> charStreams){
		int noCharStreams = charStreams.size();
		
		String seq1, seq2;
		int lengthSeq1, lengthSeq2, maxLength, dist;
		float distanceNorm1;
		
		for(int i = 0; i < noCharStreams; i++){
		
			seq1 = charStreams.get(i);
			lengthSeq1 = seq1.length()/encodingLength;
			
			for(int j = 0; j < i; j++){
				seq2 = charStreams.get(j);
				
				if(seq1.equals(seq2)){
					distanceNorm1 = 0;
				}else{
				
					lengthSeq2 = seq2.length()/encodingLength;
				
					maxLength = lengthSeq1;
					if(lengthSeq2 > maxLength)
						maxLength = lengthSeq2;
					
					dist = getLevenshteinDistanceLinearSpace(seq1, seq2);
					distanceNorm1 = dist/(float)(lengthSeq1+lengthSeq2);
				}
				
				distanceMatrix[i][j] = distanceNorm1;
				distanceMatrix[j][i] = distanceNorm1;
				
			}
		}
	}

	public int getLevenshteinDistanceLinearSpace(String seq1, String seq2){
		
		String sI, tJ;
		int lengthSeq1, lengthSeq2, cost;
		
		
		lengthSeq1 = seq1.length()/encodingLength;
		lengthSeq2 = seq2.length()/encodingLength;
		
		int[] S = new int[lengthSeq2+1];
		
		S[0] = 0;
		S[1] = S[0] + 1; //Insert first symbol
		for(int j = 2; j <= lengthSeq2; j++){
			S[j] = S[j-1]+1;
		}
		
		int s,c;
		for(int i = 1; i <= lengthSeq1; i++){
			s = S[0];
			S[0] = c = S[0]+ 1; //Insertion of first symbol
			sI = seq1.substring((i-1)*encodingLength, i*encodingLength);
			for(int j = 1; j <= lengthSeq2; j++){
				tJ = seq2.substring((j-1)*encodingLength, j*encodingLength);
				
				cost = 0;
				if(!sI.equals(tJ))
					cost = 1;
				c = Minimum(S[j]+1, s+cost, c+1);
				s = S[j];
				S[j] = c;
			}
		}
		
		return S[lengthSeq2];
		
	}

	private int Minimum(int a, int b, int c) {
		int mi;

		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;

	}
	
	public float[][] getDistanceMatrix(){
		return distanceMatrix;
	}
}
