import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Comparator;

import javax.swing.JOptionPane;

public class History
{
    private ArrayList<Contest> contests;
    private ArrayList<Member> members;
    String lastContestName;

    public History()
    {
        members = new ArrayList<Member>();
        contests = new ArrayList<Contest>();
        lastContestName = "";
    }

    public void addEntry(String contestName, String memberName, String tag, boolean myHasURL, String URL, boolean myHasVotes, int myVotes, boolean myHasUncertainty, int overrideCode) {
        Contest contestRetrieved;
        // check if the contest being requested hasn't been formed yet
        if ((contestRetrieved = getContestByName(contestName)) == null) {
            // if it hasn't, add it
            contestRetrieved = new Contest(contestName);
            contests.add(contestRetrieved);
        }

        Member memberRetrieved;
        // Prefer to look up by tag since tags are intended to be unique; otherwise, look up by name
        if (!tag.isEmpty()) {
            if ((memberRetrieved = getMemberByTag(tag)) == null) {
                // If there is no member with this tag, create a new member that has this tag
                memberRetrieved = new Member(tag, memberName);
                members.add(memberRetrieved);
            }
        } else {
            if ((memberRetrieved = getMemberByName(memberName)) == null) {
                // If there is no member with this name, create a new member with this name
                memberRetrieved = new Member(memberName);
                members.add(memberRetrieved);
            }
        }

        Entry entryAdding = new Entry(memberRetrieved, contestRetrieved, myHasURL, URL, myHasVotes, myVotes, myHasUncertainty, overrideCode);

        // regardless of the above, add the entry to the member's and contest's records
        contestRetrieved.addEntry(entryAdding);
        memberRetrieved.addEntry(entryAdding);
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
            if (members.get(i).hasTag() && members.get(i).getTag() == memberTag) {
                return members.get(i);
            }
        }
        return null;
    }

    public Contest getContestByName(String nameGetting)
    {
        for (int i=0; i<contests.size(); i++)
        {
            if (contests.get(i).getName().equals(nameGetting))
                return contests.get(i);
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
                        members.add(new Member(tagSplit[0], new ArrayList<String>(Arrays.asList(namesRead))));                        
                    } else {
                        // Place the split-up names in an array.
                        namesRead = strLine.split(nameRegex);
                        // Incorporate these names into a new untagged Member object. (Not pretty.)
                        members.add(new Member(new ArrayList<String>(Arrays.asList(namesRead))));
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
    {   //IT might be possible in this method to pass references to individual contests instead of "current contest names" and so forth
        try
        {
            // allocate new stream object
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine; // String in which to place new lines as they are read

            String[] splits; // array for the output of the regex split
            String currentContestName = "";
            int currentSynch = 0;
            Contest contestRetrieved;

            ParsedLine parse;

            //Read File Line By Line

            boolean blockComment = false;
            String blockOpen = "/*";
            String blockClose = "*/";

            while ((strLine = br.readLine()) != null)
            {
                blockComment |= strLine.startsWith(blockOpen);
                parse = new ParsedLine(strLine, currentContestName);

                if (parse.hasContestInfo && !blockComment)
                {
                    currentContestName = parse.contestName; // will change this

                    if ((contestRetrieved = getContestByName(currentContestName)) == null)
                    {
                        if (!parse.synchronous)
                            currentSynch++;

                        if (parse.hasTopicInfo)
                            contestRetrieved = new Contest(currentContestName, parse.hasTopicInfo, parse.topic, currentSynch);
                        else
                            contestRetrieved = new Contest(currentContestName, currentSynch);
                        contests.add(contestRetrieved);
                    }
                }

                if (parse.hasMemberInfo && !blockComment)
                {
                    addEntry(currentContestName, parse.memberName, parse.tag, parse.hasURL, parse.URL, parse.hasVotes, parse.votes, !parse.hasVotes, parse.overrideCode);
                }   

                blockComment &= !strLine.endsWith(blockClose);
            }
            lastContestName = currentContestName;   // this is so we can name the output files intelligently
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
        members.add(memberAdding);
    }

    public void addContest(Contest contestAdding)
    {
        contests.add(contestAdding);
    }

    public ArrayList<Member> getMembers()
    {
        return members;
    }

    public ArrayList<Contest> getContests()
    {
        return contests;
    }

    public String getLastContestName()
    {
        return lastContestName;
    }
}
