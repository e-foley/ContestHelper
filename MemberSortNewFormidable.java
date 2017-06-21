import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortNewFormidable implements MemberDataRetriever<Integer>
{
    public Integer getValue(Member member) {
        return member.getNewFormidableRating();
    }
    
    public int compare(Member m1, Member m2) {
        return new Integer(m2.getNewFormidableRating()).compareTo(m1.getNewFormidableRating());
    }
    
    public String getData(Member m)
    {
        return ""+NumberFormat.getInstance().format(m.getNewFormidableRating());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        return "";
    }
}