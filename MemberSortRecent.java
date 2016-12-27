import java.util.Comparator;
import java.util.ArrayList;

public class MemberSortRecent implements Comparator<Member>
{
    public int compare(Member m1, Member m2)
    {
        int offset = 0;
        int result = 0;
        Entry entry1;
        Entry entry2;
        
        while (result == 0)
        {
            entry1 = m1.getRecentEntry(offset);
            entry2 = m2.getRecentEntry(offset);
        
            if (entry1 == null && entry2 != null)
                return 1;
            if (entry1 != null && entry2 == null)
                return -1;
            if (entry1 == null && entry2 == null)
                return 0;
           
            result = m2.getRecentEntry(offset).getContest().getSynch() - m1.getRecentEntry(offset).getContest().getSynch();
    
            if (result == 0) // i.e. if both members last participated in the same contest
            {
                if (m2.getRecentEntry(offset).hasUncertainty() && !m1.getRecentEntry(offset).hasUncertainty())
                    result = 1;
                else if (m1.getRecentEntry(offset).hasUncertainty() && !m2.getRecentEntry(offset).hasUncertainty())
                    result = -1;
                else 
                    result = m2.getRecentEntry(offset).getVotes() - m1.getRecentEntry(offset).getVotes();
            }
            
            offset++;
        }
        return result;
    }
}