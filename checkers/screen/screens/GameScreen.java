package checkers.screen.screens;

import checkers.CheckerBoardDrawer;
import checkers.Game.Board;
import checkers.Game.CaptureMove;
import checkers.Game.Move;
import checkers.Game.Piece;
import checkers.Game.PieceColor;
import checkers.input.inputs.Button;
import checkers.packet.Packet;
import checkers.packet.PacketHandler;
import checkers.packet.PacketUtil;
import checkers.packet.client.CaptureMovePacket;
import checkers.packet.client.GameOptionPacket;
import checkers.packet.client.NormalMovePacket;
import checkers.packet.server.CreateBoardPacket;
import checkers.packet.server.GameOverPacket;
import checkers.packet.server.GameStatePacket;
import checkers.packet.server.PlayerColorPacket;
import checkers.packet.server.PlayerTurnPacket;
import checkers.packet.server.ServerDisconnectPacket;
import checkers.packet.server.SetBoardEmptyPacket;
import checkers.packet.server.SetBoardPiecePacket;
import checkers.packet.shared.ChatPacket;
import checkers.packet.shared.HeartbeatPacket;
import checkers.packet.shared.LoginPacket;
import checkers.screen.QuitException;
import checkers.screen.Screen;
import checkers.screen.ScreenConstructionParameters;
import checkers.screen.ScreenUpdateParameters;
import checkers.screen.TransitionInfo;
import checkers.server.GameOption;
import checkers.server.PacketPackage;
import checkers.utility.EventQueue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.ConstTexture;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;
import org.jsfml.system.Vector2i;
import org.jsfml.window.Mouse;
import org.jsfml.window.Window;
import org.jsfml.window.event.Event;
import org.jsfml.window.event.Event.Type;


public class GameScreen extends Screen
{
    private boolean failure = false;
    
    private final ChatInputThread thread = new ChatInputThread(this);
    
    private final Text playerText = new Text();
    private final Text opponentText = new Text();
    private final Text turnText = new Text();
    private final Text kingText = new Text();
    private final Text gameResultText = new Text();
    
    private final Button rematchButton;
    private final Button menuButton;
    
    private final Socket socket = new Socket();
    
    private final PacketHandler handler = new PacketHandler();

    private final List<PacketPackage> packetsToSend = new ArrayList<>();
    
    private Board board;
    
    private Vector2i previousSquare = new Vector2i(-1, -1);
    
    private boolean holdingPiece;
    private boolean canMove = false;
    private boolean makingMultiCapture = false;
    private boolean gameOver = false;
    private boolean quit = false;
    
    private Piece heldPiece = Piece.NONE;
    
    private PieceColor playerColor;
    
    private long lastHeartbeat;
    
    private final FloatRect boardArea;
    
    private float squareSizeX;
    private float squareSizeY;
    
    private final ConstTexture whitePiece;
    private final ConstTexture blackPiece;
    
    private final Sprite sprite = new Sprite();
    
    private final Window window;
    
    void addChatInput(String message)
    {
        sendPacket(socket, new ChatPacket(message));
    }
    
    private void sendPacket(Socket socket, Packet packet)
    {
        synchronized (packetsToSend)
        {
            packetsToSend.add(new PacketPackage(socket, packet));
        }
    }
    
    private boolean captureMoveAvaliable()
    {
        for (int x = 0; x < board.getSize().x; ++x)
            for (int y = 0; y < board.getSize().y; ++y)
            {
                Piece piece = board.getPiece(x, y);
                
                if (piece == Piece.NONE)
                    continue;
                
                if (piece.color != playerColor)
                    continue;
                
                List<Move> moves = board.getAvaliableMoves(new Vector2i(x, y));
                
                boolean containsCapture = false;
                
                for (Move move : moves)
                    if (move.isCaptureMove())
                    {
                        containsCapture = true;
                        
                        break;
                    }
                
                if (containsCapture)
                    return true;
            }
        
        return false;
    }
    
    private Vector2i mouseSquarePosition()
    {
        Vector2i mousePos = Mouse.getPosition(window);
        
        if (!boardArea.contains(new Vector2f(mousePos)))
            return null;
        
        int squareX = (int) ((mousePos.x - boardArea.left)/squareSizeX);
        int squareY = (board.getSize().y - 1) - (int) ((mousePos.y - boardArea.top)/squareSizeY);
        
        return new Vector2i(squareX, squareY);
    }
    
    private FloatRect getSquareTextureRect(int squareX, int squareY)
    {
        return new FloatRect(boardArea.left + (squareX*squareSizeX + squareSizeX/2) - whitePiece.getSize().x/2, boardArea.top + ((board.getSize().y - 1 - squareY)*squareSizeY + squareSizeY/2) - whitePiece.getSize().y/2, whitePiece.getSize().x, blackPiece.getSize().x);
    }
    
    private Vector2i reverseVector(Vector2i vector)
    {
        return new Vector2i(vector.x, board.getSize().y - 1 - vector.y);
    }
    
    private int reverseY(int y)
    {
        return board.getSize().y - 1 - y;
    }
    
    private boolean BLACK()
    {
        return playerColor == PieceColor.BLACK;
    }
    
    private void updateHeldPiece(EventQueue events)
    {
        for (Event event : events)
        {
            if (event.type == Type.MOUSE_BUTTON_PRESSED)
            {
                Vector2i activeSquare = mouseSquarePosition();
                
                if (activeSquare == null)
                    continue;
                
                FloatRect textureBounds = getSquareTextureRect(activeSquare.x, activeSquare.y);
                
                if (textureBounds.contains(new Vector2f(Mouse.getPosition(window))))
                {
                    heldPiece = board.getPiece(activeSquare.x, BLACK() ? reverseY(activeSquare.y) : activeSquare.y);
                    
                    if (heldPiece == Piece.NONE)
                        continue;

                    if (heldPiece.color != playerColor)
                        continue;
                    
                    previousSquare = activeSquare;
                    
                    holdingPiece = true;
                }
            }
            
            if (event.type == Type.MOUSE_BUTTON_RELEASED)
            {
                if (!holdingPiece)
                    continue;
                
                holdingPiece = false;
                
                Vector2i activeSquare = mouseSquarePosition();
                
                if (activeSquare == null)
                {
                    previousSquare = new Vector2i(-1, -1);
                    
                    heldPiece = Piece.NONE;
                    
                    continue;
                }
                
                List<Move> validMoves = board.getAvaliableMoves(BLACK() ? reverseVector(previousSquare) : previousSquare);
                
                Move validMove = null;
                
                boolean needCaptureMove = captureMoveAvaliable();
                
                for (Move move : validMoves)
                    if (needCaptureMove && !move.isCaptureMove())
                        continue;
                    else
                        if (move.to.equals(BLACK() ? reverseVector(activeSquare) : activeSquare))
                        {
                            validMove = move;

                            break;
                        }
                
                if (validMove != null)
                {
                    move(validMove);
                }
                
                previousSquare = new Vector2i(-1, -1);
                
                heldPiece = Piece.NONE;
            }
        }
    }
    
    private void move(Move move)
    {
        board.setPiece(move.to.x, move.to.y, heldPiece);
        
        List<Move> validMoves = board.getAvaliableMoves(move.to);
        
        boolean containsCapture = false;

        for (Move move2 : validMoves)
            if (move2.isCaptureMove())
            {
                containsCapture = true;
                
                break;
            }
        
        if (move.isCaptureMove() && containsCapture)
            makingMultiCapture = true;
        else
        {
            makingMultiCapture = false;
            
            canMove = false;
            
            turnText.setString("Opponent's turn");
            turnText.setPosition(boardArea.left - (turnText.getLocalBounds().width + 10), boardArea.top + boardArea.height/2);
        }
        
        board.setPiece(move.from.x, move.from.y, Piece.NONE);
        
        if (move.isCaptureMove())
        {
            CaptureMove capture = move.asCaptureMove();
            
            board.setPiece(capture.capturedPos.x, capture.capturedPos.y, Piece.NONE);
            
            sendPacket(socket, new CaptureMovePacket(move, makingMultiCapture));
        }
        else
            sendPacket(socket, new NormalMovePacket(move));
    }
    
    private void handleDisconnectPacket(Socket socket, Packet packet, ScreenConstructionParameters params)
    {
        ServerDisconnectPacket disconnect = (ServerDisconnectPacket) packet;
                    
        params.screenChanger.setScreen(new ServerDisconnectScreen(params, disconnect.reason));
    }
    
    private void handleLoginPacket(Socket socket, Packet packet)
    {
        LoginPacket login = (LoginPacket) packet;
                    
        opponentText.setString(login.playerName);

        opponentText.setPosition(boardArea.left - (opponentText.getLocalBounds().width + 10), boardArea.top);
    }
    
    private void handleCreateBoardPacket(Socket socket, Packet packet)
    {
        CreateBoardPacket createBoard = (CreateBoardPacket) packet;

        board = new Board(createBoard.size.x, createBoard.size.y);

        squareSizeX = boardArea.width/board.getSize().x;
        squareSizeY = boardArea.height/board.getSize().y;
        
        board.initializeBoard(createBoard.whiteRows, createBoard.blackRows);
    }
    
    private void handleSetBoardEmptyPacket(Socket socket, Packet packet)
    {
        SetBoardEmptyPacket set = (SetBoardEmptyPacket) packet;

        board.setPiece(set.pos.x, set.pos.y, Piece.NONE);
    }
    
    private void handleSetBoardPiecePacket(Socket socket, Packet packet)
    {
        SetBoardPiecePacket set = (SetBoardPiecePacket) packet;
        
        board.setPiece(set.pos.x, set.pos.y, set.piece);
    }
    
    private void handlePlayerColorPacket(Socket socket, Packet packet)
    {
        PlayerColorPacket color = (PlayerColorPacket) packet;

        playerColor = color.color;
    }
    
    private void handlePlayerTurnPacket(Socket socket, Packet packet)
    {
        canMove = true;
                    
        turnText.setString("Your Turn");
        turnText.setPosition(boardArea.left - (turnText.getLocalBounds().width + 10), boardArea.top + boardArea.height/2);
    }
    
    private void handleGameOverPacket(Socket socket, Packet packet)
    {
        GameOverPacket game = (GameOverPacket) packet;
        
        gameOver = true;
        
        if (game.color == playerColor)
            gameResultText.setString("You win.");
        else
            gameResultText.setString("You lose.");
        
        gameResultText.setOrigin(gameResultText.getGlobalBounds().width/2, gameResultText.getGlobalBounds().height/2);
        gameResultText.setPosition(window.getSize().x/2, window.getSize().y/2);
    }
    
    private void handleChatPacket(Socket socket, Packet packet)
    {
        ChatPacket chat = (ChatPacket) packet;
        
        System.out.println(chat.message);
    }
    
    private void handleHeartbeatPacket(Socket socket, Packet packet)
    {
        lastHeartbeat = System.currentTimeMillis();
        
        sendPacket(socket, packet);
    }
    
    private void registerHandlers(ScreenConstructionParameters params)
    {
        handler.registerHandler(ServerDisconnectPacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    handleDisconnectPacket(socket, packet, params);
                });
        
        handler.registerHandler(LoginPacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    handleLoginPacket(socket, packet);
                });
        
        handler.registerHandler(GameStatePacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    
                });
        
        handler.registerHandler(CreateBoardPacket.class,
                (Socket socket, Packet packet) -> 
                {
                    handleCreateBoardPacket(socket, packet);
                });
        
        handler.registerHandler(SetBoardEmptyPacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handleSetBoardEmptyPacket(socket, packet);
                });
        
        handler.registerHandler(SetBoardPiecePacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handleSetBoardPiecePacket(socket, packet);
                });
        
        handler.registerHandler(PlayerColorPacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handlePlayerColorPacket(socket, packet);
                });
        
        handler.registerHandler(PlayerTurnPacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handlePlayerTurnPacket(socket, packet);
                });
        
        handler.registerHandler(GameOverPacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    handleGameOverPacket(socket, packet);
                });
        
        handler.registerHandler(ChatPacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handleChatPacket(socket, packet);
                });
        
        handler.registerHandler(HeartbeatPacket.class,
                (Socket socket, Packet packet) ->
                {
                    handleHeartbeatPacket(socket, packet);
                });
    }
    
    public GameScreen(ScreenConstructionParameters params, InetAddress IP, String name)
    {
        new Thread(thread).start();
        
        window = params.window;
        
        boardArea = new FloatRect(268, 0, params.window.getSize().x - 268, params.window.getSize().y - 64);
        
        whitePiece = params.textures.get("whitePiece");
        blackPiece = params.textures.get("blackPiece");
        
        playerText.setFont(params.font);
        playerText.setColor(Color.WHITE);
        playerText.setCharacterSize(20);
        playerText.setString(name);
        playerText.setPosition(boardArea.left - (playerText.getLocalBounds().width + 10), boardArea.top + boardArea.height - playerText.getLocalBounds().height);
        
        opponentText.setFont(params.font);
        opponentText.setColor(Color.WHITE);
        opponentText.setCharacterSize(20);
        
        turnText.setFont(params.font);
        turnText.setColor(Color.WHITE);
        turnText.setCharacterSize(20);
        turnText.setString("Opponent's turn");
        turnText.setPosition(boardArea.left - (turnText.getLocalBounds().width + 10), boardArea.top + boardArea.height/2);
        
        kingText.setFont(params.font);
        kingText.setColor(Color.BLACK);
        kingText.setCharacterSize(15);
        kingText.setString("K");
        kingText.setOrigin(kingText.getLocalBounds().width/2, kingText.getLocalBounds().height/2);
        
        gameResultText.setFont(params.font);
        gameResultText.setColor(Color.BLACK);
        gameResultText.setCharacterSize(20);
        
        menuButton = new Button(
                params.textures.get("button"),
                params.font,
                "Menu",
                params.window.getSize().x/2,
                params.window.getSize().y*(3/5f),
                true,
                () -> 
                {
                    sendPacket(socket, new GameOptionPacket(GameOption.QUIT));
                    
                    quit = true;
                });
        
        rematchButton = new Button(
                params.textures.get("button"),
                params.font,
                "Rematch",
                params.window.getSize().x/2,
                params.window.getSize().y*(4/5f),
                true,
                () -> 
                {
                    sendPacket(socket, new GameOptionPacket(GameOption.REMATCH));
                    
                    gameOver = false;
                });
                                 
        
        try
        {
            socket.connect(new InetSocketAddress(IP, 11111));
            
            handler.addSocket(socket);
            
            registerHandlers(params);
            
            sendPacket(socket, new LoginPacket(name));
        }
        
        catch (IOException ex)
        {
            failure = true;
            
            return;
        }
        
        lastHeartbeat = System.currentTimeMillis();
    } 
    
    @Override
    public void update(ScreenUpdateParameters params)
    {
        if (failure)
        {
            params.screenChanger.setScreen(new ServerDisconnectScreen(params.screenConstructionParameters, "Couldn't connect to server"));
            
            thread.quit();
        
            return;
        }
        
        if (quit)
        {
            params.screenChanger.setScreen(new MainMenuScreen(params.screenConstructionParameters));
            
            thread.quit();
            
            return;
        }
        
        EventQueue events = new EventQueue(params.window);       
        
        for (Event event : events)
            if (event.type == Type.CLOSED)
                throw new QuitException();
        
        if (gameOver)
        {
            menuButton.update(events);
            rematchButton.update(events);
        }
        
        synchronized (packetsToSend)
        {
            for (PacketPackage pack : packetsToSend)
                PacketUtil.sendPacket(pack.socket, pack.packet);

            packetsToSend.clear();
        }
        
        if ((System.currentTimeMillis() - lastHeartbeat)/1000f > 10)
        {
            params.screenChanger.setScreen(new ServerDisconnectScreen(params.screenConstructionParameters, "Server Timed Out"));
            
            thread.quit();
        }
        
        if (canMove)
            updateHeldPiece(events);
    }
    
    private void drawBoard(RenderTarget target)
    {
        CheckerBoardDrawer.draw(target, board.getSize().x, board.getSize().y, boardArea, new Color(222, 196, 140), new Color(125, 102, 51));
    }
    
    private void drawPieces(RenderTarget target)
    {
        for (int x = 0; x < board.getSize().x; ++x)
            for (int y = 0; y < board.getSize().y; ++y)
            {
                if (new Vector2i(x, BLACK() ? reverseY(y) : y).equals(previousSquare))
                    continue;

                Piece piece = board.getPiece(x, y);
                
                if (piece != Piece.NONE)
                {
                    if (piece.color == PieceColor.WHITE)
                        sprite.setTexture(whitePiece);
                    else
                        sprite.setTexture(blackPiece);
                    
                    sprite.setOrigin(sprite.getGlobalBounds().width/2, sprite.getGlobalBounds().height/2);
                    
                    float height;
                    
                    if (playerColor == PieceColor.WHITE)
                        height = boardArea.top + ((board.getSize().y - 1 - y)*squareSizeY + squareSizeY/2);
                    else
                        height = boardArea.top + y*squareSizeY + squareSizeY/2;
                    
                    sprite.setPosition(boardArea.left + x*squareSizeX + squareSizeX/2, height);
                    
                    target.draw(sprite);
                    
                    if (piece.king)
                    {
                        kingText.setPosition(sprite.getPosition());
                    
                        target.draw(kingText);
                    }
                }
            }
    }
    
    private void drawHeldPiece(RenderTarget target)
    {
        if (!holdingPiece)
            return;
        
        if (heldPiece.color == PieceColor.WHITE)
            sprite.setTexture(whitePiece);
        else
            sprite.setTexture(blackPiece);
        
        sprite.setPosition(new Vector2f(Mouse.getPosition(window)));
        
        target.draw(sprite);

        if (heldPiece.king)
        {
            kingText.setPosition(sprite.getPosition());

            target.draw(kingText);
        }
    }
    
    @Override
    public void draw(RenderTarget target, TransitionInfo transition)
    {
        target.clear(Color.BLACK);
        
        if (board != null)
        {
            drawBoard(target);
            drawPieces(target);
            drawHeldPiece(target);
        }
        
        target.draw(opponentText);
        target.draw(playerText);
        target.draw(turnText);
        
        if (board == null)
            return;

        if (gameOver)
        {
            CheckerBoardDrawer.draw(target, new FloatRect(0, 0, target.getSize().x, target.getSize().y), new Color(255, 102, 0), new Color(255, 153, 0));
            
            target.draw(gameResultText);
            
            target.draw(menuButton);
            target.draw(rematchButton);
        }
    }
}
