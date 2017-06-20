import java.io.*;
import java.util.ArrayList;

public class LeaderboardFormatter {
    
    public LeaderboardFormatter() {}

    // NOTE: `prefix` is not presently used
    public void addToFile(Leaderboard leaderboard, int delta, String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID) {
        addToFile(leaderboard, delta, title, prefix, suffixSingular, suffixPlural, out, hidden, details, linksInDetails, ID, Integer.MAX_VALUE);
    }
    
    // TODO: Order tied members by most recent entry date (or any other metric we want to use)
    public void addToFile(Leaderboard leaderboard, int delta, String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID, int limit) {
        ArrayList<Member> members = leaderboard.getMembers();
        MemberDataRetriever metric = leaderboard.getMetric();
        History history = leaderboard.getHistory();
        int subhistory_start = 0;
        int subhistory_end = Math.max(0, history.getPolls().size() - delta - 1);
        Leaderboard comparison = new Leaderboard(history.getSubhistory(subhistory_start, subhistory_end), metric);
        
        try
        {
            out.newLine();
            
            if (hidden)
                out.write("<div class='leaderboard' style='display: show;' id='" + ID + "_closed'>");
            else
                out.write("<div class='leaderboard' style='display: none;' id='" + ID + "_closed'>");
            out.write("<table class='leaderboard-table stunt'><tr class='sort-title'><td>");
            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 1);'><img src='https://dl.dropboxusercontent.com/u/10663130/PureZC/exp_plus.png' border='0'  alt='Expand' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 1);'>" + title + "</a></td></tr></table></div>");
            
            if (hidden)
                out.write("<div class='leaderboard' style='display: none;' id='" + ID + "_open'>");
            else
                out.write("<div class='leaderboard' style='display: show;' id='" + ID + "_open'>");
            
            out.write("<table class='leaderboard-table'><tr class='sort-title'><td colspan=");
            
            if (details)
                out.write("4>");
            else
                out.write("3>");

            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 0);'><img src='https://dl.dropboxusercontent.com/u/10663130/PureZC/exp_minus.png' border='0'  alt='Collapse' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 0);'>" + title + "</a></td></tr>");
            out.newLine();
            out.write("<tr class='header-row'><td>Rank</td>");
            out.write("<td>Name</td><td>" + suffixPlural + "</td>");
            
            if (details)
                out.write("<td>Details</td>");
                
            out.write("</tr>");
            
            //NOTE: Deciding places really shouldn't be the responsibility of this section... Oh well.
            int p = 0;
            while (p < Math.min(members.size(), limit)) {
                ArrayList<Member> coplacers = leaderboard.getMembersAtPlace(p);
                for (int c = 0; c < coplacers.size(); ++c) {
                    Member member = coplacers.get(c);
                    
                    out.newLine();
                    out.write("<tr class='");
    
                    switch(p + 1) {
                        case 1:
                            out.write("performance first");
                            break;
                        case 2:
                            out.write("performance second");
                            break;
                        case 3:
                            out.write("performance third");
                            break;
                        case 4:
                            out.write("performance fourth");
                            break;
                        default:
                            out.write("performance");
                    }
                    
                    out.write("'>");
                        
                    // Place column
                    out.write("<td class='place-cell'>");
                    if (coplacers.size() > 1) {
                        out.write("T&#8209;");  // &#8209 is non-breaking hyphen
                    }
                    out.write(Integer.toString(p + 1));
                    // Figure out if the member has been newly added to the leaderboard by checking if their earliest entry occured after our subhistory end cut-off
                    // TODO: This is a terribly indirect method that would benefit greatly if contests were stored by ID and held their own ID
                    boolean is_member_new = (member.getEntries().isEmpty() || leaderboard.getHistory().getPolls().indexOf(member.getEntries().get(0).getPoll()) > subhistory_end);
                    if (is_member_new) {
                        out.write("&nbsp;<span class='place-delta'>(<span class='new-text'>NEW</span>)</span>");
                    } else {
                        int comparison_place = comparison.getPlaceOfMember(member.getId());
                        if (comparison_place != Leaderboard.NO_PLACE) {
                            int gain = (comparison_place + 1) - (p + 1);  // Written this way for consistency.  Note that gain is good (lower place).
                            if (gain > 0) {
                                out.write("&nbsp;<span class='place-delta'>(<span class='gain-arrow'>&#9650;</span>" + gain + ")</span>");
                            } else if (gain < 0) {
                                out.write("&nbsp;<span class='place-delta'>(<span class='loss-arrow'>&#9660;</span>" + (-gain) + ")</span>");
                            } else {
                                // out.write("(<span class='same-arrow'>&#177;</span>0)");
                            }
                        }
                    }
                    out.write("</td>");
                    
                    // Name column
                    out.write("<td class='name-cell'>");
                    out.write("<a class='black' href='" + UserProfile.getProfileDropboxURL(member) + "'>");
                    out.write(member.getMostRecentName() + " (" + member.getId() + ")");
                    out.write("</a>");
                    out.write("</td>");
                    
                    // Data column
                    out.write("<td class='number-cell'>");
                    out.write(metric.getData(member));
                    out.write("</td>");

                    // Details column
                    if (details) {
                        out.write("<td class='details'>");
                        out.write(metric.getDetails(member, linksInDetails));
                        out.write("</td>");
                    }
                    out.write("</tr>");
                }
                
                p += coplacers.size();
            }
            
            out.write("</table>");
            out.write("</div>"); // end leaderboard-div
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
