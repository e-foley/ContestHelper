
/**
 * Write a description of class MemberStanding here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MemberStanding
{
    // instance variables - replace the example below with your own
    private String name;
    private int[] standings;     

    /**
     * Constructor for objects of class MemberStanding
     */
    public MemberStanding(String myName, int[] myStandings)
    {
        name = myName;
        standings = myStandings;
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public String getName()
    {
        return name;
    }
    
    public int[] getStandings()
    {
        return standings;
    }
    
    public int getStanding(int contestIndex)
    {
        if (contestIndex >= standings.length)
            return -1;
        else return standings[contestIndex];
    }
}
