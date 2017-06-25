import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

public class History
{
    private ArrayList<Poll> polls;
    private HashMap<Integer, Member> members;  // Key is member ID, which should also be held by the Members themselves
    String lastPollName;

    public History()
    {
        members = new HashMap<Integer, Member>();
        polls = new ArrayList<Poll>();
        lastPollName = "";
    }

    public void addEntry(Entry entry_adding, boolean requestId, int memberId) {
        addEntry(entry_adding.getPoll().getName(),
                 entry_adding.getPoll().hasTopic(),
                 entry_adding.getPoll().getTopic(),
                 entry_adding.getPoll().getSynch(),
                 entry_adding.getNameSubmittedUnder(),
                 entry_adding.getMember().getTag(),
                 requestId,
                 memberId,
                 entry_adding.hasURL(),
                 entry_adding.getURL(),
                 entry_adding.hasVotes(),
                 entry_adding.getVotes(),
                 entry_adding.hasUncertainty(),
                 entry_adding.getOverrideCode());
    }
    
    public void addEntry(String pollName, boolean hasTopicInfo, int topic, int currentSynch, String memberName, String tag, boolean requestId, int memberId, boolean myHasURL, String URL, boolean myHasVotes, int myVotes, boolean myHasUncertainty, int overrideCode) {
        Poll pollRetrieved;
        // check if the poll being requested hasn't been formed yet
        if ((pollRetrieved = getPollByName(pollName)) == null) {
            // if it hasn't, add it
            pollRetrieved = new Poll(pollName, hasTopicInfo, topic, currentSynch);
            polls.add(pollRetrieved);
        }

        Member memberRetrieved;
        // Prefer to look up by tag since tags are intended to be unique; otherwise, look up by name
        if (!tag.isEmpty()) {
            if ((memberRetrieved = getMemberByTag(tag)) == null) {
                // If there is no member with this tag, create a new member that has this tag.  (Is this kosher or will we run into duplication issues?)
                memberRetrieved = new Member(tag, memberName);
                if (requestId) {
                    memberRetrieved.setId(members.size());
                } else {
                    memberRetrieved.setId(memberId);
                }
                members.put(memberRetrieved.getId(), memberRetrieved);
                System.out.println("Warning: tag \"" + tag + "\" in data file does not exist in associations file.");
            }
        } else {
            if ((memberRetrieved = getMemberByName(memberName)) == null) {
                // If there is no member with this name, create a new member with this name
                memberRetrieved = new Member(memberName);
                if (requestId) {
                    memberRetrieved.setId(members.size());
                } else {
                    memberRetrieved.setId(memberId);
                }
                members.put(memberRetrieved.getId(), memberRetrieved);
            }
        }

        Entry entryAdding = new Entry(memberRetrieved, pollRetrieved, myHasURL, URL, myHasVotes, myVotes, myHasUncertainty, overrideCode, memberName);

        // regardless of the above, add the entry to the member's and poll's records
        pollRetrieved.addEntry(entryAdding);
        memberRetrieved.addEntry(entryAdding);
    }
    
    // Creates a new history that possesses cloned members and polls from this history (over a given span of polls).
    public History getSubhistory(int index_start, int index_end) {
        History returning = new History();

        returning.members = new HashMap<Integer, Member>();
        
        Set<Integer> member_ids = members.keySet();
        for (Integer member_id : member_ids) {
            Member member = new Member(members.get(member_id));  // Performs incomplete clone per custom Member constructor
            returning.members.put(member.getId(), member);  // Populate a sort of copy of the members array
            ArrayList<Entry> entries_to_remove = new ArrayList<Entry>();
            ArrayList<Entry> entries = member.getEntries();
            for (Entry ent : entries) {
                int contest_index = polls.indexOf(ent.getPoll());
                if (contest_index != -1 && (contest_index < index_start || contest_index > index_end)) {
                    entries_to_remove.add(ent);
                }
            }
            // Now actually remove these entries from the Member
            for (Entry ent_rem : entries_to_remove) {
                // System.out.println("Entry removed!" + ent_rem.getURL());
                member.removeEntry(ent_rem);
            }
        }
        
        for (int i = index_start; i <= index_end; ++i) {
          returning.polls.add(this.polls.get(i));
        }
        
        if (!returning.polls.isEmpty()) {
            returning.lastPollName = returning.polls.get(returning.polls.size() - 1).getName();
        }
        
        return returning;
    }

    public Member getMemberByName(String memberName)
    {
        for (int i=0; i<members.size(); i++)
        {
            if (members.get(i).hasName(memberName))
                return members.get(i);
        }
        return null;
    }
    
    public Member getMemberByTag(String memberTag) {
        for (int i = 0; i < members.size(); ++i) {
            if (members.get(i).hasTag() && members.get(i).getTag().equals(memberTag)) {
                return members.get(i);
            }
        }
        return null;
    }
    
    public Member getMemberById(int id) {
        return members.get(id);
    }

    public Poll getPollByName(String nameGetting)
    {
        for (int i=0; i<polls.size(); i++)
        {
            if (polls.get(i).getName().equals(nameGetting))
                return polls.get(i);
        }
        return null;
    }

    public boolean populateMembersFromFile(String filename)
    {
        try
        {
            // allocate new stream ovject
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine; // String in which to place new lines as they are read
            String nameRegex = "(\\s+)?>(\\s+)?";   // regular expression to divide associations file
            String tagRegex = "(\\s+)?\\:(\\s+)?";  // Regular expression that divides lines into a tag section and a names section
            String[] namesRead; // array for the output of the regex split

            //Read File Line By Line
            while ((strLine = br.readLine()) != null)
            {
                // Ignore empty lines and those starting with two slashes
                if (!strLine.equals("")  &&  !strLine.startsWith("//"))
                {
                    // If the line contains a colon, we treat all before it as the "tag" for that member.
                    // If there are multiple colons, everything at and beyond the second colon is ignored.
                    if (strLine.contains(":")) {
                        // Divide line into two strings: the tag and the names associated with that tag.
                        String[] tagSplit = strLine.split(tagRegex);
                        // Place the split-up names in an array.
                        namesRead = tagSplit[1].split(nameRegex);
                        // Incorporate the tag and names into a new Member object.  (Not pretty.)
                        Member memberAdding = new Member(tagSplit[0], new ArrayList<String>(Arrays.asList(namesRead)));
                        memberAdding.setId(members.size());
                        members.put(memberAdding.getId(), memberAdding);
                    } else {
                        // Place the split-up names in an array.
                        namesRead = strLine.split(nameRegex);
                        // Incorporate these names into a new untagged Member object. (Not pretty.)
                        Member memberAdding = new Member(new ArrayList<String>(Arrays.asList(namesRead)));
                        memberAdding.setId(members.size());
                        members.put(memberAdding.getId(), memberAdding);
                    }
                }
            }
            //Close the input stream
            in.close();
        }
        catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
            //JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
        return true;
    }

    public boolean populateEntriesFromFile(String filename)
    {   //IT might be possible in this method to pass references to individual polls instead of "current poll names" and so forth
        try
        {
            // allocate new stream object
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine; // String in which to place new lines as they are read

            String[] splits; // array for the output of the regex split
            String currentPollName = "";
            int currentSynch = 0;
            Poll pollRetrieved = null;

            ParsedLine parse;

            //Read File Line By Line

            boolean blockComment = false;
            String blockOpen = "/*";
            String blockClose = "*/";

            while ((strLine = br.readLine()) != null)
            {
                blockComment |= strLine.startsWith(blockOpen);
                parse = new ParsedLine(strLine, currentPollName);

                if (parse.hasPollInfo && !blockComment)
                {
                    currentPollName = parse.pollName; // will change this

                    if ((pollRetrieved = getPollByName(currentPollName)) == null)
                    {
                        if (!parse.synchronous)
                            currentSynch++;

                        if (parse.hasTopicInfo)
                            pollRetrieved = new Poll(currentPollName, parse.hasTopicInfo, parse.topic, currentSynch);
                        else
                            pollRetrieved = new Poll(currentPollName, currentSynch);
                        polls.add(pollRetrieved);
                    }
                }

                if (parse.hasMemberInfo && !blockComment)
                {
                    addEntry(currentPollName, parse.hasTopicInfo, parse.topic, currentSynch, parse.memberName, parse.tag, true, -1, parse.hasURL, parse.URL, parse.hasVotes, parse.votes, !parse.hasVotes, parse.overrideCode);
                }
                
                if (parse.isPollNote && !blockComment && pollRetrieved != null) {
                    pollRetrieved.addNote(parse.note);
                }

                blockComment &= !strLine.endsWith(blockClose);
            }
            lastPollName = currentPollName;   // this is so we can name the output files intelligently
            //Close the input stream
            in.close(); 
        }
        catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
            //JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
        return true;
    }

    public void addMember(Member memberAdding)
    {
        memberAdding.setId(members.size());
        members.put(memberAdding.getId(), memberAdding);
    }

    public void addPoll(Poll pollAdding)
    {
        polls.add(pollAdding);
    }

    public Collection<Member> getMembers()
    {
        return members.values();
    }
    
    public HashMap<Integer, Member> getMemberMap() {
        return members;
    }

    public ArrayList<Poll> getPolls()
    {
        return polls;
    }

    public String getLastPollName()
    {
        return lastPollName;
    }
    
    private void setLastPollName(String lastPollNameSetting) {
        lastPollName = lastPollNameSetting;
    }
}
