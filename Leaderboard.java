import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

//?&#9650;  ?&#9660;  ?&#9670;

public class Leaderboard
{
    private ArrayList<Member> members;
    private MemberDataRetriever metric;
    private boolean is_sorted;
    
    // TODO: Clone the members_set field so that additions to the list made outside this class don't mess up our assumptions about sorting
    public Leaderboard(History history_set, MemberDataRetriever metric_set) {
        members = history_set.getMembers();
        metric = metric_set;
        is_sorted = false;
    }
    
    private void sort() {
        Collections.sort(members, metric);
        is_sorted = true;
    }
    
    private void ensureOrder() {
        if (!is_sorted) {
            sort();
        }
    }
    
    // Problem: if you ask for a place that is smothered by a tie at a different place, not everybody at the same rank will be represented
    // Note: this method is 0-indexed for the moment, meaning "first place" has index 0
    public ArrayList<Member> getMembersAtPlace(int place) {
        ensureOrder();
        
        Member initial = members.get(place);
        if (initial == null) {
            return new ArrayList<Member>();
        }
        
        ArrayList<Member> returning = new ArrayList<Member>();
        returning.add(initial);
        
        for (int i = place + 1; i < members.size(); ++i) {
            if (metric.compare(members.get(i), initial) == 0) {
                // The two members are tied in this metric, so add the new member to our list
                returning.add(members.get(i));
            } else {
                // The two members are not tied in this metric, so stop adding to our list of tied members
                break;
            }
        }
        
        return returning;
    }
    
    public ArrayList<Member> getMembers() {
        return members;
    }
    
    public MemberDataRetriever getMetric() {
        return metric;
    }
}
