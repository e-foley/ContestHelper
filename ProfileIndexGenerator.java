import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

public abstract class ProfileIndexGenerator {
    static class NameLinkData implements Comparable<NameLinkData> {
        public String name;
        public int id;
        public boolean has_id;
        public String most_recent_name;
        public String link;
        
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
                data.link = UserProfile.getProfileUrl(member);
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
                        out.write("</tr></table>\n");
                    }
                    out.write("<div class='character-heading'>" + starting_char + "</div>\n");
                    out.write("<table class='profile-index-table'>\n");
                    out.write("<tr class='profile-index-header-row'><td>Name</td></tr>\n");
                    last_starting_char = starting_char;
                    table_active = true;
                }
                
                out.write("<tr class='profile-index-row'><td class='profile-index-cell profile-index-name-cell'>");
                out.write("<a class='green' href='" + line.link + "'>" + line.name + "</a></td></tr>\n");
            }
            
            if (table_active) {
                out.write("</tr></table>\n");
            }
        } catch (Exception e) {
            System.err.println("Error caught in ProfileIndexGenerator: " + e.getMessage());
        }
    }
}
