import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortVictories implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getTotalWinningness();
    }
    
    public int compare(Member m1, Member m2) {
        return new Float(m2.getTotalWinningness()).compareTo(m1.getTotalWinningness());
    }
    
    public String getData(Member m)
    {
        return getFormat().format(m.getTotalWinningness());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        ArrayList<Member.EntryStakePair> pairs = m.getEntries();
        ArrayList<Member.EntryStakePair> winners = new ArrayList<Member.EntryStakePair>();
        for (int i=0; i<pairs.size(); i++)
        {
            Member.EntryStakePair pair = pairs.get(i);
            if (pair.entry.getWinningness() * pair.stake > 0) {
                winners.add(pair);
            }
        }
        
        if (winners.size() == 0)
        {
            return "N/A";
        }
        
        for (int i=0; i<winners.size(); i++)
        {
            Member.EntryStakePair pair = winners.get(i);
            
            if (linkTopics && pair.entry.getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + pair.entry.getPoll().getTopic() + "'>#" + pair.entry.getPoll().getShortName() + "</a>");
            else
                building += ("#" + pair.entry.getPoll().getShortName());
            if (pair.entry.getWinningness() * pair.stake < 1.0f)
            {
                building += " (" + getFormat().format(pair.entry.getWinningness() * pair.stake) + ")";
            }
            if (i < winners.size()-2)
                building += ", ";
            else if (i == winners.size()-2)
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
    
    public void precalculate(History history) {}
    
    public Object clone() {
        return this;
    }
}