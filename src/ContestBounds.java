public class ContestBounds {
    private Poll contest_start_;
    private Poll contest_end_;

    public ContestBounds(Poll contest_start, Poll contest_end) {
        contest_start_ = contest_start;
        contest_end_ = contest_end;
    }

    public Poll getStart() {
        return contest_start_;
    }
    
    public Poll getEnd() {
        return contest_end_;
    }
}
