import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortOldFormidable implements MemberDataRetriever<Integer>
{
    public Integer getValue(Member member) {
        return member.getOldFormidableRating();
    }
    
    public int compare(Member m1, Member m2) {
        return new Integer(m2.getOldFormidableRating()).compareTo(m1.getOldFormidableRating());
    }
    
    public String getData(Member m)
    {
        return ""+NumberFormat.getInstance().format(m.getOldFormidableRating());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        return "";
    }
}