import java.io.*;
import java.util.ArrayList;

public class ArchivesGenerator {
    public ArchivesGenerator() {

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

                if (contest.getApparentContestNumber() >= contestStart && contest.getApparentContestNumber() <= contestEnd)
                {
                    winners = contest.getWinners();
                    out.write("<table class='results-table'>");
                    out.newLine();
                    
                    // If there are no winners in the contest, don't create a cell for the winners' pictures lest we accumulate borders.
                    if (!winners.isEmpty()) {
                        out.write("<tr><td class='picture-cell' colspan=3>");
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
                    out.write("<tr><td class='contest-title' colspan=3>");
                    if (contest.hasTopic())
                        out.write("<a class='contest' href='http://www.purezc.net/forums/index.php?showtopic=" + contest.getTopic() + "'>");
                    //out.write("(" + contest.getSynch() + ") "); // TEMPORARY!
                    out.write("Screenshot of the Week " + contest.getName());
                    if (contest.hasTopic())
                        out.write("</a>");
                    out.write("</td></tr>");
                    
                    out.newLine();
                    out.write("<tr class='header-row'><td class='left'>Name</td><td class='center'>Votes</td><td class='center'>Points");
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

                        out.write("<td class='name'>");
                        if (entry.hasURL())
                            out.write("<a class='image' href='" + entry.getURL() + "'>");
                        out.write(entry.getMember().getMostRecentName());
                        if (entry.hasURL())
                            out.write("</a>");
                            
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

                    out.write("<tr class='info-row'><td class='numentries'>");
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
}
