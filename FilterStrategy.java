import java.util.ArrayList;

public interface FilterStrategy {
    ArrayList<Poll> filterContests(ArrayList<Poll> contests);
}
