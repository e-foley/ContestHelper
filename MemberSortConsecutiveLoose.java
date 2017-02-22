import java.util.Comparator;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortConsecutiveLoose implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        ArrayList<ArrayList<Entry>> listOne = m1.getEntriesInLongestStreak(false);
        ArrayList<ArrayList<Entry>> listTwo = m2.getEntriesInLongestStreak(false);
        int result = new Float(Member.getLongestStreak(listTwo)).compareTo(Member.getLongestStreak(listOne));
        if (result == 0) {
            result = Member.getNumberOfLongestStreaks(listTwo) - Member.getNumberOfLongestStreaks(listOne);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        ArrayList<ArrayList<Entry>> list = m.getEntriesInLongestStreak(false);
        DecimalFormat df = new DecimalFormat("#.##");
        
//         if (Member.getNumberOfLongestStreaks(list) > 1)
//             return df.format(Member.getLongestStreak(list)) + "×" + Member.getNumberOfLongestStreaks(list);
//         else
            return df.format(Member.getLongestStreak(list));
        //return Member.getNumberOfLongestStreaks(list) + "×" + df.format(Member.getLongestStreak(list));
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        //ArrayList<Entry> entries = m.getEntries();
        ArrayList<ArrayList<Entry>> winners = m.getEntriesInLongestStreak(false);
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
                if (linkTopics && winners.get(h).get(i).getContest().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                    building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + winners.get(h).get(i).getContest().getTopic() + "'>#" + winners.get(h).get(i).getContest().getName() + "</a>");
                else
                    building += ("#" + winners.get(h).get(i).getContest().getName());
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
}