import java.util.ArrayList;

public class ShowMember implements FilterStrategy {
    private Member member_;

    public ShowMember(Member member) {
        member_ = member;
    }

    public ArrayList<Poll> filterPolls(ArrayList<Poll> polls) {
        // TODO: FILTER
        return new ArrayList<Poll>();
    }
}
