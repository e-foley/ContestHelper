import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortConsecutiveStrict implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getLongestWinStreak(true);
    }
    
    public int compare(Member m1, Member m2) {
        int result = new Float(m2.getLongestWinStreak(true)).compareTo(m1.getLongestWinStreak(true));
        if (result == 0) {
            result = m2.getNumberOfLongestWinStreaks(true) - m1.getNumberOfLongestWinStreaks(true);
        }
        return result;
    }
    
    public String getData(Member m)
    {        
        if (m.getNumberOfLongestWinStreaks(true) > 1) {
            return "(" + (NumberFormat.getInstance()).format(m.getNumberOfLongestWinStreaks(true)) + "&times;)&nbsp;" + getFormat().format(m.getLongestWinStreak(true));
        } else {
            return getFormat().format(m.getLongestWinStreak(true));
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        String building = new String();
        ArrayList<ArrayList<Member.EntryStakePair>> winners = m.getEntriesInLongestWinStreak(true);
        
        if (winners == null || winners.size() == 0)
        {
            return "N/A";
        }
        
        for (int h=0; h<winners.size(); h++)
        {
            for (int i=0; i<winners.get(h).size(); i++)
            {
                Member.EntryStakePair pair = winners.get(h).get(i);
                
                if (linkTopics && pair.entry.getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                    building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + pair.entry.getPoll().getTopic() + "'>#" + pair.entry.getPoll().getShortName() + "</a>");
                else
                    building += ("#" + pair.entry.getPoll().getShortName());
                if (pair.entry.getWinningness() * pair.stake < 1.0f)
                {
                    building += " (" + getFormat().format(pair.entry.getWinningness() * pair.stake) + ")";
                }
                if (i < winners.get(h).size()-2)
                    building += ", ";
                else if (i == winners.get(h).size()-2)
                {
                    if (i == 0)
                        building += " and ";
                    else
                        building += ", and ";
                }
            }
            if (h != winners.size()-1)
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