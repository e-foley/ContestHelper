import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortVotesSingle implements MemberDataRetriever
{
    public float getValue(Member member) {
        ArrayList<Member.EntryStakePair> list = member.getEntriesWithMostVotes();
        return Member.getMostVotesSingle(list);
    }
    
    public int compare(Member m1, Member m2) {
        ArrayList<Member.EntryStakePair> listOne = m1.getEntriesWithMostVotes();
        ArrayList<Member.EntryStakePair> listTwo = m2.getEntriesWithMostVotes();
        int result = new Float(Member.getMostVotesSingle(listTwo)).compareTo(Member.getMostVotesSingle(listOne));
        if (result == 0) {
            result = Member.getNumberOfEntriesWithMostVotes(listTwo) - Member.getNumberOfEntriesWithMostVotes(listOne);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        ArrayList<Member.EntryStakePair> list = m.getEntriesWithMostVotes();
        if (m.getNumberOfEntriesWithMostVotes(list) > 1) {
            return "(" + NumberFormat.getInstance().format(m.getNumberOfEntriesWithMostVotes(list)) + "&times;)&nbsp;" + getFormat().format(Member.getMostVotesSingle(list));
        } else {
            return getFormat().format(Member.getMostVotesSingle(list));
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        String building = new String();
        ArrayList<Member.EntryStakePair> winners = m.getEntriesWithMostVotes();

        if (winners == null || winners.size() == 0)
        {
            return "N/A";
        }
        
        for (int i=0; i<winners.size(); i++)
        {
            Member.EntryStakePair pair = winners.get(i);
            if (linkTopics && pair.entry.getPoll().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + pair.entry.getPoll().getTopic() + "'>#" + pair.entry.getPoll().getName() + "</a>");
            else
                building += ("#" + pair.entry.getPoll().getName());
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
}