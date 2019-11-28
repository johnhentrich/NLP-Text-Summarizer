import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * A class that scrapes the New York Times web-site and gives the user a list of articles
 * to select from.  Once an article is picked It will return the body of the article as a string
 * so the text can be summarized and sentiment can be derived from it using NLP.
 * 
 * This class takes advantage of Jsoup for web scraping and parsing the html.
 * @author jkuchmek
 *
 */
public class NewsScraper {
	String content;
	Document doc;
	String url = "https://www.nytimes.com/";
	HashMap<String, String> articles = new HashMap<String,String>();
	HashMap<Integer, String> selection = new HashMap<Integer, String>();
	HashMap<Integer, String> selectionClean = new HashMap<Integer, String>();
	static Scanner pickedArticle  = new Scanner(System.in);

	/**
	 * This method takes the New York Times url and returns a HashMap
	 * that consists of article names as keys and links as values
	 * @param url
	 * @return
	 */

	public HashMap<String, String> articleReader(String url) {
		try {
			this.doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements links = this.doc.select("a[href]");
		Set<String> keys = this.articles.keySet();
		Iterator<String> keyIter = keys.iterator();
					
		for (Element link: links) {
			if( link.attr("href").contains("/subscription") || link.attr("href").contains("/briefing/") || link.text().contains("© 2019 The New York Times Company")) {
				
			}
			else if(link.attr("href").contains(".html")) {
				this.articles.put(link.text(), link.attr("href"));
				while(keyIter.hasNext()){
					String key = keyIter.next();
					String value = this.articles.get(key);
					this.articles.put(key,value);
				}
			}
		}
		return this.articles;		
	}
	
	/**
	 * This method takes as input a HashMap as article list and returns a HashMap 
	 * consisting of numbers as keys and article names as values.
	 * 
	 * It also cleans the list up a bit to not include what is percieved as not
	 * real articles
	 * 
	 * @param articleList
	 * @return
	 */
	public HashMap<Integer, String> selectionContent(HashMap<String, String> articleList){
		this.articles = articleList;
		int articleIndex = 0;	
		Set<String> articleKeys = this.articles.keySet();
		for(String key: articleKeys) {
			if(key.isEmpty()) {
		}
			else {
				this.selection.put(articleIndex,key);
				articleIndex++;
			}
		
	}
		int num = 0;
		//HashMap<Integer, String> selectionClean = new HashMap<Integer, String>();
		for(String key: articleKeys) {
			String[] titles = key.split(" ");
			if(key != null) {
				if(titles.length > 3) {
					this.selectionClean.put(num, key);
					num++;
				}
			}
		}
		
		return this.selectionClean;
	}
	
	/**
	 * This method prints out a list of todays New York Times articles and returns a number.
	 * @return
	 */
	
	public int userSelction() {
		boolean properInput = false;
		int selected = 0;
		HashMap<String, String> newsArticles = new HashMap<String, String>();
		HashMap<Integer, String> todaysArticles = new HashMap<Integer, String>();
		System.out.println("Here is a list of todays articles from the New York Times ");
		//System.out.println("Please select a number of an article you would like to summerize: ");
		System.out.println();
		NewsScraper news = new NewsScraper();
		newsArticles = news.articleReader("https://www.nytimes.com/");		
		todaysArticles = news.selectionContent(newsArticles);
		Integer maxKey = Collections.max(todaysArticles.keySet());
		Iterator<Map.Entry<Integer, String>> entries = todaysArticles.entrySet().iterator();
		while(entries.hasNext()) {
			Map.Entry<Integer, String> entry = entries.next();
			int oneFromKey = entry.getKey() + 1;
			System.out.println(oneFromKey + ". " + entry.getValue());
		}

		
		do{
			if(pickedArticle.hasNextInt()) {
				int keySelected = pickedArticle.nextInt();
				if (keySelected - 1 > maxKey || keySelected < 1) {
					System.out.println("The number you selected is out of range. Please try again: ");
				}else {
				selected = keySelected - 1;
				properInput = true;
				}
			}
			else {
				System.out.println("You must input a number! Please try again: ");
				pickedArticle.nextLine();
			}
		}while (!properInput);
		System.out.println();
		System.out.println("You selected article: " + "\n" + "\"" + todaysArticles.get(selected) + "\"");
		System.out.println();
		System.out.println("Here is the content of the article.");
		return selected;
	}
	
	/**
	 * This method takes a number as input and returns the content of the article chosen
	 * 
	 * @param num
	 * @return
	 */
	public String summerizeArticle(int num, HashMap<Integer, String> selectionClean, HashMap<String,String> articles) {		
		Integer articleNum = num;
		String content = null;
		this.selectionClean = selectionClean;
		String title = selectionClean.get(articleNum);
		this.articles = articles;
		String articleURL = this.articles.get(title);
		Document doc;
		String cleanContent = null;
		String strippedContent = null;
		String newContent = null;
		String finalContent = null;
		try {
			doc = Jsoup.connect("https://www.nytimes.com" + articleURL).get();
			content = doc.body().text();
			title = doc.title();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Pattern contentPattern = Pattern.compile("(?<=by ).*$");
		Matcher contentMatcher = contentPattern.matcher(content);
		if(contentMatcher.find()) {
			cleanContent = contentMatcher.group(0);
		}
		Pattern footerPattern = Pattern.compile("^[^»]*");
		Matcher footerMatcher = footerPattern.matcher(cleanContent);
		if(footerMatcher.find()) {
			strippedContent = footerMatcher.group(0);
		}
		Pattern finalPattern = Pattern.compile(".*(?=\\.)");
		Matcher finalMatcher = finalPattern.matcher(strippedContent);
		if(finalMatcher.find()) {
			finalContent = finalMatcher.group(0);
		}

		
		newContent = finalContent.replaceAll("Image.+?(?=The New York Times )The New York Times ", "").replaceAll("Credit.+?(?=The New York Times )The New York Times ", "")
				.replaceAll("By.+?(?=[A-Za-z]{3} \\d{2}, \\d{4})[A-Za-z]{3} \\d{2}, \\d{4} ", "")
				.replaceAll("([A-Z])\\.(?=[ A-Z.])", "$1")
				.replaceAll("(\\.)([A-Za-z])", "$1 $2")
				.replaceAll("(?<=[a-z])([A-Z])", " $1");
		String contentDone = newContent + ".";
		
		if(contentDone.contains("[A-Za-z].[A-Za-z}")) {
			
		}
		return contentDone.replace("/", " ");
	}
	/**
	 * This method asks the user how many summarization sentences they would like and uses the input.
	 * @return int
	 */
	public int maxSentence() {
		int num = 0;
		boolean numRange = false;
		System.out.println("How many summarization sentences would you like? ");
		System.out.println();
		do{
			//Scanner in  = pickedArticle;
			if(pickedArticle.hasNextInt()) {
				int maxNumber = pickedArticle.nextInt();
				if (maxNumber > 7 || maxNumber < 1) {
					System.out.println("The number you selected is out of range. Please try again: ");
				}else {
				num = maxNumber;
				numRange = true;
				}
			}
			else {
				System.out.println("You must input a number! Please try again: ");
				pickedArticle.nextLine();
			}
		}while (!numRange);
	      return num;
	   }
	
	public static void main (String[] args) {


		}
	
}