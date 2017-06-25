import java.util.ArrayList;

public class Entry
{
    public static final int AUTO_WIN = 1;
    public static final int AUTO_LOSE = -1;
    
    private Member member;
    private Poll poll;
    private String URL;
    private boolean hasURL;
    private int votes;
    private boolean hasUncertainty;
    private int overrideCode;
    private boolean hasVotes;
    private String nameSubmittedUnder;
    
    private int points;
    private boolean pointsAssigned;
    private int plusMinusPoints;
    private boolean plusMinusPointsAssigned;
    private int plusMinusHeads;
    private boolean plusMinusHeadsAssigned;
    
    public Entry(Member myMember, Poll myPoll, boolean myHasURL, String myURL, boolean myHasVotes, int votesSet, boolean myHasUncertainty, int myOverrideCode, String myNameSubmittedUnder)
    {
        member = myMember;
        poll = myPoll;
        URL = myURL;
        hasURL = myHasURL;
        votes = votesSet;
        hasUncertainty = myHasUncertainty;
        overrideCode = myOverrideCode;
        hasVotes = myHasVotes;
        nameSubmittedUnder = myNameSubmittedUnder;
        points = 0;
        pointsAssigned = false;
        plusMinusPoints = 0;
        plusMinusPointsAssigned = false;
        plusMinusHeads = 0;
        plusMinusPointsAssigned = false;
    }
    
    public Member getMember()
    {
        return member;
    }
    
    public Poll getPoll()
    {
        return poll;
    }
    
    public void setMember(Member memberSetting)
    {
        member = memberSetting;
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
    
    public String getNameSubmittedUnder()
    {
        return nameSubmittedUnder;
    }
    
    public void setNameSubmittedUnder(String name)
    {
        nameSubmittedUnder = name;
    }
    
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