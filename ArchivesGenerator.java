import java.io.BufferedWriter;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class ArchivesGenerator {
    public ArchivesGenerator() {

    }

    public void generate(History history, BufferedWriter out) {
        generate(history, out, 1, history.getPolls().size());
    }

    public void generate(History history, BufferedWriter out, int totalEntriesToShow) {
        generate(history, out, history.getPolls().size() - totalEntriesToShow, history.getPolls().size() - 1);
    }
    
    public void generate(History history, BufferedWriter out, int pollStart, int pollEnd) {
        try
        {
            Entry entry;
            Poll poll;
            boolean isWinner;
            ArrayList<Entry> winners;
            
            // Big board used to be a table whose cells contained individual polls; now it is just one big cell
            out.write("<table class='big-board'><tr><td style='text-align: center;'><span>");
            out.newLine();
            int countUp = 0;
            //for (int i=0; i<polls.size(); i++)
            for (int i=history.getPolls().size()-1; i>=0; i--)
            {
                poll = history.getPolls().get(i);
                if (i >= pollStart && i <= pollEnd)
                {
                    winners = poll.getWinners();
                    out.write("<table class='results-table'>");
                    out.newLine();
                    
                    // If there are no winners in the poll, don't create a cell for the winners' pictures lest we accumulate borders.
                    if (!winners.isEmpty()) {
                        out.write("<tr><td class='picture-cell' colspan=5>");
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

                    // Poll title
                    out.write("<tr><td class='contest-title' colspan=5>");
                    if (poll.hasTopic())
                        out.write("<a class='contest' href='http://www.purezc.net/forums/index.php?showtopic=" + poll.getTopic() + "'>");
                    //out.write("(" + poll.getSynch() + ") "); // TEMPORARY!
                    out.write("Screenshot of the Week " + poll.getName());
                    if (poll.hasTopic())
                        out.write("</a>");
                    out.write("</td></tr>");
                    
                    out.newLine();
                    out.write("<tr class='header-row'><td></td><td class='left'>Name</td><td class='center'>Votes</td><td class='center'>Pct</td><td class='center'>Points");
                    out.write("<span class='tooltip' title='Votes plus sum of vote margins over lower-ranking shots'>[?]</span>");
                    out.write("</td></tr>");
                    out.newLine();

                    for (int j=0; j<history.getPolls().get(i).numEntries(); j++)
                    {
                        entry = poll.getEntryByIndex(j);
                        isWinner = (entry.isWinner());

                        if (isWinner)
                            out.write("<tr class='entry winner'>");
                        else
                            out.write("<tr class='entry'>");

                        out.write("<td class='center has-shot-icon-cell'>");
                        if (entry.hasURL()) {
                            out.write("<a href='" + entry.getURL() + "'>");
                            out.write("<img class='has-shot-icon' title='Click to view this member&#8217;s shot' src='images/camera.png' onmouseenter='enterCamera(\"" + entry.getURL() + "\")' onmousemove='hover(event)' onmouseout='exitCamera()'/>");
                            out.write("</a>");
                        }
                        out.write("</td>");
                        
                        out.write("<td class='name'>");
                        // TODO: Rename 'image' class.  It's a holdover from a much older version of the page.
                        out.write("<a class='image' href='" + UserProfile.getProfileUrl(entry.getMember()) + "'>");
                        out.write(entry.getMember().getMostRecentName());
                        // Add an icon if the shot won the poll
                        if (isWinner) {
                            out.write(" <img class='winnericon' title='Winner!' src='images/star.png'/>");
                        }
                        
                        // List the member's old name if they use a different name now
                        if (!entry.getMember().getMostRecentName().equals(entry.getNameSubmittedUnder())) {
                            out.write("<br/><span class='old-name'>(as " + entry.getNameSubmittedUnder() + ")</span>");
                        }
                        
                        out.write("</a></td>");

                        if (entry.hasVotes())
                        {
                            out.write("<td class='votes'>"+ entry.getVotes() + "</td>");
                            out.write("<td class='percentage'>");
                            if (poll.numVotes() > 0) {
                                DecimalFormat df = new DecimalFormat("##0.00%");
                                out.write(df.format((double)(entry.getVotes()) / poll.numVotes()));
                            } else {
                                out.write("&mdash;");
                            }
                            out.write("</td><td class='points'>" + entry.getPoints() + "</td>");
                        }
                        else
                        {
                            out.write("<td class='votes'>&mdash;</td><td class='percentage'>&mdash;</td><td class='points'>&mdash;</td>");
                        }
                        out.newLine();
                        out.write("</tr>");
                    }
                    //                     if (winners.size() >= 2)
                    //                     {
                    //                         out.write("<br/>");
                    //                         out.newLine();
                    //                         out.write("<span class='tie-note'>Screenshot of the Week " + poll.getName() + " ended in a draw.</span>");
                    //                     }

                    out.write("<tr class='info-row'><td></td><td class='numentries'>");
                    out.write(poll.numEntries() + " entr");
                    if (poll.numEntries() != 1)
                        out.write("ies");
                    else
                        out.write("y");
                    out.write("</td><td class='votes'>");
                    out.write(""+poll.numVotes());
                    out.write("</td><td>");  // percentage doesn't need a total
                    out.write("</td><td class='points'>");
                    out.write(""+poll.numPoints());
                    out.write("</td></tr>");

                    if (poll.numNotes() > 0) {
                        out.write("<tr><td class='contest-notes-cell' colspan=5><ul class='contest-notes-list'>");
                        for (int j = 0; j < poll.numNotes(); ++j) {
                            out.write("<li class='poll-notes-item'>" + poll.getNote(j) + "</li>");
                        }
                        out.write("</ul>");
                        out.write("</td></tr>");
                    }
                    
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
    public void insertNavigationBar(BufferedWriter out, History history, int current_page, int num_pages, int contests_per_page) {
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
            //out.write("<td class='navtable-cell'><span class='current-page'>Page " + current_page + "</span></td>");
            out.write("<td class='navtable-cell'><div class='page-selector'><label><select onChange='window.location.href=\"archives-page\" + this.value + \".html\"'>\n");
            // Dropbown
            for (int i = 0; i < num_pages; ++i) {
                ContestBounds bounds = getPageBounds(history, contests_per_page, i + 1);
                if (bounds != null) {
                    out.write("<option value='" + (i + 1) + "'");
                    if ((i + 1) == current_page) {
                        out.write(" selected='selected'");
                    }
                    out.write(">Page " + (i + 1) + " (#" + bounds.getStart().getName() + "&ndash;#" + bounds.getEnd().getName() + ")</option>\n");
                }
            }
            out.write("</select></label></div></td>");
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
    
    ContestBounds getPageBounds(History history, int contests_per_page, int page) {
        // This method assumes that polls are already sorted within History.
        // This is also a place where we'll switch from Poll-centricness to Contest-centricness at a later time.
        if (contests_per_page < 1 || page < 1) {
            return null;
        }
        
        int total_contests = history.numPolls();
        int max_page = (total_contests + contests_per_page - 1) / contests_per_page;
        if (page > max_page) {
            return null;
        }
        
        int contest_end_index = total_contests - 1 - (page - 1) * contests_per_page;
        int contest_start_index = Math.max(0, contest_end_index - contests_per_page + 1);
        
        return new ContestBounds(history.getPoll(contest_start_index), history.getPoll(contest_end_index));
    }
}
