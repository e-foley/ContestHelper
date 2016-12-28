// 160103:  Added DIGEST_LIST_LENGTH constant

import java.io.*;
import java.nio.channels.FileChannel;
import javax.swing.JOptionPane;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Write a description of class Master here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public abstract class Master
{
    public static final int PAGE_LENGTH = 50;
    public static final int STARTING_INDEX = 1;
    public static final int DIGEST_LIST_LENGTH = 10;  // Number of members to list in "digest" version of the leaderboards
    
    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public static void main(String[] args)
    {
        String testText;
        
        testText = "";
        // testText = "-test";  // Comment out when done experimenting with things
        //testText = "-motm_test";
        
        long startTime = System.nanoTime();        
        
        History history = new History();
        String strLine = new String();
        if (!history.populateMembersFromFile("web/associations.txt"))
        {
            JOptionPane.showMessageDialog(null, "Error while parsing associations.txt.\nPerhaps data aren't delimited correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!history.populateEntriesFromFile("web/data.txt"))
        {
            JOptionPane.showMessageDialog(null, "Error while parsing data.txt.\nPerhaps data aren't delimited correctly or a numeric value is misplaced.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        FileWriter fstream;
        BufferedWriter out;
        
        String[][] swaps = new String[][] {{"###",""+history.getLastContestName()}};
        
        try
        {        
            // make backups of input
            Master.copyFile(new File("web/data.txt"),new File("backup/data-" + history.getLastContestName() + ".txt"));
            Master.copyFile(new File("web/associations.txt"),new File("backup/associations-" + history.getLastContestName() + ".txt"));
            
            // Begin archives
//             int index = STARTING_INDEX;
//             int low;
//             int high;
//             ArrayList<Contest> contests = history.getContests();
//             int numContests = contests.size();
//             int maxContest = (int)(Math.ceil(contests.get(contests.size()-1).getApparentContestNumber()));
//             while (index <= numContests)
//             {
//                 low = index;
//                 high = Math.min(index + PAGE_LENGTH - 1, maxContest);
//                 
//                 fstream = new FileWriter("archives/archive-" + history.getLastContestName() + "_" + low + "-" + high + ".txt");
//                 out = new BufferedWriter(fstream);
//                 if (index <= STARTING_INDEX)
//                     Master.addFileToBuffer("config/archives_header.txt", out, swaps);
//                 history.commitToFile(out, low, high);
//                 if (index >= numContests-PAGE_LENGTH)
//                     Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
//                 out.close();
//                 index+= PAGE_LENGTH;
//             }
                
                //fstream = new FileWriter("archives/archive-" + history.getLastContestName() + ".html");
                fstream = new FileWriter("web/archives" + testText + ".html");
                out = new BufferedWriter(fstream);
                Master.addFileToBuffer("config/archives_header.txt", out, swaps);
                history.commitToFile(out);
                Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
                out.close();
                
                fstream = new FileWriter("web/archives-digest" + testText + ".html");
                out = new BufferedWriter(fstream);
                Master.addFileToBuffer("config/archives_header.txt", out, swaps);
                history.commitToFile(out, 15);
                Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
                out.close();
                
                // ATTENTION:  This doesn't work because the value of numContests() includes the split contests as two entries.  Redesign this accordingly.
                // Should also number the files by page instead of contest number such that new upoads actually overwrite old ones instead of leaving remnants of the past.  Though remnants of the past are cool.
                final int CONTESTS_PER_PAGE = 15;
                int num_contests = history.getContests().size();
                int max_contest = (int)(Math.ceil(history.getContests().get(num_contests-1).getApparentContestNumber()));  // Still a terrible method
                //for (int i=1; i<num_contests; i+=CONTESTS_PER_PAGE) {
                
                    
                    //for (int p = max_contest / CONTESTS_PER_PAGE + max_contest != CONTESTS_PER_PAGE; p >= 1; p -= CONTESTS_PER_PAGE) {
                    for (int p = max_contest / CONTESTS_PER_PAGE + max_contest; p >= 1; p -= CONTESTS_PER_PAGE) {
                        
                //for (int i=max_contest; i >= 1; i-= CONTESTS_PER_PAGE) {
                    int i = max_contest - CONTESTS_PER_PAGE * (p - 1);
                    //int contest_start = i;
                    //int contest_end = Math.min(i + CONTESTS_PER_PAGE, num_contests);
                    int contest_start = Math.max(i - CONTESTS_PER_PAGE + 1, 1);
                    int contest_end = i;
                    //fstream = new FileWriter("../SOTW_Stats/archives-" + contest_start + "-" + contest_end + testText + ".html");
                    fstream = new FileWriter("web/archives-page" + p + ".html");
                    out = new BufferedWriter(fstream);
                    Master.addFileToBuffer("config/archives_header.txt", out, swaps);
                    history.commitToFile(out, contest_start, contest_end);
                    Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
                    out.close();
                }
            
            
            // Have an "old" leaderboard with a separate history that omits the most recent contest?

            // LEADERBOARD
            //fstream = new FileWriter("leaderboards/leaderboard-" + history.getLastContestName() + ".html");
            fstream = new FileWriter("web/leaderboards" + testText + ".html");
            out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/leaderboard_header.txt", out, swaps);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVotes(), "Most votes (all-time)", "", " vote", " Votes", out, true, true, true, 1);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPoints(), "Most points (all-time)", "", " point", " Points", out, true, true, true, 2);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVotesSingle(), "Most votes (single contest, by member)", "", " votes", " Votes", out, true, true, true, 3);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPointsSingle(), "Most points (single contest, by member)", "", " points", " Points", out, true, true, true, 4);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVictories(), "Most victories", "", " victory", " Wins", out, true, true, true, 5);
            // NOTE: "Max entries" list has no details because the same information is available in the gallery.
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortEntries(), "Most participation", "", " entry", " Entries", out, true, false, true, 6);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortConsecutiveStrict(), "Longest winning streaks (in consecutive contests, by member)", "", " streak", " Streak", out, true, true, true, 7);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortConsecutiveLoose(), "Longest winning streaks (in consecutive attempts, by member)", "", " streak", " Streak", out, true, true, true, 8);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortWinRatio(), "Best win ratios (minimum 5 entries)", "", "", " Ratio", out, true, true, true, 9);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPlusMinusPoints(), "Best plus/minus records (head-to-head vote basis)", "", "", " +/- votes", out, true, true, true, 10);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPlusMinusHeads(), "Best plus/minus records (head-to-head victory basis)", "", "", " +/- wins", out, true, true, true, 11);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortFormidable(), "Most formidable opponents", "", "", " Rating", out, true, false, true, 12);
            Master.addFileToBuffer("config/leaderboard_footer.txt", out, swaps);
            out.close();
            
            fstream = new FileWriter("web/leaderboards-digest" + testText + ".html");
            out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/leaderboard_header.txt", out, swaps);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVotes(), "Most votes (all-time)", "", " vote", " Votes", out, false, false, true, 1, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPoints(), "Most points (all-time)", "", " point", " Points", out, false, false, true, 2, DIGEST_LIST_LENGTH);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVotesSingle(), "Most votes (single contest, by member)", "", " votes", " Votes", out, false, true, true, 3, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPointsSingle(), "Most points (single contest, by member)", "", " points", " Points", out, false, true, true, 4, DIGEST_LIST_LENGTH);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortVictories(), "Most victories", "", " victory", " Wins", out, false, false, true, 5, DIGEST_LIST_LENGTH);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortEntries(), "Most participation", "", " entry", " Entries", out, false, false, true, 6, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortConsecutiveStrict(), "Longest winning streaks (in consecutive contests, by member)", "", " streak", " Streak", out, false, true, true, 7, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortConsecutiveLoose(), "Longest winning streaks (in consecutive attempts, by member)", "", " streak", " Streak", out, false, true, true, 8, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortWinRatio(), "Best win ratios (minimum 5 entries)", "", "", " Ratio", out, false, true, true, 9, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPlusMinusPoints(), "Best plus/minus records (head-to-head vote basis)", "", "", " +/- votes", out, false, false, true, 10, DIGEST_LIST_LENGTH);
            //Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortPlusMinusHeads(), "Best plus/minus records (head-to-head victory basis)", "", "", " +/- wins", out, false, false, true, 11, DIGEST_LIST_LENGTH);
            Leaderboard.addLeaderboardToFile(history.getMembers(), new MemberSortFormidable(), "Most formidable opponents", "", "", " Rating", out, false, false, true, 12, DIGEST_LIST_LENGTH);
            Master.addFileToBuffer("config/leaderboard_footer.txt", out, swaps);
            out.close();
            
            // MEMBER LIST (temp)
            fstream = new FileWriter("members/members-" + history.getLastContestName() + ".txt");
            out = new BufferedWriter(fstream);
            Collections.sort(history.getMembers(), new MemberSortAlphabetical());
            for (int i=0; i<history.getMembers().size(); i++)
            {
                out.write(history.getMembers().get(i).getMostRecentName());
                out.newLine();
            }
            out.newLine();
            out.write(history.getMembers().size() + " unique members in all.");
            out.close();
            
            
            
            
            
            
            
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            JOptionPane.showMessageDialog(null, "Archive output saved to archives\\archive-" + history.getLastContestName() + testText + ".html.\nLeaderboard output saved to leaderboards\\leaderboards-" + history.getLastContestName() + testText + ".html.\nTime elapsed: " + ((double)(duration))/1000000000.0 + " seconds.", "Success!", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "A strange problem occurred. Talk to nicklegends about it.\n\"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        
        // TESTING
        
        
        
        //try
        //{
            //String[][] u_swaps = new String[][] {{"###",""+history.getLastContestName()}};
            //fstream = new FileWriter("../SOTW_Stats/profiles/nicklegends.html");
            //out = new BufferedWriter(fstream);
            //Master.addFileToBuffer("config/profile_header.txt", out, u_swaps);
            
            ArrayList<Member> mems = history.getMembers();
            for (int i=0; i<mems.size(); i++) {
                UserProfile.createProfilePage(mems.get(i));
            }
            
            
            //UserProfile.createProfilePage(history.getMemberByName("nicklegends"));
            //UserProfile.createProfilePage(history.getMemberByName("Orithan"));
            //Master.addFileToBuffer("config/profile_footer.txt", out, u_swaps);
            //out.close();
        //}
        //catch (Exception e)
        //{
            //System.err.println("Error: " + e.getMessage());
            //JOptionPane.showMessageDialog(null, "One or more user profiles were not written.\n\"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        //}
        
        //UserProfile.createProfilePage(history.getMemberByName("Orithan") , out);
        
        
        
    }

    public static void addFileToBuffer(String filename, BufferedWriter out)
    {
        addFileToBuffer(filename, out, new String[0][0]);
    }
    
    public static void addFileToBuffer(String filename, BufferedWriter out, String[][] swaps)
    {
        String strLine;
        try
        {
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in)); 
            
            while ((strLine = br.readLine()) != null)
            {
                for(int i=0; i<swaps.length; i++)
                    strLine = strLine.replaceAll(swaps[i][0], swaps[i][1]);
                out.write(strLine);
                out.newLine();
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, filename + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException
    {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel source = null;
        FileChannel destination = null;
        try {
            fIn = new FileInputStream(sourceFile);
            source = fIn.getChannel();
            fOut = new FileOutputStream(destFile);
            destination = fOut.getChannel();
            long transfered = 0;
            long bytes = source.size();
            while (transfered < bytes) {
                transfered += destination.transferFrom(source, 0, source.size());
                destination.position(transfered);
            }
        } finally {
            if (source != null) {
                source.close();
            } else if (fIn != null) {
                fIn.close();
            }
            if (destination != null) {
                destination.close();
            } else if (fOut != null) {
                fOut.close();
            }
        }
    }
}