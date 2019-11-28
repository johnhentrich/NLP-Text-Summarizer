import java.util.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Sentiment {
	private StanfordCoreNLP nlp;

	/***
	 * this method takes a string as an input and outputs a list of sentiment scores
	 * for each sentence in the string
	 * 
	 * @param text consists of the string of text you want to analyze for sentiments
	 * @return this returns an arraylist of integers corresponding to the sentiment
	 *         for each sentence you have passed through in the string
	 */

	public String stringify(LinkedHashMap<String, Integer> scoreMap) {
		String text = "";

		for (String keys : scoreMap.keySet()) {
			text = text + " "+ keys;
		}
		return text;
	}

	public ArrayList<Integer> weight(LinkedHashMap<String, Integer> scoreMap) {
		ArrayList<Integer> weightList = new ArrayList<Integer>();

		for (String keys : scoreMap.keySet()) {
			int value = scoreMap.get(keys);
			weightList.add(value);
		}
		return weightList;
	}

	public ArrayList<Integer> scores(String text) {

		ArrayList<Integer> scoresList = new ArrayList<Integer>();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		nlp = new StanfordCoreNLP(props);

		Annotation nlpAnnotate = nlp.process(text);

		for (CoreMap element : nlpAnnotate.get(CoreAnnotations.SentencesAnnotation.class)) {
			Tree nlpTree = element.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
			scoresList.add(RNNCoreAnnotations.getPredictedClass(nlpTree));
		}
		return scoresList;
	}

	/***
	 * this calculates the average sentiment across the list of integers
	 * 
	 * @param list is a list of integers corresponding to sentiment scores
	 * @return this returns the average of all of the sentiments in the list
	 */

	public Double avgSentimentNum(ArrayList<Integer> list) {

		double sum = 0;
		for (Integer element : list) {
			sum += element;
		}
		return sum / list.size();
	}

	/***
	 * this calculates the weighted average sentiment across the list of integers
	 * 
	 * @param sent    is a list of integers corresponding to sentiment scores
	 * @param weights is a list of the weights corresponding to the weighted value
	 *                of importance of each sentence
	 * @return this returns the weighted average of all of the sentiments in the
	 *         list
	 */

	public Double weightedAvgSentimentNum(ArrayList<Integer> sent, ArrayList<Integer> weights) {
		double weightedAvg = 0;
		double sum = 0;

		for (Integer element : weights) {
			sum += element;
		}

		for (int i = 0; i < sent.size(); i++) {
			double weight = 0;
			weight = sent.get(i) * weights.get(i) / sum;
			weightedAvg += weight;
		}

		return weightedAvg;
	}

	/**
	 * this interprets the avg sentiment score translating it to a sentiment
	 * classification
	 * 
	 * @param input is the avg sentiment score double
	 * @return this returns a string classification based on the inputted double
	 */
	public String interpret(Double input) {

		if (Math.rint(input) == 0) {
			return "Very negative";

		} else if (Math.rint(input) == 1) {
			return "Negative";

		} else if (Math.rint(input) == 2) {
			return "Neutral";

		} else if (Math.rint(input) == 3) {
			return "Positive";

		} else if (Math.rint(input) == 4) {
			return "Very positive";

		} else
			return "error";
	}
	
}