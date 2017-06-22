import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortPoints implements MemberDataRetriever<Integer>
{
    public Integer getValue(Member member) {
        return member.getTotalPoints();
    }
    
    public int compare(Member m1, Member m2) {
        int result = new Integer(m2.getTotalPoints()).compareTo(m1.getTotalPoints());
        if (result == 0) {
            result = (new MemberSortUncertainty()).compare(m1, m2);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        if (m.hasUncertainty())
            return ""+NumberFormat.getInstance().format(m.getTotalPoints())+"+";
        return ""+NumberFormat.getInstance().format(m.getTotalPoints());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        ArrayList<Entry> entries = m.getEntries();
        Entry entry;
        for (int i=0; i<entries.size(); i++)
        {
            entry = entries.get(i);
            if (entry.hasUncertainty())
                building += "?";
            else
                building += ""+entry.getPoints();
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
    
    public NumberFormat getFormat() {
        return NumberFormat.getInstance();
    }
}