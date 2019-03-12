import java.util.ArrayList;

public class ShowMember implements FilterStrategy {
    private Member member_;

    public ShowMember(Member member) {
        member_ = member;
    }

    public ArrayList<Poll> filterPolls(ArrayList<Poll> polls) {
        ArrayList<Poll> returning = new ArrayList<Poll>();
        for (Poll poll : polls) {
            boolean found = false;
            for (Entry entry : poll.getEntries()) {
                for (Entry.MemberNameCouple member_name_couple : entry.getMemberNameCouples()) {
                    if (member_name_couple.member == member_) {
                        found = true;
                        returning.add(poll);
                    }
                    
                    if (found) {break;}  // Shortcut
                }
                
                if (found) {break;}  // Shortcut
            }
        }
        
        return returning;
    }
}
