import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;
import java.nio.file.Files;

abstract class UserProfile
{
    public static final String TEMP_PATH = "temp.txt";

    public static void createProfilePage(Member mem, History history, EloEvaluator elo_evaluator, boolean explicit, ArrayList<FormattedLeaderboard> stats, String input_origin, String path)
    {
        String recent_name = mem.getMostRecentName();
        //String safe_name = getSafeName(recent_name);
        String[][] swaps = new String[][] {{"###","test"}, {"#NAME", recent_name}};
        //String[][] swaps = new String[][] {{}};
        
        String initial_target;
        if (explicit) {
            initial_target = path;
        } else {
            initial_target = TEMP_PATH;
        }
        
        try
        {
            FileOutputStream fstream = new FileOutputStream(initial_target);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fstream, StandardCharsets.UTF_8));
            Master.addFileToBuffer(input_origin + "config/profile_header.txt", out, swaps);
            
            ArrayList<String> unique_names = mem.getUniqueNames();
            if (unique_names.size() > 1) {
                out.write("<div class='former-names'>Other names used: ");
                ListIterator<String> li = unique_names.listIterator(unique_names.size());
                boolean first_written = false;
                while(li.hasPrevious()) {
                    String unique_name = li.previous();
                    if (!unique_name.equals(mem.getMostRecentName())) {
                        if (first_written) {
                            out.write(", ");
                        }
                        out.write(unique_name);
                        first_written = true;
                    }
                }
                out.write("</div>");
            }
            
            addStatsTableToFile(mem, stats, true, true, out);
            
            ArchivesGenerator archives_generator = new ArchivesGenerator();
            archives_generator.generate(history, elo_evaluator, out, new ShowMember(mem), new HighlightMember(mem), "../images");
            
//             out.write("<div class='picture-large-list'>");
//             out.newLine();
//             
//             ArrayList<Member.EntryStakePair> pairs = mem.getEntries();
//             // Note: this assumes that the entries have been ordered chronologically
//             for (int i = pairs.size()-1; i >= 0; i--) {
//                 Entry ent = pairs.get(i).entry;
//                 Poll poll = ent.getPoll();
//                 
//                 out.write("<div class='picture-large-div'>");
//                 if (ent.hasURL()) {
//                     out.write("<img class='picture-large' title='" + ent.getPoll().getName() + "' src='" + ent.getURL() + "'/>");
//                 } else {
//                     out.write("<img class='picture-large' title='" + ent.getPoll().getName() + "' src='../images/no_image.png'/>");
//                 }
//                 out.write("<div class='picture-large-caption'>");
//                 if (poll.hasTopic()) {
//                     out.write("<a class='alt' href='" + poll.getURL() + "'>#" + poll.getName() + "</a>");
//                 } else {
//                     out.write("#" + poll.getName());
//                 }
//                 out.write("</div></div>");
//                 out.newLine();
//             }
//  
//             out.write("</div>");
//             out.newLine();
            
            Master.addFileToBuffer(input_origin + "config/profile_footer.txt", out, swaps);
            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Error caught in UserProfile: " + e.getMessage());
            //JOptionPane.showMessageDialog(null, "User profile could not be generated. Talk to nicklegends about it.\n\"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if (explicit) {
            return;
        } else {
            File temp_file = new File(TEMP_PATH);
            File profile_file = new File(path);
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
    
    // I don't remember why I have this method.
    public static String getProfileUrl(Member mem)
    {
        String body = getSafeName(mem.getMostRecentName());
        if (mem.hasTag()) {
            body += ("-" + mem.getTag());
        }
        return "profiles/" + body + ".html";
    }
    
    public static void addStatsTableToFile(Member member, ArrayList<FormattedLeaderboard> stats, boolean details, boolean links_in_details, BufferedWriter out) {
        try {
            out.write("<div class='member-details-div'><table class='member-details-table'><tr class='member-details-header-row'><td colspan='");
            out.write(details ? "4" : "3");
            out.write("'>" + member.getMostRecentName() + "&rsquo;s stats</td></tr>");
            out.newLine();
            out.write("<tr class='member-details-subheader-row'><td class='member-details-subheader-cell'>Category</td><td class='member-details-subheader-cell'>Value</td><td class='member-details-subheader-cell'>Rank</td><td class='member-details-subheader-cell'>Details</td></tr>");
            out.newLine();
        
            for (FormattedLeaderboard stat : stats) {
                Leaderboard leaderboard = stat.getLeaderboard();
                MemberDataRetriever metric = leaderboard.getMetric();
                History history = leaderboard.getHistory();
                out.write("<tr class='member-details-row'><td class='member-details-cell category-cell'>" + stat.getContextlessTitle() + "</td>");
                if (metric.qualifies(member)) {
                    out.write("<td class='member-details-cell value-cell'>" + metric.getData(member) + "</td>");
                    out.write("<td class='member-details-cell rank-cell'>");
                    int rank = leaderboard.getPlaceOfMember(member.getId());  // 0-indexed!!
                    out.write(leaderboard.getMembersAtPlace(rank).size() > 1 ? "t&#8209;": "");  // Non-breaking hyphen
                    out.write("" + (rank + 1) + "&nbsp;<span class='num-qualifiers'>/&nbsp;" + leaderboard.countQualifiers() + "</span></td>");
                } else {
                    out.write("<td class='member-details-cell value-cell'>N/A</td>");
                    out.write("<td class='member-details-cell rank-cell'>&#8210;&nbsp;<span class='num-qualifiers'>/&nbsp;" + leaderboard.countQualifiers() + "</span></td>");
                }
                out.write("<td class='member-details-cell details'>" + metric.getDetails(member, true) + "</td></tr>");
                out.newLine();
            }
            
            out.write("</table></div>");
            out.newLine();
        } catch (Exception e) {
            System.err.println("Error caught in UserProfile: " + e.getMessage());
        }
    }
}
