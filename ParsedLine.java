public class ParsedLine
{
    public boolean isBlank;
    public boolean isComment;
    
    public boolean hasContestInfo;
    public String contestName;
    public boolean hasTopicInfo;
    public int topic;
    
    public boolean hasMemberInfo;
    public String memberName;
    public boolean hasURL;
    public String URL;
    public boolean hasVotes;
    public int votes;
    public int overrideCode;
    
    public boolean synchronous;
    
    public boolean hasUncertainty;
    
    private static final double URL_DIGITS = 2;
    
    public ParsedLine(String line, String currentContestName)
    {
        //System.out.println("Starting parse of \"" + line + "\"");
        
        String regexMember = "(\\s+)?;(\\s+)?";   // regular expression to divide associations file
        String regexContest = "(\\s+)?@(\\s+)?";    // regular expression for the contest number and topic
        String[] splits;
        
        isBlank = line.equals("");
        isComment = line.startsWith("//");
        
        hasContestInfo = false;
        contestName = "";
        hasTopicInfo = false;
        topic = -1;
        memberName = "";
        hasURL = false;
        URL = "";
        hasVotes = false;
        votes = -1;
        overrideCode = 0;
        synchronous = false;
        hasUncertainty = true;
        
        if (isBlank || isComment)
            return;
        
        if (line.startsWith("#") || (synchronous=line.startsWith("&")))
        {
            splits = line.split(regexContest);
            if (splits.length >= 1)
            {
                hasContestInfo = true;
                contestName = (splits[0].substring(1));  // this will be changed as soon contest names are implemented
            }
            if (splits.length >= 2)
            {
                if (isInteger(splits[1]))    // a little weak since we run the parseInt effectively twice...
                {
                    hasTopicInfo = true;
                    topic = Integer.parseInt(splits[1]);
                }
                else
                {
                    hasTopicInfo = false;   // just to be safe
                    topic = -1;             // safe here, too
                }
            }
        }
        else // treat as member line
        {
            int voteIndex = -1;
            splits = line.split(regexMember);
            
            if (splits.length > 0)
            {
                hasMemberInfo = true;
                memberName = splits[0];
            }
            
            if (splits.length == 2)
            {
                voteIndex = 1;
            }
            
            if (splits.length == 3)
            {
                voteIndex = 2;
                
                if (splits[1].length() > 4)
                {
                    // treat as explicit URL
                    hasURL = true;
                    URL = splits[1];
                }
                else
                {
                     // treat as extension
                     String contestIDString = currentContestName.replaceAll("\\D","");  /** REVIEW ME!!!*/
                     while (contestIDString.length() < URL_DIGITS)    // note: this check should use a constant
                        contestIDString = "0" + contestIDString;
                     
                    hasURL = true;
                    URL = "http://sotw.purezc.net/SOTW" + contestIDString + "/" + memberName.replace(" ","%20").replace("'","%27") + "." + splits[1];
                }
            }
            
            if (splits.length == 4)
            {
                voteIndex = 3;
                hasURL = true;
                
                String contestIDString = currentContestName.replaceAll("\\D","");
                while (contestIDString.length() < URL_DIGITS)    // note: this check should use a constant
                    contestIDString = "0" + contestIDString;
                
                URL = "http://sotw.purezc.net/SOTW" + contestIDString + "/" + splits[1].replace(" ","%20").replace("'","%27") + "." + splits[2];
            }

            String voteString = new String();
            
            if (voteIndex >= 0)
            {
                voteString = splits[voteIndex];
                if (voteString.contains("W"))
                    overrideCode = 1;
                else if (voteString.contains("L"))
                    overrideCode = -1;
                else overrideCode = 0;  // just in case
                
                voteString = voteString.replace("W","").replace("L","");
            }
            
            if (isInteger(voteString))
            {
                hasVotes = true;
                votes = Integer.parseInt(voteString);
            }
            else
            {
                hasVotes = false;   // just covering bases -- not really necessary
                votes = 0;
            }
        }
    }
    
    private boolean isInteger(String testing)  
    {
        try  
        {
            Integer.parseInt( testing );  
            return true;
        }  
        catch(Exception e)  
        {  
            return false;  
        }  
    }
}