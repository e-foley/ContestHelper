import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Write a description of class UserProfile here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
abstract class UserProfile
{
    public static final String TEMP_PATH = "temp.txt";

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public static void createProfilePage(Member mem, boolean explicit)
    {
        String recent_name = mem.getMostRecentName();
        //String safe_name = getSafeName(recent_name);
        String[][] swaps = new String[][] {{"###","test"}, {"#NAME", recent_name}};
        //String[][] swaps = new String[][] {{}};
        
        String initial_target;
        if (explicit) {
            initial_target = getProfileURL(mem);
        } else {
            initial_target = TEMP_PATH;
        }
        
        try
        {
            System.out.println("Attempting to write file " + getProfileURL(mem) + "...");
            FileWriter fstream = new FileWriter(initial_target);
            BufferedWriter out = new BufferedWriter(fstream);
            Master.addFileToBuffer("config/profile_header.txt", out, swaps);
            
            out.write("<div class='picture-large-list'>");
            
            ArrayList<Entry> entries = mem.getEntries();
            // Note: this assumes that the entries have been ordered chronologically
            for (int i = entries.size()-1; i >= 0; i--) {
                Entry ent = entries.get(i);
                Poll poll = ent.getPoll();
                
                out.write("<div class='picture-large-div'>");
                if (ent.hasURL()) {
                    out.write("<img class='picture-large' title='" + ent.getPoll().getName() + "' src='" + ent.getURL() + "'/>");
                } else {
                    out.write("<img class='picture-large' title='" + ent.getPoll().getName() + "' src='../images/no_image.png'/>");
                }
                out.write("<div class='picture-large-caption'>");
                if (poll.hasTopic()) {
                    out.write("<a class='alt' href='" + poll.getURL() + "'>" + poll.getName() + "</a>");
                } else {
                    out.write(poll.getName());
                }
                out.write("</div></div>");
            }
            
            out.write("</div>");
            
            Master.addFileToBuffer("config/profile_footer.txt", out, swaps);
            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            //JOptionPane.showMessageDialog(null, "User profile could not be generated. Talk to nicklegends about it.\n\"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if (explicit) {
            return;
        } else {
            File temp_file = new File(TEMP_PATH);
            File profile_file = new File(getProfileURL(mem));
            if (Master.fileEquals(temp_file, profile_file)) {
                try {
                    Files.deleteIfExists(temp_file.toPath());
                } catch (Exception e) {}
                return;
            } else {
                try {
                    Master.copyFile(temp_file, profile_file);
                } catch (Exception e) {
                    System.err.println("Could not copy file...");
                }
            }
        }
    }
    
    public static String getSafeName(String orig)
    {
        return orig.replaceAll("[^a-zA-Z0-9]", "");
    }
    
    public static String getProfileURL(Member mem)
    {
        return "web/profiles/" + getSafeName(mem.getMostRecentName()) + ".html";
    }
    
    // I don't remember why I have this method.
    public static String getProfileDropboxURL(Member mem)
    {
        return "profiles/" + getSafeName(mem.getMostRecentName()) + ".html";
        // return "http://sotw.elfractal.com/profiles/" + getSafeName(mem.getMostRecentName()) + ".html";
    }
    
    public static void startMemberDetailsTable(Member member, boolean details, BufferedWriter out) {
        try {
            out.write("<table class='member-details-table'><tr class='member-details-header-row'><td colspan='");
            out.write(details ? "4" : "3");
            out.write("'>" + member.getMostRecentName() + "&rsquo;s stats</td></tr>\n");
            out.write("<tr class='member-details-subheader-row'><td class='member-details-subheader-cell'>Category</td><td class='member-details-subheader-cell'>Value</td><td class='member-details-subheader-cell'>Rank</td><td class='member-details-subheader-cell'>Details</td></tr>\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public static void endMemberDetailsTable(BufferedWriter out) {
        try {
            out.write("</table>\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
        
    // TODO: Order tied members by most recent entry date (or any other metric we want to use)
    public static void addMemberDetailsRow(FormattedLeaderboard formatted, Member member, BufferedWriter out, String title, boolean details, boolean linksInDetails) {
        Leaderboard leaderboard = formatted.getLeaderboard();
        MemberDataRetriever metric = leaderboard.getMetric();
        History history = leaderboard.getHistory();
        
        try
        {
            out.write("<tr class='member-details-row'><td class='member-details-cell'>" + title + "</td>");
            out.write("<td class='member-details-cell'>" + metric.getData(member) + "</td>");
            out.write("<td class='member-details-cell'>" + leaderboard.getPlaceOfMember(member.getId()) + "/" + leaderboard.countQualifiers() + "</td>");
            out.write("<td class='member-details-cell details'>" + metric.getDetails(member, true) + "</td></tr>/n");
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
