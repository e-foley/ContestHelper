import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;

public class MemberSortWinRatio implements MemberDataRetriever<Float>
{
    public MemberSortWinRatio(int min_entries) {
        min_entries_ = min_entries;
    }
    
    // Note: this sort of ignores the minimum entry requirement...
    public Float getValue(Member member) {
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
        return ""+NumberFormat.getInstance().format(m.getWinRatio());
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {        
        String building = new String();
        building += (NumberFormat.getInstance().format(m.getTotalWinningness()) + " win");
        if (m.getTotalWinningness() != 1.0)
            building += "s";
        building += (" in " + m.getTotalEntries() + " attempt");
        if (m.getTotalEntries() != 1)
            building += "s";
        return building;
    }
    
    public NumberFormat getFormat() {
        return NumberFormat.getInstance();
    }
    
    private int min_entries_;
}