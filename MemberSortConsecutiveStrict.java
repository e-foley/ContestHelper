import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortConsecutiveStrict implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getLongestStreak(true);
    }
    
    public int compare(Member m1, Member m2) {
        int result = new Float(m2.getLongestStreak(true)).compareTo(m1.getLongestStreak(true));
        if (result == 0) {
            result = m2.getNumberOfLongestStreaks(true) - m1.getNumberOfLongestStreaks(true);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        
        if (m.getNumberOfLongestStreaks(true) > 1) {
            return "(" + m.getNumberOfLongestStreaks(true) + "&times;)&nbsp;" + df.format(m.getLongestStreak(true));
        } else {
            return df.format(m.getLongestStreak(true));
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        //ArrayList<Entry> entries = m.getEntries();
        ArrayList<ArrayList<Entry>> winners = m.getEntriesInLongestStreak(true);
        DecimalFormat df = new DecimalFormat("#.##");
//         for (int i=0; i<entries.size(); i++)
//         {
//             if (entries.get(i).getWinningness() > 0)
//                 winners.add(entries.get(i));
//         }
        
        //System.out.print("Comparing...");
        if (winners == null || winners.size() == 0)
        {
            return "N/A";
        }
        
        for (int h=0; h<winners.size(); h++)
        {
            for (int i=0; i<winners.get(h).size(); i++)
            {
//                 if (winners.get(h).get(i).getWinningness() < 1.0f)
//                 {
//                     building += (df.format(winners.get(h).get(i).getWinningness()) + " in ");
//                 }
                if (linkTopics && winners.get(h).get(i).getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                    building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + winners.get(h).get(i).getPoll().getTopic() + "'>#" + winners.get(h).get(i).getPoll().getName() + "</a>");
                else
                    building += ("#" + winners.get(h).get(i).getPoll().getName());
                if (winners.get(h).get(i).getWinningness() < 1.0f)
                {
                    building += " (" + df.format(winners.get(h).get(i).getWinningness()) + ")";
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
        return new DecimalFormat("#.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
}