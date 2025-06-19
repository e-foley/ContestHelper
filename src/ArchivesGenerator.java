import java.io.BufferedWriter;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class ArchivesGenerator {
    private static final boolean POWER_USER_MODE = false;
    
    public ArchivesGenerator() {}

    public void generate(History history, EloEvaluator elo_evaluator, BufferedWriter out, String profiles_relative_path, String images_relative_path) {
        generate(history, elo_evaluator, out, new ShowAll(), new HighlightWinners(), profiles_relative_path, images_relative_path);
    }
    
    public void generate(History history, EloEvaluator elo_evaluator, BufferedWriter out, int pollStart, int pollEnd, String profiles_relative_path, String images_relative_path) {
        generate(history, elo_evaluator, out, new ShowRange(pollStart, pollEnd), new HighlightWinners(), profiles_relative_path, images_relative_path);
    }
        
    public void generate(History history, EloEvaluator elo_evaluator, BufferedWriter out, FilterStrategy filter_strategy, HighlightStrategy highlight_strategy, String profiles_relative_path, String images_relative_path) {
        // Elo is evaluated in Master and passed to this method, so no need to recalculate for every member.
        // elo_evaluator.evaluate(history);
        
        try {
            Entry entry;
            Poll poll;
            boolean isWinner;
            boolean isHighlighted;
            ArrayList<Entry> highlights;
            
            // Big board used to be a table whose cells contained individual polls; now it is just one big cell
            out.write("<table class='big-board'><tr><td style='text-align: center;'><span>");
            out.newLine();
            int countUp = 0;
            ArrayList<Poll> to_show = filter_strategy.filterPolls(history.getPolls());
            
            for (int i = to_show.size() - 1; i >= 0; --i) {
                poll = to_show.get(i);
                highlights = highlight_strategy.getHighlights(poll);
                out.write("<table class='results-table'>");
                out.newLine();
                
                // If there is nothing to highlight (e.g. a split contest's poll that doesn't contain the contest's winner), don't create a cell lest we accumulate borders.
                if (!highlights.isEmpty()) {
                    out.write("<tr><td class='picture-cell' colspan=5>");
                    for (int j = 0; j < highlights.size(); ++j) {
                        if (!highlights.get(j).hasURL()) {
                            out.write("<img class='picture-picture' title='The image is missing from the archives. Sorry.' src='" + images_relative_path + "/no_image.png'/>");
                        } else {
                            out.write("<a href='" + highlights.get(j).getURL() + "'>");
                            out.write("<img class='picture-picture' title='");
                            for (int k = 0; k < highlights.get(j).getMemberNameCouples().size(); ++k) {
                                if (k != 0) {
                                    out.write(" + ");
                                }
                                out.write(highlights.get(j).getMemberNameCouples().get(k).member.getMostRecentName());
                            }
                            out.write("' src='" + highlights.get(j).getURL() + "'/>");
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
                out.write(poll.getLongName());
                if (poll.hasTopic())
                    out.write("</a>");
                out.write("</td></tr>");
                
                out.newLine();
                out.write("<tr class='header-row'><td></td><td class='left'>Name</td><td class='center'>Votes</td><td class='center'>Pct</td><td class='center'>Rating");
                //out.write("<span class='tooltip' title='Votes plus sum of vote margins over lower-ranking shots'>[?]</span>");
                out.write("<span class='tooltip' title='[v" + EloEvaluator.VERSION_STRING + "] Elo-based determination of entrant&rsquo;s performance in contests up to and including this one. (Change from prior value is in parentheses.)'>[?]</span>");
                out.write("</td></tr>");
                out.newLine();
    
                for (int j = 0; j < poll.numEntries(); j++) {
                    entry = poll.getEntryByIndex(j);
                    isWinner = (entry.isWinner());
                    isHighlighted = false;
                    for (Entry highlight : highlights) {
                        if (highlight == entry) {
                            isHighlighted = true;
                            break;
                        }
                    }
    
                    out.write("<tr class='entry");
                    
                    if (isWinner) {
                        out.write(" winner");
                    }
                    
                    if (isHighlighted) {
                        out.write(" highlight");
                    }
                    
                    out.write("'>");
    
                    out.write("<td class='center has-shot-icon-cell'>");
                    if (entry.hasURL()) {
                        out.write("<a href='" + entry.getURL() + "'>");
                        out.write("<img class='has-shot-icon' title='Click to view this shot' src='" + images_relative_path + "/camera.png' onmouseenter='enterCamera(\"" + entry.getURL() + "\")' onmousemove='hover(event)' onmouseout='exitCamera()'/>");
                        out.write("</a>");
                    }
                    out.write("</td>");
                    
                    out.write("<td class='name'>");
                    // TODO: Rename 'image' class.  It's a holdover from a much older version of the page.
                    for (int k = 0; k < entry.getMemberNameCouples().size(); ++k) {
                        if (k != 0) {
                            out.write(" + ");
                        }
                        Entry.MemberNameCouple couple = entry.getMemberNameCouples().get(k);
                        out.write("<div class='archiveprofilelinkdiv'><a class='image' href='" + profiles_relative_path + UserProfile.getProfileUrl(couple.member) + "'>");
                        out.write(couple.member.getMostRecentName());
                        out.write("</a>");
                        // List the member's old name if they use a different name now
                        if (!couple.member.getMostRecentName().equals(couple.name_submitted_under)) {
                            out.write("<br/><span class='old-name'>(as " + couple.name_submitted_under + ")</span>");
                        }
                        out.write("</div>");
                    }
                    
                    // Add an icon if the shot won the poll
                    if (isWinner) {
                        out.write(" <img class='winnericon' title='Winner!' src='" + images_relative_path + "/star.png'/>");
                    }
                    
                    out.write("</span></td>");
    
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
                        EloEvaluator.RatingCalc calc = elo_evaluator.getRatingDetails(entry.getMemberNameCouples().get(0).member.getId(), poll.getSynch());
                        if (entry.numMembers() == 1) {
                            long old_int = Math.round(calc.rating_before);
                            long new_int = Math.round(calc.rating_after);
                            long difference = new_int - old_int;
                            DecimalFormat rating_format = new DecimalFormat("#,##0");
                            out.write("</td><td class='rating'>" + rating_format.format(new_int) + " (");
                            if (difference > 0) {
                                out.write("+" + rating_format.format(difference));
                            } else if (difference < 0) {
                                out.write("" + rating_format.format(difference));
                            } else {
                                out.write("&#177;" + rating_format.format(0));  // Plus/minus sign
                            }
                            out.write(")");
                            
                            if (POWER_USER_MODE) {
                                out.write(" ");
                                out.write(new DecimalFormat("0.000").format(calc.e_before));
                                out.write("->");
                                out.write(new DecimalFormat("0.000").format(calc.e_after));
                                out.write(" ");
                                out.write(new DecimalFormat("0.0").format(calc.boost));
                                out.write("x");
                            }
                            
                            out.write("</td>");
                        } else {
                            // TODO: Replace me with something more elegant
                            out.write("</td><td class='points'></td>");
                        }
                    } else {
                        out.write("<td class='votes'>&mdash;</td><td class='percentage'>&mdash;</td><td class='points'>&mdash;</td>");
                    }
                    out.newLine();
                    out.write("</tr>");
                }
    
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
                //out.write(""+poll.numPoints());
                out.write("");
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

            // Closing 'big-board'-class table
            out.write("</span></td></tr></table>");
        } catch (IndexOutOfBoundsException e) {
            //System.err.println("Bad array index in ArchivesGenerator: " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception e)
        {
            //Catch exception if any
            System.err.println("Error caught in ArchivesGenerator: " + e.getMessage());
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
            out.write("<td class='navtable-cell'><div class='page-selector'><label><select onChange='window.location.href=\"archives-page\" + this.value + \".html\"'>");
            out.newLine();
            // Dropbown
            for (int i = 0; i < num_pages; ++i) {
                ContestBounds bounds = getPageBounds(history, contests_per_page, i + 1);
                if (bounds != null) {
                    out.write("<option value='" + (i + 1) + "'");
                    if ((i + 1) == current_page) {
                        out.write(" selected='selected'");
                    }
                    out.write(">Page " + (i + 1) + " (#" + bounds.getStart().getShortName() + "&ndash;#" + bounds.getEnd().getShortName() + ")</option>");
                    out.newLine();
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
