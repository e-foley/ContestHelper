import java.util.ArrayList;

public class ShowAll implements FilterStrategy {
    public ShowAll() {}

    public ArrayList<Poll> filterPolls(ArrayList<Poll> polls) {
        return polls;
    }
}
