package checkers.screen;


public class ScreenChanger 
{
    private final ScreenEngine engine;
    
    public ScreenChanger(ScreenEngine engine) {this.engine = engine;}
    
    public void setScreen(Screen screen) {engine.setScreen(screen);}
}
