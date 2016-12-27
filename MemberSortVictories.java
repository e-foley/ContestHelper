import java.util.Comparator;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MemberSortVictories implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        int result = new Float(m2.getTotalWinningness()).compareTo(m1.getTotalWinningness());
        /*if (result == 0)
            result = (new MemberSortUncertainty()).compare(m1, m2);*/ //not necessary for victories or entries
        if (result == 0)
            result = (new MemberSortRecent()).compare(m1, m2);
        if (result == 0)
            result = (new MemberSortAlphabetical()).compare(m1, m2);
        return result;
    }
    
    public String getData(Member m)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(m.getTotalWinningness());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        //boolean linkTopics = false;
        String building = new String();
        ArrayList<Entry> entries = m.getEntries();
        ArrayList<Entry> winners = new ArrayList<Entry>();
        DecimalFormat df = new DecimalFormat("#.##");
        for (int i=0; i<entries.size(); i++)
        {
            if (entries.get(i).getWinningness() > 0)
                winners.add(entries.get(i));
        }
        
        if (winners.size() == 0)
        {
            return "N/A";
        }
        
        for (int i=0; i<winners.size(); i++)
        {
            if (linkTopics && winners.get(i).getContest().hasTopic()) // NOTE: The below should strip the A and B designations from multi-thread contests
                building += ("<a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + winners.get(i).getContest().getTopic() + "'>#" + winners.get(i).getContest().getName() + "</a>");
            else
                building += ("#" + winners.get(i).getContest().getName());
            if (winners.get(i).getWinningness() < 1.0f)
            {
                building += " (" + df.format(winners.get(i).getWinningness()) + ")";
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
}