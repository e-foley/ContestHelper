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
        float stake1;
        float stake2;
        
        while (result == 0)
        {
            Member.EntryStakePair temp1 = m1.getRecentEntry(offset);
            Member.EntryStakePair temp2 = m2.getRecentEntry(offset);
        
            if (temp1 == null && temp2 != null)
                return 1;
            if (temp1 != null && temp2 == null)
                return -1;
            if (temp1 == null && temp2 == null)
                return 0;
           
            entry1 = temp1.entry;
            entry2 = temp2.entry;
                
            result = entry2.getPoll().getSynch() - entry1.getPoll().getSynch();
    
            if (result == 0) // i.e. if both members last participated in the same contest
            {
                if (entry2.hasUncertainty() && !entry1.hasUncertainty())
                    result = 1;
                else if (entry1.hasUncertainty() && !entry2.hasUncertainty())
                    result = -1;
                else
                    result = (int)(entry2.getVotes() * temp2.stake - entry1.getVotes() * temp1.stake);
            }
            
            offset++;
        }
        return result;
    }
}