import java.util.Comparator;
import java.util.ArrayList;

public class MemberSortUncertainty implements Comparator<Member>
{
    public int compare(Member m1, Member m2)
    {
        if (m2.hasUncertainty() == m1.hasUncertainty())
            return 0;
        if (m2.hasUncertainty() && !m1.hasUncertainty())
            return 1;
        if (m1.hasUncertainty() && !m2.hasUncertainty())
            return -1;
        return 0; // can't get here
    }
}