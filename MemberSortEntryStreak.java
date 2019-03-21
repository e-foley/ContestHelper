import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortEntryStreak implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getLongestEntryStreak();
    }
    
    public int compare(Member m1, Member m2) {
        int result = new Float(m2.getLongestEntryStreak()).compareTo(m1.getLongestEntryStreak());
        if (result == 0) {
            result = m2.getNumberOfLongestEntryStreaks() - m1.getNumberOfLongestEntryStreaks();
        }
        return result;
    }
    
    public String getData(Member m)
    {        
        if (m.getNumberOfLongestEntryStreaks() > 1) {
            return "(" + (NumberFormat.getInstance()).format(m.getNumberOfLongestEntryStreaks()) + "&times;)&nbsp;" + getFormat().format(m.getLongestEntryStreak());
        } else {
            return getFormat().format(m.getLongestEntryStreak());
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        String building = new String();
        ArrayList<ArrayList<Member.EntryStakePair>> entries = m.getEntriesInLongestEntryStreak();
        
        if (entries == null || entries.size() == 0) {
            return "N/A";
        }
        
        for (int h=0; h<entries.size(); h++)
        {
            for (int i=0; i<entries.get(h).size(); i++)
            {
                Member.EntryStakePair pair = entries.get(h).get(i);
                
                if (linkTopics && pair.entry.getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                    building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + pair.entry.getPoll().getTopic() + "'>#" + pair.entry.getPoll().getName() + "</a>");
                else
                    building += ("#" + pair.entry.getPoll().getName());
                if (pair.stake < 1.0f)
                {
                    building += " (" + getFormat().format(pair.stake) + ")";
                }
                if (i < entries.get(h).size()-2)
                    building += ", ";
                else if (i == entries.get(h).size()-2)
                {
                    if (i == 0)
                        building += " and ";
                    else
                        building += ", and ";
                }
            }
            if (h != entries.size()-1)
                building += "; ";
        }
        return building;
    }
    
    public NumberFormat getFormat() {
        return new DecimalFormat("#,###.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
    
    public void precalculate(History history) {}
    
    public Object clone() {
        return this;
    }
}