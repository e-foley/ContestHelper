import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Write a description of class EloEvaluator here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class EloEvaluator implements Cloneable
{
    class RatingCalc {       
        double rating_before = 0.0f;
        double rating_after = 0.0f;
        double stake = 0.0f;
    }
    
    // TODO: Rethink the inner container. There may be a better choice.
    private TreeMap<Integer, TreeMap<Integer, RatingCalc>> rating_history;
    double starting_rating;
    double base;
    double divisor;
    double aggressiveness;

    public EloEvaluator(double starting_rating, double base, double divisor, double aggressiveness)
    {
        rating_history = new TreeMap<Integer, TreeMap<Integer, RatingCalc>>();
        this.starting_rating = starting_rating;
        this.base = base;
        this.divisor = divisor;
        this.aggressiveness = aggressiveness;
    }

    public void evaluate(History history) {
        // Reinitialize the history
        rating_history = new TreeMap<Integer, TreeMap<Integer, RatingCalc>>();
        
        // Run through all polls... add member IDs to master tree as they enter the picture.
        ArrayList<Poll> polls = history.getPolls();
        
        // TODO: I have no idea how the system would handle cases where the same member enters two simultaneous contests...
        for (int i = 0; i < polls.size(); ++i) {
            Poll poll = polls.get(i);
            ArrayList<Entry> entries = poll.getEntries();
            for (int j = 0; j < entries.size(); ++j) {
                Entry entry = entries.get(j);
                ArrayList<Entry.MemberNameCouple> couples = entry.getMemberNameCouples();
                for (int k = 0; k < couples.size(); ++k) {
                    Entry.MemberNameCouple couple = couples.get(k);
                    // TODO: Handle lack of ID better.
                    if (!couple.member.hasId()) {
                        System.err.println("Member doesn't have ID!");
                    }
                    
                    int member_id = couple.member.getId();
                    RatingCalc this_calc = new RatingCalc();
                    
                    // TODO: Do we need to clone this_calc when we add it!?
                   
                    if (!rating_history.containsKey(new Integer(member_id))) {
                        // Member isn't in charts yet, so create a row for the member and make "rating before" our default.                        
                        rating_history.put(member_id, new TreeMap<Integer, RatingCalc>());
                        this_calc.rating_before = starting_rating;
                    } else {
                        // Member is in charts; set "rating before" to the "rating after" of the prior entry.
                        TreeMap<Integer, RatingCalc> line = rating_history.get(new Integer(member_id));
                        RatingCalc last_calc = line.get(line.lastKey());
                        this_calc.rating_before = last_calc.rating_after;
                    }
                    
                    // TODO: There's already a notion of "stake" in Member.EntryStakePair... Maybe these could be linked somehow.
                    this_calc.rating_after = this_calc.rating_before;  // (temp value)
                    this_calc.stake = 1.0 / couples.size();
                    
                    rating_history.get(new Integer(member_id)).put(poll.getSynch(), this_calc);
                }
            }
                
            // By this point, we have a provisional RatingCalc for this contest in every member's row.
            // This RatingCalc has the correct "rating_before" field and a temporary "rating_after" value to match.
            
            // Next, we want to calculate an expected result for every head-to-head matchup in this poll.
            for (int j = 0; j < entries.size() - 1; ++j) {                
                for (int k = j + 1; k < entries.size(); ++k) {
                    if (entries.get(j).numMembers() != 1 || entries.get(k).numMembers() != 1) {
                        // Skip entries with multiple members contributing.
                        // TODO: Change this.
                        continue;
                    }
                    
                    // TODO: Our method treats the pool of votes available to be those that the two members collect together...
                    // But it would be sweet if this took into account all other opponents and all other votes at the same time somehow.
                    RatingCalc j_details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                    RatingCalc k_details = getRatingDetails(entries.get(k).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                    double q_j = Math.pow(base, j_details.rating_before / divisor);
                    double q_k = Math.pow(base, k_details.rating_before / divisor);
                    double s_j = entries.get(j).getVotes();
                    double s_k = entries.get(k).getVotes();
                    double e_j = (q_j / (q_j + q_k)) * (s_j + s_k);
                    double e_k = (q_k / (q_j + q_k)) * (s_j + s_k);
                    j_details.rating_after += aggressiveness * (s_j - e_j);
                    k_details.rating_after += aggressiveness * (s_k - e_k);
                }
            }  
        }
        
    }
    
    public RatingCalc getRatingDetails(int member_id, int poll_id) {
        // TODO: Consider better way of testing whether the details exist.
        
        if (!rating_history.containsKey(member_id)) {
            return new RatingCalc();
        }
        
        if (!rating_history.get(member_id).containsKey(poll_id)) {
            return new RatingCalc();
        }
        
        return rating_history.get(member_id).get(poll_id);
    }
   
    // Grabs most recent one
    public RatingCalc getRatingDetails(int member_id) {
        if (!rating_history.containsKey(member_id)) {
            return new RatingCalc();
        }
        
        TreeMap<Integer, RatingCalc> line = rating_history.get(member_id);
        
        if (line.isEmpty()) {
            return new RatingCalc();
        }
        
        return line.lastEntry().getValue();
    }
    
    public Object clone() {
        EloEvaluator returning = new EloEvaluator(starting_rating, base, divisor, aggressiveness);
        returning.rating_history = (TreeMap<Integer, TreeMap<Integer, RatingCalc>>)(rating_history.clone());
        return returning;
    }
}
