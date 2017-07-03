import java.io.*;
import java.util.ArrayList;

public class FormattedLeaderboard {
    public static final boolean SHOW_MEMBER_IDS = false;
    
    public FormattedLeaderboard() {}

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
            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 1);'><img src='images/exp_plus.png' border='0'  alt='Expand' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 1);'>" + title + "</a></td></tr></table></div>");
            
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
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 0);'>" + title + "</a></td></tr>");
            out.newLine();
            out.write("<tr class='header-row'><td>Rank</td><td>&#177;</td>");
            out.write("<td>Name</td><td>" + suffixPlural + "</td><td>&#177;</td>");
            
            if (details)
                out.write("<td>Details</td>");
                
            out.write("</tr>");
            
            //NOTE: Deciding places really shouldn't be the responsibility of this section... Oh well.
            int p = 0;
            while (p < Math.min(members.size(), limit)) {
                ArrayList<Member> coplacers = leaderboard.getMembersAtPlace(p);
                for (int c = 0; c < coplacers.size(); ++c) {
                    Member member = coplacers.get(c);
                    
                    // Don't print if member is unqualified
                    if (!leaderboard.getMetric().qualifies(member)) {
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
                                             leaderboard.getHistory().getPolls().indexOf(member.getEntries().get(0).getPoll()) > subhistory_end ||
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
    
    public void startMemberDetailsTable(Member member, boolean details, BufferedWriter out) {
        try {
            out.write("<table class='member-details-table'><tr class='member-details-header-row'><td colspan='");
            out.write(details ? "4" : "3");
            out.write("'>" + member.getMostRecentName() + "&rsquo;s stats</td></tr>\n");
            out.write("<tr class='member-details-subheader-row'><td class='member-details-subheader-cell'>Category</td><td class='member-details-subheader-cell'>Value</td><td class='member-details-subheader-cell'>Rank</td><td class='member-details-subheader-cell'>Details</td></tr>\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public void endMemberDetailsTable(BufferedWriter out) {
        try {
            out.write("</table>\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
        
    // TODO: Order tied members by most recent entry date (or any other metric we want to use)
    public void addMemberDetailsRow(Leaderboard leaderboard, Member member, /*int delta,*/ String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, /*boolean hidden,*/ boolean details, boolean linksInDetails/*, int ID, int limit*/) {
        // ArrayList<Member> members = leaderboard.getMembers();
        MemberDataRetriever metric = leaderboard.getMetric();
        History history = leaderboard.getHistory();
        //int subhistory_start = 0;
//         int subhistory_end = Math.max(0, history.getPolls().size() - delta - 1);
//         Leaderboard comparison = new Leaderboard(history.getSubhistory(subhistory_start, subhistory_end), metric);
        
        try
        {
            out.write("<tr class='member-details-row'><td class='member-details-cell'>" + title + "</td>");
            out.write("<td class='member-details-cell'>" + metric.getData(member) + "</td>");
            out.write("<td class='member-details-cell'>" + leaderboard.getPlaceOfMember(member.getId()) + "/" + leaderboard.countQualifiers() + "</td>");
            out.write("<td class='member-details-cell details'>" + metric.getDetails(member, true) + "</td></tr>/n");
        }
            
//             out.newLine();
//             
//             if (hidden)
//                 out.write("<div class='leaderboard' style='display: show;' id='" + ID + "_closed'>");
//             else
//                 out.write("<div class='leaderboard' style='display: none;' id='" + ID + "_closed'>");
//             out.write("<table class='leaderboard-table stunt'><tr class='sort-title'><td>");
//             out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 1);'><img src='images/exp_plus.png' border='0'  alt='Expand' /></a></div>");
//             out.write("<a class='contest' href='javascript:toggle(" + ID + ", 1);'>" + title + "</a></td></tr></table></div>");
//             
//             if (hidden)
//                 out.write("<div class='leaderboard' style='display: none;' id='" + ID + "_open'>");
//             else
//                 out.write("<div class='leaderboard' style='display: show;' id='" + ID + "_open'>");
            
//             out.write("<table class='leaderboard-table'><tr class='sort-title'><td colspan=");
//             
//             if (details)
//                 out.write("6>");
//             else
//                 out.write("5>");
// 
//             out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 0);'><img src='images/exp_minus.png' border='0'  alt='Collapse' /></a></div>");
//             out.write("<a class='contest' href='javascript:toggle(" + ID + ", 0);'>" + title + "</a></td></tr>");
//             out.newLine();
//             out.write("<tr class='header-row'><td>Rank</td><td>&#177;</td>");
//             out.write("<td>Name</td><td>" + suffixPlural + "</td><td>&#177;</td>");
//             
//             if (details)
//                 out.write("<td>Details</td>");
//                 
//             out.write("</tr>");
            
            //NOTE: Deciding places really shouldn't be the responsibility of this section... Oh well.
//             int p = 0;
//             while (p < Math.min(members.size(), limit)) {
//                 ArrayList<Member> coplacers = leaderboard.getMembersAtPlace(p);
//                 for (int c = 0; c < coplacers.size(); ++c) {
//                     Member member = coplacers.get(c);
//                     
//                     // Don't print if member is unqualified
//                     if (!leaderboard.getMetric().qualifies(member)) {
//                         continue;
//                     }
//                     
//                     out.newLine();
//                     out.write("<tr class='");
//     
//                     switch(p + 1) {
//                         case 1:
//                             out.write("performance first");
//                             break;
//                         case 2:
//                             out.write("performance second");
//                             break;
//                         case 3:
//                             out.write("performance third");
//                             break;
//                         case 4:
//                             out.write("performance fourth");
//                             break;
//                         default:
//                             out.write("performance");
//                     }
//                     
//                     out.write("'>");
//                         
//                     // Place column
//                     out.write("<td class='place-cell'>");
// //                     if (coplacers.size() > 1) {
// //                         out.write("T&#8209;");  // &#8209 is non-breaking hyphen
// //                     }
//                     if (c == 0) {
//                         out.write(Integer.toString(p + 1) + ".");
//                     }
//                     out.write("</td>");
//                     
//                     // Delta place column
//                     out.write("<td class='delta-cell'>");
//                     // Figure out if the member has been newly added to the leaderboard by checking if their earliest entry occured after our subhistory end cut-off
//                     // TODO: This is a terribly indirect method that would benefit greatly if contests were stored by ID and held their own ID
//                     boolean is_member_new = (member.getEntries().isEmpty() ||
//                                              leaderboard.getHistory().getPolls().indexOf(member.getEntries().get(0).getPoll()) > subhistory_end ||
//                                              comparison.getPlaceOfMember(member.getId()) == Leaderboard.NO_PLACE);
//                     if (is_member_new) {
//                         out.write("<span class='place-delta'><span class='new-text'>NEW</span></span>");
//                     } else {
//                         int comparison_place = comparison.getPlaceOfMember(member.getId());
//                         if (comparison_place != Leaderboard.NO_PLACE) {
//                             int gain = (comparison_place + 1) - (p + 1);  // Written this way for consistency.  Note that gain is good (lower place).
//                             if (gain > 0) {
//                                 out.write("<span class='place-delta'><span class='gain-arrow'>&#9650;</span>" + gain + "</span>");
//                             } else if (gain < 0) {
//                                 out.write("<span class='place-delta'><span class='loss-arrow'>&#9660;</span>" + (-gain) + "</span>");
//                             } else {
//                                 // out.write("<span class='place-delta'><span class='same-arrow'>&#9671;</span></span>");
//                             }
//                         }
//                     }
//                     out.write("</td>");
//                     
//                     // Name column
//                     out.write("<td class='name-cell'>");
//                     out.write("<a class='black' href='" + UserProfile.getProfileDropboxURL(member) + "'>");
//                     out.write(member.getMostRecentName() + (SHOW_MEMBER_IDS ? " (" + member.getId() + ")" : ""));
//                     out.write("</a>");
//                     out.write("</td>");
//                     
//                     // Data column
//                     out.write("<td class='number-cell'>");
//                     out.write(metric.getData(member));
//                     out.write("</td>");
// 
//                     // Data delta column
//                     out.write("<td class='data-delta-cell'>");
//                     // Only write a delta if the member existed before.
//                     if (comparison.getHistory().getMemberMap().containsKey(member.getId()) &&
//                         comparison.getHistory().getMemberMap().get(member.getId()).getTotalEntries() > 0 &&
//                         comparison.getPlaceOfMember(member.getId()) != Leaderboard.NO_PLACE) {
//                         // Record old and new values for future use...
//                         float old_value = metric.getValue(comparison.getHistory().getMemberById(member.getId()));
//                         float new_value = metric.getValue(member);
//                         if (new_value == old_value) {
//                             // Nothing?
//                         } else if (new_value > old_value) {
//                             out.write("<span class='good-delta'>+" + metric.getFormat().format(new_value - old_value) + "</span>");
//                         } else {
//                             out.write("<span class='bad-delta'>" + metric.getFormat().format(new_value - old_value) + "</span>");
//                         }
//                     }
//                     out.write("</td>");
//                     
//                     // Details column
//                     if (details) {
//                         out.write("<td class='details'>");
//                         out.write(metric.getDetails(member, linksInDetails));
//                         out.write("</td>");
//                     }
//                     out.write("</tr>");
//                 }
//                 
//                 p += coplacers.size();
//             }
//             
//             out.write("</table>");
//             out.write("</div>"); // end leaderboard-div
//         }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
