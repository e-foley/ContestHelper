import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Write a description of class EloEvaluator here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class EloEvaluator
{
    class RatingCalc {       
        int rating_before = -1;
        int rating_after = -1;
    }
    
    // TODO: Rethink the inner container. There may be a better choice.
    private TreeMap<Integer, TreeMap<Integer, RatingCalc>> rating_history;
    int starting_rating;

    public EloEvaluator()
    {
        rating_history = new TreeMap<Integer, TreeMap<Integer, RatingCalc>>();
        starting_rating = 1500;
    }

    public void evaluate(History history) {
        // Reinitialize the history
        rating_history = new TreeMap<Integer, TreeMap<Integer, RatingCalc>>();
        
        // Run through all polls... add member IDs to master tree as they enter the picture.
        ArrayList<Poll> polls = history.getPolls();
        
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
                    
                    rating_history.get(new Integer(member_id)).put(poll.getSynch(), this_calc);
                }
            }
                
            // By this point, we have a provisional RatingCalc for this contest in every member's row.
            // This RatingCalc has the correct "rating_before" field.
            
            // Next, we want to calculate an expected result for every head-to-head matchup in this poll.
            for (int j = 0; j < entries.size(); ++j) {
                
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
   
}
