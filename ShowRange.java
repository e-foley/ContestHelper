import java.util.ArrayList;

public class ShowRange implements FilterStrategy {
    private int lower_;
    private int upper_;

    public ShowRange(int lower, int upper) {
        lower_ = lower;
        upper_ = upper;
    }

    public ArrayList<Poll> filterContests(ArrayList<Poll> contests) {
        // TODO: FILTER
        return new ArrayList<Poll>();
    }
}
