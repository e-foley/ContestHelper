import java.util.Comparator;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortPointsSingle implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        ArrayList<Entry> listOne = m1.getEntriesWithMostPoints();
        ArrayList<Entry> listTwo = m2.getEntriesWithMostPoints();
        int result = new Integer(Member.getMostPointsSingle(listTwo)).compareTo(Member.getMostPointsSingle(listOne));
        if (result == 0) {
            result = Member.getNumberOfEntriesWithMostPoints(listTwo) - Member.getNumberOfEntriesWithMostPoints(listOne);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        ArrayList<Entry> list = m.getEntriesWithMostPoints();
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(Member.getMostPointsSingle(list));
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        //ArrayList<Entry> entries = m.getEntries();
        ArrayList<Entry> winners = m.getEntriesWithMostPoints();
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
        
//         for (int h=0; h<winners.size(); h++)
//         {
        for (int i=0; i<winners.size(); i++)
        {
//                 if (winners.get(h).get(i).getWinningness() < 1.0f)
//                 {
//                     building += (df.format(winners.get(h).get(i).getWinningness()) + " in ");
//                 }
            if (linkTopics && winners.get(i).getContest().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + winners.get(i).getContest().getTopic() + "'>#" + winners.get(i).getContest().getName() + "</a>");
            else
                building += ("#" + winners.get(i).getContest().getName());
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
//             if (i != winners.size()-1)
//                 building += "; ";
//         }
        return building;
    }
}