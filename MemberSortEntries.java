import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortEntries implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getTotalEntries();
    }
    
    public int compare(Member m1, Member m2) {
        return new Float(m2.getTotalEntries()).compareTo(m1.getTotalEntries());
    }
    
    public String getData(Member m)
    {
        return getFormat().format(m.getTotalEntries());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        ArrayList<Member.EntryStakePair> pairs = m.getEntries();
        
        if (pairs.size() == 0)
        {
            return "N/A";   // How did we get here?
        }
        
        for (int i=0; i<pairs.size(); i++) {
            Member.EntryStakePair pair = pairs.get(i);
            Entry entry = pair.entry;
            float stake = pair.stake;
            // The below will strip contests of their letters in the case of multi-thread contests
            if (linkTopics && entry.getPoll().hasTopic()) {
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + entry.getPoll().getTopic() + "'>#" + entry.getPoll().getName() + "</a>");
            } else {
                building += ("#" + entry.getPoll().getName());
            }
            if (stake != 1.0f)
            {
                building += " (" + getFormat().format(stake) + ")";
            }
            if (i < pairs.size()-2)
                building += ", ";
            else if (i == pairs.size()-2)
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
        return new DecimalFormat("#,###.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
}