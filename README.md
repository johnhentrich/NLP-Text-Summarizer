# nlpTextSummarizer
**A project for MCIT which ingests news content and then summarizes it using Stanford NLP library**
<br/>
This project utilizes two packages:<br/>
Jsoup - https://jsoup.org/ <br/>
Stanford NLP - https://nlp.stanford.edu/software/ <br/>
<br/>
<br/>
**Overview**<br/>
We only concentrate on New York Times for right now but this could expand to other news sites.<br/>
The user is given a list of today's articles from the New York Times. They then select which<br/>
one they want to get summarized.  From here the content is parsed over using Stanford's NLP libary<br/>
and the top N sentances are returned.  Sentiment is then averaged out over those sentances.<br/>
<br/>
<br/>
**Instructions**<br/>
To run the code you must download the release package to include both Jsoup and Stanford NLP jars.  If you do not wish to download the release package, you can find thise packages online from the links provided above.<br/>
<br/>
There are 4 main classes contained in this project<br/>
1. NewsScraper.java<br/>
2. NlpPipeline.java<br/>
3. Sentiment.java<br/>
4. Summarizer.java<br/>
<br/>
In order to run the project you must run Summarizer.java.  Once run it will give the user a list
of articles that exist for that day in the New York Times.  It will ask the user to input the number associated with the article they would like summarized and have the sentiment of.<br/>
<br/>
<p>
 <img src="https://i.imgur.com/egKtuIM.png"
         </p>
<br/>
<br/>
The after the user inputs the number they want it will then ask the user how many top sentences it would like returned.  The user is asked to input a number not greater than 7.<br/>
<br/>
<p>
 <img src="https://i.imgur.com/RKpQxk8.png"
         </p>
<br/>
<br/>
The user is then returned the article title, the article content, the summarization sentences and the sentiment.<br/>
<br/>
<p>
 <img src="https://i.imgur.com/ZdO2kVT.png"
         </p>
<br/>
<br/>
**How it Works**<br/>
When Summarizer.java is triggered it calls an instance of NewsScraper.java which then goes out to the New York Times and scrapes the entire webpage, only looking for content body that contains url links.  Some links are not included (based upon certain attributes) as they are not deemed news articles.  A HashMap is created containing a "title" : "url" pair.  From there a new HashMap is created creating a list of numbers and titles.  When the user selects a number, the HashMap index is selected returning a title.  This title then is used as the lookup index HashMap to obtain the url.  That url is then scraped and the content is cleaned.<br/>
<br/>
That content is then called by the NlpPipeline.java class which takes the content and splits it up.  A list of stop words is obtained from a local file stopWords.txt and they are removed from the scoring.  The bag of words method was used for scoring giving some words slightly more value than others.  Then the sentences are scored based on the average score of each word in the sentence.  The average was taken as to mitigate bias from longer sentences. The N number of sentences chosen by the user are then showed to the user.<br/>
<br/>
The sentences are stored in a HashMap that contains the content of the sentence as well as average scoring.  These are then fed into Sentiment.java which takes the sentiment of each sentence as well as the average score of each sentence to create an average sentiment.  The sentiment can range from very negative, negative, neutral, positive, and very positive.  The sum of the weighted averages is then scored and averaged giving the entire article an overall sentiment.  The sentiment is then printed out for the user.
<br/>
<br/>
Wendy Chan - John Hentrich - John Kuchmek<br/>
<br/>
<br/>
<br/>
<br/>
<p align="center">
  <img width="250" height="200" src="https://www.eurorc.com/tuotekuvat/1200x1200/logo_teamC.jpg"
       </p>
<br/>
<br/>
<br/>
 <p align="center">
 <img src="https://upenn.imodules.com/s/1587/images/gid2/editor/eng/pennengineeringlogo.jpg"
         </p>
