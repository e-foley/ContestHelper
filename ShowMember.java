import java.util.ArrayList;

public class ShowMember implements FilterStrategy {
    private Member member_;

    public ShowMember(Member member) {
        member_ = member;
    }

    public ArrayList<Poll> filterContests(ArrayList<Poll> contests) {
        // TODO: FILTER
        return new ArrayList<Poll>();
    }
}
