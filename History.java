import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Comparator;

import javax.swing.JOptionPane;

/**
 * Write a description of class History here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class History
{
    public static final int CONTESTS_PER_LINE = 3;

    private ArrayList<Contest> contests;
    private ArrayList<Member> members;

    String lastContestName;

    /**
     * Constructor for objects of class History
     */
    public History()
    {
        members = new ArrayList<Member>();
        contests = new ArrayList<Contest>();
        lastContestName = "";
    }

    public void addEntry(String contestName, String memberName, boolean myHasURL, String URL, boolean myHasVotes, int myVotes, boolean myHasUncertainty, int overrideCode)
    {
        Contest contestRetrieved;
        // check if the contest being requested hasn't been formed yet
        if ((contestRetrieved = getContestByName(contestName)) == null)
        {
            // if it hasn't, add it
            contestRetrieved = new Contest(contestName);
            contests.add(contestRetrieved);
        }

        Member memberRetrieved;
        // check if the member being requested hasn't been formed yet
        if ((memberRetrieved = getMemberByName(memberName)) == null)
        {
            // if it hasn't, add it
            memberRetrieved = new Member(memberName);
            members.add(memberRetrieved);
        }

        Entry entryAdding = new Entry(memberRetrieved, contestRetrieved, myHasURL, URL, myHasVotes, myVotes, myHasUncertainty, overrideCode);
        //System.out.println("Adding entry: " + memberRetrieved.getMostRecentName());

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
            String regex = "(\\s+)?>(\\s+)?";   // regular expression to divide associations file
            String[] namesRead; // array for the output of the regex split

            //Read File Line By Line
            while ((strLine = br.readLine()) != null)
            {
                // Ignore empty lines and those starting with two slashes
                if (!strLine.equals("")  &&  !strLine.startsWith("//"))
                {
                    // put the split up names in an array
                    namesRead = strLine.split(regex);
                    // incorporate these names into new Member objects (not pretty...)
                    members.add(new Member(new ArrayList<String>(Arrays.asList(namesRead))));
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
                    addEntry(currentContestName, parse.memberName, parse.hasURL, parse.URL, parse.hasVotes, parse.votes, !parse.hasVotes, parse.overrideCode);
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

    public void commitToFile(BufferedWriter out)
    {
        //commitToFile(out, Integer.MIN_VALUE, Integer.MAX_VALUE);
        commitToFile(out, 1, (int)(contests.get(contests.size()-1).getApparentContestNumber()));  // This plays better with row breaks in generic commitToFile()
    }

    public void commitToFile(BufferedWriter out, int totalEntriesToShow)
    {
        commitToFile(out, (int)(contests.get(contests.size()-1).getApparentContestNumber()-totalEntriesToShow+1), Integer.MAX_VALUE);
    }

    public void commitToFile(BufferedWriter out, int contestStart, int contestEnd)
    {
        try
        {
            Entry entry;
            Contest contest;
            boolean isWinner;
            ArrayList<Entry> winners;
            //int currentSynch;
            out.write("<table class='big-board'>");
            out.newLine();
            int countUp = 0;
            //for (int i=0; i<contests.size(); i++)
            for (int i=contests.size()-1; i>=0; i--)
            {
                contest = contests.get(i);

                if (contest.getApparentContestNumber() >= contestStart && contest.getApparentContestNumber() <= contestEnd)
                {

                    //The below block of code used to be above the if statement checking apparent contest numbers... why? 
                    //countUp = contests.size()-1-i;

                    if (countUp % CONTESTS_PER_LINE == 0) {
                        out.write("<tr>");
                    }
                    //if((contests.size()-1-i)%CONTESTS_PER_LINE == 0)
                    //    out.write("<tr>");
                    //countUp++;
                    out.write("<td class='contest-cell'>");

                    //END BLOCK OF CODE

                    winners = contest.getWinners();
                    out.write("<table class='results-table'>");
                    //out.write("<div class='contest-block'>");
                    out.newLine();
                    out.write("<tr><td class='contest-title' colspan=3>");
                    if (contest.hasTopic())
                        out.write("<a class='contest' href='http://www.purezc.net/forums/index.php?showtopic=" + contest.getTopic() + "'>");
                    //out.write("(" + contest.getSynch() + ") "); // TEMPORARY!
                    out.write("Screenshot of the Week " + contest.getName());
                    if (contest.hasTopic())
                        out.write("</a>");
                    out.write("</td></tr>");

                    //New code to show the pictures
                    out.newLine();
                    out.write("<tr><td class='picture-cell' colspan=3>");
                    for (int j=0; j<winners.size(); j++)
                    {
                        if (!winners.get(j).hasURL()) {
                            out.write("<img class='picture-picture' title='The winner&#8217;s image is missing from the archives. Sorry.' src='images/no_image.png'/>");
                        } else {
                            out.write("<a href='" + winners.get(j).getURL() + "'>");
                            out.write("<img class='picture-picture' title='" + winners.get(j).getMember().getMostRecentName() + "' src='" + winners.get(j).getURL() + "'/>");
                            out.write("</a>");
                        }
                    }
                    out.write("</td></tr>");

                    out.newLine();
                    out.write("<tr class='header-row'><td class='left'>Name</td><td class='center'>Votes</td><td class='center'>Points</td></tr>");
                    out.newLine();

                    for (int j=0; j<contests.get(i).numEntries(); j++)
                    {
                        entry = contest.getEntryByIndex(j);
                        isWinner = (entry.isWinner());

                        if (isWinner)
                            out.write("<tr class='entry winner'>");
                        else
                            out.write("<tr class='entry'>");

                        //out.newLine();

                        out.write("<td class='name'>");
                        if (entry.hasURL())
                            out.write("<a class='image' href='" + entry.getURL() + "'>");
                        out.write(entry.getMember().getMostRecentName());
                        if (entry.hasURL())
                            out.write("</a>");
                            
                        // Add an icon if the shot won the contest
                        if (isWinner) {
                            out.write(" <img class='winnericon' title='Winner!' src='images/star.png'/>");
                        }
                            
                        out.write("</td>");

                        if (entry.hasVotes())
                        {
                            out.write("<td class='votes'>"+ entry.getVotes() + "</td>");
                            //                             if (entry.getVotes() != 1)
                            //                                 out.write("s");
                            //                             out.write("</td>");
                            out.write("<td class='points'>" + entry.getPoints() + "</td>");
                            //                             if (entry.getPoints() != 1)
                            //                                 out.write("s");
                            //                             out.write(")</span>");
                        }
                        else
                        {
                            out.write("<td class='votes'>&mdash;</td><td class='points'>&mdash;</td>");
                        }
                        out.newLine();
                        out.write("</tr>");
                        //else
                        //out.write(" – ? votes (? points)");
                        //out.write("</span>");
                    }
                    //                     if (winners.size() >= 2)
                    //                     {
                    //                         out.write("<br/>");
                    //                         out.newLine();
                    //                         out.write("<span class='tie-note'>Screenshot of the Week " + contest.getName() + " ended in a draw.</span>");
                    //                     }

                    out.write("<tr class='info-row'><td>");
                    out.write(contest.numEntries() + " entr");
                    if (contest.numEntries() != 1)
                        out.write("ies");
                    else
                        out.write("y");
                    out.write("</td><td class='votes'>");
                    out.write(""+contest.numVotes());
                    out.write("</td><td class='points'>");
                    out.write(""+contest.numPoints());
                    out.write("</td></tr>");

                    out.write("</table>");
                    out.newLine();
                    //out.write("</div>");
                    //out.newLine();
                    out.newLine();

                    // end if synchs, etc.

                    out.write("</td>");
                    //if (countUp%CONTESTS_PER_LINE >= CONTESTS_PER_LINE-1  ||  countUp == contests.size()-1)
                    if (countUp % CONTESTS_PER_LINE >= CONTESTS_PER_LINE - 1) {
                        out.write("</tr>");
                    }

                    countUp++;
                }
            }
            
            if (countUp % CONTESTS_PER_LINE != 1) {
                out.write("</tr>");
            }
            
            out.write("</table>");
        }
        catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
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
