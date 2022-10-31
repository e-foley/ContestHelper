
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
import java.nio.charset.StandardCharsets;

public class History {
    static class MemberInfo {
        public String member_name;
        public String tag;
        int id;
    }
    
    private ArrayList<Poll> polls;
    private HashMap<Integer, Member> members;  // Key is member ID, which should also be held by the Members themselves
    // TODO: The polls maintain their own names. Can't we just return a Poll where we need its names?
    String lastPollLongName;
    String lastPollShortName;

    public History()
    {
        members = new HashMap<Integer, Member>();
        polls = new ArrayList<Poll>();
        lastPollLongName = "";
        lastPollShortName = "";
    }

//     public void addEntry(Entry entry_adding, boolean requestId, int memberId) {
//         addEntry(entry_adding.getPoll().getShortName(),
//                  entry_adding.getPoll().hasTopic(),
//                  entry_adding.getPoll().getTopic(),
//                  entry_adding.getPoll().getSynch(),
//                  entry_adding.getNameSubmittedUnder(),
//                  entry_adding.getMember().getTag(),
//                  requestId,
//                  memberId,
//                  entry_adding.hasURL(),
//                  entry_adding.getURL(),
//                  entry_adding.hasVotes(),
//                  entry_adding.getVotes(),
//                  entry_adding.hasUncertainty(),
//                  entry_adding.getOverrideCode());
//     }

    public void addEntry(String pollShortName, String pollLongName, boolean hasTopicInfo, int topic, int currentSynch, ArrayList<MemberInfo> member_infos, boolean requestId, boolean myHasURL, String URL, boolean myHasVotes, int myVotes, boolean myHasUncertainty, int overrideCode) {
        Poll pollRetrieved;
        // check if the poll being requested hasn't been formed yet
        if ((pollRetrieved = getPollByShortName(pollShortName)) == null) {
            // if it hasn't, add it
            pollRetrieved = new Poll(pollShortName, pollLongName, hasTopicInfo, topic, currentSynch);
            polls.add(pollRetrieved);
        }

        ArrayList<Entry.MemberNameCouple> members_retrieved = new ArrayList<Entry.MemberNameCouple>();
        for (MemberInfo info : member_infos) {
            // Aliases for convenience
            String memberName = info.member_name;
            String tag = info.tag;
            int memberId = info.id;
            // Member identified by other information, if one exists.
            Member memberRetrieved = null;
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
            // TODO: Clean this up... This can't be most efficient, can it?
            Entry.MemberNameCouple couple = new Entry.MemberNameCouple();
            couple.member = memberRetrieved;
            couple.name_submitted_under = memberName;
            
            members_retrieved.add(couple);
        }
        
        Entry entryAdding = new Entry(members_retrieved, pollRetrieved, myHasURL, URL, myHasVotes, myVotes, myHasUncertainty, overrideCode);
    
        // regardless of the above, add the entry to the member's and poll's records
        pollRetrieved.addEntry(entryAdding);
        
        for (Entry.MemberNameCouple couple : members_retrieved) {
            couple.member.addEntry(entryAdding, 1.0f / members_retrieved.size());
        }
    }

    // Creates a new history that possesses cloned members and polls from this history (over a given span of polls).
    public History getSubhistory(int index_start, int index_end) {
        History returning = new History();

        returning.members = new HashMap<Integer, Member>();

        Set<Integer> member_ids = members.keySet();
        for (Integer member_id : member_ids) {
            Member member = new Member(members.get(member_id));  // Performs incomplete clone per custom Member constructor
            returning.members.put(member.getId(), member);  // Populate a sort of copy of the members array
            ArrayList<Member.EntryStakePair> pairs_to_remove = new ArrayList<Member.EntryStakePair>();
            ArrayList<Member.EntryStakePair> pairs = member.getEntries();
            for (Member.EntryStakePair pair : pairs) {
                int contest_index = polls.indexOf(pair.entry.getPoll());
                if (contest_index != -1 && (contest_index < index_start || contest_index > index_end)) {
                    pairs_to_remove.add(pair);
                }
            }
            // Now actually remove these entries from the Member
            for (Member.EntryStakePair pair_rem : pairs_to_remove) {
                member.removeEntry(pair_rem);
            }
            
            // Remove members without any entries (since they didn't exist...)
            if (member.getTotalEntries() <= 0.0) {
                returning.members.remove(member.getId());
            }
        }

        for (int i = index_start; i <= index_end; ++i) {
          returning.polls.add(this.polls.get(i));
        }

        if (!returning.polls.isEmpty()) {
            Poll lastPoll = returning.polls.get(returning.polls.size() - 1);
            returning.lastPollShortName = lastPoll.getShortName();            
            returning.lastPollLongName = lastPoll.getLongName();
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

    public Poll getPollByShortName(String shortNameGetting)
    {
        // Why isn't this just a map lookup?
        for (int i=0; i<polls.size(); i++)
        {
            if (polls.get(i).getShortName().equals(shortNameGetting))
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
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String strLine; // String in which to place new lines as they are read
            String nameRegex = "(\\s+)?>(\\s+)?";   // regular expression to divide associations file
            String tagRegex = "(\\s+)?\\:(\\s+)?";  // Regular expression that divides lines into a tag section and a names section
            String[] namesRead; // array for the output of the regex split

            //Read File Line By Line
            int lines_to_ignore = 1;
            int line = 0;
            while ((strLine = br.readLine()) != null) {
                line++;
                if (line <= lines_to_ignore) {
                    // Some junk appears at the top of Notepad UTF-8 files and I'm not sure what it is... But we can ignore it.
                    continue;
                }
                
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
            // Catch exception if any
            System.err.println("Error caught in History: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean populateEntriesFromFile(String filename)
    {   //It might be possible in this method to pass references to individual polls instead of "current poll names" and so forth
        try
        {
            // allocate new stream object
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String strLine; // String in which to place new lines as they are read

            String[] splits; // array for the output of the regex split
            String currentPollShortName = "";
            String currentPollLongName = "";
            int currentSynch = 0;
            Poll pollRetrieved = null;

            ParsedLine parse;
            
            // Read file line by line
            boolean blockComment = false;
            String blockOpen = "/*";
            String blockClose = "*/";
            int lines_to_ignore = 1;
            int line = 0;
            while ((strLine = br.readLine()) != null) {
                line++;
                if (line <= lines_to_ignore) {
                    // Some junk appears at the top of Notepad UTF-8 files and I'm not sure what it is... But we can ignore it.
                    continue;
                }
                blockComment |= strLine.startsWith(blockOpen);
                parse = new ParsedLine(strLine, currentPollShortName);

                if (parse.hasPollInfo && !blockComment)
                {
                    currentPollShortName = parse.pollShortName; // will change this
                    currentPollLongName = parse.pollLongName;

                    if ((pollRetrieved = getPollByShortName(currentPollShortName)) == null)
                    {
                        if (!parse.synchronous)
                            currentSynch++;

                        if (parse.hasTopicInfo)
                            pollRetrieved = new Poll(currentPollShortName, parse.pollLongName, parse.hasTopicInfo, parse.topic, currentSynch);
                        else
                            pollRetrieved = new Poll(currentPollShortName, parse.pollLongName, currentSynch);
                        polls.add(pollRetrieved);
                    }
                }

                if (parse.hasMemberInfo && !blockComment)
                {
                    // addEntry(currentPollName, parse.hasTopicInfo, parse.topic, currentSynch, parse.member_infos, true, -1, parse.hasURL, parse.URL, parse.hasVotes, parse.votes, !parse.hasVotes, parse.overrideCode);
                    addEntry(currentPollShortName, parse.pollLongName, parse.hasTopicInfo, parse.topic, currentSynch, parse.member_infos, true, parse.hasURL, parse.URL, parse.hasVotes, parse.votes, !parse.hasVotes, parse.overrideCode);
                 }

                if (parse.isPollNote && !blockComment && pollRetrieved != null) {
                    pollRetrieved.addNote(parse.note);
                }

                blockComment &= !strLine.endsWith(blockClose);
            }
            lastPollLongName = currentPollLongName;   // this is so we can name the output files intelligently
            //Close the input stream
            in.close();
        }
        catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error caught in History: " + e.getMessage());
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

    public int numPolls() {
        return polls.size();
    }

    public Poll getPoll(int index) {
        return polls.get(index);
    }

    public String getLastPollShortName()
    {
        return lastPollShortName;
    }
    
    public String getLastPollLongName()
    {
        return lastPollLongName;
    }
}
