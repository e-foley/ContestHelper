import java.util.ArrayList;

public class ShowRange implements FilterStrategy {
    private int start_;
    private int end_;

    public ShowRange(int start, int end) {
        start_ = start;
        end_ = end;
    }

    public ArrayList<Poll> filterPolls(ArrayList<Poll> polls) {
        ArrayList<Poll> returning = new ArrayList<Poll>();
        
        for (int i = 0; i < polls.size(); ++i) {
            if (i >= start_ && i <= end_) {
                returning.add(polls.get(i));
            }
        }
        
        return returning;
    }
}
