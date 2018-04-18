import java.util.ArrayList;

public class Member
{
    static class EntryStakePair {
        Entry entry;
        float stake;
    }
    
    private ArrayList<String> names;
    private ArrayList<EntryStakePair> entries;
    private String tag;
    private boolean isTagged;
    private boolean hasId;
    private int id;

    private boolean dirty;  // Whether stats need to be calculated anew.  We COULD change this to do dirty flags for each stat, maybe with a fancy class...
    private float total_votes = 0;
    private float total_points = 0;
    private float total_plus_minus_points = 0;
    private float total_plus_minus_heads = 0;
    private float total_winningness = 0.0f;
    private ArrayList<ArrayList<EntryStakePair>> winning_streak_loose = new ArrayList<ArrayList<EntryStakePair>>();
    private ArrayList<ArrayList<EntryStakePair>> winning_streak_strict = new ArrayList<ArrayList<EntryStakePair>>();
    private float longest_streak_loose = 0.0f;
    private float longest_streak_strict = 0.0f;
    private int formidable_rating = 0;
    
    private float weighted_plus_minus_points = 0.0f;
    private float weighted_potential = 0.0f;
    private int weighted_formidable_rating = 0;
    
    public static final float DECAY = 0.933033f;  // 0.933033 corresponds to half-life of 10 contests.
    public static final float BAYESIAN_ALLOWANCE = 200.0f;
    
    public Member()
    {
        names = new ArrayList<String>();
        entries = new ArrayList<EntryStakePair>();
        tag = new String();
        isTagged = false;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    // Incomplete clone
    public Member(Member mem) {
        names = new ArrayList<String>(mem.names);
        entries = new ArrayList<EntryStakePair>(mem.entries);
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
        entries = new ArrayList<EntryStakePair>();
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
        entries = new ArrayList<EntryStakePair>();
        tag = myTag;
        isTagged = true;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    public Member(ArrayList<String> myNames)
    {
        names = myNames;
        entries = new ArrayList<EntryStakePair>();
        tag = new String();
        isTagged = false;
        hasId = false;
        id = 0;
        dirty = true;
    }
    
    public Member(String myTag, ArrayList<String> myNames) {
        names = myNames;
        entries = new ArrayList<EntryStakePair>();
        tag = myTag;
        isTagged = true;
        hasId = false;
        id = 0;
        dirty = true;
    }

    public void addEntry(Entry entryAdding, float stake)
    {
        EntryStakePair pair = new EntryStakePair();
        pair.entry = entryAdding;
        pair.stake = stake;
        entries.add(pair);
        dirty = true;  // Adding an entry invalidates any cached stats.
    }
    
    public boolean removeEntry(EntryStakePair removing) {
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
    
    public ArrayList<EntryStakePair> getEntries()
    {
        return entries;
    }
    
    public ArrayList<String> getNames() {
        return names;
    }
    
    // Preferably return this in order from old to new (makes "other usernames" logic consistent when generating profiles)
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
    
    public float getTotalVotes() {
        refreshStats();
        return total_votes;
    }
    
    private float calcTotalVotes()
    {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getVotes() * pair.stake;
        }
        return sum;
    }
    
    public float getTotalPoints() {
        refreshStats();
        return total_points;
    }
    
    private float calcTotalPoints()
    {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getPoints() * pair.stake;
        }
        return sum;
    }
    
    public float getTotalPlusMinusPoints() {
        refreshStats();
        return total_plus_minus_points;
    }
    
    private float calcTotalPlusMinusPoints()
    {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getPlusMinusPoints() * pair.stake;
        }
        return sum;
    }
    
    public float getTotalPlusMinusHeads() {
        refreshStats();
        return total_plus_minus_heads;
    }
    
    private float calcTotalPlusMinusHeads()
    {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getPlusMinusHeads() * pair.stake;
        }
        return sum;
    }
    
    public float getWeightedPlusMinusPoints() {
        refreshStats();
        return weighted_plus_minus_points;
    }
    
    private float calcWeightedPlusMinusPoints() {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); ++i) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getPlusMinusPoints() * pair.stake * Math.pow(DECAY, entries.size() - i - 1);
        }
        return sum;
    }
    
    public float getTotalEntries()
    {
        float sum = 0.0f;
        for (EntryStakePair pair : entries) {
            sum += pair.stake;
        }
        return sum;
    }
    
    public float getTotalWinningness() {
        refreshStats();
        return total_winningness;
    }
    
    private float calcTotalWinningness()
    {
        float sum = 0.0f;
        for (int i=0; i<entries.size(); i++) {
            Member.EntryStakePair pair = entries.get(i);
            sum += pair.entry.getWinningness() * pair.stake;
        }
        return sum;
    }
    
    public EntryStakePair getMostRecentEntry()
    {
        return getRecentEntry(0);
    }
    
    public EntryStakePair getRecentEntry(int offset)
    {
        if (offset >= entries.size())
            return null;
            
        return entries.get(entries.size()-1-offset);
    }
    
    public boolean hasUncertainty()
    {
        for (int i=0; i<entries.size(); i++) {
            if (entries.get(i).entry.hasUncertainty()) {
                return true;
            }
        }
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
        ArrayList<ArrayList<EntryStakePair>> list = calcEntriesInLongestStreak(strict);  // IS IT PROPER TO FORCE A CALCULATION HERE VIA CALC?
        
        if (list.size() <= 0)
            return 0.0f;
            
        float sum = 0.0f;
        for (int i=0; i<list.get(0).size(); i++) {
            EntryStakePair pair = list.get(0).get(i);
            sum += pair.entry.getWinningness() * pair.stake;
        }
        return sum;
    }
    
    public ArrayList<ArrayList<EntryStakePair>> getEntriesInLongestStreak(boolean strict) {
        refreshStats();
        return strict ? winning_streak_strict : winning_streak_loose;
    }
    
    private ArrayList<ArrayList<EntryStakePair>> calcEntriesInLongestStreak(boolean strict)
    {
        //System.out.println("Getting for member " + getMostRecentName());
        float current = 0.0f;
        float max = 0.0f;
        ArrayList<EntryStakePair> currentList = new ArrayList<EntryStakePair>();
        ArrayList<ArrayList<EntryStakePair>> maxList = new ArrayList<ArrayList<EntryStakePair>>();
        int lastWinSynch = 0;
        EntryStakePair pair;
        for (int i=0; i<entries.size(); i++)
        {
            pair = entries.get(i);
            if (pair.entry.getWinningness() > 0.0f) // if a winner...
            {
                if (!strict || (pair.entry.getPoll().getSynch() - lastWinSynch) <= 1)
                {
                    current += pair.entry.getWinningness() * pair.stake;
                    currentList.add(pair);
                }
                else
                {
                    current = pair.entry.getWinningness() * pair.stake;
                    currentList = new ArrayList<EntryStakePair>();
                    currentList.add(pair);
                }
                lastWinSynch = pair.entry.getPoll().getSynch();
            }
            else // if not a winner
            {
                current = 0;
                currentList = new ArrayList<EntryStakePair>();   // this can probably be removed to save time if necessary
            }
                
            if (current == max && max > 0.0f)
            {
                maxList.add(currentList);
            }
            else if (current > max)
            {
                max = current;
                maxList = new ArrayList<ArrayList<EntryStakePair>>();
                maxList.add(currentList);
            }
        }
        return maxList;
    }
    
    public static int getNumberOfEntriesWithMostVotes(ArrayList<EntryStakePair> list)
    {
        return list.size();
    }
    
    public static float getMostVotesSingle(ArrayList<EntryStakePair> list)
    {
        if (list == null || list.size() == 0)
            return 0;
        else return list.get(0).entry.getVotes() * list.get(0).stake;
    }
    
    public ArrayList<EntryStakePair> getEntriesWithMostVotes()
    {
        float max = 0.0f;
        ArrayList<EntryStakePair> maxList = new ArrayList<EntryStakePair>();
        for (int i=0; i<entries.size(); i++)
        {
            EntryStakePair pair = entries.get(i);
            if (pair.entry.getVotes() * pair.stake == max)
            {
                maxList.add(pair);
            }
            else if (pair.entry.getVotes() * pair.stake >= max)
            {
                max = pair.entry.getVotes() * pair.stake;
                maxList = new ArrayList<EntryStakePair>();
                maxList.add(pair);
            }
        }
        return maxList;
    }
    
    public static int getNumberOfEntriesWithMostPoints(ArrayList<EntryStakePair> list)
    {
        return list.size();
    }
    
    public static float getMostPointsSingle(ArrayList<EntryStakePair> list)
    {
        if (list == null || list.size() == 0)
            return 0;
        else return list.get(0).entry.getPoints() * list.get(0).stake;
    }
    
    public ArrayList<EntryStakePair> getEntriesWithMostPoints()
    {
        float max = 0.0f;
        ArrayList<EntryStakePair> maxList = new ArrayList<EntryStakePair>();
        for (int i=0; i<entries.size(); i++)
        {
            EntryStakePair pair = entries.get(i);
            if (pair.entry.getPoints() * pair.stake == max)
            {
                maxList.add(pair);
            }
            else if (pair.entry.getPoints() * pair.stake >= max)
            {
                max = pair.entry.getPoints() * pair.stake;
                maxList = new ArrayList<EntryStakePair>();
                maxList.add(pair);
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
    
    public float getTotalNumOpponents() {
        float opponent_count = 0.0f;
        for (int i = 0; i < entries.size(); ++i) {
            EntryStakePair pair = entries.get(i);
            opponent_count += (pair.entry.getPoll().numEntries() - 1) * pair.stake;
        }
        return opponent_count;
    }
    
    public float getWeightedNumOpponents() {
        refreshStats();
        return weighted_potential;
    }

    public float calcWeightedPotential() {
        float potential = 0.0f;
        int i = 0;
        for (EntryStakePair pair : entries) {
            potential += pair.entry.getPotential() * pair.stake * Math.pow(DECAY, entries.size() - i - 1);
            ++i;
        } 
        return potential;
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
        // The Math.pow() thing has the effect of pretending there was a big contest, predating all others, with BAYESIAN_ALLOWANCE "potential" for the entry of which 0 was achieved.
        return (int)(Math.round(5000.0f + 5000.0f * calcWeightedPlusMinusPoints() / (calcWeightedPotential() + BAYESIAN_ALLOWANCE * Math.pow(DECAY, entries.size()))));
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
        weighted_plus_minus_points = calcWeightedPlusMinusPoints();
        weighted_potential = calcWeightedPotential();
        weighted_formidable_rating = calcWeightedFormidableRating();
        
        // We're up to date!
        dirty = false;
    } 
}
