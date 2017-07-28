
package checkers.Game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jsfml.graphics.IntRect;
import org.jsfml.system.Vector2i;


public class Board 
{
    private final int width;
    private final int height;
    
    private final Piece[][] pieces;
    
    private int getSquareNumberForPosition(Vector2i pos)
    {
        return (pos.x*width) + pos.y;
    }
    
    public Board()
    {
        this(8, 8);
    }
    
    public Board(int width, int height)
    {
        this.width = width;
        this.height = height;
        
        pieces = new Piece[width][height];
    }
    
    public boolean isValidSquare(int x, int y)
    {
        return (x + y) % 2 != 0;
    }
    
    public Vector2i getMovedPosition(Vector2i pos, Direction direction)
    {
        switch (direction)
        {
            case UP_LEFT:
                return new Vector2i(pos.x - 1, pos.y + 1);
                
            case UP_RIGHT:
                return new Vector2i(pos.x + 1, pos.y + 1);

            case DOWN_LEFT:
                return new Vector2i(pos.x - 1, pos.y - 1);
                
            case DOWN_RIGHT:
                return new Vector2i(pos.x + 1, pos.y - 1);
        }
        
        return null;
    }
    
    public List<Move> getAvaliableMoves(Vector2i from)
    {   
        List<Move> moves = new ArrayList<>();
        
        Piece fromPiece = getPiece(from.x, from.y);

        if (fromPiece == Piece.NONE)
            return moves;
        
        List<Direction> possibleDirections = new ArrayList<>();
        
        if (fromPiece.king)
        {
            possibleDirections.add(Direction.UP_LEFT);
            possibleDirections.add(Direction.UP_RIGHT);
            possibleDirections.add(Direction.DOWN_LEFT);
            possibleDirections.add(Direction.DOWN_RIGHT);
        }
        else
        {
            if (fromPiece.color == PieceColor.WHITE)
            {
                possibleDirections.add(Direction.UP_LEFT);
                possibleDirections.add(Direction.UP_RIGHT);
            }
            else
            {
                possibleDirections.add(Direction.DOWN_LEFT);
                possibleDirections.add(Direction.DOWN_RIGHT);
            }
        }
        
        IntRect bounds = new IntRect(0, 0, width, height);
        
        for (Direction direction : possibleDirections)
        {
            Vector2i movedPos = getMovedPosition(from, direction);
            
            if (!bounds.contains(movedPos))
                continue;
            
            Piece toPiece = getPiece(movedPos.x, movedPos.y);
            
            if (toPiece == Piece.NONE)
            {
                moves.add(new Move(from, movedPos));
                
                continue;
            }
            
            if (toPiece.color == fromPiece.color)
                continue;
            else
            {
                Vector2i possibleJump = getMovedPosition(movedPos, direction);
                
                if (!bounds.contains(possibleJump))
                    continue;
                
                if (getPiece(possibleJump.x, possibleJump.y) == Piece.NONE)
                    moves.add(new CaptureMove(from, possibleJump));
            }
        }
        
        boolean containsCaptureMove = false;
        
        for (Move move : moves)
            if (move.isCaptureMove())
            {
                containsCaptureMove = true;
                
                break;
            }
        
        if (containsCaptureMove)
            for (Iterator<Move> iter = moves.iterator(); iter.hasNext();)
                if (!iter.next().isCaptureMove())
                    iter.remove();
        
        return moves;
    }
            
    public void setPiece(int x, int y, Piece piece)
    {
        pieces[x][y] = piece;
    }
    
    public Piece getPiece(int x, int y)
    {
        return pieces[x][y];
    }
    
    public void initializeBoard(int rowsWhite, int rowsBlack)
    {
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
            {
                if (isValidSquare(x, y))
                {
                    if (y < rowsWhite)
                        setPiece(x, y, new Piece(PieceColor.WHITE, false));
                    else if (y > height - 1 - rowsBlack)
                        setPiece(x, y, new Piece(PieceColor.BLACK, false));
                    else
                        setPiece(x, y, Piece.NONE);
                }
                else
                    setPiece(x, y, Piece.NONE);
            }
    }
    
    public Vector2i getSize()
    {
        return new Vector2i(width, height);
    }
}
