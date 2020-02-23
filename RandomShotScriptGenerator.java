import java.io.BufferedWriter;
import java.util.ArrayList;

public abstract class RandomShotScriptGenerator
{
    static public void generate(History history, BufferedWriter out) {
        try {
            out.write("// This script is auto-generated.");
            out.newLine();
            out.write("// Typically, one would maintain a database and pull an entry at random instead.");
            out.newLine();
            out.write("// This project is based on old code that doesn't use a database, so here we are.");
            out.newLine();
            out.newLine();
            out.write("var all_shots = [");
            out.newLine();
            
            boolean first = true;
            for (Poll poll : history.getPolls()) {
                for (Entry entry : poll.getEntries()) {
                    if (!entry.hasURL()) {
                        continue;
                    }
                    if (first) {
                        first = false;
                    } else {
                        out.write(",");
                        out.newLine();
                    }
                    out.write("    {'url': '" + entry.getURL() + "', 'names': '");
                        
                    ArrayList<Entry.MemberNameCouple> couples = entry.getMemberNameCouples();
                    for (int i = 0; i < couples.size(); ++i) {
                        if (i > 0 && couples.size() >= 3) {
                            out.write(",");
                        }
                        if (i > 0) {
                            out.write(" ");
                        }
                        if (i > 0 && i == couples.size() - 1) {
                            out.write("and ");
                        }
                        out.write(couples.get(i).member.getMostRecentName());
                    }
                    out.write("', 'contest': '");
                    out.write(poll.getName());
                    out.write("', 'hasTopic': '");
                    out.write(poll.hasTopic() ? "true" : "false");
                    out.write("', 'topicUrl': '");
                    out.write(poll.getURL());
                    out.write("'}");
                }
            }
            //out.write("    {'name': 'Shane', 'url': 'https://sotw.purezc.net/SOTW719/Shane.png', 'contest': 719}");
            
            out.newLine();
            out.write("];");
            out.newLine();
            out.newLine();
            out.write("function getRandomShot() {");
            out.newLine();
            out.write("  var rando = Math.random() * all_shots.length;");
            out.newLine();
            out.write("  return all_shots[Math.floor(rando)];");
            out.newLine();
            out.write("}");
            out.newLine();
            //out.write("document.write(getRandomShot().url);");
        } catch (Exception e) {
            System.err.println("Error caught in RandomShotGenerator: " + e.getMessage());
        }
    }
}
