import java.util.Comparator;

/**
 * Write a description of interface MemberDataRetriever here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public interface MemberDataRetriever extends Comparator<Member>
{
    /**
     * An example of a method header - replace this comment with your own
     * 
     * @param  y    a sample parameter for a method
     * @return        the result produced by sampleMethod 
     */
    String getData(Member member);
    int compare(Member m1, Member m2);
    String getDetails(Member member, boolean linkTopics);
}
