import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortPlusMinusPoints implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        return new Integer(m2.getTotalPlusMinusPoints()).compareTo(m1.getTotalPlusMinusPoints());
    }
    
    public String getData(Member m)
    {
        return ""+NumberFormat.getInstance().format(m.getTotalPlusMinusPoints());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        Entry entry;
        ArrayList<Entry> entries = m.getEntries();
        for (int i=0; i<entries.size(); i++)
        {
            entry = entries.get(i);
            if (entry.hasUncertainty())
                building += "?";
            else
                building += ""+entry.getPlusMinusPoints();
            if (linkTopics && entry.getPoll().hasTopic())
                building += (" in <a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + entry.getPoll().getTopic() + "'>#" + entry.getPoll().getName() + "</a>");
            else
                building += (" in #" + entry.getPoll().getName());
            if (i < entries.size()-2)
                building += ", ";
            else if (i == entries.size()-2)
            {
                if (i == 0)
                    building += " and ";
                else
                    building += ", and ";
            }
        }
        return building;
    }
}