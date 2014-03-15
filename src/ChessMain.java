import java.io.*;
import java.util.Random;

/* IMPORTANT !
 *  Parcurgeti codul si voi si spuneti-va parerea.
 */

enum State {
	ACTIV, NEW_GAME, FORCE, GO, INACTIVE;
}

enum Color {
	WHITE, BLACK, NONE;
}

/*
 * END - turn atunci cand engine-ul nu mai are nicio varianta de a muta.
 */

enum Turn {
	ENGINE, PLAYER, END;
}

/*
 * Clasa Moves este folosita pentru a crea mutarile de pe tabla cu variabilele
 * sale membre destul de intuitive.
 */
class Position {
	
	public int line;
	public int column;
	
	public Position(int line, int column) {
		
		this.line = line;
		this.column = column;
	}
}
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
	 * Starea initiala jocului este inactiva, engine-ul are culoarea implicita
	 * negru si primul pentru a muta este jucatorul, variabilele lineTest si
	 * columTest sunt doar pentru teste.
	 */
	public Color engineColor = Color.BLACK;
	public Turn turn = Turn.PLAYER;
	public static final int COLUMNS = 8;
	public static final int ROWS = 8;
	public int[][] table;
	public int lineTest = 6, columnTest = 3;
	public State state = State.INACTIVE;
	public int colorState = -1;
	public boolean colorChanged = false;

	public static void main(String[] args) throws IOException {

		String command = new String();
		BufferedReader buff = new BufferedReader(new InputStreamReader(
				System.in));
		ChessMain chess = new ChessMain();

		/*
		 * http://home.hccnet.nl/h.g.muller/engine-intf.html#7 Sectiunea 9.
		 * Commands from the engine to xboard pentru a scapa de eroarea pe care
		 * o primeam dupa una sau doua mutari.
		 */

		System.out.println("feature sigint=0");
		System.out.flush();
		System.out.println("feature sigterm=0");
		System.out.flush();
		System.out.println("feature usermove=1");
		System.out.flush();

		do {
			try {

				if (chess.turn == Turn.ENGINE) {
					if (chess.moveEngine() == false) {
						chess.turn = Turn.END;
					} else {
						chess.turn = Turn.PLAYER;
					}
				}

				/*
				 * Daca engine-ul nu mai are mutari valide posibile si starea
				 * jocului este activa atunci dam resign.
				 */

				if (chess.turn == Turn.END && chess.state == State.ACTIV) {
					chess.state = State.INACTIVE;
					System.out.println("resign");
					System.out.flush();
					continue;
				}

				command = buff.readLine();
				
				if (command.startsWith("usermove")) {
					chess.movePlayer(command.split(" ")[1]);
					chess.turn = Turn.ENGINE;
					continue;
				}

				if (command.startsWith("xboard")) {
					chess.state = State.ACTIV;
					continue;
				}

				if (command.startsWith("new")) {
					chess.colorState = -1;
					chess.turn = Turn.PLAYER;
					chess.engineColor = Color.BLACK;
					chess.state = State.ACTIV;
					chess.initTable();
					chess.colorChanged = false;
					continue;
				}
				
				if (command.startsWith("white") && chess.state == State.ACTIV) {
					if (chess.colorChanged == false) {
						chess.turn = Turn.ENGINE;
						chess.engineColor = Color.BLACK;
						chess.colorState = -1;
						Position pawnPosition = chess.positionPiece(Pieces.BLACK_PAWN);
						chess.lineTest = pawnPosition.line;
						chess.columnTest = pawnPosition.column;
						chess.colorChanged = true;
					} else
						chess.colorChanged = false;
					// + treaba cu clock-urile nu imi e deloc clara + INVERSARE TABLA si comportament engine

					continue;
				}
				
				if (command.startsWith("black") && chess.state == State.ACTIV) {
					if (chess.colorChanged == false) {
						chess.turn = Turn.ENGINE;
						chess.engineColor = Color.WHITE;
						chess.colorState = 1;
						Position pawnPosition = chess.positionPiece(Pieces.WHITE_PAWN);
						chess.lineTest = pawnPosition.line;
						chess.columnTest = pawnPosition.column;
						chess.colorChanged = true;
					} else
						chess.colorChanged = false;
						// + treaba cu clock-urile nu imi e deloc clara + INVERSARE TABLA si comportament engine
					continue;
				}
				
				/*
				TODO
				 Dubioase nu imi sunt clare;
				 
				if (command.startsWith("go")) {
					
				}
				
				if (command.startsWith("force")) {
					
				}		
				 */
				
				if (command.startsWith("quit"))
					return;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (true);
	}
	
	/* 
	public void reverseTable() {

		int aux;
		for (int i = 0; i < ROWS / 2; i++)
			for (int j = 0; j < COLUMNS; j++) {
				aux = table[i][j];
				table[i][j] = table[ROWS - i - 1][COLUMNS - j - 1];
				table[ROWS - i - 1][COLUMNS - j - 1] = aux;
			}
	}
	*/
	
	public void initTable() {

		lineTest = 6;
		columnTest = 3;
		table = new int[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; ++i)
			for (int j = 0; j < COLUMNS; ++j)
				table[i][j] = 0;

		for (int i = 0; i < 8; ++i) {
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
	 * Metoda pentru a decodifica comanda primita de la xboard din partea
	 * jucatorului returneaza mutarea.
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
	 * Metoda pentru a codifica o mutare a engine-ului pentru a transmite-o
	 * xbordului. returneaza StringBufferul de forma a2a3 etc. .
	 */

	public StringBuffer encodeMove(Moves move) {

		StringBuffer moveEngine = new StringBuffer(100000);

		moveEngine.append("move ");
		moveEngine.append((char) ('a' + move.currentColumn));
		moveEngine.append((char) ('1' + move.currentLine));
		moveEngine.append((char) ('a' + move.futureColumn));
		moveEngine.append((char) ('1' + move.futureLine));

		return moveEngine;
	}

	/*
	 * Mutarea jucatorului.
	 */

	public boolean movePlayer(String moveCommand) {

		Moves move = decodeMove(moveCommand);
		if (makeMove(move))
			return true;
		return false;
	}

	/*
	 * Metoda de determinare a urmatoarei mutari a engine-ului este
	 * extrem de primitiva in sensul ca verifca daca intalneste piese de
	 * culoarea inversa.
	 */

	public boolean moveEngine() {
		
		int currentLine, currentColumn;
		currentLine = lineTest;
		currentColumn = columnTest;
		
		// piesa i-a fost luata deci da resign;
		if (currentLine == 0)
			return false; // sau sa ia tot cu regina
		if ((-colorState) * table[currentLine][currentColumn] > 0)
			return false; 
		
		int takePieceLeft, takePieceRight, goAhead;
		takePieceLeft = takePieceRight = goAhead = 0;
		
		if (table[lineTest + colorState][columnTest] == 0)
			goAhead = 1;
		if (columnTest - 1 >= 0)
			if ((-colorState) * table[lineTest + colorState][columnTest - 1] > 0)
				takePieceLeft = 1;
		if (columnTest + 1 < 8)
			if ((-colorState) * table[lineTest + colorState][columnTest + 1] > 0)
				takePieceRight = 1;
		
		if (goAhead == 0 && takePieceLeft == 0 && takePieceRight == 0)
			return false;
		else
			if (goAhead == 0 && takePieceLeft == 0 && takePieceRight == 1) {
				lineTest = lineTest + colorState;
				columnTest++;
			} else
				if (goAhead == 0 && takePieceLeft == 1 && takePieceRight == 0) {
					lineTest = lineTest + colorState;
					columnTest--;
				} else
					if (goAhead == 1 && takePieceLeft == 0 && takePieceRight == 0) {
						lineTest = lineTest + colorState;
					} else
						if (goAhead == 0 && takePieceLeft == 1 && takePieceRight == 1) {
							lineTest = lineTest + colorState;
							Random rand = new Random();
							int choice = rand.nextInt(2);
							if (choice == 0)
								columnTest--;
							else
								columnTest++;
						} else
							if (goAhead == 1 && takePieceLeft == 0 && takePieceRight == 1) {
								lineTest = lineTest + colorState;
								Random rand = new Random();
								int choice = rand.nextInt(2);
								if (choice == 1)
									columnTest++;
							}  else
								if (goAhead == 1 && takePieceLeft == 1 && takePieceRight == 0) {
									lineTest = lineTest + colorState;
									Random rand = new Random();
									int choice = rand.nextInt(2);
									if (choice == 1)
										columnTest--;
								} else
									if (goAhead == 1 && takePieceLeft == 1 && takePieceRight == 1) {
										lineTest = lineTest + colorState;
										Random rand = new Random();
										int choice = rand.nextInt(3);
										if (choice == 1)
											columnTest--;
										if (choice == 2)
											columnTest++;
									}
	
		Moves move = new Moves(currentLine, currentColumn, lineTest, columnTest);
		StringBuffer moveEngine = encodeMove(move);
		makeMove(move);
		System.out.println(moveEngine);
		System.out.flush();
		
		return true;
	}

	/*
	 * Metoda ce realizeaza mutarea pe tabla (matrice).
	 */

	public boolean makeMove(Moves move) {

		// TODO valid move;
		table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
		table[move.currentLine][move.currentColumn] = Pieces.BLANK;
		return true;
	}
	
	public Position positionPiece (int piece) {
		
		Position pos = new Position(-1, -1);
		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLUMNS; j++)
				if (table[i][j] == piece) {
					pos = new Position(i, j);
					return pos;
				}
		return pos;
	}
	
	/*
	 * Pentru testare printare matrice decomentati.
	 */

	
	  public void printMatrix() {
		  for (int line = 0; line < 8; ++line) {
			  for (int col = 0; col < 8; ++col) 
				  System.out.print(table[line][col] + " ");
			  System.out.println();
		  }
	  }
	 
}
