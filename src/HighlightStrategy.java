import java.util.ArrayList;

public interface HighlightStrategy {
    ArrayList<Entry> getHighlights(Poll poll);
}
