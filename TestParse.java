import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

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
            Document doc = Jsoup.connect("https://www.purezc.net/forums/index.php?showforum=45").get();
            System.out.println(doc.title());
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
