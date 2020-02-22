import java.io.BufferedWriter;

public abstract class RandomShotPageGenerator
{
    static public void generate(History history, BufferedWriter out) {
        try {
            out.write("<script src='random_shot.js'></script>");
            out.newLine();
            out.write("<img id='randoshot' src='https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png' />");
            out.newLine();
            out.write("<script>document.getElementById('randoshot').src=getRandomShot().url;</script>");
        } catch (Exception e) {
            System.err.println("Error caught in RandomShotGenerator: " + e.getMessage());
        }
    }
}
