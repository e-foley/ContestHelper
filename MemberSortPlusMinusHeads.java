import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortPlusMinusHeads implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getTotalPlusMinusHeads();
    }
    
    public int compare(Member m1, Member m2) {
        return new Float(m2.getTotalPlusMinusHeads()).compareTo(m1.getTotalPlusMinusHeads());
    }
    
    public String getData(Member m)
    {
        return getFormat().format(m.getTotalPlusMinusHeads());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        Entry entry;
        ArrayList<Member.EntryStakePair> entries = m.getEntries();
        DecimalFormat df = new DecimalFormat("#.##");
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            entry = pair.entry;
            float stake = pair.stake;
            if (entry.hasUncertainty())
                building += "?";
            else
                building += ""+df.format(entry.getPlusMinusHeads() * stake);
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
        return new DecimalFormat("#.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
}