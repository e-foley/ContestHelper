import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortValleyElo implements MemberDataRetriever
{
    public EloEvaluator evaluator;
    
    public MemberSortValleyElo(EloEvaluator evaluator) {
        this.evaluator = evaluator;
    }
    
    public float getValue(Member member) {
        return (float)(Math.round(evaluator.getRatingDetails(member.getId()).valley_rating_after));
    }
    
    public int compare(Member m1, Member m2) {
        // It's important to check qualification or else entries get numbered improperly on the leaderboard.
        if (qualifies(m1) && !qualifies(m2)) {
            return -1;
        }
        if (qualifies(m2) && !qualifies(m1)) {
            return 1;
        }
        if (!qualifies(m1) && !qualifies(m2)) {
            return 0;
        }
        
        int result = new Float(getValue(m2)).compareTo(getValue(m1));
        return result;
    }
    
    public String getData(Member m)
    {
        return getFormat().format(getValue(m));
    }
    
    public String getDetails(Member m, boolean linkTopics)
    {
        return "";
    }
    
    public NumberFormat getFormat() {
        return new DecimalFormat("#,##0");
    }
    
    // Require a rating rise and a rating drop so that we don't call the starting rating a peak or valley.
    public boolean qualifies(Member mem) {
        EloEvaluator.RatingCalc calc = evaluator.getRatingDetails(mem.getId());
        return calc.has_had_rating_rise_after && calc.has_had_rating_drop_after;
    }
    
    public void precalculate(History history) {
        evaluator.evaluate(history);
    }
    
    public Object clone() {
        return new MemberSortValleyElo((EloEvaluator)(evaluator.clone()));
    }
}