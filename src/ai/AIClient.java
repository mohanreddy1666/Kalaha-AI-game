package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */

    public int getMove(GameState currentBoard)
    {
        int myMove;
        myMove = findbestmove(currentBoard);
        return myMove;
    }

    private static final int initialdepth = 0;
    private static final int Timelimit = 4999;

    private int currentDepth;
    private int bestMove;
    private int globalBestMove;
    private long startTime;
    private boolean timeout;
    public int max_player = 1;
    public int min_player = 2;



    public int findbestmove(GameState currentboard)
    {
        timeout = false;
        startTime= System.currentTimeMillis();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int player;
        if(currentboard.getNextPlayer()==1){
            player=max_player;

        }
        else {
            player = min_player;
        }


        int d = 0;
        while(true)
        {
            if(d>0)
                globalBestMove=bestMove;
            currentDepth= initialdepth+d;
            maximizer(currentboard,currentDepth,alpha,beta,player);
            if(timeout){
                return globalBestMove;
            }
            d++;
        }
        
    }

    

    /*
     * @param GameState- curent game state
     *
     * @param depth- depth for search
     *
     * @param player- if AI is maximizer or minimizer
     *
     * @param alpha- Aplha value
     *
     * @param beta- Beta value
     *
     * @param startTime- current system time is sent to function
     *
     * @return bestmove among the best possible moves
     */
    public int maximizer(GameState kalaha,int depth,int alpha,int beta,int player)
    {
        int nextplayer;
        if(player == 1)
            nextplayer = 2;
        else
            nextplayer = 1;

        if(System.currentTimeMillis()-startTime> Timelimit)
        {
            timeout=true;
            return alpha;
        }
        if(kalaha.gameEnded()||depth==0)
        {
            return  getscore(kalaha,player);
        }


        for(int i = 1; i < 7; i++)
        {
            GameState kalahaboard = kalaha.clone();
            if(kalahaboard.moveIsPossible(i)){
                kalahaboard.makeMove(i);
                int val = minimizer(kalahaboard,depth-1,alpha,beta,nextplayer);
                if(val>alpha)
                {
                    alpha=val;
                    if(depth == currentDepth)
                    {
                        bestMove=i;
                    }
                }
                if(alpha>=beta)
                    return alpha;
            }
        }
        return alpha;
    }

    /*
     * @param GameState - current board
     *
     * @param depth- depth for search
     *
     * @param player- if AI is maximizer or minimizer
     *
     * @param alpha- Aplha value
     *
     * @param beta- Beta value
     *
     * @param startTime- current system time is sent to function
     *
     * @return bestmove among the best possible moves
     */
    public int minimizer(GameState kalaha,int depth,int alpha,int beta,int player)
    {
        int nextplayer;
        if(player==1)
            nextplayer = 2;
        else
            nextplayer = 1;


        if(depth==0||kalaha.gameEnded())
        {
            return getscore(kalaha,player);
        }
        for(int i = 1; i < 7;i++)
        {
            GameState kalahaBoard = kalaha.clone();
            if(kalahaBoard.moveIsPossible(i))
            {
                kalahaBoard.makeMove(i);
                int val = maximizer(kalahaBoard,depth-1,alpha,beta, nextplayer);
                if(val<=beta)
                {
                    beta = val;
                }
                if(alpha>=beta)
                {
                    return beta;
                }
            }
        }
        return beta;
    }

    /*
    *
    * @param GameState current board
    *
    * @param player
    *
    * @return utility score
     */
    public int getscore(GameState kalaha,int player){
        int score;
        int nextplayer;
        if(player == 1) {
            nextplayer = 2;
            score = kalaha.getScore(player) - kalaha.getScore(nextplayer);
        }
        else {
            nextplayer = 1;
            score = kalaha.getScore(nextplayer) - kalaha.getScore(player);
        }

        return score;
    }


}