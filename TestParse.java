import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;

public abstract class TestParse
{
    public static int doTheThing()
    {
        ArrayList<SotwTopic> sotw_topics = new ArrayList<SotwTopic>();
        
        try {
            // Set up our certificates and such.
            System.setProperty("javax.net.ssl.trustStore", "cert/wwwpurezcnet.jks");
            // Navigate to the main SotW listing.
            Document doc = Jsoup.connect("https://www.purezc.net/forums/index.php?showforum=45").get();
            // System.out.println(doc.title());
            // Look at all the contest numbers in the threads.
            // (https://jsoup.org/apidocs/org/jsoup/select/Selector.html is a good resource for this syntax.)
            Elements showtopic_links = doc.select("a[href*=showtopic]");
            
            for (Element elem : showtopic_links) {
                // All our SotW topic names are in a <span itemprop="name"></span> sequence.
                Element candidate = elem.selectFirst("span[itemprop=name]");
                if (candidate == null) {
                    continue;
                }
                
                String topic_title = candidate.ownText();
                if (!topic_title.matches("Screenshot of the Week \\d+")) {
                    continue;
                }
                
                // 23 is chosen to be where the numbers should start.
                int contest_number = Integer.parseInt(topic_title.substring(23));
                String url = elem.attributes().get("href");
                
                if (contest_number <= 0) {
                    System.out.println("Invalid contest number: " + contest_number);
                    continue;
                } else if (url.isEmpty()) {
                    System.out.println("Invalid contest URL: " + url);
                    continue;
                }
                
                SotwTopic topic = new SotwTopic();
                topic.number = contest_number;
                topic.url = url;
                sotw_topics.add(topic);
            }
        } catch (java.io.IOException e) {
            System.out.println(e.toString());
            return 1;
        }
        
        for (SotwTopic topic : sotw_topics) {
            System.out.println(topic.number + ": " + topic.url);
        }
        
        return 42;
    }
}
