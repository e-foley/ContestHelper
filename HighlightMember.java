import java.util.ArrayList;

public class HighlightMember implements HighlightStrategy
{
    private Member member_;

    public HighlightMember(Member member) {
        member_ = member;
    }

    public ArrayList<Entry> getHighlights(Poll poll) {
        ArrayList<Entry> returning = new ArrayList<Entry>();
        for (Entry entry : poll.getEntries()) {
            // If we decide to use ArrayList.contains() instead, we need to override equals method.
            for (Entry.MemberNameCouple couple : entry.getMemberNameCouples()) {
                if (couple.member == member_) {
                    returning.add(entry);
                }
            }
//             if (entry.getMemberNameCouples().contains(member_)) {
//                 returning.add(entry);
//             }
        }
        return returning;
    }
}
