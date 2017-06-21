import java.util.Comparator;

public interface MemberDataRetriever<value_type> extends Comparator<Member>
{
    value_type getValue(Member member);
    String getData(Member member);
    int compare(Member m1, Member m2);
    String getDetails(Member member, boolean linkTopics);
}
