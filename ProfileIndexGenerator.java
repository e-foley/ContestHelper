import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.text.DecimalFormat;

public abstract class ProfileIndexGenerator {
    static class NameLinkData implements Comparable<NameLinkData> {
        public String name;
        public int id;
        public boolean has_id;
        public String most_recent_name;
        public String link;
        public float num_entries;
        public String first_contest;
        public String most_recent_contest;
        
        // Define sorting as alphabetizing by name in case-insensitive way, then ID.
        @Override
        public int compareTo(NameLinkData other) {
            int name_result = name.toLowerCase().compareTo(other.name.toLowerCase());
            if (name_result != 0) {
                return name_result;
            }
            return new Integer(id).compareTo(other.id);
        }
    }

    static public void generate(History history, BufferedWriter out) {
        ArrayList<NameLinkData> list = new ArrayList<NameLinkData>();
        
        // Start off with list of all Members...
        Collection<Member> members = history.getMembers();
        for (Member member : members) {
            ArrayList<String> names = member.getUniqueNames();
            // Add all names to the list we're building.
            for (String name : names) {
                NameLinkData data = new NameLinkData();
                data.name = name;
                data.id = member.getId();
                data.has_id = member.hasId();
                data.most_recent_name = member.getMostRecentName();
                data.link = "profiles/" + UserProfile.getProfileUrl(member);
                data.num_entries = member.getTotalEntries();
                ArrayList<Member.EntryStakePair> pairs = member.getEntries();
                if (!pairs.isEmpty()) {
                    data.first_contest = "#" + pairs.get(0).entry.getPoll().getName();
                    data.most_recent_contest = "#" + pairs.get(pairs.size() - 1).entry.getPoll().getName();
                } else {
                    data.first_contest = "N/A";
                    data.most_recent_contest = "N/A";
                }
                list.add(data);
            }
        }
        
        // Sort the results!
        Collections.sort(list);
        
        try {
            // Now, the fun part... Build the page itself!
            // Record first letter of first name so we know when to declare new letter...
            char last_starting_char = new Character(' ');
            boolean table_active = false;
            for (NameLinkData line : list) {
                if (line.name.length() <= 0) {
                    continue;
                }
                
                char starting_char = line.name.toUpperCase().charAt(0);
                if (starting_char != last_starting_char) {
                    if (table_active) {
                        out.write("</table>");
                        out.newLine();
                    }
                    out.write("<div class='character-heading'>" + starting_char + "</div>");
                    out.newLine();
                    out.write("<table class='profile-index-table'>");
                    out.newLine();
                    out.write("<tr class='profile-index-header-row'><td>Name</td><td>Entries</td><td>First</td><td>Most recent</td></tr>");
                    out.newLine();
                    last_starting_char = starting_char;
                    table_active = true;
                }
                
                out.write("<tr class='profile-index-row'><td class='profile-index-cell profile-index-name-cell'>");
                if (!line.name.equals(line.most_recent_name)) {
                    out.write(line.name + " (see <a class='green' style='font-weight: bold' href='" + line.link + "'>" + line.most_recent_name + "</a>)</td>");
                } else {
                    out.write("<a class='green' style='font-weight: bold' href='" + line.link + "'>" + line.name + "</a></td>");
                }
                out.write("<td>" + (new DecimalFormat("#.##")).format(line.num_entries) + "</td>");
                out.write("<td>" + line.first_contest + "</td>");
                out.write("<td>" + line.most_recent_contest + "</td>");
                out.write("</tr>");
                out.newLine();
            }
            
            if (table_active) {
                out.write("</table>");
                out.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error caught in ProfileIndexGenerator: " + e.getMessage());
        }
    }
}
