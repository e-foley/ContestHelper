import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortVotesSingle implements MemberDataRetriever
{
    public float getValue(Member member) {
        ArrayList<Entry> list = member.getEntriesWithMostVotes();
        return Member.getMostVotesSingle(list);
    }
    
    public int compare(Member m1, Member m2) {
        ArrayList<Entry> listOne = m1.getEntriesWithMostVotes();
        ArrayList<Entry> listTwo = m2.getEntriesWithMostVotes();
        int result = new Integer(Member.getMostVotesSingle(listTwo)).compareTo(Member.getMostVotesSingle(listOne));
        if (result == 0) {
            result = Member.getNumberOfEntriesWithMostVotes(listTwo) - Member.getNumberOfEntriesWithMostVotes(listOne);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        ArrayList<Entry> list = m.getEntriesWithMostVotes();
        if (m.getNumberOfEntriesWithMostVotes(list) > 1) {
            return "(" + m.getNumberOfEntriesWithMostVotes(list) + "&times;)&nbsp;" + df.format(Member.getMostVotesSingle(list));
        } else {
            return df.format(Member.getMostVotesSingle(list));
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        //ArrayList<Entry> entries = m.getEntries();
        ArrayList<Entry> winners = m.getEntriesWithMostVotes();
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
            if (linkTopics && winners.get(i).getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + winners.get(i).getPoll().getTopic() + "'>#" + winners.get(i).getPoll().getName() + "</a>");
            else
                building += ("#" + winners.get(i).getPoll().getName());
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
    
    public NumberFormat getFormat() {
        return new DecimalFormat("#.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
}