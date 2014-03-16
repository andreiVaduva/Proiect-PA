import java.io.*;
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
	public int lineTest, columnTest;
	public State state = State.INACTIVE;
	public int colorState = -1;
	public boolean colorChanged = false;

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
			if (chess.turn == Turn.ENGINE  && chess.state == State.ACTIV) {
				if (chess.moveEngine() == false) {
					chess.turn = Turn.END;
				} else {
					chess.turn = Turn.PLAYER;
				}
			}
			/*
			 * Dacă engine-ul nu mai are mutări valide posibile şi starea
			 * jocului este activă atunci dăm resign apoi avem varianta new sau quit.
			 */
			if (chess.turn == Turn.END && chess.state == State.ACTIV) {
				chess.state = State.INACTIVE;
				System.out.println("resign");
				System.out.flush();
				continue;
			}

			command = buff.readLine();

			if (command.startsWith("usermove")  && chess.state == State.ACTIV) {
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
				chess.engineColor = Color.BLACK;
				chess.state = State.ACTIV;
				chess.initTable();
				chess.colorChanged = false;
				continue;
			}

			if (command.startsWith("white") && chess.state == State.ACTIV) {
				if (chess.colorChanged == true) {
					chess.turn = Turn.ENGINE;
					chess.engineColor = Color.WHITE;
					chess.colorState = 1;
					Position pawnPosition = chess.positionPiece(Pieces.WHITE_PAWN);
					chess.lineTest = pawnPosition.line;
					chess.columnTest = pawnPosition.column;
					chess.colorChanged = false;
				} else
					chess.colorChanged = true;
				continue;
			}

			if (command.startsWith("black") && chess.state == State.ACTIV) {
				if (chess.colorChanged == true) {
					chess.turn = Turn.ENGINE;
					chess.engineColor = Color.BLACK;
					chess.colorState = -1;
					Position pawnPosition = chess.positionPiece(Pieces.BLACK_PAWN);
					chess.lineTest = pawnPosition.line;
					chess.columnTest = pawnPosition.column;
					chess.colorChanged = false;
				} else
					chess.colorChanged = true;
				continue;
			}
			if (command.startsWith("force")) {
				continue;
			}
			if (command.startsWith("go")) {
				continue;
			}
			if (command.startsWith("quit"))
				return;

		} while (true);
	}

	public void initTable() {

		lineTest = 6; // Pion negru d6 de plecare.
		columnTest = 3;

		table = new int[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; ++i)
			for (int j = 0; j < COLUMNS; ++j)
				table[i][j] = 0;

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

	public boolean movePlayer(String moveCommand) {

		Moves move = decodeMove(moveCommand);
		if (makeMove(move))
			return true;
		return false;
	}

	/*
	 * Metodă ce determină următoarea mutare a engine-ului şi returnează
	 * true dacă mai există mutări valide şi execută mutarea şi false în
	 * caz contrar.(more in README).
	 */

	public boolean moveEngine() {
	
		int currentLine, currentColumn;
		currentLine = lineTest;
		currentColumn = columnTest;
		
		if (currentLine == 7)
			return false; 											// a ajuns pe ultima linie.
		if (currentLine == 0)
			return false; 											// a ajuns pe ultima linie.
		if ((-colorState) * table[currentLine][currentColumn] > 0)	
			return false; 											// piesa cu care muta i-a fost luată.
	
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
			return false;											// nu are opţiuni.
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
	 * Metodă ce realizeaza mutarea pe tablă (matrice).
	 */

	public boolean makeMove(Moves move) {
		
		table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
		table[move.currentLine][move.currentColumn] = Pieces.BLANK;

		return true;
	}
	
	/*
	 * Metodă ce determină poziţia pe tabla a primei apariţii a
	 * unei anumite piese. (pion în cazul nostru white/black)
	 * folosită atunci când se schimbă culorile şi engine-ul
	 * îşi alege primul pion pentru a-l muta.
	 */

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
}

