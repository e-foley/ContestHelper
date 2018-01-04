import java.util.Comparator;
import java.util.ArrayList;

public class MemberSortMinEntries implements Comparator<Member>
{
    float minimum;
    
    public MemberSortMinEntries(float minimumSet)
    {
        minimum = minimumSet;
    }
    
    public int compare(Member m1, Member m2)
    {
        if (m2.getTotalEntries() >= minimum && m1.getTotalEntries() < minimum)
            return 1;
        if (m2.getTotalEntries() < minimum && m1.getTotalEntries() >= minimum)
            return -1;
        return 0;
    }
}