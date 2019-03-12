import java.util.ArrayList;

public class HighlightNone implements HighlightStrategy {
    public HighlightNone() {}
    
    public ArrayList<Entry> getHighlights(Poll poll) {
        return new ArrayList<Entry>();
    }
}
