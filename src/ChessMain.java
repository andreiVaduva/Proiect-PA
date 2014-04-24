import java.io.*;
import java.util.ArrayList;
import java.util.Random;

enum State {

    ACTIV, INACTIVE, NONE;
}

enum Color {

    WHITE, BLACK, NONE;
}

enum Turn {

    ENGINE, PLAYER, END;
}

/*
 * Clasa Position este folosită pentru a determina poziţia unei piese pe tablă.
 */
class Position {

    public int line;
    public int column;

    public Position(int line, int column) {

        this.line = line;
        this.column = column;
    }
}

/*
 * Clasa Moves este folosită pentru a crea mutările de pe tablă.
 */
class Moves {

    public int currentLine;
    public int currentColumn;
    public int futureLine;
    public int futureColumn;

    public Moves(int currentLine, int currentColumn, int futureLine,
            int futureColumn) {

        this.currentLine = currentLine;
        this.currentColumn = currentColumn;
        this.futureLine = futureLine;
        this.futureColumn = futureColumn;
    }
    
    public Moves(){
    	this(0,0,0,0);
    }
    
    public Moves clone(){
    	return new Moves(currentLine, currentColumn, futureLine, futureColumn);
    }
    
    public String toString(){
        String s = new String();
        s = "(" + currentLine + " " + currentColumn + " " + futureLine + " " + futureColumn + ")";
        return s; 
    }
}

class Pieces {

    public static final int WHITE_PAWN = 1;
    public static final int BLACK_PAWN = -1;
    public static final int WHITE_HORSE = 2;
    public static final int BLACK_HORSE = -2;
    public static final int WHITE_BISHOP = 3;
    public static final int BLACK_BISHOP = -3;
    public static final int WHITE_ROOK = 4;
    public static final int BLACK_ROOK = -4;
    public static final int WHITE_QUEEN = 5;
    public static final int BLACK_QUEEN = -5;
    public static final int WHITE_KING = 6;
    public static final int BLACK_KING = -6;
    public static final int BLANK = 0;
    
}

public class ChessMain {

    /*
     * Starea iniţială a jocului este inactivă, engine-ul are culoarea implicită
     * negru şi primul la rând pentru a muta este jucătorul.
     */
    public Color engineColor = Color.BLACK;
    public Turn turn = Turn.PLAYER;
    public static final int COLUMNS = 8;
    public static final int ROWS = 8;
    public int[][] table;
    public State state = State.INACTIVE;
    public int colorState = -1;
    public boolean colorChanged = false;
    public static ArrayList<Moves> allmoves = new ArrayList<Moves>();
    public String castling [] = {"e1g1","e8g8"};
    public String wild_castling [] = {"e1c1","e8c8"};
    public boolean king_side_castling = false;
    public boolean queen_side_castling = false;
    public boolean rook1_moves = true;
    public boolean rook2_moves = true;
    public static FileWriter file ;//= new FileWriter("game.txt");
    public static String finalcommand ;
    public static int MAXDEPTH = 2;
    public static int CHECKMATE = 100000;
    
    public static void main(String[] args) throws IOException {

        String command = new String();
        BufferedReader buff = new BufferedReader(new InputStreamReader(
                System.in));
        ChessMain chess = new ChessMain();

        System.out.println("feature sigint=0");
        System.out.flush();
        System.out.println("feature sigterm=0");
        System.out.flush();
        System.out.println("feature usermove=1");
        System.out.flush();

        do {
            if (chess.turn == Turn.ENGINE && chess.state == State.ACTIV) {
               // allmoves = chess.generateAllMoves();
               // System.out.println(allmoves);
                if (chess.moveEngine() == false) {
                    chess.turn = Turn.END;
                } else {
                    chess.turn = Turn.PLAYER;
                }
            }
            /*
             * Dacă engine-ul nu mai are mutări valide posibile şi starea
             * jocului este activă atunci dăm resign apoi avem varianta new sau
             * quit.
             */
            if (chess.turn == Turn.END && chess.state == State.ACTIV) {
                chess.state = State.INACTIVE;
                System.out.println(finalcommand);
                System.out.flush();
                continue;
            }
            
            command = buff.readLine();

            if (command.startsWith("usermove") && chess.state == State.ACTIV) {
                chess.movePlayer(command.split(" ")[1]);
                chess.turn = Turn.ENGINE;
                continue;
            }

            if (command.startsWith("xboard")) {
                chess.state = State.ACTIV;
                continue;
            }
            /*
             * Sunt reiniţializate toate variabilele.
             */
            if (command.startsWith("new")) {
                chess.colorState = -1;
                chess.turn = Turn.PLAYER;
                //chess.engineColor = Color.BLACK;
                //chess.engineColor = Color.BLACK;
                chess.state = State.ACTIV;
                chess.initTable();
                try{
                    file = new FileWriter("game.txt");
                    file.append("A INCEPUT UN ALT JOC");
                }
                catch(Exception e){
                    
                }
                
                //chess.printTable();
                //chess.metodaVerifica();
                chess.colorChanged = false;
                continue;
            }

            if (command.startsWith("white") && chess.state == State.ACTIV) {
                if (chess.colorChanged == true) {
                    chess.turn = Turn.ENGINE;
                    //chess.engineColor = Color.WHITE;
                    chess.colorState = 1;
                    chess.colorChanged = false;
                } else {
                    chess.colorChanged = true;
                }
                continue;
            }

            if (command.startsWith("black") && chess.state == State.ACTIV) {
                if (chess.colorChanged == true) {
                    chess.turn = Turn.ENGINE;
                    //chess.engineColor = Color.BLACK;
                    chess.colorState = -1;
                    chess.colorChanged = false;
                } else {
                    chess.colorChanged = true;
                }
                continue;
            }
            if (command.startsWith("force")) {
                continue;
            }
            if (command.startsWith("go")) {
                continue;
            }
            if (command.startsWith("quit")) {
                file.close();
                return;
            }
            
            //chess.printTable();
            
            
        } while (true);
    }

    public void initTable() {

        table = new int[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLUMNS; ++j) {
                table[i][j] = 0;
            }
        }

        for (int i = 0; i < COLUMNS; ++i) {
            table[1][i] = Pieces.WHITE_PAWN;
            table[6][i] = Pieces.BLACK_PAWN;
        }

        table[0][1] = table[0][6] = Pieces.WHITE_HORSE;
        table[7][1] = table[7][6] = Pieces.BLACK_HORSE;

        table[0][2] = table[0][5] = Pieces.WHITE_BISHOP;
        table[7][2] = table[7][5] = Pieces.BLACK_BISHOP;

        table[0][0] = table[0][7] = Pieces.WHITE_ROOK;
        table[7][0] = table[7][7] = Pieces.BLACK_ROOK;

        table[0][3] = Pieces.WHITE_QUEEN;
        table[7][3] = Pieces.BLACK_QUEEN;

        table[0][4] = Pieces.WHITE_KING;
        table[7][4] = Pieces.BLACK_KING;
    }

    /*
     * Metodă pentru a decodifica comanda primită de la xboard din partea
     * playerul-ui şi returnează mutarea reprezentând indicii din matrice.
     */
    public Moves decodeMove(String moveCommand) {

        int currentLine, currentColumn, futureLine, futureColumn;

        currentColumn = moveCommand.charAt(0) - 'a';
        currentLine = moveCommand.charAt(1) - '1';
        futureColumn = moveCommand.charAt(2) - 'a';
        futureLine = moveCommand.charAt(3) - '1';

        Moves move = new Moves(currentLine, currentColumn, futureLine,
                futureColumn);

        return move;
    }

    /*
     * Metodă pentru a codifica o mutare a engine-ului pentru a transmite-o
     * xbordului, returnează StringBufferul de forma a2a3 etc. .
     */
    public StringBuffer encodeMove(Moves move) {

        StringBuffer moveEngine = new StringBuffer(20);

        moveEngine.append("move ");
        moveEngine.append((char) ('a' + move.currentColumn));
        moveEngine.append((char) ('1' + move.currentLine));
        moveEngine.append((char) ('a' + move.futureColumn));
        moveEngine.append((char) ('1' + move.futureLine));

        return moveEngine;
    }

    /*
     * Mutarea playerul-ui.
     */
    public boolean movePlayer(String moveCommand) throws IOException {
      
        Moves move = decodeMove(moveCommand);

        if (moveCommand.endsWith("q")) {
            if (makeMove(move, 1)) {
                return true;
            } else {
                return false;
            }   
        } 
        
        if (castling[0].equals(moveCommand) || castling[1].equals(moveCommand)){
            makeMoveCastling(move);  
            return true;
        }
            
        if (wild_castling[0].equals(moveCommand) || wild_castling[1].equals(moveCommand)){
            makeMoveCastlingWild(move);
            return true;
        }
            
        makeMove(move,0);
        return true;
    }
    
    public ArrayList<Moves> generateAllMoves() {

        ArrayList<Moves> allmoves = new ArrayList<Moves>();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_PAWN * (-colorState)) {
                    allmoves.addAll(generateAllMovesPaws(new Position(i, j)));
                }
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_HORSE * (-colorState)) {
                    allmoves.addAll(generateAllMovesHorse(new Position(i, j)));
                }
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_BISHOP * (-colorState)) {
                    allmoves.addAll(generateAllMovesBishop(new Position(i, j)));
                }
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_ROOK * (-colorState)) {
                    allmoves.addAll(generateAllMovesRook(new Position(i, j)));
                }
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_QUEEN * (-colorState)) {
                    allmoves.addAll(generateAllMovesQueen(new Position(i, j)));
                }
                if ((-colorState) * table[i][j] < 0 && table[i][j] == Pieces.BLACK_KING * (-colorState)) {
                    allmoves.addAll(generateAllMovesKing(new Position(i, j)));
                }
            }
        }
        
        allmoves.addAll(generateCastlingMoves());
        return allmoves;
    }

    public ArrayList<Moves> generateAllMovesPaws(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + colorState < ROWS && initialposition.line + colorState >= 0) {
            if (table[initialposition.line + colorState][initialposition.column] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + colorState, initialposition.column));
            }
        }

        if (initialposition.line + colorState < ROWS && initialposition.column - 1 >= 0 && initialposition.line + colorState >= 0) {
            if ((-colorState) * table[initialposition.line + colorState][initialposition.column - 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + colorState, initialposition.column - 1));
            }
        }

        if (initialposition.line + colorState < ROWS && initialposition.column + 1 < COLUMNS && initialposition.line + colorState >= 0) {
            if ((-colorState) * table[initialposition.line + colorState][initialposition.column + 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + colorState, initialposition.column + 1));
            }
        }

        if (colorState == 1) {
            if (initialposition.line == 1) {
                if (table[initialposition.line + 1][initialposition.column] == 0) {
                    if (table[initialposition.line + 2][initialposition.column] == 0) {
                        moves.add(new Moves(initialposition.line, initialposition.column,
                                initialposition.line + 2, initialposition.column));
                    }
                }
            }
        }

        if (colorState == -1) {
            if (initialposition.line == 6) {
                if (table[initialposition.line - 1][initialposition.column] == 0) {
                    if (table[initialposition.line - 2][initialposition.column] == 0) {
                        moves.add(new Moves(initialposition.line, initialposition.column,
                                initialposition.line - 2, initialposition.column));
                    }
                }
            }
        }

        return moves;
    }

    public ArrayList<Moves> generateAllMovesHorse(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + 2 < ROWS && initialposition.column + 1 < COLUMNS) {
            if ((-colorState) * table[initialposition.line + 2][initialposition.column + 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 2, initialposition.column + 1));
            }
        }

        if (initialposition.line + 2 < ROWS && initialposition.column - 1 >= 0) {
            if ((-colorState) * table[initialposition.line + 2][initialposition.column - 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 2, initialposition.column - 1));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column + 2 < COLUMNS) {
            if ((-colorState) * table[initialposition.line + 1][initialposition.column + 2] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 1, initialposition.column + 2));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column - 2 >= 0) {
            if ((-colorState) * table[initialposition.line + 1][initialposition.column - 2] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 1, initialposition.column - 2));
            }
        }

        if (initialposition.line - 2 >= 0 && initialposition.column + 1 < COLUMNS) {
            if ((-colorState) * table[initialposition.line - 2][initialposition.column + 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 2, initialposition.column + 1));
            }
        }

        if (initialposition.line - 2 >= 0 && initialposition.column - 1 >= 0) {
            if ((-colorState) * table[initialposition.line - 2][initialposition.column - 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 2, initialposition.column - 1));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column + 2 < COLUMNS) {
            if ((-colorState) * table[initialposition.line - 1][initialposition.column + 2] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 1, initialposition.column + 2));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column - 2 >= 0) {
            if ((-colorState) * table[initialposition.line - 1][initialposition.column - 2] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 1, initialposition.column - 2));
            }
        }

        return moves;
    }

    public ArrayList<Moves> generateAllMovesBishop(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int line, column;
        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS && (column + 1) < COLUMNS) {
            if (table[line + 1][column + 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column + 1));
                line++;
                column++;
            } else if ((-colorState) * table[line + 1][column + 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column + 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS && (column - 1) >= 0) {
            if (table[line + 1][column - 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column - 1));
                line++;
                column--;
            } else if ((-colorState) * table[line + 1][column - 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column - 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0 && (column + 1) < COLUMNS) {
            if (table[line - 1][column + 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column + 1));
                line--;
                column++;
            } else if ((-colorState) * table[line - 1][column + 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column + 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0 && (column - 1) >= 0) {
            if (table[line - 1][column - 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column - 1));
                line--;
                column--;
            } else if ((-colorState) * table[line - 1][column - 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column - 1));
                break;
            } else {
                break;
            }
        }

        return moves;
    }

    public ArrayList<Moves> generateAllMovesRook(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int line, column;
        line = initialposition.line;
        column = initialposition.column;

        while ((line + 1) < ROWS) {
            if (table[line + 1][column] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column));
                line++;
            } else if ((-colorState) * table[line + 1][column] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line + 1, column));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((line - 1) >= 0) {
            if (table[line - 1][column] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column));
                line--;
            } else if ((-colorState) * table[line - 1][column] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line - 1, column));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((column + 1) < COLUMNS) {
            if (table[line][column + 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line, column + 1));
                column++;
            } else if ((-colorState) * table[line][column + 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line, column + 1));
                break;
            } else {
                break;
            }
        }

        line = initialposition.line;
        column = initialposition.column;

        while ((column - 1) >= 0) {
            if (table[line][column - 1] == 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line, column - 1));
                column--;
            } else if ((-colorState) * table[line][column - 1] > 0) {
                moves.add(new Moves(initialposition.line, initialposition.column, line, column - 1));
                break;
            } else {
                break;
            }
        }

        return moves;
    }

    public ArrayList<Moves> generateAllMovesQueen(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        moves.addAll(generateAllMovesBishop(initialposition));
        moves.addAll(generateAllMovesRook(initialposition));

        return moves;
    }

    public ArrayList<Moves> generateAllMovesKing(Position initialposition) {

        ArrayList<Moves> moves = new ArrayList<Moves>();

        if (initialposition.line + 1 < ROWS) {
            if ((-colorState) * table[initialposition.line + 1][initialposition.column] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 1, initialposition.column));
            }
        }

        if (initialposition.line - 1 >= 0) {
            if ((-colorState) * table[initialposition.line - 1][initialposition.column] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 1, initialposition.column));
            }
        }

        if (initialposition.column + 1 < COLUMNS) {
            if ((-colorState) * table[initialposition.line][initialposition.column + 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line, initialposition.column + 1));
            }
        }

        if (initialposition.column - 1 >= 0) {
            if ((-colorState) * table[initialposition.line][initialposition.column - 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line, initialposition.column - 1));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column + 1 < COLUMNS) {
            if ((-colorState) * table[initialposition.line + 1][initialposition.column + 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 1, initialposition.column + 1));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column + 1 < COLUMNS) {
            if ((-colorState) * table[initialposition.line - 1][initialposition.column + 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 1, initialposition.column + 1));
            }
        }

        if (initialposition.line + 1 < ROWS && initialposition.column - 1 >= 0) {
            if ((-colorState) * table[initialposition.line + 1][initialposition.column - 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line + 1, initialposition.column - 1));
            }
        }

        if (initialposition.line - 1 >= 0 && initialposition.column - 1 >= 0) {
            if ((-colorState) * table[initialposition.line - 1][initialposition.column - 1] >= 0) {
                moves.add(new Moves(initialposition.line, initialposition.column,
                        initialposition.line - 1, initialposition.column - 1));
            }
        }

        return moves;
    }
    
    public ArrayList<Moves> generateCastlingMoves(){
        ArrayList<Moves> moves = new ArrayList<Moves>();
        if(colorState == 1){
            if(rook2_moves && kingCastlingCondition())
                moves.add(new Moves(7,4,7,6));
            if(rook1_moves && queenCastlingCondition())
                moves.add(new Moves(7,4,7,2));
        }
        
        if(colorState == -1){
            if(rook2_moves && kingCastlingCondition())
                moves.add(new Moves(0,4,0,6));
            if(rook1_moves && queenCastlingCondition())
                moves.add(new Moves(0,4,0,2));
        }
        return moves;
    }

    public int[][] tableCopy(int[][] table) {

        int[][] table_copy = new int[ROWS][COLUMNS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                table_copy[i][j] = table[i][j];
            }
        }

        return table_copy;
    }

    public ArrayList<Moves> checkMoves() {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int[][] table_copy;

        for (Moves move : allmoves) {
            table_copy = tableCopy(table);
            table_copy[move.futureLine][move.futureColumn] = table_copy[move.currentLine][move.currentColumn];
            table_copy[move.currentLine][move.currentColumn] = Pieces.BLANK;
            if (!check(table_copy)) {
                moves.add(move);
            }
        }
        return moves;
    }
    
    public ArrayList<Moves> checkMoves(ArrayList<Moves> allmoves) {

        ArrayList<Moves> moves = new ArrayList<Moves>();
        int[][] table_copy;

        for (Moves move : allmoves) {
            table_copy = tableCopy(table);
            table_copy[move.futureLine][move.futureColumn] = table_copy[move.currentLine][move.currentColumn];
            table_copy[move.currentLine][move.currentColumn] = Pieces.BLANK;
            if (!check(table_copy)) {
                moves.add(move);
            }
        }
        return moves;
    }

    
    public int eval(int depth){
    	int score = 0;
    	
    	for(int i = 0; i < 8; i++){
    		for(int j = 0; j < 8; j++){
    			switch(colorState *table[i][j]){
    			case Pieces.WHITE_PAWN:
    				score += 100; break;
    			case Pieces.WHITE_BISHOP: 
    			case Pieces.WHITE_HORSE:
    				score += 300; break;
    			case Pieces.WHITE_ROOK:
    				score += 500; break;
    			case Pieces.WHITE_QUEEN:
    				score += 900; break;
    				
    			}
    		}
    	}
    	
    	if(depth %2 != 0)
    		depth *= -1;
    	
    	return score;
    }
    
    public Pair<Moves, Integer> negaMax(int[][] level_table, int depth) throws IOException{
    	
    	if(depth == MAXDEPTH)
    		return new Pair<Moves, Integer>(null,eval(depth));
    	
    	
    	int[][] table_copy = tableCopy(level_table);
    	table = table_copy;
    	ArrayList<Moves> playerMoves = generateAllMoves();
    	playerMoves = checkMoves(playerMoves);
    	Pair<Moves, Integer> score, max = new Pair<Moves, Integer>(null,  -CHECKMATE - 1);
    	
    	
    	if(playerMoves.size() == 0){
    		System.out.println("probleme size");
    		return new Pair<Moves, Integer>(null, (depth%2 == 0)?(-CHECKMATE):(CHECKMATE));
    	
    	}
    	colorState *= -1;
    	for(Moves move:playerMoves){
    		
    		StringBuffer moveEngine = encodeMove(move);         
            if (castling[0].equals(moveEngine) || castling[1].equals(moveEngine)){
            	colorState *= -1;
                makeMoveCastling(move);  
                colorState *= -1;
            } else if (wild_castling[0].equals(moveEngine) || wild_castling[1].equals(moveEngine)){
            	colorState *= -1;
            	makeMoveCastlingWild(move);
            	colorState *= -1;
            } else if (move.futureLine == 0 || move.futureLine == 7) {
                makeMove(move, 1);
            } else {
                makeMove(move, 0);
            }
            
            score = negaMax(table, depth + 1);
            
            if( -score.second > max.second){
            	max.second = -score.second;
            	System.out.println("un socor "+ depth +" "+ max.second);
            	max.first = move.clone();
            }
            
            
            table_copy = tableCopy(level_table);
        	table = table_copy;
    	}
    	
    	colorState *= -1;
    	return max;
    }
    
    public boolean moveEngine() throws IOException{
    	Pair<Moves, Integer> move = negaMax(tableCopy(table), 0);
    	if(move.first == null){
    		if(Math.abs(move.second) == CHECKMATE){
    			if(colorState == -1)
                    finalcommand = "1-0 {White mates}";
                else if (colorState == 1 && check(table))    
                    finalcommand = "0-1 {Black mates}";
    			return false;
    		} else {
    			if(colorState == -1)
                    finalcommand = "1/2-1/2 {Stalemate}";
                else if (colorState == 1 && !check(table))   
                    finalcommand = "1/2-1/2 {Stalemate}";       
                return false;
    		}
    	}
    	
    	StringBuffer moveEngine = encodeMove(move.first);
    	 System.out.println(moveEngine);
         System.out.flush();
    	if (castling[0].equals(moveEngine) || castling[1].equals(moveEngine)){
            makeMoveCastling(move.first);  
            return true;
        }
            
        if (wild_castling[0].equals(moveEngine) || wild_castling[1].equals(moveEngine)){
            makeMoveCastlingWild(move.first);
            return true;
        }
        
        if (move.first.futureLine == 0 || move.first.futureLine == 7) {
            makeMove(move.first, 1);
        } else {
            makeMove(move.first, 0);
        }
       
        return true;
    }
    
  /*  public boolean moveEngine() throws IOException{
        
        //allmoves = generateAllMoves();
        allmoves = checkMoves();
        System.out.println(allmoves);
        
        if (allmoves.size() == 0 && check(table)){
            if(colorState == -1)
                finalcommand = "1-0 {White mates}";
            else if (colorState == 1 && check(table))    
                finalcommand = "0-1 {Black mates}";       
            return false;
        }
                
        if (allmoves.size() == 0 && !check(table)){
            if(colorState == -1)
                finalcommand = "1/2-1/2 {Stalemate}";
            else if (colorState == 1 && !check(table))   
                finalcommand = "1/2-1/2 {Stalemate}";       
            return false;
        }
        
                
        Moves move = allmoves.get(new Random().nextInt(allmoves.size()));
        StringBuffer moveEngine = encodeMove(move);
        
        if (castling[0].equals(moveEngine) || castling[1].equals(moveEngine)){
            makeMoveCastling(move);  
            return true;
        }
            
        if (wild_castling[0].equals(moveEngine) || wild_castling[1].equals(moveEngine)){
            makeMoveCastlingWild(move);
            return true;
        }
        
        if (move.futureLine == 0 || move.futureLine == 7) {
            makeMove(move, 1);
        } else {
            makeMove(move, 0);
        }
        System.out.println(moveEngine);
        System.out.flush();
        
        
        colorState = 0 - colorState;
        allmoves = new ArrayList<Moves>();
        allmoves = generateAllMoves();
        allmoves = checkMoves();
                
        if (allmoves.size() == 0 && check(table)){
            if(-colorState == -1)
                finalcommand = "0-1 {Black mates}";
            else if (-colorState == 1)      
                finalcommand = "1-0 {White mates}";       
            return false;
        }
                
        if (allmoves.size() == 0 && !check(table)){
            if(-colorState == -1)
                finalcommand = "1/2-1/2 {Stalemate}";
            else if (-colorState == 1)    
                finalcommand = "1/2-1/2 {Stalemate}";     
            return false;
        }
                
        //Revenim la culoarea Engine-ului nostru
        colorState = 0 - colorState;
        return true;
    }*/

    public boolean check_up(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingLine + 1; i < ROWS; i++) {
            if ((-colorState) * table_copy[i][kingColumn] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[i][kingColumn] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
                        || table_copy[i][kingColumn] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean check_down(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingLine - 1; i >= 0; i--) {
            if ((-colorState) * table_copy[i][kingColumn] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[i][kingColumn] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
                        || table_copy[i][kingColumn] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean check_right(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingColumn + 1; i < COLUMNS; i++) {
            if ((-colorState) * table_copy[kingLine][i] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[kingLine][i] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
                        || table_copy[kingLine][i] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean check_left(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, i;
        ownPiece = 0;
        oppPiece = 0;

        for (i = kingColumn - 1; i >= 0; i--) {
            if ((-colorState) * table_copy[kingLine][i] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[kingLine][i] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
                        || table_copy[kingLine][i] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean check_rightUp(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line < ROWS - 1 && column < COLUMNS - 1) {
            line++;
            column++;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
                if (colorState == 1) {
                    if (table_copy[line][column] == Pieces.BLACK_PAWN) {
                        if (line == kingLine + 1 && column == kingColumn + 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean check_rightDown(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line > 0 && column < COLUMNS - 1) {
            line--;
            column++;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
                if (colorState == -1) {
                    if (table_copy[line][column] == Pieces.WHITE_PAWN) {
                        if (line == kingLine - 1 && column == kingColumn + 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean check_leftUp(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line < ROWS - 1 && column > 0) {
            line++;
            column--;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
                if (colorState == 1) {
                    if (table_copy[line][column] == Pieces.BLACK_PAWN) {
                        if (line == kingLine + 1 && column == kingColumn - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean check_leftDown(int[][] table_copy, int kingLine, int kingColumn) {

        int ownPiece, oppPiece, line, column;
        ownPiece = 0;
        oppPiece = 0;
        line = kingLine;
        column = kingColumn;

        while (line > 0 && column > 0) {
            line--;
            column--;
            if ((-colorState) * table_copy[line][column] < 0) {
                ownPiece = 1;
                break;
            }
            if ((-colorState) * table_copy[line][column] > 0) {
                oppPiece = 1;
                break;
            }
        }

        if (ownPiece == 0) {
            if (oppPiece == 1) {
                if (table_copy[line][column] == Pieces.BLACK_BISHOP * colorState
                        || table_copy[line][column] == Pieces.BLACK_QUEEN * colorState) {
                    return true;
                }
                if (colorState == -1) {
                    if (table_copy[line][column] == Pieces.WHITE_PAWN) {
                        if (line == kingLine - 1 && column == kingColumn - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean check_Horse(int[][] table_copy, int kingLine, int kingColumn) {

        if (kingLine + 2 < ROWS && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine + 2][kingColumn + 1] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine + 2 < ROWS && kingColumn - 1 >= 0) {
            if (table_copy[kingLine + 2][kingColumn - 1] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn + 2 < COLUMNS) {
            if (table_copy[kingLine + 1][kingColumn + 2] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn - 2 >= 0) {
            if (table_copy[kingLine + 1][kingColumn - 2] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine - 2 >= 0 && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine - 2][kingColumn + 1] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine - 2 >= 0 && kingColumn - 1 >= 0) {
            if (table_copy[kingLine - 2][kingColumn - 1] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn - 2 >= 0) {
            if (table_copy[kingLine - 1][kingColumn - 2] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn + 2 < COLUMNS) {
            if (table_copy[kingLine - 1][kingColumn + 2] == Pieces.BLACK_HORSE * colorState) {
                return true;
            }
        }

        return false;
    }

    public boolean check_King(int[][] table_copy, int kingLine, int kingColumn) {

        if (kingLine + 1 < ROWS) {
            if (table_copy[kingLine + 1][kingColumn] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0) {
            if (table_copy[kingLine - 1][kingColumn] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingColumn - 1 >= 0) {
            if (table_copy[kingLine][kingColumn - 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine][kingColumn + 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine + 1][kingColumn + 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn + 1 < COLUMNS) {
            if (table_copy[kingLine - 1][kingColumn + 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingLine + 1 < ROWS && kingColumn - 1 >= 0) {
            if (table_copy[kingLine + 1][kingColumn - 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }
        if (kingLine - 1 >= 0 && kingColumn - 1 >= 0) {
            if (table_copy[kingLine - 1][kingColumn - 1] == Pieces.BLACK_KING * colorState) {
                return true;
            }
        }

        return false;
    }

    public boolean check(int[][] table_copy) {

        int kingLine, kingColumn, i = 0, j = 0, found = 0;

        for (i = 0; i < ROWS; i++) {
            for (j = 0; j < COLUMNS; j++) {
                if (table_copy[i][j] == Pieces.BLACK_KING * (-colorState)) {
                    found = 1;
                    break;
                }
            }
            if (found == 1) {
                break;
            }
        }

        kingLine = i;
        kingColumn = j;
        System.out.println(i + " " + j);
        if(i==8 && j==8)
            return true;

        if (check_up(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_down(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_left(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_right(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_rightUp(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_rightDown(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_leftUp(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_leftDown(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_Horse(table_copy, kingLine, kingColumn)) {
            return true;
        }
        if (check_King(table_copy, kingLine, kingColumn)) {
            return true;
        }

        return false;
    }

    /*
     * Metodă ce realizeaza mutarea pe tablă (matrice).
     */
    public boolean makeMove(Moves move, int changePawn) throws IOException {
        
      //  if(engineColor == Color.BLACK){
            if(table[move.currentLine][move.currentColumn] == -6){
                rook1_moves = false;
                rook2_moves = false;
            } else
            
            if(table[move.currentLine][move.currentColumn] == -4 && move.currentColumn == 0){
                rook1_moves = false;
            } else
            
            if(table[move.currentLine][move.currentColumn] == -4 && move.currentColumn == 7){
                rook2_moves = false;
            } else
      //  }
      //  if(engineColor == Color.WHITE){
            if(table[move.currentLine][move.currentColumn] == 6){
                rook1_moves = false;
                rook2_moves = false;
            } else
            
            if(table[move.currentLine][move.currentColumn] == 4 && move.currentColumn == 0){
                rook1_moves = false;
            } else
            
            if(table[move.currentLine][move.currentColumn] == 4 && move.currentColumn == 7){
                rook2_moves = false;
            }
   //     }  
        if(table[move.futureLine][move.futureColumn] == 0 && table[move.currentLine][move.currentColumn] == 1
                && table[move.futureLine - 1][move.futureColumn] == -1){
                table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
                table[move.currentLine][move.currentColumn] = Pieces.BLANK;
                table[move.futureLine - 1][move.futureColumn] = Pieces.BLANK;
                return true;
        }
        
        if(table[move.futureLine][move.futureColumn] == 0 && table[move.currentLine][move.currentColumn] == -1
                && table[move.futureLine + 1][move.futureColumn] == 1){
                table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
                table[move.currentLine][move.currentColumn] = Pieces.BLANK;
                table[move.futureLine + 1][move.futureColumn] = Pieces.BLANK;        
                return true;
        }
        
        if (changePawn == 0) {
            table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
            table[move.currentLine][move.currentColumn] = Pieces.BLANK;
        } else {
            if (table[move.currentLine][move.currentColumn] == 1) {
                table[move.futureLine][move.futureColumn] = Pieces.WHITE_QUEEN;
            } else if (table[move.currentLine][move.currentColumn] == -1) {
                table[move.futureLine][move.futureColumn] = Pieces.BLACK_QUEEN;
            } else {
                table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
            }
            table[move.currentLine][move.currentColumn] = Pieces.BLANK;
        }
        
        
            
        
            String s;
            s = printTabletoString();
            file.append(s);
            file.append("\n");
            //file.close();
        
        return true;
    }
    
    public boolean makeMoveCastling(Moves move) {

            table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
            table[move.currentLine][move.currentColumn] = Pieces.BLANK;
            table[move.futureLine][move.futureColumn-1] = table[move.currentLine][move.futureColumn+1];
            table[move.futureLine][move.futureColumn+1] = Pieces.BLANK;        
                    
        return true;
    }
    
    public boolean makeMoveCastlingWild(Moves move) {

            table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
            table[move.currentLine][move.currentColumn] = Pieces.BLANK;
            table[move.futureLine][move.futureColumn+1] = table[move.currentLine][move.futureColumn-2];
            table[move.futureLine][move.futureColumn-2] = Pieces.BLANK;        
                    
        return true;
    }
    
    public boolean kingCastlingCondition(){
        if(engineColor == Color.WHITE){
            Position king = new Position(0, 0);
            int table_copy [][];
            
            
            for(int i=0;i<8;i++)
                for(int j=0;j<8;j++)
                    if(table[i][j] == 6){
                        king = new Position(i, j);
                        break;
                    }
            
            
            if(check(table))
                return false;
            if(king.line != 0  || king.column != 4)
                return false;
            
            if(table[king.line][king.column+1] !=0 || table[king.line][king.column+2] !=0)
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column+1] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column+2] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            return true;   
        }
        
        
        else{
            Position king = new Position(0, 0);
            int table_copy [][];
            
            for(int i=0;i<8;i++)
                for(int j=0;j<8;j++)
                    if(table[i][j] == -6){
                        king = new Position(i, j);
                        break;
                    }
        
            if(king.line != 7  || king.column != 4)
                return false;
            
            if(table[king.line][king.column+1] !=0 || table[king.line][king.column+2] !=0)
                return false;
            
            
            if(check(table))
                return false;
            table_copy = tableCopy(table);
            table_copy[king.line][king.column+1] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column+2] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            return true;     
        }
    }
    
    public boolean queenCastlingCondition(){
        if(engineColor == Color.WHITE){
            Position king = new Position(0, 0);
            int table_copy [][];
            
            
            for(int i=0;i<8;i++)
                for(int j=0;j<8;j++)
                    if(table[i][j] == 6){
                        king = new Position(i, j);
                        break;
                    }
            
            if(check(table))
                return false;
            if(king.line != 0  || king.column != 4)
                return false;
            
            if(table[king.line][king.column-1] !=0 || table[king.line][king.column-2] !=0 || table[king.line][king.column-3] !=0)
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column-1] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column-2] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
         
            return true;   
        }
        
        
        else{
            Position king = new Position(0, 0);
            int table_copy [][];
            
            for(int i=0;i<8;i++)
                for(int j=0;j<8;j++)
                    if(table[i][j] == 6){
                        king = new Position(i, j);
                        break;
                    }
        
            if(king.line != 7  || king.column != 4)
                return false;
            
            if(table[king.line][king.column-1] !=0 || table[king.line][king.column-2] !=0 && table[king.line][king.column-3] !=0)
                return false;
            
            if(check(table))
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column-1] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            table_copy = tableCopy(table);
            table_copy[king.line][king.column-2] = table_copy[king.line][king.column] ;
            table_copy[king.line][king.column] = Pieces.BLANK;
            
            if(check(table_copy))
                return false;
            
            return true;     
        }
    }
    
    public void printTable() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (table[i][j] < 0) {
                    System.out.print(" " + table[i][j]);
                } else {
                    System.out.print("  " + table[i][j]);
                }

            }
            System.out.println();
        }
    }
    
    
    public String printTabletoString() {
        String s = new String();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (table[i][j] < 0) {
                    s = s + " " + table[i][j];
                } else {
                    s = s + "  " + table[i][j];
                }

            }
            s = s + '\n';
        }
        return s;
    }
    

    //Am testat un scenariu
    public void metodaVerifica() {

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                table[i][j] = Pieces.BLANK;
            }
        }
        printTable();
        colorState = 1;
        table[0][0] = Pieces.WHITE_ROOK;
        //table[0][2] = Pieces.WHITE_BISHOP;
        table[0][4] = Pieces.WHITE_KING;
        table[2][3] = Pieces.BLACK_QUEEN;  
        /*
        table[1][1] = Pieces.WHITE_PAWN;
        table[1][2] = Pieces.WHITE_KING;
        table[1][6] = Pieces.WHITE_PAWN;
        table[2][4] = Pieces.BLACK_PAWN;
        table[2][6] = Pieces.BLACK_BISHOP;
        table[2][7] = Pieces.WHITE_PAWN;
        table[3][2] = Pieces.BLACK_PAWN;
        table[3][3] = Pieces.BLACK_QUEEN;
        table[4][0] = Pieces.WHITE_PAWN;
        table[4][5] = Pieces.BLACK_BISHOP;
        table[4][6] = Pieces.BLACK_PAWN;
        table[5][5] = Pieces.BLACK_PAWN;
        table[6][0] = Pieces.BLACK_PAWN;
        table[6][1] = Pieces.BLACK_PAWN;
        table[6][2] = Pieces.BLACK_PAWN;
        table[6][6] = Pieces.BLACK_PAWN;
        table[7][0] = Pieces.BLACK_ROOK;
        table[7][1] = Pieces.BLACK_HORSE;
        table[7][4] = Pieces.BLACK_KING;
        table[7][7] = Pieces.BLACK_ROOK;
        */        
        System.out.println();
        printTable();

        allmoves = generateAllMoves();
        System.out.println("ACESTEA SUNT TOATE MUTARILE POSIBILE");
        for (Moves move : allmoves) {
            StringBuffer moveEngine = encodeMove(move);
            System.out.println(moveEngine);
        }

        ArrayList<Moves> remainingmoves = new ArrayList<Moves>();
        remainingmoves = checkMoves();
        System.out.println("ACESTEA SUNT TOATE MUTARILE RAMASE");
        for (Moves move : remainingmoves) {
            StringBuffer moveEngine = encodeMove(move);
            System.out.println(moveEngine);
        }
    }
}

class Pair<F, S>{
	public F first;
	public S second;
	public Pair(F first, S second){
		this.first = first;
		this.second = second;
	}
}



