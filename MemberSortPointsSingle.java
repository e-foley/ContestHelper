import java.util.Comparator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortPointsSingle implements MemberDataRetriever
{
    public float getValue(Member member) {
        ArrayList<Member.EntryStakePair> list = member.getEntriesWithMostPoints();
        return Member.getMostPointsSingle(list);
    }
    
    public int compare(Member m1, Member m2) {
        ArrayList<Member.EntryStakePair> listOne = m1.getEntriesWithMostPoints();
        ArrayList<Member.EntryStakePair> listTwo = m2.getEntriesWithMostPoints();
        int result = new Float(Member.getMostPointsSingle(listTwo)).compareTo(Member.getMostPointsSingle(listOne));
        if (result == 0) {
            result = Member.getNumberOfEntriesWithMostPoints(listTwo) - Member.getNumberOfEntriesWithMostPoints(listOne);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        ArrayList<Member.EntryStakePair> list = m.getEntriesWithMostPoints();
        if (m.getNumberOfEntriesWithMostPoints(list) > 1) {
            return "(" + NumberFormat.getInstance().format(m.getNumberOfEntriesWithMostPoints(list)) + "&times;)&nbsp;" + getFormat().format(Member.getMostPointsSingle(list));
        } else {
            return getFormat().format(Member.getMostPointsSingle(list));
        }
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        String building = new String();
        ArrayList<Member.EntryStakePair> winners = m.getEntriesWithMostPoints();

        if (winners == null || winners.size() == 0)
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