import java.util.ArrayList;
import java.util.List;

public class Member
{
    private ArrayList<String> names;
    private ArrayList<Entry> entries;
    private String tag;
    private boolean isTagged;
    private boolean hasId;
    private int id;

    private boolean dirty;  // Whether stats need to be calculated anew.  We COULD change this to do dirty flags for each stat, maybe with a fancy class...
    private int total_votes = 0;
    private int total_points = 0;
    private int total_plus_minus_points = 0;
    private int total_plus_minus_heads = 0;
    private float total_winningness = 0.0f;
    private ArrayList<ArrayList<Entry>> winning_streak_loose = new ArrayList<ArrayList<Entry>>();
    private ArrayList<ArrayList<Entry>> winning_streak_strict = new ArrayList<ArrayList<Entry>>();
    private float longest_streak_loose = 0.0f;
    private float longest_streak_strict = 0.0f;
    private int formidable_rating = 0;
    
    private float weighted_plus_minus_heads = 0.0f;
    private float weighted_opponent_count = 0.0f;
    private int weighted_formidable_rating = 0;
    
    public static final float DECAY = 0.933033f;  // 0.933033 corresponds to half-life of 10 about contests.
    public static final float BAYESIAN_ALLOWANCE = 10.0f;
    
    public Member()
    {
        names = new ArrayList<String>();
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    // Incomplete clone
    public Member(Member mem) {
        names = new ArrayList<String>(mem.names);
        entries = new ArrayList<Entry>(mem.entries);
        tag = mem.tag;
        isTagged = mem.isTagged;
        hasId = mem.hasId;
        id = mem.id;
        dirty = true;  // MAYBE this can be mem.dirty, but I don't want to risk it.
    }
    
    public Member(String myName)
    {
        names = new ArrayList<String>();
        names.add(myName);
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    public Member(String myTag, String myName)
    {
        names = new ArrayList<String>();
        names.add(myName);
        entries = new ArrayList<Entry>();
        tag = myTag;
        isTagged = true;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    public Member(ArrayList<String> myNames)
    {
        names = myNames;
        entries = new ArrayList<Entry>();
        tag = new String();
        isTagged = false;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    public Member(String myTag, ArrayList<String> myNames) {
        names = myNames;
        entries = new ArrayList<Entry>();
        tag = myTag;
        isTagged = true;
        hasId = false;
        id = 0;
        dirty = true;
    }

    public void addEntry(Entry entryAdding)
    {
        entries.add(entryAdding);
        dirty = true;  // Adding an entry invalidates any cached stats.
    }
    
    public boolean removeEntry(Entry removing) {
        if (entries.remove(removing)) {
            dirty = true;
            return true;
        }
        return false;
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
    
    public ArrayList<String> getNames() {
        return names;
    }
    
    public ArrayList<String> getUniqueNames() {
        ArrayList<String> unique_names = new ArrayList<String>();
        for (String name : names) {
            if (!unique_names.contains(name)) {
                unique_names.add(name);
            }
        }
        return unique_names;
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
    
    public void setId(int idSetting) {
        id = idSetting;
        hasId = true;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean hasId() {
        return hasId;
    }
    
    public int getTotalVotes() {
        refreshStats();
        return total_votes;
    }
    
    private int calcTotalVotes()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getVotes();
        }
        return sum;
    }
    
    public int getTotalPoints() {
        refreshStats();
        return total_points;
    }
    
    private int calcTotalPoints()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPoints();
        }
        return sum;
    }
    
    public int getTotalPlusMinusPoints() {
        refreshStats();
        return total_plus_minus_points;
    }
    
    private int calcTotalPlusMinusPoints()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPlusMinusPoints();
        }
        return sum;
    }
    
    public int getTotalPlusMinusHeads() {
        refreshStats();
        return total_plus_minus_heads;
    }
    
    private int calcTotalPlusMinusHeads()
    {
        int sum = 0;
        for (int i=0; i<entries.size(); i++)
        {
            sum += entries.get(i).getPlusMinusHeads();
        }
        return sum;
    }
    
    public float getWeightedPlusMinusHeads() {
        refreshStats();
        return weighted_plus_minus_heads;
    }
    
    private float calcWeightedPlusMinusHeads() {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); ++i) {
            sum += entries.get(i).getPlusMinusHeads() * Math.pow(DECAY, entries.size() - i - 1);
        }
        return sum;
    }
    
    public int getTotalEntries()
    {
        return entries.size();
    }
    
    public float getTotalWinningness() {
        refreshStats();
        return total_winningness;
    }
    
    private float calcTotalWinningness()
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
    
    public int getNumberOfLongestStreaks(boolean strict) {
        refreshStats();
        return strict ? winning_streak_strict.size() : winning_streak_loose.size();
    }
    
    public float getLongestStreak(boolean strict) {
        refreshStats();
        return strict ? longest_streak_strict : longest_streak_loose;
    }
    
    private float calcLongestStreak(boolean strict)
    {
        //ArrayList<ArrayList<Entry>> list = getEntriesInLongestStreak(strict);
        ArrayList<ArrayList<Entry>> list = calcEntriesInLongestStreak(strict);  // IS IT PROPER TO FORCE A CALCULATION HERE VIA CALC?
        
        if (list.size() <= 0)
            return 0.0f;
            
        float sum = 0.0f;
        for (int i=0; i<list.get(0).size(); i++)
            sum += list.get(0).get(i).getWinningness();
        return sum;
    }
    
    public ArrayList<ArrayList<Entry>> getEntriesInLongestStreak(boolean strict) {
        refreshStats();
        return strict ? winning_streak_strict : winning_streak_loose;
    }
    
    private ArrayList<ArrayList<Entry>> calcEntriesInLongestStreak(boolean strict)
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
                if (!strict || (entry.getPoll().getSynch() - lastWinSynch) <= 1)
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
                lastWinSynch = entry.getPoll().getSynch();
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
            opponent_count += (entries.get(i).getPoll().numEntries() - 1);
        }
        return opponent_count;
    }
    
    public float getWeightedNumOpponents() {
        refreshStats();
        return weighted_opponent_count;
    }

    public float calcWeightedNumOpponents() {
        float opponent_count = 0.0f;
        for (int i = 0; i < entries.size(); ++i) {
            opponent_count += (entries.get(i).getPoll().numEntries() - 1) * Math.pow(DECAY, entries.size() - i - 1);
        }
        return opponent_count;
    }
    
    public int getNewFormidableRating() {
        refreshStats();
        return formidable_rating;
    }
    
    private int calcNewFormidableRating() {
        // TODO: Just calcLongestStreak, don't use 'calc' here.  Instead, cache results individually, since work is done twice!
        return Math.round(5000.0f + 5000.0f * calcTotalPlusMinusHeads() / (getTotalNumOpponents() + 10));
    }
    
    public int getWeightedFormidableRating() {
        refreshStats();
        return weighted_formidable_rating;
    }
    
    private int calcWeightedFormidableRating() {
        // The Math.pow() thing has the effect of pretending there was a contest with 10 opponents and 0 plus-minus wins that predated all other contests.
        return (int)(Math.round(5000.0f + 5000.0f * calcWeightedPlusMinusHeads() / (calcWeightedNumOpponents() + BAYESIAN_ALLOWANCE * Math.pow(DECAY, entries.size() - 2))));
    }
        
    
    private void refreshStats() {
        if (!dirty) {
            // If we're up-to-date, there's no need to calculate.
            return;
        }
        
        // Update all fundamental stats
        total_votes = calcTotalVotes();
        total_points = calcTotalPoints();
        total_plus_minus_points = calcTotalPlusMinusPoints();
        total_plus_minus_heads = calcTotalPlusMinusHeads();
        total_winningness = calcTotalWinningness();
        winning_streak_strict = calcEntriesInLongestStreak(true);
        winning_streak_loose = calcEntriesInLongestStreak(false);
        longest_streak_strict = calcLongestStreak(true);
        longest_streak_loose = calcLongestStreak(false);
        formidable_rating = calcNewFormidableRating();
        
        // Experiments
        weighted_plus_minus_heads = calcWeightedPlusMinusHeads();
        weighted_opponent_count = calcWeightedNumOpponents();
        weighted_formidable_rating = calcWeightedFormidableRating();
        
        // We're up to date!
        dirty = false;
    } 
}
