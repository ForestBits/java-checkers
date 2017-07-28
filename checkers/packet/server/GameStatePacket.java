package checkers.packet.server;

import checkers.Game.GameState;
import checkers.packet.Packet;


public class GameStatePacket implements Packet
{
    public GameState state;
    
    public GameStatePacket(GameState state)
    {
        this.state = state;
    }
}
