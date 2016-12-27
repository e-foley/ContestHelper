import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

//?&#9650;  ?&#9660;  ?&#9670;

/**
 * Write a description of class Leaderboard here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Leaderboard
{
    /*private ArrayList<Member> orderedMembers;

    public Leaderboard(ArrayList<Member> myMembers, MemberDataRetriever myMethod)
    {
        orderedMembers = myMembers;
        Collections.sort(orderedMembers, myMethod);
    }*/

    /*public static void addLeaderboardToFile(ArrayList<Member> members, MemberDataRetriever c, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out)
    {
        addLeaderboardToFile(members, c, prefix, suffixSingular, suffixPlural, out, false, "", );
    }*/
    
    // NOTE: Not even sure I use prefix anymore.
    public static void addLeaderboardToFile(ArrayList<Member> members, MemberDataRetriever c,  String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID)
    {
        addLeaderboardToFile(members, c, title, prefix, suffixSingular, suffixPlural, out, hidden, details, linksInDetails, ID, Integer.MAX_VALUE);
    }
    
    public static void addLeaderboardToFile(ArrayList<Member> members, MemberDataRetriever c,  String title, String prefix, String suffixSingular, String suffixPlural, BufferedWriter out, boolean hidden, boolean details, boolean linksInDetails, int ID, int limit)
    {
        Collections.sort(members, c);
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
            
//             if (hidden)
//                 out.write("[hidden=[size=4]" + title + "[/size]]");
//             else
//             {
//             out.newLine();
//             out.write("<span class='sort-title'>" + title + "</span>");
//             }
            out.write("<table class='leaderboard-table'><tr class='sort-title'><td colspan=");
            
            if (details)
                out.write("4>");
            else
                out.write("3>");

            out.write("<div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 0);'><img src='https://dl.dropboxusercontent.com/u/10663130/PureZC/exp_minus.png' border='0'  alt='Collapse' /></a></div>");
            out.write("<a class='contest' href='javascript:toggle(" + ID + ", 0);'>" + title + "</a></td></tr>");
            out.newLine();
            out.write("<tr class='header-row'><td>Rank</td>");
            //out.write("<td>Chg</td>");
            out.write("<td>Name</td><td>" + suffixPlural + "</td>");
            
            if (details)
                out.write("<td>Details</td>");
                
            out.write("</tr>");
            
            //out.write("[size=3][list=1]");
            
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
                
                dataString = c.getData(member);
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
                    
                    
                //out.write("[*]");
                if (i < limit || unresolvedTie)
                {
                    out.newLine();
                    out.write("<tr class='");
    
                    switch(thisPlace)
                    {
                        case 1:
                            //out.write("[b][shadow=#000][color=#CFB53B]");
                            out.write("performance first");
                            break;
                        case 2:
                            //out.write("[b][shadow=#000][color=#A8A8B0]");
                            out.write("performance second");
                            break;
                        case 3:
                            //out.write("[b][shadow=#000][color=#A67D3D]");
                            out.write("performance third");
                            break;
                        case 4:
                            //out.write("[shadow=#000]");
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
                    //out.write("<td class='change-cell'>");
                    //out.write("XX");
                    out.write("</td>");
                    out.write("<td class='name-cell'>");
                    
                    
                    //out.write(member.getMostRecentName());
                    out.write("<a class='black' href='" + UserProfile.getProfileDropboxURL(member) + "'>");
                    out.write(member.getMostRecentName());
                    out.write("</a>");
                    
                    out.write("</td>");
                    out.write("<td class='number-cell'>");
                    out.write(dataString);
                    out.write("</td>");
                
                
                /*out.write(member.getMostRecentName() + " – " + prefix + c.getData(member));
                if (c.getData(member).equals("1"))   // SLOPPY
                    out.write(suffixSingular);
                else
                    out.write(suffixPlural);   
                if (i <= 2)
                    out.write("[/color][/shadow][/b]");
                if (i == 3)
                    out.write("[/shadow]");*/
                
//                 if (details)
//                     out.write(" [size=1][color=#CCC](" + c.getDetails(member, linksInDetails) + ")[/color][/size]");
                    if (details)
                    {
                        out.write("<td class='details'>");
                        out.write(c.getDetails(member, linksInDetails));
                        out.write("</td>");
                    }
                    out.write("</tr>");
                    //out.newLine();
                    
                    lastPlace = thisPlace;
                    lastData = thisData;
                }
            }
            //out.write("[/list][/size]");
            
            
            //JUST FOR NOW!!!!!!
            //out.write("<tr class='sort-title'><td colspan=4><div class='tableheaderright'><a href='javascript:toggle(" + ID + ", 0);'><img src='https://dl.dropboxusercontent.com/u/10663130/PureZC/exp_minus.png' border='0'  alt='Collapse' /></a></div></td></tr>");
            
            out.write("</table>");
            
//             if (hidden)
//                 out.write("[/hidden]");
                
            out.write("</div>"); // end leaderboard-div
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
