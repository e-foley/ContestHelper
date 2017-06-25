import java.util.Comparator;
import java.text.NumberFormat;

public interface MemberDataRetriever extends Comparator<Member>
{
    float getValue(Member member);
    String getData(Member member);
    int compare(Member m1, Member m2);
    String getDetails(Member member, boolean linkTopics);
    NumberFormat getFormat();
    boolean qualifies(Member mem);
}
