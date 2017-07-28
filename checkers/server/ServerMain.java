package checkers.server;

import checkers.Game.Board;
import checkers.Game.GameState;
import checkers.Game.Move;
import checkers.Game.Piece;
import checkers.Game.PieceColor;
import checkers.packet.Packet;
import checkers.packet.PacketHandler;
import checkers.packet.PacketUtil;
import checkers.packet.client.CaptureMovePacket;
import checkers.packet.client.GameOptionPacket;
import checkers.packet.client.NormalMovePacket;
import checkers.packet.server.CreateBoardPacket;
import checkers.packet.server.GameOverPacket;
import checkers.packet.server.PlayerColorPacket;
import checkers.packet.server.PlayerTurnPacket;
import checkers.packet.server.ServerDisconnectPacket;
import checkers.packet.server.SetBoardEmptyPacket;
import checkers.packet.server.SetBoardPiecePacket;
import checkers.packet.shared.ChatPacket;
import checkers.packet.shared.HeartbeatPacket;
import checkers.packet.shared.LoginPacket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.jsfml.system.Vector2i;

public class ServerMain 
{   
    private enum GameCondition
    {
        WHITE_WINS,
        BLACK_WINS,
        NO_WINNER
    }
    
    private GameState state = GameState.WAITING_FOR_PLAYERS;
    
    private PlayerInfo p1 = new PlayerInfo();
    private PlayerInfo p2 = new PlayerInfo();
    
    private PlayerTurn turn;
    
    private Random random = new Random();
    
    private Board board;
    
    private int whiteRows;
    private int blackRows;
    
    private long lastHeartBeat = System.currentTimeMillis();
    
    private final IncomingConnectionThread thread = new IncomingConnectionThread(this);
    
    private final PacketHandler handler = new PacketHandler();
    
    private final List<PacketPackage> packetsToSend = new ArrayList<>();
    
    private void nullConnection(PlayerInfo info)
    {
        info.choice = null;
        info.lastHeartbeat = -1;
        info.name = null;
        info.socket = null;
    }
    
    void addConnection(Socket socket)
    {
        if (p1.socket == null)
        {
            p1.socket = socket;
            
            handler.addSocket(socket);
            
            p1.lastHeartbeat = System.currentTimeMillis();
        }
        else if (p2.socket == null)
        {
            p2.socket = socket;
            
            p2.lastHeartbeat = System.currentTimeMillis();
            
            handler.addSocket(socket);
        }
        else
        {
            sendPacket(socket, new ServerDisconnectPacket("The game is already full."));
        }
    }
    
    private void sendPacket(Socket socket, Packet packet)
    {if (socket == null) return;
        if (packet instanceof SetBoardPiecePacket)
        {
            SetBoardPiecePacket set = (SetBoardPiecePacket) packet;
            
            if (set.piece == null)
                throw new RuntimeException();
        }
        
        synchronized (packetsToSend)
        {
            packetsToSend.add(new PacketPackage(socket, packet));
        }
    }
    
    private void println(String message)
    {
        System.out.println(message);
    }
    
    private void promoteKings()
    {
        for (int x = 0; x < board.getSize().x; ++x)
        {
            Piece piece1 = board.getPiece(x, 0);
            Piece piece2 = board.getPiece(x, board.getSize().y - 1);
            
            if (piece1 != null)
            {
                if (piece1.color == PieceColor.BLACK)
                {
                    Piece king = new Piece(PieceColor.BLACK, true);
                    
                    board.setPiece(x, 0, king);
                    
                    sendPacket(p1.socket, new SetBoardPiecePacket(new Vector2i(x, 0), king));
                    sendPacket(p2.socket, new SetBoardPiecePacket(new Vector2i(x, 0), king));
                }
            }
            
            if (piece2 != null)
            {
                if (piece2.color == PieceColor.WHITE)
                {
                    Piece king = new Piece(PieceColor.WHITE, true);
                    
                    board.setPiece(x, board.getSize().y - 1, king);
                    
                    sendPacket(p1.socket, new SetBoardPiecePacket(new Vector2i(x, board.getSize().y - 1), king));
                    sendPacket(p2.socket, new SetBoardPiecePacket(new Vector2i(x, board.getSize().y - 1), king));
                }
            }
        }
    }
    
    private GameCondition getGameCondition()
    {
        short whitePieceCount = 0;
        short blackPieceCount = 0;
        
        boolean avaliableWhiteMove = false;
        boolean avaliableBlackMove = false;
        
        for (int x = 0; x < board.getSize().x; ++x)
            for (int y = 0; y < board.getSize().y; ++y)
            {
                Piece piece = board.getPiece(x, y);
                
                if (piece == Piece.NONE)
                    continue;
                
                if (piece.color == PieceColor.WHITE)
                {
                    ++whitePieceCount;
                    
                    if (!avaliableWhiteMove)
                    {
                        List<Move> moves = board.getAvaliableMoves(new Vector2i(x, y));
                        
                        if (!moves.isEmpty())
                            avaliableWhiteMove = true;
                    }
                }
                
                if (piece.color == PieceColor.BLACK)
                {
                    ++blackPieceCount;
                    
                    if (!avaliableBlackMove)
                    {
                        List<Move> moves = board.getAvaliableMoves(new Vector2i(x, y));
                        
                        if (!moves.isEmpty())
                            avaliableBlackMove = true;
                    }
                }
            }
        
        if (whitePieceCount == 0 || !avaliableWhiteMove)
            return GameCondition.BLACK_WINS;
        
        if (blackPieceCount == 0 || !avaliableBlackMove)
            return GameCondition.WHITE_WINS;
        
        return GameCondition.NO_WINNER;
    }
    
    void afterMoveAction()
    {
        promoteKings();
        
        GameCondition condition = getGameCondition();
        
        if (condition == GameCondition.WHITE_WINS || condition == GameCondition.BLACK_WINS)
        {
            state = GameState.WAITING_FOR_ENDGAME;
            
            PieceColor winner;
            
            if (condition == GameCondition.WHITE_WINS)
                winner = PieceColor.WHITE;
            else
                winner = PieceColor.BLACK;
            
            sendPacket(p1.socket, new GameOverPacket(winner));
            sendPacket(p2.socket, new GameOverPacket(winner));
        }
    }
    
    private void handleLoginPacket(Socket socket, Packet packet)
    {
        LoginPacket login = (LoginPacket) packet;

        if (socket == p1.socket)
            p1.name = login.playerName;

        if (socket == p2.socket)
            p2.name = login.playerName;

        if (p1.name != null && p2.name != null)
        {
            sendPacket(p1.socket, new LoginPacket(p2.name));
            sendPacket(p2.socket, new LoginPacket(p1.name));

            state = GameState.PLAYING_GAME;

            startGame();
        }

        println(login.playerName + " logged in.");
    }
    
    private void handleNormalMovePacket(Socket socket, Packet packet)
    {
        NormalMovePacket movePacket = (NormalMovePacket) packet;
                    
        Move move = movePacket.move;

        Piece piece = board.getPiece(move.from.x, move.from.y);

        board.setPiece(move.from.x, move.from.y, Piece.NONE);
        board.setPiece(move.to.x, move.to.y, piece);

        Socket otherSocket;

        if (socket == p1.socket)
            otherSocket = p2.socket;
        else
            otherSocket = p1.socket;

        sendPacket(otherSocket, new SetBoardEmptyPacket(move.from));
        sendPacket(otherSocket, new SetBoardPiecePacket(move.to, piece));

        if (turn == PlayerTurn.PLAYER_1)
            turn = PlayerTurn.PLAYER_2;
        else
            turn = PlayerTurn.PLAYER_1;

        sendPacket(otherSocket, new PlayerTurnPacket());

        afterMoveAction();
    }
    
    private void handleCaptureMovePacket(Socket socket, Packet packet)
    {
        CaptureMovePacket capture = (CaptureMovePacket) packet;
                    
        Move move = capture.move;

        Vector2i capturePos = new Vector2i((move.from.x + move.to.x)/2, (move.from.y + move.to.y)/2);

        Piece piece = board.getPiece(move.from.x, move.from.y);

        board.setPiece(move.from.x, move.from.y, Piece.NONE);
        board.setPiece(move.to.x, move.to.y, piece);
        board.setPiece(capturePos.x, capturePos.y, Piece.NONE);

        Socket otherSocket;

        if (socket == p1.socket)
            otherSocket = p2.socket;
        else
            otherSocket = p1.socket;

        sendPacket(otherSocket, new SetBoardEmptyPacket(move.from));
        sendPacket(otherSocket, new SetBoardPiecePacket(move.to, piece));
        sendPacket(otherSocket, new SetBoardEmptyPacket(capturePos));

        if (!capture.multiCapture)
        {
            if (turn == PlayerTurn.PLAYER_1)
                turn = PlayerTurn.PLAYER_2;
            else
                turn = PlayerTurn.PLAYER_1;

            sendPacket(otherSocket, new PlayerTurnPacket());
        }

        afterMoveAction();
    }
    
    private void handleGameOptionPacket(Socket socket, Packet packet)
    {
        GameOptionPacket gop = (GameOptionPacket) packet;
        
        if (socket == p1.socket)
            p1.choice = gop.option;
        else
            p2.choice = gop.option;
        
        if (p1.choice != null && p2.choice != null)
        {
            if (p1.choice == GameOption.REMATCH && p2.choice == GameOption.REMATCH)
            {
                p1.choice = null;
                p2.choice = null;
                
                startGame();
            }
            else
            {
                if (p1.choice == GameOption.REMATCH)
                    sendPacket(p1.socket, new ServerDisconnectPacket("Opponent left the match."));
                
                if (p2.choice == GameOption.REMATCH)
                    sendPacket(p2.socket, new ServerDisconnectPacket("Opponent left the match."));
                
                handler.removeSocket(p1.socket);
                handler.removeSocket(p2.socket);
                
                nullConnection(p1);
                nullConnection(p2);
            }
        }
    }
    
    private void handleChatPacket(Socket socket, Packet packet)
    {
        ChatPacket chat = (ChatPacket) packet;
        
        String name = socket == p1.socket ? p1.name : p2.name;
        
        String formattedMessage = "<" + name + "> " + chat.message;
        
        println(formattedMessage);
        
        sendPacket(p1.socket, new ChatPacket(formattedMessage));
        sendPacket(p2.socket, new ChatPacket(formattedMessage));
    }
    
    private void handleHeartbeatPacket(Socket socket, Packet packet)
    {
        if (socket == p1.socket)
            p1.lastHeartbeat = System.currentTimeMillis();
        else
            p2.lastHeartbeat = System.currentTimeMillis();
    }

    private void registerHandlers()
    {
        handler.registerHandler(LoginPacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    handleLoginPacket(socket, packet);
                });
        
        handler.registerHandler(NormalMovePacket.class,
                (Socket socket, Packet packet) ->
                {
                    handleNormalMovePacket(socket, packet);
                });
    
        handler.registerHandler(CaptureMovePacket.class, 
                (Socket socket, Packet packet) -> 
                {
                    handleCaptureMovePacket(socket, packet);
                });
        
        handler.registerHandler(GameOptionPacket.class, 
                (Socket socket, Packet packet) ->
                {
                    handleGameOptionPacket(socket, packet);
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
    
    public void startGame()
    {
        turn = random.nextBoolean() ? PlayerTurn.PLAYER_1 : PlayerTurn.PLAYER_2;
        
        if (turn == PlayerTurn.PLAYER_1)
        {
            sendPacket(p1.socket, new PlayerColorPacket(PieceColor.WHITE));
            sendPacket(p2.socket, new PlayerColorPacket(PieceColor.BLACK));
            sendPacket(p1.socket, new PlayerTurnPacket());
        }
        else
        {
            sendPacket(p1.socket, new PlayerColorPacket(PieceColor.BLACK));
            sendPacket(p2.socket, new PlayerColorPacket(PieceColor.WHITE));
            sendPacket(p2.socket, new PlayerTurnPacket());
        }
        
        board.initializeBoard(whiteRows, blackRows);
        
        sendPacket(p1.socket, new CreateBoardPacket(board.getSize(), whiteRows, blackRows));
        sendPacket(p2.socket, new CreateBoardPacket(board.getSize(), whiteRows, blackRows));
    }
    
    public void run()
    {
        println("Starting server");
        
        try
        {

            File file = new File(System.getProperty("user.dir") + File.separator + "config.txt");

            if (!file.exists() || !file.isFile())
            {
                file.createNewFile();
                
                PrintStream out = new PrintStream(file);
                
                out.println(8);
                out.println(8);
                out.println(3);
                out.println(3);
                
                out.close();
            }
            
            BufferedReader buf = new BufferedReader(new FileReader(file));
            
            int boardWidth = Integer.parseInt(buf.readLine());
            int boardHeight = Integer.parseInt(buf.readLine());
            
            whiteRows = Integer.parseInt(buf.readLine());
            blackRows = Integer.parseInt(buf.readLine());
            
            board = new Board(boardWidth, boardHeight);
        }
        
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        registerHandlers();
        
        new Thread(thread).start();
        
        while (true)
        {
            if ((System.currentTimeMillis() - lastHeartBeat)/1000f > 5)
            {
                lastHeartBeat = System.currentTimeMillis();
                
                if (p1.socket != null)
                {
                    if ((System.currentTimeMillis() - p1.lastHeartbeat)/1000f > 12)
                    {
                        sendPacket(p2.socket, new ServerDisconnectPacket("Opponent Timed Out"));
                        
                        handler.removeSocket(p1.socket);
                        handler.removeSocket(p2.socket);
                        
                        nullConnection(p1);
                        nullConnection(p2);
                    }
                    else
                    {
                        sendPacket(p1.socket, new HeartbeatPacket());
                        sendPacket(p2.socket, new HeartbeatPacket());
                    }
                }
                else if (p2.socket != null)
                {
                    if ((System.currentTimeMillis() - p2.lastHeartbeat)/1000f > 12)
                    {
                        sendPacket(p1.socket, new ServerDisconnectPacket("Opponent Timed Out"));
                        
                        handler.removeSocket(p1.socket);
                        handler.removeSocket(p2.socket);
                        
                        nullConnection(p1);
                        nullConnection(p1);
                    }
                    else
                    {
                        sendPacket(p1.socket, new HeartbeatPacket());
                        sendPacket(p2.socket, new HeartbeatPacket());
                    }
                }
            }
            
            synchronized (packetsToSend)
            {                
                Iterator<PacketPackage> iter = packetsToSend.iterator();
                
                boolean quit = false;
                
                while (!quit && iter.hasNext())
                {
                    PacketPackage pack = iter.next();
                    System.out.println(pack.packet.getClass().getSimpleName());
                    if (!PacketUtil.sendPacket(pack.socket, pack.packet))
                    {
                        Socket otherSocket;
                        
                        if (pack.socket == p1.socket)
                            otherSocket = p2.socket;
                        else
                            otherSocket = p1.socket;
                        
                        PacketUtil.sendPacket(otherSocket, new ServerDisconnectPacket("Opponent Disconnected"));
                        
                        handler.removeSocket(p1.socket);
                        handler.removeSocket(p2.socket);
                        
                        nullConnection(p1);
                        nullConnection(p2);
                        
                        quit = true;
                    }
                }
                
                packetsToSend.clear();
            }
            
            try
            {
                Thread.sleep(10);
            }
            
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
