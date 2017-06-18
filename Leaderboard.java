import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

//?&#9650;  ?&#9660;  ?&#9670;

public class Leaderboard
{
    public static final int NO_PLACE = -1;
    
    private ArrayList<Member> members;
    private MemberDataRetriever metric;
    private boolean is_sorted;
    private History history;
    
    // TODO: Clone the members_set field so that additions to the list made outside this class don't mess up our assumptions about sorting
    // Follow-up: did we do this when we switched to a HashMap for Members?
    public Leaderboard(History history_set, MemberDataRetriever metric_set) {
        history = history_set;
        members = new ArrayList<Member>(history_set.getMembers());
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
    
    public int getPlaceOfMember(int id) {
        // TODO: This is really, really inefficient as written, but I know I'll be replacing it once we rework our handling of Entrys
        for (int p = 0; p < members.size(); ++p) {
            ArrayList<Member> members_at_rank = getMembersAtPlace(p);
            for (int m = 0; m < members_at_rank.size(); ++m) {
                if (members_at_rank.get(m).getId() == id) {
                    return p;
                }
            }
        }
        
        return NO_PLACE;
    }
    
    public ArrayList<Member> getMembers() {
        return members;
    }
    
    public MemberDataRetriever getMetric() {
        return metric;
    }
    
    public History getHistory() {
        return history;
    }
}
