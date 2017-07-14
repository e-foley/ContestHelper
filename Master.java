import java.io.*;
import java.nio.channels.FileChannel;
import javax.swing.JOptionPane;
import java.util.Collections;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;

public abstract class Master
{
    public static final int PAGE_LENGTH = 50;
    public static final int STARTING_INDEX = 1;
    public static final int DIGEST_LIST_LENGTH = 10;  // Number of members to list in "digest" version of the leaderboards
    public static final int NUM_ARCHIVES_DIGEST_ENTRIES = 12;
    public static final int CONTESTS_PER_PAGE = 12;
    public static final int DELTA = 10;
    public static final int WIN_RATIO_MIN_ENTRIES = 5;
    
    public static void main(String[] args)
    {
        ArrayList<NamedStamp> stamps = new ArrayList<NamedStamp>();
        stamps.add(new NamedStamp("Begin"));
        
        String testText;
        
        testText = "";
        // testText = "-test";  // Comment out when done experimenting with things
        
        boolean generate_user_galleries = true;
        
        long startTime = System.nanoTime();        
        
        History history = new History();
        ArchivesGenerator archivesGenerator = new ArchivesGenerator();
        String strLine = new String();
        
        stamps.add(new NamedStamp("Populating members"));
        if (!history.populateMembersFromFile("web/associations.txt"))
        {
            JOptionPane.showMessageDialog(null, "Error while parsing associations.txt.\nPerhaps data aren't delimited correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        stamps.add(new NamedStamp("Populating entries"));
        if (!history.populateEntriesFromFile("web/data.txt"))
        {
            JOptionPane.showMessageDialog(null, "Error while parsing data.txt.\nPerhaps data aren't delimited correctly or a numeric value is misplaced.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        FileWriter fstream;
        BufferedWriter out;
        
        String[][] swaps = new String[][] {{"###",""+history.getLastPollName()}};
        
        try
        {
            stamps.add(new NamedStamp("Copying input files"));
            // make backups of input
            Master.copyFile(new File("web/data.txt"),new File("backup/data-" + history.getLastPollName() + ".txt"));
            Master.copyFile(new File("web/associations.txt"),new File("backup/associations-" + history.getLastPollName() + ".txt"));
            
            // ARCHIVES
            stamps.add(new NamedStamp("Preparing archive generation"));
            fstream = new FileWriter("web/archives" + testText + ".html");
            out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/archives_header.txt", out, swaps);
            stamps.add(new NamedStamp("Generating archives"));
            archivesGenerator.generate(history, out);
            stamps.add(new NamedStamp("Done generating archives"));
            Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
            out.close();
            stamps.add(new NamedStamp("Done writing archives"));
            
            final int num_polls = history.getPolls().size();
            final int num_pages = (num_polls + CONTESTS_PER_PAGE - 1) / CONTESTS_PER_PAGE;
            int p = 0;  // Page index.  Page number for URLs is one greater than this.
            stamps.add(new NamedStamp("Beginning individual archives pages"));
            for (int e = num_polls - 1; e >= 0; e -= CONTESTS_PER_PAGE) {
                final int poll_end = e;
                final int poll_start = Math.max(poll_end - CONTESTS_PER_PAGE + 1, 0);
                fstream = new FileWriter("web/archives-page" + Integer.toString(p + 1) + ".html");
                out = new BufferedWriter(fstream);
                Master.addFileToBuffer("config/archives_header.txt", out, swaps);
                archivesGenerator.insertNavigationBar(out, history, p + 1, num_pages, CONTESTS_PER_PAGE);
                archivesGenerator.generate(history, out, poll_start, poll_end);
                archivesGenerator.insertNavigationBar(out, history, p + 1, num_pages, CONTESTS_PER_PAGE);
                Master.addFileToBuffer("config/archives_footer.txt", out, swaps);
                out.close();
                ++p;
            }

            // LEADERBOARD
            // Define all boards
            stamps.add(new NamedStamp("Defining leaderboards"));
            FormattedLeaderboard votes_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVotes(), new MemberSortRecent()), "Most votes (all-time)", "Total votes", "", " vote", " Votes");
            FormattedLeaderboard points_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPoints(), new MemberSortRecent()), "Most points (all-time)", "Total points", "", " point", " Points");
            FormattedLeaderboard votes_single_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVotesSingle(), new MemberSortRecent()), "Most votes (single contest, by member)", "Most votes in one contest", "", " votes", " Votes");
            FormattedLeaderboard points_single_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPointsSingle(), new MemberSortRecent()), "Most points (single contest, by member)", "Most points in one contest", "", " points", " Points");
            FormattedLeaderboard victories_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVictories(), new MemberSortRecent()), "Most victories", "Wins", "", " victory", " Wins");
            FormattedLeaderboard entries_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortEntries(), new MemberSortRecent()), "Most participation", "Entries", "", " entry", " Entries");
            FormattedLeaderboard consecutive_strict_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortConsecutiveStrict(), new MemberSortRecent()), "Longest winning streaks (in consecutive contests, by member)", "Most consecutive contests won", "", " streak", " Streak");
            FormattedLeaderboard consecutive_loose_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortConsecutiveLoose(), new MemberSortRecent()), "Longest winning streaks (in consecutive attempts, by member)", "Most consecutive attempts won", "", " streak", " Streak");
            FormattedLeaderboard win_ratio_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortWinRatio(WIN_RATIO_MIN_ENTRIES), new MemberSortRecent()), "Best win ratios (minimum " + WIN_RATIO_MIN_ENTRIES + " entries)", "Win ratio (" + WIN_RATIO_MIN_ENTRIES + "-entry minimum)", "", "", " Ratio");
            FormattedLeaderboard plus_minus_points_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPlusMinusPoints(), new MemberSortRecent()), "Best plus/minus records (head-to-head vote basis)", "Plus/minus votes", "", "", " +/- votes");
            FormattedLeaderboard plus_minus_wins_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPlusMinusHeads(), new MemberSortRecent()), "Best plus/minus records (head-to-head victory basis)", "Plus/minus wins", "", "", " +/- wins");
            FormattedLeaderboard formidable_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortNewFormidable(), new MemberSortRecent()), "Most formidable opponents", "Rating", "", "", " Rating");
            
            // Associate select boards with pages
            stamps.add(new NamedStamp("Claiming leaderboards for individual pages"));
            ArrayList<FormattedLeaderboard> leaderboards_full = new ArrayList<FormattedLeaderboard>();
            leaderboards_full.add(votes_board);
            leaderboards_full.add(points_board);
            leaderboards_full.add(votes_single_board);
            leaderboards_full.add(points_single_board);
            leaderboards_full.add(victories_board);
            leaderboards_full.add(entries_board);
            leaderboards_full.add(consecutive_strict_board);
            leaderboards_full.add(consecutive_loose_board);
            leaderboards_full.add(win_ratio_board);
            leaderboards_full.add(plus_minus_points_board);
            leaderboards_full.add(plus_minus_wins_board);
            leaderboards_full.add(formidable_board);
            
            ArrayList<FormattedLeaderboard> leaderboards_brief = new ArrayList<FormattedLeaderboard>();
            leaderboards_brief.add(votes_board);
            leaderboards_brief.add(votes_single_board);
            leaderboards_brief.add(victories_board);
            leaderboards_brief.add(entries_board);
            leaderboards_brief.add(formidable_board);
            
            // Now begin to write these pages
            stamps.add(new NamedStamp("Writing big leaderboards page"));
            fstream = new FileWriter("web/leaderboards" + testText + ".html");
            out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/leaderboard_header.txt", out, swaps);
            for (int i = 0; i < leaderboards_full.size(); ++i) {
                stamps.add(new NamedStamp("Leaderboard category index " + i));
                leaderboards_full.get(i).addToFile(DELTA, out, true, false, false, i + 1);
            }
            Master.addFileToBuffer("config/leaderboard_footer.txt", out, swaps);
            out.close();
            
            stamps.add(new NamedStamp("Writing leaderboards digest page"));
            fstream = new FileWriter("web/leaderboards-digest" + testText + ".html");
            out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/leaderboard_header.txt", out, swaps);
            for (int i = 0; i < leaderboards_brief.size(); ++i) {
                leaderboards_brief.get(i).addToFile(DELTA, out, false, false, false, i + 1, DIGEST_LIST_LENGTH);
            }
            Master.addFileToBuffer("config/leaderboard_footer.txt", out, swaps);
            out.close();
            
            // MEMBER LIST
            stamps.add(new NamedStamp("Generating member list"));
            fstream = new FileWriter("members/members-" + history.getLastPollName() + ".txt");
            out = new BufferedWriter(fstream);
            ArrayList<Member> member_list = new ArrayList<Member>(history.getMembers());
            Collections.sort(member_list, new MemberSortAlphabetical());
            for (int i=0; i<member_list.size(); i++)
            {
                out.write(member_list.get(i).getMostRecentName());
                if (member_list.get(i).hasTag()) {
                    out.write(" [" + member_list.get(i).getTag() + "]");
                }
                out.newLine();
            }
            out.newLine();
            out.write(member_list.size() + " unique members in all.");
            out.close();
            
            // PROFILES
            stamps.add(new NamedStamp("Generating user profiles"));
            if (generate_user_galleries) {
                ArrayList<Member> mems = member_list;
                for (int i=0; i<mems.size(); i++) {
                    UserProfile.createProfilePage(mems.get(i), false, leaderboards_full);
                }
            }

            stamps.add(new NamedStamp("Pretty much done!"));
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            JOptionPane.showMessageDialog(null, "Archive output saved to archives\\archive-" + history.getLastPollName() + testText + ".html.\nLeaderboard output saved to leaderboards\\leaderboards-" + history.getLastPollName() + testText + ".html.\nTime elapsed: " + ((double)(duration))/1000000000.0 + " seconds.", "Success!", JOptionPane.INFORMATION_MESSAGE);
            
            if (!stamps.isEmpty()) {
                long start = stamps.get(0).getStamp();
                System.out.println("0\tN/A\t" + stamps.get(0).getName());
                for (int i = 1; i < stamps.size(); ++i) {
                    System.out.println("" + (stamps.get(i).getStamp() - start) + "\t" + (stamps.get(i).getStamp() - stamps.get(i-1).getStamp()) + "\t" + stamps.get(i).getName());
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "A strange problem occurred. Talk to nicklegends about it.\n\"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
    
        // Borrowed from http://stackoverflow.com/questions/27379059/determine-if-two-files-store-the-same-content
    public static boolean fileEquals(File file1, File file2) {
        byte[] f1;
        byte[] f2;
        try {
            f1 = Files.readAllBytes(file1.toPath());
            f2 = Files.readAllBytes(file2.toPath());
        } catch(Exception e) {
            return false;
        }
        return Arrays.equals(f1, f2);
    }
}