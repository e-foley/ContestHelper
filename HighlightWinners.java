import java.util.ArrayList;

public class HighlightWinners implements HighlightStrategy
{
    public HighlightWinners() {}

    public ArrayList<Entry> getHighlights(Poll poll) {
        return poll.getWinners();
    }
}
