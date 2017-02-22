import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

//?&#9650;  ?&#9660;  ?&#9670;

public class Leaderboard
{
    private ArrayList<Member> members;
    private MemberDataRetriever metric;
    private boolean is_sorted;
    
    // TODO: Clone the members_set field so that additions to the list made outside this class don't mess up our assumptions about sorting
    public Leaderboard(ArrayList<Member> members_set, MemberDataRetriever metric_set) {
        members = members_set;
        metric = metric_set;
        is_sorted = false;
    }
    
    private void sort() {
        Collections.sort(members, metric);
        is_sorted = true;
    }
    
    private void ensureOrder() {
        if (!is_sorted) {
            sort();
        }
    }
    
    // Problem: if you ask for a place that is smothered by a tie at a different place, not everybody at the same rank will be represented
    public ArrayList<Member> getMembersAtPlace(int place) {
        Member initial = members.get(place);
        if (initial == null) {
            return new ArrayList<Member>();
        }
        
        ArrayList<Member> returning = new ArrayList<Member>();
        returning.add(initial);
        
        for (int i = place + 1; i < members.size(); ++i) {
            if (metric.compare(members.get(i), initial) == 0) {
                // The two members are tied in this metric, so add the new member to our list
                returning.add(members.get(i));
            } else {
                // The two members are not tied in this metric, so stop adding to our list of tied members
                break;
            }
        }
        
        return returning;
    }
    
    // NOTE: `prefix` is not presently used
    public void addToFile(String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID) {
        addToFile(title, prefix, suffixSingular, suffixPlural, out, hidden, details, linksInDetails, ID, Integer.MAX_VALUE);
    }
    
    public void addToFile(String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID, int limit)
    {
       sort();
        Member member;
        
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
            
            float lastData = Integer.MIN_VALUE;
            float thisData = Integer.MIN_VALUE;
            int lastPlace = 0;
            int thisPlace = 0;
            String dataString;
            boolean unresolvedTie = false;
            
            //NOTE: Deciding places really shouldn't be the responsibility of this section... Oh well.
            
            for (int i=0; i<members.size() && (i<limit || unresolvedTie); i++)
            {                
                member = members.get(i);
                
                dataString = metric.getData(member);
                thisData = Float.parseFloat(dataString.replace(",","").replace("+","")); //THIS IS ESPECIALLY CRAPPY
                if (thisData == lastData)               //NOT SUPPOSED TO USE EQUALS WITH FLOATS...
                {
                    thisPlace = lastPlace;
                    unresolvedTie = true;
                }
                else
                {
                    thisPlace = i+1;
                    unresolvedTie = false;
                }
                    
                if (i < limit || unresolvedTie)
                {
                    out.newLine();
                    out.write("<tr class='");
    
                    switch(thisPlace)
                    {
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
                    out.write("<td class='place-cell'>");
                    if (thisPlace == lastPlace)
                        out.write("");
                    else
                        out.write("" + thisPlace);
                    out.write("</td>");
                    out.write("</td>");
                    out.write("<td class='name-cell'>");
                    
                    out.write("<a class='black' href='" + UserProfile.getProfileDropboxURL(member) + "'>");
                    out.write(member.getMostRecentName());
                    out.write("</a>");
                    
                    out.write("</td>");
                    out.write("<td class='number-cell'>");
                    out.write(dataString);
                    out.write("</td>");

                    if (details)
                    {
                        out.write("<td class='details'>");
                        out.write(metric.getDetails(member, linksInDetails));
                        out.write("</td>");
                    }
                    out.write("</tr>");
                    
                    lastPlace = thisPlace;
                    lastData = thisData;
                }
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
