import java.io.*;
import java.util.ArrayList;

public class ArchivesGenerator {
    public ArchivesGenerator() {

    }

    public void generate(History history, BufferedWriter out) {
        generate(history, out, 1, history.getContests().size());
    }

    public void generate(History history, BufferedWriter out, int totalEntriesToShow) {
        generate(history, out, history.getContests().size() - totalEntriesToShow, history.getContests().size() - 1);
    }
    
    public void generate(History history, BufferedWriter out, int contestStart, int contestEnd) {
        try
        {
            Entry entry;
            Contest contest;
            boolean isWinner;
            ArrayList<Entry> winners;
            
            // Big board used to be a table whose cells contained individual contests; now it is just one big cell
            out.write("<table class='big-board'><tr><td style='text-align: center;'><span>");
            out.newLine();
            int countUp = 0;
            //for (int i=0; i<contests.size(); i++)
            for (int i=history.getContests().size()-1; i>=0; i--)
            {
                contest = history.getContests().get(i);
                if (i >= contestStart && i <= contestEnd)
                {
                    winners = contest.getWinners();
                    out.write("<table class='results-table'>");
                    out.newLine();
                    
                    // If there are no winners in the contest, don't create a cell for the winners' pictures lest we accumulate borders.
                    if (!winners.isEmpty()) {
                        out.write("<tr><td class='picture-cell' colspan=4>");
                        for (int j=0; j<winners.size(); j++)
                        {
                            if (!winners.get(j).hasURL()) {
                                out.write("<img class='picture-picture' title='The winner&#8217;s image is missing from the archives. Sorry.' src='images/no_image.png'/>");
                            } else {
                                out.write("<a href='" + winners.get(j).getURL() + "'>");
                                out.write("<img class='picture-picture' title='" + winners.get(j).getMember().getMostRecentName() + "' src='" + winners.get(j).getURL() + "'/>");
                                out.write("</a>");
                            }
                        }
                        out.write("</td></tr>");
                        out.newLine();
                    }

                    // Contest title
                    out.write("<tr><td class='contest-title' colspan=4>");
                    if (contest.hasTopic())
                        out.write("<a class='contest' href='http://www.purezc.net/forums/index.php?showtopic=" + contest.getTopic() + "'>");
                    //out.write("(" + contest.getSynch() + ") "); // TEMPORARY!
                    out.write("Screenshot of the Week " + contest.getName());
                    if (contest.hasTopic())
                        out.write("</a>");
                    out.write("</td></tr>");
                    
                    out.newLine();
                    out.write("<tr class='header-row'><td></td><td class='left'>Name</td><td class='center'>Votes</td><td class='center'>Points");
                    out.write("<span class='tooltip' title='Votes plus sum of vote margins over lower-ranking shots'>[?]</span>");
                    out.write("</td></tr>");
                    out.newLine();

                    for (int j=0; j<history.getContests().get(i).numEntries(); j++)
                    {
                        entry = contest.getEntryByIndex(j);
                        isWinner = (entry.isWinner());

                        if (isWinner)
                            out.write("<tr class='entry winner'>");
                        else
                            out.write("<tr class='entry'>");

                        out.write("<td class='center'>");
                        if (entry.hasURL()) {
                            out.write("<a href='" + entry.getURL() + "'>");
                            out.write("<img class='has-shot-icon' src='images/camera.png'/></a>");
                        }
                        out.write("</td>");
                        
                        out.write("<td class='name'>");
                        // TODO: Rename 'image' class.  It's a holdover from a much older version of the page.
                        out.write("<a class='image' href='" + UserProfile.getProfileDropboxURL(entry.getMember()) + "'>");
                        out.write(entry.getMember().getMostRecentName());
                        // Add an icon if the shot won the contest
                        if (isWinner) {
                            out.write(" <img class='winnericon' title='Winner!' src='images/star.png'/>");
                        }
                            
                        out.write("</td>");

                        if (entry.hasVotes())
                        {
                            out.write("<td class='votes'>"+ entry.getVotes() + "</td>");
                            out.write("<td class='points'>" + entry.getPoints() + "</td>");
                        }
                        else
                        {
                            out.write("<td class='votes'>&mdash;</td><td class='points'>&mdash;</td>");
                        }
                        out.newLine();
                        out.write("</tr>");
                    }
                    //                     if (winners.size() >= 2)
                    //                     {
                    //                         out.write("<br/>");
                    //                         out.newLine();
                    //                         out.write("<span class='tie-note'>Screenshot of the Week " + contest.getName() + " ended in a draw.</span>");
                    //                     }

                    out.write("<tr class='info-row'><td></td><td class='numentries'>");
                    out.write(contest.numEntries() + " entr");
                    if (contest.numEntries() != 1)
                        out.write("ies");
                    else
                        out.write("y");
                    out.write("</td><td class='votes'>");
                    out.write(""+contest.numVotes());
                    out.write("</td><td class='points'>");
                    out.write(""+contest.numPoints());
                    out.write("</td></tr>");

                    out.write("</table>");
                    out.newLine();
                    out.newLine();
                    
                    countUp++;
                }
            }

            // Closing 'big-board'-class table
            out.write("</span></td></tr></table>");
        }
        catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    // In this function, `current_page` is 1-indexed.
    public void insertNavigationBar(BufferedWriter out, int current_page, int num_pages) {
        try {
            out.write("<table class='navtable'>");
            out.newLine();
            
            /*
            // Add nice "Navigation" heading to the top.  Colspan needs to be calculated based on number of navigation links later.
            final int colspan = 1 + ((current_page > 1) ? 2 : 0) + ((current_page < num_pages) ? 2 : 0);
            out.write("<tr><td class='navtable-header' colspan='" + colspan + "'>Navigation</td></tr>");
            out.newLine();
            */
            
            out.write("<tr>");
            // If a previous page, create links to it and the first page
            if (current_page > 1) {
                out.write("<td class='navtable-cell'><a class='green' href='archives-page" + 1 + ".html'>&lt;&lt; Newest</a></td>");
                out.write("<td class='navtable-cell'><a class='green' href='archives-page" + (current_page - 1) + ".html'>&lt; Newer</a></td>");
            }
            // We can always declare the current page...
            out.write("<td class='navtable-cell'><span class='current-page'>Page " + current_page + "</span></td>");
            // If a next page, create a link to it and the oldest page
            if (current_page < num_pages) {
                out.write("<td class='navtable-cell'><a class='green' href='archives-page" + (current_page + 1) + ".html'>Older &gt;</a></td>");
                out.write("<td class='navtable-cell'><a class='green' href='archives-page" + num_pages + ".html'>Oldest &gt;&gt;</a></td>");
            }
            out.write("</tr>");
            out.newLine();
            out.write("</table>");
            out.newLine();
        } catch (Exception e) {
            System.err.println("Error inserting navigation bar...");
        }
    }
}
