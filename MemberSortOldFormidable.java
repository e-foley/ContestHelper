import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortOldFormidable implements MemberDataRetriever
{
    public int compare(Member m1, Member m2) {
        int result = new Integer(m2.getOldFormidableRating()).compareTo(m1.getOldFormidableRating());
        //if (result == 0)
        //    result = (new MemberSortUncertainty()).compare(m1, m2);
        if (result == 0)
            result = (new MemberSortRecent()).compare(m1, m2);
        if (result == 0)
            result = (new MemberSortAlphabetical()).compare(m1, m2);
        return result;
    }
    
    public String getData(Member m)
    {
        //if (m.hasUncertainty())
        //    return ""+NumberFormat.getInstance().format(m.getTotalPlusMinusPoints())+"+";
        return ""+NumberFormat.getInstance().format(m.getOldFormidableRating());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        return "";
//         //boolean linkTopics = false;
//         String building = new String();
//         Entry entry;
//         ArrayList<Entry> entries = m.getEntries();
//         for (int i=0; i<entries.size(); i++)
//         {
//             entry = entries.get(i);
//             if (entry.hasUncertainty())
//                 building += "?";
//             else
//                 building += ""+entry.getPlusMinusPoints();
//             if (linkTopics && entry.getContest().hasTopic())
//                 building += (" in <a class='green' href='http://www.purezc.net/forums/index.php?showtopic=" + entry.getContest().getTopic() + "'>#" + entry.getContest().getName() + "</a>");
//             else
//                 building += (" in #" + entry.getContest().getName());
//             if (i < entries.size()-2)
//                 building += ", ";
//             else if (i == entries.size()-2)
//             {
//                 if (i == 0)
//                     building += " and ";
//                 else
//                     building += ", and ";
//             }
//         }
//         return building;
    }
}