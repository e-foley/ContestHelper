import java.util.ArrayList;

public class ParsedLine
{
    public boolean isBlank;
    public boolean isComment;
    
    public boolean hasPollInfo;
    public String pollName;
    public boolean hasTopicInfo;
    public int topic;
    
    public boolean hasMemberInfo;
    public ArrayList<History.MemberInfo> member_infos;
    //public boolean hasTag;
    //public String tag;
    public boolean hasURL;
    public String URL;
    public boolean hasVotes;
    public int votes;
    public int overrideCode;
    public boolean synchronous;
    public boolean hasUncertainty;
    public boolean isPollNote;
    public String note;
    
    private static final double URL_DIGITS = 2;
    
    public ParsedLine(String line, String currentPollName)
    {
        //System.out.println("Starting parse of \"" + line + "\"");
        
        String regexMember = "(\\s+)?;(\\s+)?";   // regular expression to divide associations file
        String regexPoll = "(\\s+)?@(\\s+)?";    // regular expression for the poll number and topic
        // Regular expression that divides names and tags.  Note that both [ and ] are treated the same way, so tags
        // don't strictly need to be enclosed by brackets as long as there is at least one present.  For example, the
        // strings "nicklegends[2237]" and "nicklegends[2237" and "nicklegends  ]2237 [" are all treated the same way.
        String regexTag = "\\s*(\\[|\\])\\s*";
        String regexMemberSeparator = "(\\s+)?\\+(\\s+)?";
        String[] splits;
        
        isBlank = line.equals("");
        isComment = line.startsWith("//");
        isPollNote = line.startsWith("*");
        
        hasPollInfo = false;
        pollName = "";
        hasTopicInfo = false;
        topic = -1;
        hasMemberInfo = false;
        member_infos = new ArrayList<History.MemberInfo>();
        //hasTag = false;
        //tag = "";
        hasURL = false;
        URL = "";
        hasVotes = false;
        votes = -1;
        overrideCode = 0;
        synchronous = false;
        hasUncertainty = true;
        note = "";
        
        if (isBlank || isComment)
            return;
        
        if (isPollNote) {
            note = line.substring(line.indexOf('*') + 1).trim();
            return;
        }
            
        if (line.startsWith("#") || (synchronous=line.startsWith("&")))
        {
            splits = line.split(regexPoll);
            if (splits.length >= 1)
            {
                hasPollInfo = true;
                pollName = (splits[0].substring(1));  // this will be changed as soon poll names are implemented
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
                
                String[] each_member = splits[0].split(regexMemberSeparator);
                for (int i = 0; i < each_member.length; ++i) {
                    // Split the member into name and tag sections as appropriate nicklegends[2237]
                    String[] tagSplit = each_member[i].split(regexTag);
                    if (tagSplit.length > 1) {
                        History.MemberInfo info = new History.MemberInfo();
                        info.member_name = tagSplit[0];
                        info.tag = tagSplit[1];
                        // info.id = 
                        // hasTag = true;
                        // memberName = tagSplit[0];
                        member_infos.add(info);
                    } else {
                        History.MemberInfo info = new History.MemberInfo();
                        info.member_name = tagSplit[0];
                        info.tag = "";
                        // info.id = 
                        // hasTag = false;
                        member_infos.add(info);
                    }
                }
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
                     String pollIDString = currentPollName.replaceAll("\\D","");  /** REVIEW ME!!!*/
                     while (pollIDString.length() < URL_DIGITS)    // note: this check should use a constant
                        pollIDString = "0" + pollIDString;
                     
                    hasURL = true;
                    URL = "http://sotw.purezc.net/SOTW" + pollIDString + "/" + member_infos.get(0).member_name.replace(" ","%20").replace("'","%27") + "." + splits[1]; // TODO: Do we want to include other members here by default?
                }
            }
            
            if (splits.length == 4)
            {
                voteIndex = 3;
                hasURL = true;
                
                String pollIDString = currentPollName.replaceAll("\\D","");
                while (pollIDString.length() < URL_DIGITS)    // note: this check should use a constant
                    pollIDString = "0" + pollIDString;
                
                URL = "http://sotw.purezc.net/SOTW" + pollIDString + "/" + splits[1].replace(" ","%20").replace("'","%27") + "." + splits[2];
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