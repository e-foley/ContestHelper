import java.util.ArrayList;

/**
 * Write a description of class MOTMData here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Entry
{
    public static final int AUTO_WIN = 1;
    public static final int AUTO_LOSE = -1;
    
    private Member member;
    private Contest contest;
    private String URL;
    private boolean hasURL;
    private int votes;
    private boolean hasUncertainty;
    private int overrideCode;
    private boolean hasVotes;
    
    private int points;
    private boolean pointsAssigned;
    private int plusMinusPoints;
    private boolean plusMinusPointsAssigned;
    private int plusMinusHeads;
    private boolean plusMinusHeadsAssigned;
    
    public Entry(Member myMember, Contest myContest, boolean myHasURL, String myURL, boolean myHasVotes, int votesSet, boolean myHasUncertainty, int myOverrideCode)
    {
        member = myMember;
        contest = myContest;
        URL = myURL;
        hasURL = myHasURL;
        votes = votesSet;
        hasUncertainty = myHasUncertainty;
        overrideCode = myOverrideCode;
        hasVotes = myHasVotes;
        points = 0;
        pointsAssigned = false;
        plusMinusPoints = 0;
        plusMinusPointsAssigned = false;
        plusMinusHeads = 0;
        plusMinusPointsAssigned = false;
    }
    
    /*public Entry(Member myMember, Contest myContest, int myContestNumber, String myURLName, String myURLExtension, int myVotes)
    {
        member = myMember;
        contest = myContest;
        URL = generateURL("http://sotw.purezc.com/MOTM/", myContestNumber, myURLName, myURLExtension);
        hasURL = true;
        votes = myVotes;
    }
    
    public Entry(Member myMember, Contest myContest, int myVotes)
    {
        member = myMember;
        contest = myContest;
        URL = new String();
        hasURL = false;
        votes = myVotes;
    }
    
    public static String generateURL(String base, int myContestNumber, String URLName, String URLExtension)
    {
        String contestIDString = "" + myContestNumber;
        while (contestIDString.length() < URL_DIGITS)    // note: this check should use a constant
            contestIDString = "0" + contestIDString;
        return ("http://sotw.purezc.com/MOTM/" + contestIDString + "/" + URLName.replace(" ","%20").replace(".","") + "." + URLExtension);
    }*/
    
    public Member getMember()
    {
        return member;
    }
    
    public Contest getContest()
    {
        return contest;
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
    
    public int getPoints()
    {
        if (!pointsAssigned)
        {
            ArrayList<Entry> entries = contest.getEntries();          
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
            
        
        /*int points = contest.numVotes();
        for (int i=0; i<entries.size(); i++)
        {
            points += (votes - entries.get(i).getVotes());
        }*/
    }
    
    public int getPlusMinusPoints()
    {
        if (!plusMinusPointsAssigned)
        {
            ArrayList<Entry> entries = contest.getEntries();          
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
            ArrayList<Entry> entries = contest.getEntries();          
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
            return (1.0f / contest.getWinners().size());
        return 0.0f;
    }

    public int getOverrideCode()
    {
        return overrideCode;
    }
    
    // This method is fishy... think about it.
    public boolean isWinner()
    {
        return contest.getWinners().contains(this);
        //return (votes >= contest.getWinners().get(0).getVotes());
    }
    
    public boolean hasURL()
    {
        return hasURL;
    }
}