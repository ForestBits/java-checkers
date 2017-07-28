package checkers.screen.screens;

import checkers.CheckerBoardDrawer;
import checkers.Game.Board;
import checkers.Game.Piece;
import checkers.Game.PieceColor;
import checkers.screen.QuitException;
import checkers.screen.Screen;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.TransitionInfo;
import checkers.utility.EventQueue;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.ConstTexture;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.window.event.Event;


public class Test extends Screen
{   
    Board board = new Board(20, 8);
    
    int squareSizeX;
    int squareSizeY;
    
    FloatRect boardArea = new FloatRect(0, 0, 700, 500);
    
    Sprite sprite = new Sprite();
    
    ConstTexture whitePiece;
    ConstTexture blackPiece;
    
    
    public Test(ScreenConstructionParameters params)
    {
        squareSizeX = (int) boardArea.width/board.getSize().x;
        squareSizeY = (int) boardArea.height/board.getSize().y;
        
        System.out.println(squareSizeX + "  " + squareSizeY);
        
        board.initializeBoard(2, 2);
        
        whitePiece = params.textures.get("whitePiece");
        blackPiece = params.textures.get("blackPiece");
    }
    
    @Override
    public void update(ScreenUpdateParameters params)
    {
        EventQueue events = new EventQueue(params.window);
        
        for (Event event : events)
        {
            if (event.type == Event.Type.CLOSED)
                throw new QuitException();
        }
    }
    
    @Override
    public void draw(RenderTarget target, TransitionInfo info)
    {
        target.clear(Color.BLACK);

        CheckerBoardDrawer.draw(target, board.getSize().x, board.getSize().y, boardArea, new Color(222, 196, 140), new Color(125, 102, 51));
        
        for (int x = 0; x < board.getSize().x; ++x)
            for (int y = 0; y < board.getSize().y; ++y)
            {
                Piece piece = board.getPiece(x, y);
                
                if (piece == Piece.NONE)
                    continue;

                if (piece.color == PieceColor.BLACK)
                    sprite.setTexture(blackPiece);
                else
                    sprite.setTexture(whitePiece);
                
                sprite.setOrigin(sprite.getGlobalBounds().width/2, sprite.getGlobalBounds().height/2);
                
                sprite.setPosition(boardArea.left + x*squareSizeX + squareSizeX/2, boardArea.top + ((board.getSize().y - 1 - y)*squareSizeY + squareSizeY/2));
                
                target.draw(sprite);
            }
    }
}
