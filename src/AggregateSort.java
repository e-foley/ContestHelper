import java.util.ArrayList;
import java.util.Comparator;

public class AggregateSort implements Comparator<Member> {
    private ArrayList<Comparator<Member>> metrics_;

    public AggregateSort(ArrayList<Comparator<Member>> metrics) {
        metrics_ = metrics;
    }

    public int compare(Member m1, Member m2) {
        for (int i = 0; i < metrics_.size(); ++i) {
            int result = metrics_.get(i).compare(m1, m2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
