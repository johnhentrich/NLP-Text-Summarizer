import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is to build an NLP pipeline to parse sentences of a string to calculate
 * score of importance of each sentence
 *
 * @author wendychan
 *
 */
public class NlpPipeline {
	Set<String> stopWords;
	HashMap<String, Integer> wordFrequencyMap;
	LinkedHashMap<String, Integer> scoreMap;


	public NlpPipeline() {
		this.stopWords = buildStopWordsSet();
		this.wordFrequencyMap = new HashMap<>();
	}

	/**
	 * get sentences from news article that has top score of importance
	 *
	 * @param cleanText    news article text
	 * @param maxSentences maximum number of sentences to be returned in the summary
	 * @return sentences from news article within the number of maxSentences
	 */
	public String getSummary(String cleanText, int maxSentences) {
		LinkedHashMap<String, Integer> sentenceScoreMap = getSentenceScoreMap(cleanText);
		TreeMap<Integer, ArrayList<String>> sortedMap = buildSortedMap(sentenceScoreMap);
		Iterator<Entry<Integer, ArrayList<String>>> sortedMapIterator = sortedMap.entrySet().iterator();
		ArrayList<String> finalSentences = new ArrayList<String>();
		int sentenceCounter = 0;
		while (sortedMapIterator.hasNext() && sentenceCounter < maxSentences) {
			Entry<Integer, ArrayList<String>> pair = sortedMapIterator.next();
			ArrayList<String> sentList = new ArrayList<String>(pair.getValue());
			for (String sentInList : sentList) {
				finalSentences.add(sentInList);
				sentenceCounter++;
				if (sentenceCounter >= maxSentences) {
					break;
				}
			}
		}

		Iterator<Entry<String, Integer>> scoreMapIterator = sentenceScoreMap.entrySet().iterator();

		String summary = "";
		int counter = 0;
		while (scoreMapIterator.hasNext() && counter < maxSentences) {
			Entry<String, Integer> pair = scoreMapIterator.next();
			if (finalSentences.contains(pair.getKey())) {
				summary += pair.getKey();
				summary += "\n";
				counter++;
			}
		}

		return summary;
	}

	/**
	 * make score map available
	 */
	public LinkedHashMap<String, Integer> getScoreMap() {
		return scoreMap;
	}

	/**
	 * build a map for storing key as the importance score and value as an arraylist
	 * of sentences with the same score
	 *
	 * @param scoreMap a map with each sentence as key and score as value. The map
	 *                 maintains the original sequence of sentences from the news
	 *                 article
	 * @return a map for storing key as the importance score and value as an
	 *         arraylist of sentences with the same score. the map is sorted from
	 *         high to low importance score.
	 */
	private TreeMap<Integer, ArrayList<String>> buildSortedMap(LinkedHashMap<String, Integer> scoreMap) {
		TreeMap<Integer, ArrayList<String>> sortedMap = new TreeMap<Integer, ArrayList<String>>(
				Collections.reverseOrder());
		scoreMap.entrySet().forEach(entry -> {
			if (!sortedMap.containsKey(entry.getValue())) {
				ArrayList<String> arrSent = new ArrayList<String>();
				arrSent.add(entry.getKey());
				sortedMap.put(entry.getValue(), arrSent);

			} else {
				ArrayList<String> arrSent = new ArrayList<String>(sortedMap.get(entry.getValue()));
				arrSent.add(entry.getKey());
				sortedMap.put(entry.getValue(), arrSent);
			}
		});

		return sortedMap;
	}

	/**
	 *
	 * @param text news article string to be parsed and calculate the score of each
	 *             sentence
	 * @return a map with each sentence as key and score as value. The map maintains
	 *         the original sequence of sentences from the news article
	 */
	private LinkedHashMap<String, Integer> getSentenceScoreMap(String text) {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER,
		// parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		LinkedHashMap<String, Integer> posSentencesMap = new LinkedHashMap<>();

		for (CoreMap sentence : sentences) {
			String s = sentence.toString();
			posSentencesMap.put(s, 0);
		}

		for (CoreMap sentence : sentences) {

			int score = 0;
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the NER label of the token
				String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				if (posTags().contains(ne)) {
					score++;
				}

				// this is the text of the token
				String originalWord = token.get(CoreAnnotations.TextAnnotation.class);
				String word = originalWord.toLowerCase();
				wordFregencyCalculator(word);
			}

			posSentencesMap.put(sentence.toString(), score);

		}

		LinkedHashMap<String, Integer> weightedMap = addWfWeight(posSentencesMap);
		scoreMap = weightedMap;
		return weightedMap;

	}

	/**
	 * calculate and build a map to store the frequency of a word appearing in the
	 * article, ignoring all stop words
	 *
	 * @param word Word from the news article in lower case
	 */
	private void wordFregencyCalculator(String word) {
		if (!this.stopWords.contains(word)) {
			wordFrequencyMap.putIfAbsent(word, 0);
			wordFrequencyMap.put(word, wordFrequencyMap.get(word) + 1);
		}
	}

	/**
	 * create a set of Part of Speech tags to be used to calculate the importance
	 * score
	 *
	 * @return a set of Part of Speech tags to be used to calculate the importance
	 *         score
	 */
	private Set<String> posTags() {

		Set<String> posTagsSet = new HashSet<String>();
		posTagsSet.add("ORGANIZATION");
		posTagsSet.add("TITLE");
		posTagsSet.add("PERSON");
		posTagsSet.add("CITY");
		posTagsSet.add("LOCATION");
		posTagsSet.add("DATE");
		posTagsSet.add("IDEOLOGY");
		posTagsSet.add("MONEY");

		return posTagsSet;
	}

	/**
	 * add word frequency weight into the map of sentence score by adding score to
	 * the sentence containing frequent words
	 *
	 * @param posSentencesMap
	 * @return map of sentence score with word frequency weighted
	 */
	private LinkedHashMap<String, Integer> addWfWeight(LinkedHashMap<String, Integer> posSentencesMap) {
		Set<String> frequentWordSet = new HashSet<String>();

		final int MIN_FREQUENCY = 3;
		wordFrequencyMap.forEach((word, frequency) -> {
			if (frequency >= MIN_FREQUENCY) {
				frequentWordSet.add(word);
			}
		});

		posSentencesMap.forEach((sentence, score) -> {
			for (String word : frequentWordSet) {
				if (sentence.toLowerCase().contains(word)) {
					posSentencesMap.put(sentence, posSentencesMap.get(sentence) + 1);
				}
			}
		});

		return posSentencesMap;

	}

	/**
	 * parse a file and build a set containing all the stop words
	 *
	 * @return a set containing all the stop words
	 */
	private Set<String> buildStopWordsSet() {
		Set<String> stopWordsSet = new HashSet<String>();

		File file = new File("stopWords.txt");
		try (Scanner fileParser = new Scanner(file);) {
			while (fileParser.hasNextLine()) {
				String vocab = fileParser.nextLine().toLowerCase();
				stopWordsSet.add(vocab);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Stop Words File not found. Please try again");
			e.printStackTrace();
		}
		return stopWordsSet;
	}
}
