import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public abstract class Main
{
    public static final int PAGE_LENGTH = 50;
    public static final int STARTING_INDEX = 1;
    public static final int DIGEST_LIST_LENGTH = 10;  // Number of members to list in "digest" version of the leaderboards
    public static final int NUM_ARCHIVES_DIGEST_ENTRIES = 12;
    public static final int CONTESTS_PER_PAGE = 12;
    public static final int DELTA = 10;
    public static final int WIN_RATIO_MIN_ENTRIES = 5;
    public static final boolean OVERWRITE_IDENTICAL_PROFILES = true;
    // Note: Normal Elo is 1000, 10, 400, 20
    public static final double ELO_STARTING_RATING = 1500.0f;
    public static final double ELO_BASE = 2.0;
    public static final double ELO_DIVISOR = 500.0;
    public static final double ELO_AGGRESSIVENESS = 80.0;
    public static final double ELO_STARTING_BOOST = 1.0;  // Note that boost changes this from a zero-sum algorithm to something else. ({3.0, 0.5} seems about right.)
    public static final double ELO_BOOST_DECAY = 0.0;
    
    // arg0 is path to config dir (within which one would expect to see template webpages).
    // arg1 is path to input dir (within which one would expect to see data.txt and associations.txt).
    // arg2 is path to output dir (where webpages will be placed).
    public static void main(String[] args)
    {
        if (args.length < 3 || args.length > 3) {
            System.err.println("Expected exactly 3 arguments: config path, input path, output path.");
            return;
        }
        
        String config_origin = args[0] + "/";
        String input_origin = args[1] + "/";
        String output_origin = args[2] + "/";
        
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
        if (!history.populateMembersFromFile(input_origin + "associations.txt"))
        {
            System.err.println("Error while parsing associations.txt.  Perhaps data aren't delimited correctly.");
            return;
        }
        
        stamps.add(new NamedStamp("Populating entries"));
        if (!history.populateEntriesFromFile(input_origin + "data.txt"))
        {
            System.err.println("Error while parsing data.txt.  Perhaps data aren't delimited correctly or a numeric value is misplaced.");
            return;
        }
        
        // Calculate ratings
        // TODO: The EloEvaluator is like a parallel array right now... See if we can get this info inside history, or something clever...
        EloEvaluator elo_evaluator = new EloEvaluator(ELO_STARTING_RATING, ELO_BASE, ELO_DIVISOR, ELO_AGGRESSIVENESS, ELO_STARTING_BOOST, ELO_BOOST_DECAY);
        
        // Shouldn't be necessary to evaluate history here anymore since evaluation *should* happen as part of Leaderboard's call to metric's precalculate...
        // BUT, if we calculate here, we don't have to re-calculate for every member profile page that gets passed this elo_evaluator object.
        elo_evaluator.evaluate(history);
        
        FileOutputStream fstream;
        BufferedWriter out;
        
        String[][] swaps = new String[][] {{"###",""+history.getLastPollLongName()}};
        
        try
        {
            stamps.add(new NamedStamp("Copying input files"));
            // Create cache directory if it doesn't exist.
            new File(input_origin + "cache").mkdirs();
            // Cache input files. (The resulting files can serve as backups if something goes wrong.)
            Main.copyFile(new File(input_origin + "data.txt"), new File(input_origin + "cache/data-" + history.getLastPollShortName() + ".txt"));
            Main.copyFile(new File(input_origin + "associations.txt"), new File(input_origin + "cache/associations-" + history.getLastPollShortName() + ".txt"));
            
            // ARCHIVES
            stamps.add(new NamedStamp("Preparing archive generation"));
            fstream = new FileOutputStream(output_origin + "archives" + testText + ".html");
            out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            
            addFileToBuffer(config_origin + "archives_header.txt", out, swaps);
            stamps.add(new NamedStamp("Generating archives"));
            archivesGenerator.generate(history, elo_evaluator, out, "profiles/", "images/");
            stamps.add(new NamedStamp("Writing archives"));
            addFileToBuffer(config_origin + "archives_footer.txt", out, swaps);
            out.close();
            stamps.add(new NamedStamp("Done writing archives"));
            
            final int num_polls = history.getPolls().size();
            final int num_pages = (num_polls + CONTESTS_PER_PAGE - 1) / CONTESTS_PER_PAGE;
            int p = 0;  // Page index.  Page number for URLs is one greater than this.
            stamps.add(new NamedStamp("Beginning individual archives pages"));
            for (int e = num_polls - 1; e >= 0; e -= CONTESTS_PER_PAGE) {
                final int poll_end = e;
                final int poll_start = Math.max(poll_end - CONTESTS_PER_PAGE + 1, 0);
                fstream = new FileOutputStream(output_origin + "archives-page" + Integer.toString(p + 1) + ".html");
                out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
                addFileToBuffer(config_origin + "archives_header.txt", out, swaps);
                archivesGenerator.insertNavigationBar(out, history, p + 1, num_pages, CONTESTS_PER_PAGE);
                archivesGenerator.generate(history, elo_evaluator, out, poll_start, poll_end, "profiles/", "images");
                archivesGenerator.insertNavigationBar(out, history, p + 1, num_pages, CONTESTS_PER_PAGE);
                addFileToBuffer(config_origin + "archives_footer.txt", out, swaps);
                out.close();
                ++p;
            }

            // LEADERBOARD
            // Define all boards
            stamps.add(new NamedStamp("Defining leaderboards"));
            //FormattedLeaderboard weighted_formidable_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortWeightedFormidable(), new MemberSortRecent()), "Most formidable opponents", "Rating", "", "", " Rating");
            FormattedLeaderboard elo_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortElo(elo_evaluator), new MemberSortRecent()), "Most formidable opponents [v" + EloEvaluator.VERSION_STRING + "]", "Rating", "", "", " Rating");
            FormattedLeaderboard peak_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPeakElo(elo_evaluator), new MemberSortRecent()), "Peak rating (following initial gain/loss chain)", "Peak rating", "", "", " Rating");
            FormattedLeaderboard nadir_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortValleyElo(elo_evaluator), new MemberSortRecent()), "Rating nadir (following initial gain/loss chain)", "Rating nadir", "", "", " Rating");
            FormattedLeaderboard votes_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVotes(), new MemberSortRecent()), "Most votes (all-time)", "Total votes", "", " vote", " Votes");
            FormattedLeaderboard votes_single_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVotesSingle(), new MemberSortRecent()), "Most votes (single contest, by member)", "Most votes in one contest", "", " votes", " Votes");
            FormattedLeaderboard victories_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortVictories(), new MemberSortRecent()), "Most victories", "Wins", "", " victory", " Wins");
            FormattedLeaderboard entries_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortEntries(), new MemberSortRecent()), "Most participation", "Entries", "", " entry", " Entries");
            FormattedLeaderboard entry_streak_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortEntryStreak(), new MemberSortRecent()), "Longest participation streaks", "Most consecutive contests entered", "", " entry", " Entries");
            FormattedLeaderboard consecutive_strict_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortConsecutiveStrict(), new MemberSortRecent()), "Longest winning streaks (in consecutive contests, by member)", "Most consecutive contests won", "", " streak", " Streak");
            FormattedLeaderboard consecutive_loose_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortConsecutiveLoose(), new MemberSortRecent()), "Longest winning streaks (in consecutive attempts, by member)", "Most consecutive attempts won", "", " streak", " Streak");
            FormattedLeaderboard win_ratio_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortWinRatio(WIN_RATIO_MIN_ENTRIES), new MemberSortRecent()), "Best win ratios (minimum " + WIN_RATIO_MIN_ENTRIES + " entries)", "Win ratio (" + WIN_RATIO_MIN_ENTRIES + "-entry minimum)", "", "", " Ratio");
            FormattedLeaderboard plus_minus_points_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPlusMinusPoints(), new MemberSortRecent()), "Best plus/minus records (head-to-head vote basis)", "Plus/minus votes", "", "", " +/- votes");
            FormattedLeaderboard plus_minus_wins_board = new FormattedLeaderboard(new Leaderboard(history, new MemberSortPlusMinusHeads(), new MemberSortRecent()), "Best plus/minus records (head-to-head victory basis)", "Plus/minus wins", "", "", " +/- wins");
            
            // Associate select boards with pages
            stamps.add(new NamedStamp("Claiming leaderboards for individual pages"));
            ArrayList<FormattedLeaderboard> leaderboards_full = new ArrayList<FormattedLeaderboard>();
            leaderboards_full.add(elo_board);
            leaderboards_full.add(peak_board);
            //leaderboards_full.add(nadir_board);
            leaderboards_full.add(votes_board);
            leaderboards_full.add(votes_single_board);
            leaderboards_full.add(victories_board);
            leaderboards_full.add(entries_board);
            leaderboards_full.add(entry_streak_board);
            leaderboards_full.add(consecutive_strict_board);
            leaderboards_full.add(consecutive_loose_board);
            leaderboards_full.add(win_ratio_board);
            leaderboards_full.add(plus_minus_points_board);
            leaderboards_full.add(plus_minus_wins_board);
            
            ArrayList<FormattedLeaderboard> leaderboards_member = new ArrayList<FormattedLeaderboard>();
            leaderboards_member.add(elo_board);
            leaderboards_member.add(peak_board);
            //leaderboards_member.add(nadir_board);
            leaderboards_member.add(votes_board);
            leaderboards_member.add(votes_single_board);
            leaderboards_member.add(victories_board);
            leaderboards_member.add(entries_board);
            leaderboards_member.add(entry_streak_board);
            leaderboards_member.add(consecutive_strict_board);
            leaderboards_member.add(consecutive_loose_board);
            leaderboards_member.add(win_ratio_board);
            leaderboards_member.add(plus_minus_points_board);
            leaderboards_member.add(plus_minus_wins_board);
            
            ArrayList<FormattedLeaderboard> leaderboards_brief = new ArrayList<FormattedLeaderboard>();
            leaderboards_brief.add(elo_board);
            leaderboards_brief.add(votes_board);
            leaderboards_brief.add(votes_single_board);
            leaderboards_brief.add(victories_board);
            leaderboards_brief.add(entries_board);
            
            // Now begin to write these pages
            stamps.add(new NamedStamp("Writing big leaderboards page"));
            fstream = new FileOutputStream(output_origin + "leaderboards" + testText + ".html");
            out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            addFileToBuffer(config_origin + "leaderboard_header.txt", out, swaps);
            for (int i = 0; i < leaderboards_full.size(); ++i) {
                stamps.add(new NamedStamp("Leaderboard: " + leaderboards_full.get(i).getTitle()));
                leaderboards_full.get(i).addToFile(DELTA, out, true, false, false, i + 1);
            }
            addFileToBuffer(config_origin + "leaderboard_footer.txt", out, swaps);
            out.close();
            
            stamps.add(new NamedStamp("Writing leaderboards digest page"));
            fstream = new FileOutputStream(output_origin + "leaderboards-digest" + testText + ".html");
            out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            addFileToBuffer(config_origin + "leaderboard_header.txt", out, swaps);
            for (int i = 0; i < leaderboards_brief.size(); ++i) {
                leaderboards_brief.get(i).addToFile(DELTA, out, false, false, false, i + 1, DIGEST_LIST_LENGTH);
            }
            addFileToBuffer(config_origin + "leaderboard_footer.txt", out, swaps);
            out.close();
            
            // PROFILE INDEX
            stamps.add(new NamedStamp("Generating profile index"));
            fstream = new FileOutputStream(output_origin + "profile_index.html");
            out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            addFileToBuffer(config_origin + "profile_index_header.txt", out, swaps);
            ProfileIndexGenerator.generate(history, out);
            addFileToBuffer(config_origin + "profile_index_footer.txt", out, swaps);
            out.close();
            
            // RANDOM SHOT SCRIPT
            stamps.add(new NamedStamp("Generating random shot script"));
            fstream = new FileOutputStream(output_origin + "random_shot.js");
            out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            RandomShotScriptGenerator.generate(history, out);
            out.close();
            
            // RANDOM SHOT PAGE
            stamps.add(new NamedStamp("Generating random shot page"));
            copyFile(new File(config_origin + "random_shot.html"), new File(output_origin + "random_shot.html"));
            out.close();
            
            // PROFILES
            stamps.add(new NamedStamp("Generating user profiles"));
            if (generate_user_galleries) {
                ArrayList<Member> mems = new ArrayList<Member>(history.getMembers());
                for (int i=0; i<mems.size(); i++) {
                    System.out.println("Attempting to write file " + output_origin + getProfilePath(mems.get(i)) + "...");
                    UserProfile.createProfilePage(mems.get(i), history, elo_evaluator, OVERWRITE_IDENTICAL_PROFILES, leaderboards_member, config_origin, output_origin + getProfilePath(mems.get(i)));
                }
            }

            stamps.add(new NamedStamp("Pretty much done!"));
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("Archive output saved to archives\\archive-" + history.getLastPollShortName() + testText + ".html.");
            System.out.println("Leaderboard output saved to leaderboards\\leaderboards-" + history.getLastPollShortName() + testText + ".html.");
            System.out.println("Time elapsed: " + ((double)(duration))/1000000000.0 + " seconds.");
            
            if (!stamps.isEmpty()) {
                long start = stamps.get(0).getStamp();
                for (int i = 0; i < stamps.size(); ++i) {
                    if (i < stamps.size() - 1) {
                        System.out.println("" + (stamps.get(i).getStamp() - start) + "\t" + (stamps.get(i+1).getStamp() - stamps.get(i).getStamp()) + "\t" + stamps.get(i).getName());
                    } else {
                        System.out.println("" + (stamps.get(i).getStamp() - start) + "\tN/A\t" + stamps.get(i).getName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Error caught in Main: " + e.getMessage());
            System.err.println("A strange problem occurred.  Talk to nicklegends about it.");
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
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)); 
            
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
            System.err.println("Error caught in Main: " + e.getMessage());
            System.err.println("Error adding " + filename + " to buffer. Is it missing?");
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
    
    // TODO: Move getProfileUrl out of UserProfile.
    public static String getProfilePath(Member mem) {
        return "profiles/" + UserProfile.getProfileUrl(mem);
    }
}