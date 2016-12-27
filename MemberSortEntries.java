import java.util.Comparator;
import java.util.ArrayList;

public class MemberSortEntries implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        int result = new Integer(m2.getTotalEntries()).compareTo(m1.getTotalEntries());
        /*
        if (result == 0)
            result = (new MemberSortUncertainty()).compare(m1, m2);*/ // not necessary for victories or entries
        if (result == 0)
            result = (new MemberSortRecent()).compare(m1, m2);
        if (result == 0)
            result = (new MemberSortAlphabetical()).compare(m1, m2);
        return result;
    }
    
    public String getData(Member m)
    {
        return ""+m.getTotalEntries();
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        ArrayList<Entry> entries = m.getEntries();
        
        if (entries.size() == 0)
        {
            return "N/A";   // How did we get here?
        }
        
        for (int i=0; i<entries.size(); i++)
        {   // The below will strip contests of their letters in the case of multi-thread contests
            if (linkTopics && entries.get(i).getContest().hasTopic())
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + entries.get(i).getContest().getTopic() + "'>#" + entries.get(i).getContest().getName() + "</a>");
            else
                building += ("#" + entries.get(i).getContest().getName());
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