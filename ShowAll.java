import java.util.ArrayList;

public class ShowAll implements FilterStrategy {
    public ShowAll() {}

    public ArrayList<Poll> filterContests(ArrayList<Poll> contests) {
        return contests;
    }
}
