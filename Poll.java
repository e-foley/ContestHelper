import java.util.ArrayList;

public class Poll
{
    public static final int DEFAULT_SYNCH = -1;
    
    private String shortName;
    private String longName;
    private ArrayList<Entry> entries;
    boolean hasTopic;
    private int topic;
    private int synch;
    private ArrayList<String> notes;
    
    public Poll()
    {
        // initialise instance variables
        shortName = "";
        longName = "";
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = DEFAULT_SYNCH;
        notes = new ArrayList<String>();
    }
    
    public Poll(String myShortName)
    {
        shortName = myShortName;
        longName = "";
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = DEFAULT_SYNCH;
        notes = new ArrayList<String>();
    }
      
    public Poll(String myShortName, int mySynch)
    {
        shortName = myShortName;
        longName = "";
        entries = new ArrayList<Entry>();
        topic = -1;
        hasTopic = false;
        synch = mySynch;
        notes = new ArrayList<String>();
    }
    
    public Poll(String myShortName, boolean myHasTopic, int myTopic)
    {
        shortName = myShortName;
        longName = "";
        entries = new ArrayList<Entry>();
        topic = myTopic;
        hasTopic = myHasTopic;
        synch = DEFAULT_SYNCH;
        notes = new ArrayList<String>();
    }
    
    public Poll(String myShortName, boolean myHasTopic, int myTopic, int mySynch)
    {
        shortName = myShortName;
        longName = "";
        entries = new ArrayList<Entry>();
        topic = myTopic;
        hasTopic = myHasTopic;
        synch = mySynch;
        notes = new ArrayList<String>();
    }
    
    public Poll(String myShortName, String myLongName, boolean myHasTopic, int myTopic, int mySynch)
    {
        shortName = myShortName;
        longName = myLongName;
        entries = new ArrayList<Entry>();
        topic = myTopic;
        hasTopic = myHasTopic;
        synch = mySynch;
        notes = new ArrayList<String>();
    }

    public boolean hasTopic()
    {
        return hasTopic;
    }
    
    public int getSynch()
    {
        return synch;
    }
    
    public void addEntry(Entry entryAdding)
    {
        entries.add(entryAdding);
    }
    
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
    
    public String getShortName()
    {
        return shortName;
    }
    
    public String getLongName()
    {
        return longName;
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
    
    public ArrayList<String> getNotes() {
        return notes;
    }
    
    public void addNote(String note) {
        notes.add(note);
    }
    
    public String getNote(int index) {
        return notes.get(index);
    }
    
    public int numNotes() {
        return notes.size();
    }
}
