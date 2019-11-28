import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * Entry point of the program
 */
public class Summarizer {
	
	public static void main(String[] args) {
		// get clean content from NY Times article
		NewsScraper todaysNews = new NewsScraper();
		HashMap<String, String> todaysArticles = todaysNews.articleReader("https://www.nytimes.com");
		HashMap<Integer, String> articlesList = todaysNews.selectionContent(todaysArticles);
		String content = todaysNews.summerizeArticle(todaysNews.userSelction(), articlesList, todaysArticles);
		
		// print original content [this can be removed]
		System.out.println("The following is the original article from NY Times:");
		System.out.println(content);
		
		// get number of summarized sentences and returns the ton N ranked sentences
		NlpPipeline pipeline = new NlpPipeline();
		int MAX_SENTENCES = todaysNews.maxSentence();
		String summary = pipeline.getSummary(content, MAX_SENTENCES);
    	LinkedHashMap<String, Integer> scoreMap = pipeline.getScoreMap(); //for Sentiment Analysis to consume
		Sentiment sent = new Sentiment();
		String sentiment = sent.interpret(sent.weightedAvgSentimentNum(sent.scores(sent.stringify(scoreMap)), sent.weight(scoreMap)));
		System.out.println("*********************************************************");
		System.out.println("The following is the summarized version of the article:");
		System.out.println(summary);
		System.out.println("The following is the overall sentiment of the article:");
		System.out.println(sentiment);
		
	}
	
}