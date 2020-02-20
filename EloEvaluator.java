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
    public static final String VERSION_STRING = "0.4.1";
    
    // q is effectively the member's power level--a relative weight of how many votes they'd be expected to get (q = BASE^(rating/DIVISOR))
    // e is the expected proportion of votes the member will receive: e = q / (sum of all q)
    // s is the proportion of votes actually won in the contest
    class RatingCalc {  
        // Information available from start
        double stake = 0.0;
        double boost = 0.0;
        double rating_before = 0.0;
        double q_before = 0.0;
        double e_before = 0.0;
        // Information available for intermediate calculations
        double s = 0.0;
        //double rating_temp = 0.0;
        //double q_temp = 0.0;
        //double e_temp = 0.0;
        // Information available afterward
        double rating_after = 0.0;
        double q_after = 0.0;
        double e_after = 0.0;
    }
    
    // TODO: Rethink the inner container. There may be a better choice.
    private TreeMap<Integer, TreeMap<Integer, RatingCalc>> rating_history;
    double starting_rating;
    double base;
    double divisor;
    double aggressiveness;
    double starting_boost;
    double boost_decay;

    public EloEvaluator(double starting_rating, double base, double divisor, double aggressiveness, double starting_boost, double boost_decay)
    {
        rating_history = new TreeMap<Integer, TreeMap<Integer, RatingCalc>>();
        this.starting_rating = starting_rating;
        this.base = base;
        this.divisor = divisor;
        this.aggressiveness = aggressiveness;
        this.starting_boost = starting_boost;
        this.boost_decay = boost_decay;
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
            double q_before_sum = 0.0;
            int qualified_vote_sum = 0;  // We do this because we ignore votes from shared entries
            int qualified_entry_sum = 0;  // Same here
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
                        this_calc.boost = starting_boost;
                    } else {
                        // Member is in charts; set "rating before" to the "rating after" of the prior entry.
                        TreeMap<Integer, RatingCalc> line = rating_history.get(new Integer(member_id));
                        RatingCalc last_calc = line.get(line.lastKey());
                        this_calc.rating_before = last_calc.rating_after;
                        this_calc.boost = Math.max(last_calc.boost + (1.0 - last_calc.boost) * boost_decay, 1.0);
                    }
                    
                    // TODO: There's already a notion of "stake" in Member.EntryStakePair... Maybe these could be linked somehow.
                    //this_calc.rating_temp = this_calc.rating_before;  // (temp value)
                    this_calc.stake = 1.0 / couples.size();
                    this_calc.q_before = Math.pow(base, this_calc.rating_before / divisor);
                    //this_calc.q_temp = this_calc.q_before;
                    
                    // Ignore shared entries for total q calculation.
                    // TODO: Change this.
                    if (couples.size() == 1) {
                        q_before_sum += this_calc.q_before;
                        qualified_vote_sum += entry.getVotes();
                        ++qualified_entry_sum;
                    }
                    
                    rating_history.get(new Integer(member_id)).put(poll.getSynch(), this_calc);
                }
            }
                
            // By this point, we have a provisional RatingCalc for this contest in every member's row.
            // This RatingCalc has the correct "rating_before" field and a temporary "rating_after" value to match.

            // Figure out achievement (s)
            for (int j = 0; j < entries.size(); ++j) {
                // Ignore shared entries for ratings. (See also earlier logic when populating `this_calc`.)
                // TODO: Change this.
                if (entries.get(j).numMembers() != 1) {
                    continue;
                }
                
                RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                details.e_before = details.q_before / q_before_sum;  // Expected achievement (ratio of all votes)
                if (qualified_vote_sum == 0) {
                    details.s = 0.0;
                } else {
                    details.s = (double)(entries.get(j).getVotes()) / qualified_vote_sum;  // Actual achievement (ratio of all qualified votes)
                }
            }
            
            // Now, we need to iterate (based on the number of votes) to determine new ratings, calculating intermediate values.
//             for (int v = 0; v < qualified_vote_sum; ++v) {
//                 double q_temp_sum = 0.0;
//                 
//                 // Calculate q_temp_sum
//                 for (int j = 0; j < entries.size(); ++j) {
//                     if (entries.get(j).numMembers() != 1) {
//                         continue;
//                     }
//                     RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
//                     details.q_temp = Math.pow(base, details.rating_temp / divisor);
//                     q_temp_sum += details.q_temp;
//                 }
//                 
//                 for (int j = 0; j < entries.size(); ++j) {
//                     if (entries.get(j).numMembers() != 1) {
//                         continue;
//                     }
//                     RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
//                     details.e_temp = (details.q_temp / q_temp_sum);
//                     details.rating_temp += details.boost * aggressiveness * (details.s - details.e_temp);
//                 }
//             }
            
            // Loop to assign rating_after
            for (int j = 0; j < entries.size(); ++j) {
                Entry entry = entries.get(j);
                ArrayList<Entry.MemberNameCouple> couples = entry.getMemberNameCouples();
                for (int k = 0; k < couples.size(); ++k) {
                    RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(k).member.getId(), poll.getSynch());
                    if (qualified_vote_sum == 0) {
                        // Don't penalize anyone for participating in a contest that had zero votes!
                        details.rating_after = details.rating_before;
                    } else {
                        details.rating_after = details.rating_before + details.boost * aggressiveness * (details.s - details.e_before) * (qualified_entry_sum - 1);
                    }
                }
            }
            
            // Two more loops to calculate q_after and e_after
            double q_after_sum = 0.0;
            for (int j = 0; j < entries.size(); ++j) {
                if (entries.get(j).numMembers() != 1) {
                    continue;
                }
                RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                details.q_after = Math.pow(base, details.rating_after / divisor);
                q_after_sum += details.q_after;
            }
            for (int j = 0; j < entries.size(); ++j) {
                if (entries.get(j).numMembers() != 1) {
                    continue;
                }
                RatingCalc details = getRatingDetails(entries.get(j).getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                details.e_after = details.q_after / q_after_sum;                
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
        EloEvaluator returning = new EloEvaluator(starting_rating, base, divisor, aggressiveness, starting_boost, boost_decay);
        // TODO: UGH, cloning a TreeMap makes a shallow clone rather than a deep one. I believe that's why the line below triggers a compiler warning.
        // returning.rating_history = (TreeMap<Integer, TreeMap<Integer, RatingCalc>>)(rating_history.clone());
        return returning;
    }
}
