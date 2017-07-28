package checkers.input;


public abstract class ActivatableElement 
{
    private boolean active;
    
    final protected boolean isActive() {return active;} 
    
    final void setActive(ActivatableElementContainer.ActivationKey key, boolean active) {key.hashCode(); this.active = active;}
    
    protected void onStateChange() {}
    final void onStateChange(ActivatableElementContainer.ActivationKey key) {key.hashCode(); onStateChange();}
}
