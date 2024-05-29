import cs251.lab2.*;

import java.util.Random;
import java.util.random.*;

public class Gomoku implements GomokuInterface{
    //Initialize two variables useful to the overall program:
    //This one is meant to check where we clicked
    private Square[][] boardState = new Square[getNumCols()][getNumRows()];

    //This one stores which turn it is
    private Square currentTurn = Square.EMPTY;

    //Initialize a variable to detemine which game we're in
    private int gameNumber = 1;
    //private TurnResult[] winner = new TurnResult[];

    public static void showGUI(GomokuInterface game){}

    /**
     * Initialize game based on if it is the first one played
     */
    public void initializeGame()
    {
        //Randomly choose whether to have cross or ring go first
        int firstTurnChoice = new Random().nextInt(Square.values().length);

        if(gameNumber == 1)
        {
            //Here we make sure the starting player is not an "empty" or null player so we start with cross or ring player
            do
            {
                currentTurn = Square.values()[firstTurnChoice];
            }while(currentTurn == Square.EMPTY);

            //Build our initial board state
            for(int row = 0; row < getNumRows(); row++)
            {
                for(int col = 0; col < getNumCols(); col++)
                {
                    boardState[col][row] = Square.EMPTY;
                }
            }

            if(currentTurn == Square.RING)
            {
                aiMove();
            }
        }
        else
        {
            //Rebuild board state every time there is a new game
            for(int row = 0; row < getNumRows(); row++)
            {
                for(int col = 0; col < getNumCols(); col++)
                {
                    boardState[col][row] = Square.EMPTY;
                }
            }
        }
    }

    public void initComputerPlayer(String opponent){}

    public int getNumRows()
    {
        return DEFAULT_NUM_ROWS;
    }

    public int getNumCols()
    {
        return DEFAULT_NUM_COLS;
    }

    public int getNumInLineForWin()
    {
        return SQUARES_IN_LINE_FOR_WIN;
    }

    public String getCurrentBoardAsString()
    {
        //Rebuild board string after a move
        String boardAsString = "";

        for(int row = 0; row < getNumRows(); row++)
        {
            for(int col = 0; col < getNumCols(); col++)
            {
                boardAsString = boardAsString + boardState[col][row].toChar();
            }
            if(row != getNumRows() - 1)
            {
                boardAsString = boardAsString + "\n";
            }
        }

        return boardAsString;
    }

    public Square getCurrentTurn()
    {
        return currentTurn;
    }

    public TurnResult handleClickAt(int row, int col)
    {
        String currentBoard = getCurrentBoardAsString();

        //This general if statement catches whether the square we chose is considered to be empty or not
        //Does not need an else since if it isn't empty, nothing happens.
        if(boardState[col][row] == Square.EMPTY)
        {
            //Human players are cross when AI is involved. Otherwise flip flops
            if(currentTurn == Square.CROSS)
            {
                boardState[col][row] = getCurrentTurn();
                currentBoard = getCurrentBoardAsString();
                TurnResult humanWin = checkForWin(row, col);

                if(humanWin == TurnResult.CROSS_WINS || humanWin == TurnResult.RING_WINS)
                {
                    gameNumber++;
                    return humanWin;
                }
                else
                {
                    currentTurn = Square.RING;
                }
            }
        }

        return aiMove();
    }

    /**
     * No parameters, meant to make AI move and check if it won
     * @return GAME_NOT_OVER or a win
     */
    public TurnResult aiMove()
    {
        TurnResult aiWin = TurnResult.GAME_NOT_OVER;
        boolean foundEmptyTile = false;

        //Finding the next available tile to place at
        for(int row = 0; row < getNumCols() && !foundEmptyTile; row++)
        {
            for(int col = 0; col < getNumRows(); col++)
            {
                if(boardState[col][row] == Square.EMPTY)
                {
                    boardState[col][row] = Square.RING;
                    aiWin = checkForWin(row, col);
                    foundEmptyTile = true;
                    break;
                }
            }
        }

        if(aiWin == TurnResult.RING_WINS)
        {
            gameNumber++;
            return aiWin;
        }
        else
        {
            currentTurn = Square.CROSS;
        }

        return TurnResult.GAME_NOT_OVER;
    }

    /**
     * Handle any player wins
     * Checks for win in a 9x9 square around the tile placed at
     * @param row
     * @param col
     * @return GAME_NOT_OVER or a win
     */
    public TurnResult checkForWin(int row, int col)
    {
        Square startSquare = getCurrentTurn();
        boolean didWin = false;

        //Only 4 directions needed for check: West, South, Southwest, and Nothwest
        for(Directions dir : Directions.values())
        {
            if(tilesInARow(row, col, dir) >= getNumInLineForWin())
            {
                didWin = true;
                break;
            }
        }

        //Returns a win result if there is one
        if(didWin)
        {
            if(startSquare == Square.CROSS)
            {
                return TurnResult.CROSS_WINS;
            }
            else if(startSquare == Square.RING)
            {
                return TurnResult.RING_WINS;
            }
        }

        return TurnResult.GAME_NOT_OVER;
    }

    /**
     * Make sure the choice of coordinate we are attempting to check is within bounds of the board
     * @param row
     * @param col
     * @return Either a good coordinate or a null one
     */
    public Square safelyGetCellValue(int row, int col)
    {
        if(col >= 0 && col < getNumCols() && row >= 0 && row < getNumRows())
        {
            return boardState[col][row];
        }
        return Square.EMPTY;
    }

    /**
     * Checks our 9x9 area for how many tiles are in a row in a given direction
     * @param row
     * @param col
     * @param dir
     * @return Number of tiles in a row
     */
    public int tilesInARow(int row, int col, Directions dir)
    {
        int numInLine = 0;
        Square startSquare = getCurrentTurn();
        int startCol = col;
        int startRow = row;

        //Back up to the edge of the board or where tiles match to start search
        for(int i = 0; i <= getNumInLineForWin(); i++)
        {
            Square checkingSquare = safelyGetCellValue((row - (i * dir.getRowOffset())), (col - (i * dir.getColOffset())));
            if(checkingSquare != startSquare)
            {
                startCol = col - (i - 1) * dir.getColOffset();
                startRow = row - (i - 1) * dir.getRowOffset();
                break;
            }
        }

        /**
         * Once we have a tile to start from, check neighboring tiles in a direction to see if they match
         * Only have to check 4 directions this way instead of 8. Go out to up to 9 squares for the check
         * as from the tile that got clicked on to the max amount of matching tiles in a row required for
         * a win from there is 5, we can check a 9x9 area to ensure we will get a win if it is there.
         */
        for(int j = 0; j < 10; j++)
        {
            Square checkingSquare = safelyGetCellValue(startRow + (j * dir.getRowOffset()), startCol + (j * dir.getColOffset()));
            if(checkingSquare == startSquare)
            {
                numInLine++;
                
                if(numInLine == 5)
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return numInLine;
    }

    public static void main(String args[]) {
        Gomoku game = new Gomoku();
        
        if(args.length > 0)
        {
            game.initComputerPlayer(args[0]);
        }

        GomokuGUI.showGUI(game);
    }
}