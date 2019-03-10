import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortVotes implements MemberDataRetriever
{
    public float getValue(Member member) {
        return member.getTotalVotes();
    }
    
    public int compare(Member m1, Member m2) {
        int result = new Float(m2.getTotalVotes()).compareTo(m1.getTotalVotes());
        if (result == 0) {
            result = (new MemberSortUncertainty()).compare(m1, m2);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        if (m.hasUncertainty())
            return getFormat().format(m.getTotalVotes())+"+";
        return getFormat().format(m.getTotalVotes());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        Entry entry;
        ArrayList<Member.EntryStakePair> entries = m.getEntries();
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            entry = pair.entry;
            float stake = pair.stake;
            if (entry.hasUncertainty())
                building += "?";
            else
                building += ""+getFormat().format(entry.getVotes() * stake);  // Could mention stake here... later?
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
        return new DecimalFormat("#,###.##");
    }
    
    public boolean qualifies(Member mem) {
        return true;
    }
    
    public void precalculate(History history) {}
}