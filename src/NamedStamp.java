public class NamedStamp {
    private String name_;
    private long stamp_;

    public NamedStamp(String name)
    {
        name_ = name;
        stamp_ = System.currentTimeMillis();
    }

    public String getName() {
        return name_;
    }
    
    public long getStamp() {
        return stamp_;
    }
}
