package checkers.Game;

public class Piece 
{
    public final PieceColor color;
    
    public final boolean king;
    
    public Piece(PieceColor color, boolean king)
    {
        this.color = color;
        this.king = king;
    }
    
    public static final Piece NONE = null;
}
