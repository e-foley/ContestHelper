import java.util.ArrayList;

public interface FilterStrategy {
    ArrayList<Poll> filterPolls(ArrayList<Poll> polls);
}
