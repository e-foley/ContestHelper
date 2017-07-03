import java.io.*;
import java.util.ArrayList;

public class FormattedLeaderboard {
    public static final boolean SHOW_MEMBER_IDS = false;
    
    private Leaderboard leaderboard_;
    private String title_;
    private String prefix_;
    private String suffix_singular_;
    private String suffix_plural_;
    
    // NOTE: `prefix` is not presently used
    public FormattedLeaderboard(Leaderboard leaderboard, String title, String prefix, String suffixSingular, String suffixPlural) {
        leaderboard_ = leaderboard;
        title_ = title;
        prefix_ = prefix;
        suffix_singular_ = suffixSingular;
        suffix_plural_ = suffixPlural;
    }
    
    public Leaderboard getLeaderboard() {
        return leaderboard_;
    }
    
    public String getTitle() {
        return title_;
    }
    
    public String getPrefix() {
        return prefix_;
    }
    
    public String getSuffixSingular() {
        return suffix_singular_;
    }
    
    public String getSuffixPlural() {
        return suffix_plural_;
    }

    // NOTE: `prefix` is not presently used
    public void addToFile(int delta, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID) {
        addToFile(delta, out, hidden, details, linksInDetails, ID, Integer.MAX_VALUE);
    }
    
    // TODO: Order tied members by most recent entry date (or any other metric we want to use)
    public void addToFile(int delta, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID, int limit) {
        ArrayList<Member> members = leaderboard_.getMembers();
        MemberDataRetriever metric = leaderboard_.getMetric();
        History history = leaderboard_.getHistory();
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
            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 1);'><img src='images/exp_plus.png' border='0'  alt='Expand' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 1);'>" + title_ + "</a></td></tr></table></div>");
            
            if (hidden)
                out.write("<div class='leaderboard' style='display: none;' id='" + ID + "_open'>");
            else
                out.write("<div class='leaderboard' style='display: show;' id='" + ID + "_open'>");
            
            out.write("<table class='leaderboard-table'><tr class='sort-title'><td colspan=");
            
            if (details)
                out.write("6>");
            else
                out.write("5>");

            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 0);'><img src='images/exp_minus.png' border='0'  alt='Collapse' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 0);'>" + title_ + "</a></td></tr>");
            out.newLine();
            out.write("<tr class='header-row'><td>Rank</td><td>&#177;</td>");
            out.write("<td>Name</td><td>" + suffix_plural_ + "</td><td>&#177;</td>");
            
            if (details)
                out.write("<td>Details</td>");
                
            out.write("</tr>");
            
            //NOTE: Deciding places really shouldn't be the responsibility of this section... Oh well.
            int p = 0;
            while (p < Math.min(members.size(), limit)) {
                ArrayList<Member> coplacers = leaderboard_.getMembersAtPlace(p);
                for (int c = 0; c < coplacers.size(); ++c) {
                    Member member = coplacers.get(c);
                    
                    // Don't print if member is unqualified
                    if (!leaderboard_.getMetric().qualifies(member)) {
                        continue;
                    }
                    
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
//                     if (coplacers.size() > 1) {
//                         out.write("T&#8209;");  // &#8209 is non-breaking hyphen
//                     }
                    if (c == 0) {
                        out.write(Integer.toString(p + 1) + ".");
                    }
                    out.write("</td>");
                    
                    // Delta place column
                    out.write("<td class='delta-cell'>");
                    // Figure out if the member has been newly added to the leaderboard by checking if their earliest entry occured after our subhistory end cut-off
                    // TODO: This is a terribly indirect method that would benefit greatly if contests were stored by ID and held their own ID
                    boolean is_member_new = (member.getEntries().isEmpty() ||
                                             leaderboard_.getHistory().getPolls().indexOf(member.getEntries().get(0).getPoll()) > subhistory_end ||
                                             comparison.getPlaceOfMember(member.getId()) == Leaderboard.NO_PLACE);
                    if (is_member_new) {
                        out.write("<span class='place-delta'><span class='new-text'>NEW</span></span>");
                    } else {
                        int comparison_place = comparison.getPlaceOfMember(member.getId());
                        if (comparison_place != Leaderboard.NO_PLACE) {
                            int gain = (comparison_place + 1) - (p + 1);  // Written this way for consistency.  Note that gain is good (lower place).
                            if (gain > 0) {
                                out.write("<span class='place-delta'><span class='gain-arrow'>&#9650;</span>" + gain + "</span>");
                            } else if (gain < 0) {
                                out.write("<span class='place-delta'><span class='loss-arrow'>&#9660;</span>" + (-gain) + "</span>");
                            } else {
                                // out.write("<span class='place-delta'><span class='same-arrow'>&#9671;</span></span>");
                            }
                        }
                    }
                    out.write("</td>");
                    
                    // Name column
                    out.write("<td class='name-cell'>");
                    out.write("<a class='black' href='" + UserProfile.getProfileDropboxURL(member) + "'>");
                    out.write(member.getMostRecentName() + (SHOW_MEMBER_IDS ? " (" + member.getId() + ")" : ""));
                    out.write("</a>");
                    out.write("</td>");
                    
                    // Data column
                    out.write("<td class='number-cell'>");
                    out.write(metric.getData(member));
                    out.write("</td>");

                    // Data delta column
                    out.write("<td class='data-delta-cell'>");
                    // Only write a delta if the member existed before.
                    if (comparison.getHistory().getMemberMap().containsKey(member.getId()) &&
                        comparison.getHistory().getMemberMap().get(member.getId()).getTotalEntries() > 0 &&
                        comparison.getPlaceOfMember(member.getId()) != Leaderboard.NO_PLACE) {
                        // Record old and new values for future use...
                        float old_value = metric.getValue(comparison.getHistory().getMemberById(member.getId()));
                        float new_value = metric.getValue(member);
                        if (new_value == old_value) {
                            // Nothing?
                        } else if (new_value > old_value) {
                            out.write("<span class='good-delta'>+" + metric.getFormat().format(new_value - old_value) + "</span>");
                        } else {
                            out.write("<span class='bad-delta'>" + metric.getFormat().format(new_value - old_value) + "</span>");
                        }
                    }
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
