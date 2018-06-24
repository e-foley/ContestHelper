import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import javax.net.ssl.SSLSocketFactory;

/**
 * Abstract class TestParse - write a description of the class here
 * 
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class TestParse
{
    // instance variables - replace the example below with your own
    private int x;

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y    a sample parameter for a method
     * @return        the sum of x and y 
     */
    public static int doTheThing()
    {
        try {
            // Set up our certificates and such.
            System.setProperty("javax.net.ssl.trustStore", "cert/wwwpurezcnet.jks");
            // Navigate to the main SotW listing.
            Document doc = Jsoup.connect("https://www.purezc.net/forums/index.php?showforum=45").get();
            System.out.println(doc.title());
            // Look at all the contest numbers in the threads.
            // (https://jsoup.org/apidocs/org/jsoup/select/Selector.html is a good resource.)
            Elements contest_elements = doc.select("a[href*=showtopic]");
            for (Element elem : contest_elements) {
                System.out.println(elem.html());
            }
//             Elements newsHeadlines = doc.select("#mp-itn b a");
//             for (Element headline : newsHeadlines) {
//               System.out.println(headline.attr("title"));
//               System.out.println(headline.absUrl("href"));
//             }
        } catch (java.io.IOException e) {
            System.out.println(e.toString());
            return 1;
        }
        
        return 42;
    }
}
