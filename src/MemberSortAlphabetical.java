import java.util.Comparator;
import java.util.ArrayList;

public class MemberSortAlphabetical implements Comparator<Member>
{
    public int compare(Member m1, Member m2)
    {
        return m1.getMostRecentName().toLowerCase().compareTo(m2.getMostRecentName().toLowerCase());
    }
}