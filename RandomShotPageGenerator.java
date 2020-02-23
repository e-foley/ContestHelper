import java.io.BufferedWriter;

public abstract class RandomShotPageGenerator
{
    static public void generate(History history, BufferedWriter out) {
        try {
            out.write("<script src='random_shot.js'></script>");
            out.newLine();
            out.write("<div class='randoshotdiv'>");
            out.newLine();
            out.write("<img id='randoshot' src='' />");
            out.newLine();
            out.write("</div>");
            out.newLine();
            out.write("<div id='randoshotcaption'>");
            out.newLine();
            out.write("</div>");
            //out.write("<script>document.getElementById('randoshot').src=getRandomShot().url;</script>");
            out.write("<script>");
            out.newLine();
            out.write("function populateRandomInfo() {");
            out.newLine();
            out.write("  var randoshot = getRandomShot();");
            out.newLine();
            out.write("  document.getElementById('randoshot').src = randoshot.url;");
            out.newLine();
            //out.write("  document.getElementById('randoshotcaption'
            out.write("}");
            out.newLine();
            out.write("populateRandomInfo();");
            out.write("</script>");
            out.newLine();
        } catch (Exception e) {
            System.err.println("Error caught in RandomShotGenerator: " + e.getMessage());
        }
    }
}
