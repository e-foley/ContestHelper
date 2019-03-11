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
            if (entry.getMemberNameCouples().contains(member_)) {
                returning.add(entry);
            }
        }
        return returning;
    }
}
