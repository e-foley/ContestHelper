import java.util.Comparator;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class MemberSortElo implements MemberDataRetriever
{
    public EloEvaluator evaluator;
    
    public MemberSortElo(EloEvaluator evaluator) {
        this.evaluator = evaluator;
    }
    
    public float getValue(Member member) {
        return (float)(Math.round(evaluator.getRatingDetails(member.getId()).rating_after));
    }
    
    public int compare(Member m1, Member m2) {
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
    
    public boolean qualifies(Member mem) {
        return true;
    }
    
    public void precalculate(History history) {
        evaluator.evaluate(history);
    }
}