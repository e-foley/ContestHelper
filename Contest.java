import java.util.ArrayList;
import java.lang.Math;

/**
 * Write a description of class Member here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Contest
{
    public static final int DEFAULT_SYNCH = -1;
    // instance variables - replace the example below with your own
    //private int ID;
    private String name;
    private ArrayList<Entry> entries;
    boolean hasTopic;
    private int topic;
    private int synch;
    
    /**
     * Constructor for objects of class Member
     */
    public Contest()
    {
        // initialise instance variables
        name = "";
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = DEFAULT_SYNCH;
    }
    
    public Contest(String myName)
    {
        name = myName;
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = DEFAULT_SYNCH;
    }
      
    public Contest(String myName, int mySynch)
    {
        name = myName;
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = mySynch;
    }
    
    public Contest(String myName, boolean myHasTopic, int myTopic)
    {
        name = myName;
        entries = new ArrayList<Entry>();
        topic = myTopic;
        hasTopic = myHasTopic;
        synch = DEFAULT_SYNCH;
    }
    
    public Contest(String myName, boolean myHasTopic, int myTopic, int mySynch)
    {
        name = myName;
        entries = new ArrayList<Entry>();
        topic = myTopic;
        hasTopic = myHasTopic;
        synch = mySynch;
    }

    public boolean hasTopic()
    {
        return hasTopic;
    }
    
    public int getSynch()
    {
        return synch;
    }
    
    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public void addEntry(Entry entryAdding)
    {
        entries.add(entryAdding);
    }
    
    /*public void addEntry(Entry entryAdding, boolean incrementSynch)
    {
        if (incrementSynch)
            synch++;            // this is pretty sloppy :(
        entries.add(entryAdding);
    }*/
    
    public Entry getEntryByIndex(int index)
    {
        return entries.get(index);
    }
    
    public ArrayList<Entry> getEntries()
    {
        return entries;
    }
    
    public int numEntries()
    {
        return entries.size();
    }
    
    public int getTopic()
    {
        return topic;
    }
    
    public String getName()
    {
        return name;
    }
    
    public ArrayList<Entry> getWinners()
    {
        ArrayList<Entry> winners = new ArrayList<Entry>();
        int maxVotes = -1;
        boolean mustOverride = false;
        Entry current;
        for (int i=0; i<entries.size(); i++)
        {
            current = entries.get(i);
            
            if (current.getOverrideCode() >= Entry.AUTO_WIN)
            {
                //System.out.println("We're here for " + current.getMember().getMostRecentName() + " with " + current.getOverrideCode());
                // remove all entries that AREN'T auto-wins
                for (int j=0; j<winners.size(); j++)
                {
                    if (winners.get(j).getOverrideCode() < Entry.AUTO_WIN)
                    {
                        winners.remove(j);
                        j--;   // account for the index change incurred by the above. Sneaky!
                    }
                }
                winners.add(current);
                mustOverride = true;
            }
            else if (current.getOverrideCode() > Entry.AUTO_LOSE && !mustOverride)
            {
                if (current.getVotes() > maxVotes)
                {
                    winners.clear();    // flush list, start again
                    maxVotes = current.getVotes();
                    winners.add(current);
                }
                else if (current.getVotes() == maxVotes)
                {
                    winners.add(current);
                }
            }
        }
        return winners;
    }
    
    /** TERRIBLE, TERRIBLE, SUPER-KLUDGY METHOD. BEWARE. */
    public float getApparentContestNumber()
    {
        return Float.parseFloat(name.replaceAll("[^0-9.]",""));
    }
    
    public int numVotes()
    {
        int sum=0;
        for (int i=0; i<entries.size(); i++)
            sum += entries.get(i).getVotes();
        return sum;
    }
    
    public int numPoints()
    {
        int sum=0;
        for (int i=0; i<entries.size(); i++)
            sum += entries.get(i).getPoints();
        return sum;
    }
    
    public String getURL()
    {
        if (hasTopic) {
            return "http://www.purezc.net/forums/index.php?showtopic=" + topic;
        } else {
            return new String();
        }
    }
}
