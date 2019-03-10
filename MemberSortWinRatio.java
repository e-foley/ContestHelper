import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortWinRatio implements MemberDataRetriever
{
    public MemberSortWinRatio(int min_entries) {
        min_entries_ = min_entries;
    }
    
    // Note: this sort of ignores the minimum entry requirement...
    public float getValue(Member member) {
        return member.getWinRatio();
    }
    
    public int compare(Member m1, Member m2) {
        int result = new MemberSortMinEntries(min_entries_).compare(m1, m2);
        if (result == 0) {
            result = new Float(m2.getWinRatio()).compareTo(m1.getWinRatio());
        } else if (result == 0) {
            result = (new MemberSortEntries()).compare(m1, m2);
        }
        return result;
    }
    
    public String getData(Member m)
    {
        return getFormat().format(m.getWinRatio());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {        
        DecimalFormat win_df = new DecimalFormat("#,###.##");
        String building = new String();
        building += (win_df.format(m.getTotalWinningness()) + " win");
        if (m.getTotalWinningness() != 1.0)
            building += "s";
        building += (" in " + win_df.format(m.getTotalEntries()) + " attempt");
        if (m.getTotalEntries() != 1)
            building += "s";
        return building;
    }
    
    public NumberFormat getFormat() {
        return new DecimalFormat("#.000");
    }
    
    public boolean qualifies(Member mem) {
        return mem.getTotalEntries() >= min_entries_;
    }
    
    public void precalculate(History history) {}
    
    private int min_entries_;
}