import java.util.ArrayList;

public class Entry
{
    class MemberNameCouple {
        public Member member;
        public String name_submitted_under;
    }
    
    public static final int AUTO_WIN = 1;
    public static final int AUTO_LOSE = -1;
    
    private ArrayList<MemberNameCouple> members;
    private Poll poll;
    private String URL;
    private boolean hasURL;
    private int votes;
    private boolean hasUncertainty;
    private int overrideCode;
    private boolean hasVotes;
    //private String nameSubmittedUnder;
    
    private int points;
    private boolean pointsAssigned;
    private int plusMinusPoints;
    private boolean plusMinusPointsAssigned;
    private int plusMinusHeads;
    private boolean plusMinusHeadsAssigned;
    private int potential;
    private boolean potential_assigned;
    
    public Entry(ArrayList<MemberNameCouple> myMembers, Poll myPoll, boolean myHasURL, String myURL, boolean myHasVotes, int votesSet, boolean myHasUncertainty, int myOverrideCode)
    {
        members = myMembers;
        poll = myPoll;
        URL = myURL;
        hasURL = myHasURL;
        votes = votesSet;
        hasUncertainty = myHasUncertainty;
        overrideCode = myOverrideCode;
        hasVotes = myHasVotes;
        //nameSubmittedUnder = myNameSubmittedUnder;
        points = 0;
        pointsAssigned = false;
        plusMinusPoints = 0;
        plusMinusPointsAssigned = false;
        plusMinusHeads = 0;
        plusMinusPointsAssigned = false;
        potential = 0;
        potential_assigned = false;
    }
    
    public ArrayList<MemberNameCouple> getMemberNameCouples()
    {
        return members;
    }
    
    public int numMembers() {
        return members.size();
    }
    
    public Poll getPoll()
    {
        return poll;
    }
    
    public void setMemberNameCouples(ArrayList<MemberNameCouple> membersSetting)
    {
        members = membersSetting;
    }
    
    public String getURL()
    {
        return URL;
    }
    
    public int getVotes()
    {
        return votes;
    }
    
    public boolean hasVotes()
    {
        return hasVotes;
    }
    
    public boolean hasUncertainty()
    {
        return hasUncertainty;
    }
    
//     public String getNameSubmittedUnder()
//     {
//         return nameSubmittedUnder;
//     }
    
//     public void setNameSubmittedUnder(String name)
//     {
//         nameSubmittedUnder = name;
//     }
    
    public int getPoints()
    {
        if (!pointsAssigned)
        {
            ArrayList<Entry> entries = poll.getEntries();          
            int pointsNow = votes;
            for (int i=0; i<entries.size(); i++)
            {
                if (hasVotes() && entries.get(i).hasVotes())
                    pointsNow += Math.max(votes - entries.get(i).getVotes(), 0);
            }
            points = pointsNow;
            pointsAssigned = true;
        }
        return points;
    }
    
    public int getPlusMinusPoints()
    {
        if (!plusMinusPointsAssigned)
        {
            ArrayList<Entry> entries = poll.getEntries();          
            int plusMinusPointsNow = 0;
            for (int i=0; i<entries.size(); i++)
            {
                plusMinusPointsNow += (votes - entries.get(i).getVotes());
            }
            plusMinusPoints = plusMinusPointsNow;
            plusMinusPointsAssigned = true;
        }
        return plusMinusPoints;
    }

    public int getPlusMinusHeads()
    {
        if (!plusMinusHeadsAssigned)
        {
            ArrayList<Entry> entries = poll.getEntries();          
            int plusMinusHeadsNow = 0;
            for (int i=0; i<entries.size(); i++)
            {
                plusMinusHeadsNow += (new Integer(getVotes()).compareTo(entries.get(i).getVotes()));
            }
            plusMinusHeads = plusMinusHeadsNow;
            plusMinusHeadsAssigned = true;
        }
        return plusMinusHeads;
    }       
          
    // Potential for an entry is the sum of maximum vote margins against all other entries in the contest.
    // The maximum vote margin is equal to the sum of the votes between the two members.
    public int getPotential() {
        if (!potential_assigned) {
            ArrayList<Entry> entries = poll.getEntries();
            // We'll end up double-counting the entry's potential against itself, so reduce the tally accordingly.
            int temp_potential = -2 * getVotes();
            for (Entry competitor_entry : entries) {
                temp_potential += getVotes() + competitor_entry.getVotes();
            }
            potential = temp_potential;
            potential_assigned = true;
        }
        return potential;
    }
    
    public float getWinningness()
    {
        if (isWinner())
            return (1.0f / poll.getWinners().size());
        return 0.0f;
    }

    public int getOverrideCode()
    {
        return overrideCode;
    }
    
    // This method is fishy... think about it.
    public boolean isWinner()
    {
        return poll.getWinners().contains(this);
    }
    
    public boolean hasURL()
    {
        return hasURL;
    }
}