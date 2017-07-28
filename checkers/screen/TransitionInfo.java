package checkers.screen;


public class TransitionInfo 
{
    public final boolean in;
    public final boolean out;
    
    public final double percent;
    
    public TransitionInfo(boolean in, boolean out, double percent)
    {
        this.in = in;
        this.out = out;
        this.percent = percent;
    }
}
