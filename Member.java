import java.util.ArrayList;
import java.util.List;

public class Member
{
    private ArrayList<String> names;
    private ArrayList<Entry> entries;
    private String tag;
    private boolean isTagged;

    public Member()
    {
        names = new ArrayList<String>();
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
    }
    
    public Member(String myName)
    {
        names = new ArrayList<String>();
        names.add(myName);
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
    }
    
    public Member(String myTag, String myName)
    {
        names = new ArrayList<String>();
        names.add(myName);
        entries = new ArrayList<Entry>();
        tag = myTag;
        isTagged = true;
    }
    
    public Member(ArrayList<String> myNames)
    {
        names = myNames;
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
    }
    
    public Member(String myTag, ArrayList<String> myNames) {
        names = myNames;
        entries = new ArrayList<Entry>();
        tag = myTag;
        isTagged = true;
    }

    public void addEntry(Entry entryAdding)
    {
        entries.add(entryAdding);
    }
    
    public void addName(String nameAdding)
    {
        names.add(nameAdding);
    }
    
    public boolean hasName(String name)
    {
        for (int i=0; i<names.size(); i++)
        {
            if (names.get(i).equals(name))
                return true;
        }
        return false;
    }
    
    public ArrayList<Entry> getEntries()
    {
        return entries;
    }
    
    public String getMostRecentName()
    {
        return names.get(names.size()-1);
    }
    
    public void setTag(String setting) {
        tag = setting;
        isTagged = true;
    }
    
    public void removeTag() {
        tag = new String();
        isTagged = false;
    }
    
    public String getTag() {
        return tag;
    }
    
    public boolean hasTag() {
        return isTagged;
    }
    
    public int getTotalVotes()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getVotes();
        }
        return sum;
    }
    
    public int getTotalPoints()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPoints();
        }
        return sum;
    }
    
    public int getTotalPlusMinusPoints()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPlusMinusPoints();
        }
        return sum;
    }
    
    public int getTotalPlusMinusHeads()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPlusMinusHeads();
        }
        return sum;
    }
    
    public int getTotalEntries()
    {
        return entries.size();
    }
    
    public float getTotalWinningness()
    {
        float sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getWinningness();
        }
        return sum;
    }
    
    public Entry getMostRecentEntry()
    {
        return getRecentEntry(0);
    }
    
    public Entry getRecentEntry(int offset)
    {
        if (offset >= entries.size())
            return null;
            
        return entries.get(entries.size()-1-offset);
    }
    
    public boolean hasUncertainty()
    {
        for (int i=0; i<entries.size(); i++)
            if (entries.get(i).hasUncertainty())
                return true;
        return false;
    }
    
    public static int getNumberOfLongestStreaks(ArrayList<ArrayList<Entry>> list)
    {
        return list.size();
    }
    
    public static float getLongestStreak(ArrayList<ArrayList<Entry>> list)
    {
        if (list.size() <= 0)
            return 0.0f;
            
        float sum = 0.0f;
        for (int i=0; i<list.get(0).size(); i++)
            sum += list.get(0).get(i).getWinningness();
        return sum;
    }
    
    public ArrayList<ArrayList<Entry>> getEntriesInLongestStreak(boolean strict)
    {
        //System.out.println("Getting for member " + getMostRecentName());
        float current = 0.0f;
        float max = 0.0f;
        ArrayList<Entry> currentList = new ArrayList<Entry>();
        ArrayList<ArrayList<Entry>> maxList = new ArrayList<ArrayList<Entry>>();
        int lastWinSynch = 0;
        Entry entry;
        for (int i=0; i<entries.size(); i++)
        {
            entry = entries.get(i);
            if (entry.getWinningness() > 0.0f) // if a winner...
            {
                if (!strict || (entry.getContest().getSynch() - lastWinSynch) <= 1)
                {
                    current += entry.getWinningness();
                    currentList.add(entry);
                }
                else
                {
                    current = entry.getWinningness();
                    currentList = new ArrayList<Entry>();
                    currentList.add(entry);
                }
                lastWinSynch = entry.getContest().getSynch();
            }
            else // if not a winner
            {
                current = 0;
                currentList = new ArrayList<Entry>();   // this can probably be removed to save time if necessary
            }
                
            if (current == max && max > 0.0f)
            {
                maxList.add(currentList);
            }
            else if (current > max)
            {
                max = current;
                maxList = new ArrayList<ArrayList<Entry>>();
                maxList.add(currentList);
            }
        }
        return maxList;
    }
    
    public static int getNumberOfEntriesWithMostVotes(ArrayList<Entry> list)
    {
        return list.size();
    }
    
    public static int getMostVotesSingle(ArrayList<Entry> list)
    {
        if (list == null || list.size() == 0)
            return 0;
        else return list.get(0).getVotes();
    }
    
    public ArrayList<Entry> getEntriesWithMostVotes()
    {
        int max = 0;
        ArrayList<Entry> maxList = new ArrayList<Entry>();
        for (int i=0; i<entries.size(); i++)
        {
            if (entries.get(i).getVotes() == max)
            {
                maxList.add(entries.get(i));
            }
            else if (entries.get(i).getVotes() >= max)
            {
                max = entries.get(i).getVotes();
                maxList = new ArrayList<Entry>();
                maxList.add(entries.get(i));
            }
        }
        return maxList;
    }
    
    public static int getNumberOfEntriesWithMostPoints(ArrayList<Entry> list)
    {
        return list.size();
    }
    
    public static int getMostPointsSingle(ArrayList<Entry> list)
    {
        if (list == null || list.size() == 0)
            return 0;
        else return list.get(0).getPoints();
    }
    
    public ArrayList<Entry> getEntriesWithMostPoints()
    {
        int max = 0;
        ArrayList<Entry> maxList = new ArrayList<Entry>();
        for (int i=0; i<entries.size(); i++)
        {
            if (entries.get(i).getPoints() == max)
            {
                maxList.add(entries.get(i));
            }
            else if (entries.get(i).getPoints() >= max)
            {
                max = entries.get(i).getPoints();
                maxList = new ArrayList<Entry>();
                maxList.add(entries.get(i));
            }
        }
        return maxList;
    }
    
    public float getWinRatio()
    {
        return getTotalWinningness() / getTotalEntries();
    }
    
    public int getOldFormidableRating()
    {
        return Math.round(1000 + 25 * getTotalPlusMinusPoints() / (getTotalEntries() + 3));
    }
    
    public int getTotalNumOpponents() {
        int opponent_count = 0;
        for (int i = 0; i < entries.size(); ++i) {
            opponent_count += (entries.get(i).getContest().numEntries() - 1);
        }
        return opponent_count;
    }
    
    public int getNewFormidableRating() {
        return Math.round(5000.0f + 5000.0f * getTotalPlusMinusHeads() / (getTotalNumOpponents() + 10));
    }
}
