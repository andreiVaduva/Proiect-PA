import java.awt.font.NumericShaper;
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

class Piese {

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

				if (command.startsWith("xboard")) {
					chess.state = State.ACTIV;
					continue;
				}

				if (command.startsWith("new")) {
					chess.engineColor = Color.BLACK;
					chess.state = State.ACTIV;
					chess.initTable();
					continue;
				}
				
				if (command.startsWith("white")) {
					//chess.turn = Turn.;
					//chess.engineColor = Color.;
					chess.reverseTable();
					// + treaba cu clock-urile nu imi e deloc clara + INVERSARE TABLA si comportament engine
					continue;
				}
				
				if (command.startsWith("black")) {
					//chess.turn = Turn.;
					//chess.engineColor = Color.;
					chess.reverseTable();
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
				
				if (command.startsWith("move")) {
					
				}
				
				if (command.startsWith("resign")) {
					
				}
				
				 */
				
				if (command.startsWith("quit"))
					return;
				
				

				/*
				 * Modalitate de a stabili daca este o comanda de move din
				 * partea jucatorului, sunt utile alte variante mai
				 * "inteligente"
				 */

				if (command.length() == 4 && chess.isNumber(command.charAt(1))
						&& chess.isNumber(command.charAt(3))) {
					chess.movePlayer(command);
					chess.turn = Turn.ENGINE;
					continue;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (true);
	}
	
	public void reverseTable() {

		int aux;
		for (int i = 0; i < ROWS / 2; i++)
			for (int j = 0; j < COLUMNS; j++) {
				aux = table[i][j];
				table[i][j] = table[ROWS - i - 1][COLUMNS - j - 1];
				table[ROWS - i - 1][COLUMNS - j - 1] = aux;
			}
	}
	
	public void initTable() {

		lineTest = 6;
		columnTest = 3;
		table = new int[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; ++i)
			for (int j = 0; j < COLUMNS; ++j)
				table[i][j] = 0;

		for (int i = 0; i < 8; ++i) {
			table[1][i] = Piese.WHITE_PAWN;
			table[6][i] = Piese.BLACK_PAWN;
		}

		table[0][1] = table[0][6] = Piese.WHITE_HORSE;
		table[7][1] = table[7][6] = Piese.BLACK_HORSE;

		table[0][2] = table[0][5] = Piese.WHITE_BISHOP;
		table[7][2] = table[7][5] = Piese.BLACK_BISHOP;

		table[0][0] = table[0][7] = Piese.WHITE_ROOK;
		table[7][0] = table[7][7] = Piese.BLACK_ROOK;

		table[0][3] = Piese.WHITE_QUEEN;
		table[7][3] = Piese.BLACK_QUEEN;

		table[0][4] = Piese.WHITE_KING;
		table[7][4] = Piese.BLACK_KING;
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
		if (table[currentLine][currentColumn] > 0)
			return false; 
		
		int takePieceLeft, takePieceRight, goAhead;
		takePieceLeft = takePieceRight = goAhead = 0;
		
		if (table[lineTest - 1][columnTest] == 0)
			goAhead = 1;
		if (columnTest - 1 >= 0)
			if (table[lineTest - 1][columnTest - 1] > 0)
				takePieceLeft = 1;
		if (columnTest + 1 < 8)
			if (table[lineTest - 1][columnTest + 1] > 0)
				takePieceRight = 1;
		
		if (goAhead == 0 && takePieceLeft == 0 && takePieceRight == 0)
			return false;
		else
			if (goAhead == 0 && takePieceLeft == 0 && takePieceRight == 1) {
				lineTest--;
				columnTest++;
			} else
				if (goAhead == 0 && takePieceLeft == 1 && takePieceRight == 0) {
					lineTest--;
					columnTest--;
				} else
					if (goAhead == 1 && takePieceLeft == 0 && takePieceRight == 0) {
						lineTest--;
					} else
						if (goAhead == 0 && takePieceLeft == 1 && takePieceRight == 1) {
							lineTest--;
							Random rand = new Random();
							int choice = rand.nextInt(2);
							if (choice == 0)
								columnTest--;
							else
								columnTest++;
						} else
							if (goAhead == 1 && takePieceLeft == 0 && takePieceRight == 1) {
								lineTest--;
								Random rand = new Random();
								int choice = rand.nextInt(2);
								if (choice == 1)
									columnTest++;
							}  else
								if (goAhead == 1 && takePieceLeft == 1 && takePieceRight == 0) {
									lineTest--;
									Random rand = new Random();
									int choice = rand.nextInt(2);
									if (choice == 1)
										columnTest--;
								} else
									if (goAhead == 1 && takePieceLeft == 1 && takePieceRight == 1) {
										lineTest--;
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
		table[move.currentLine][move.currentColumn] = Piese.BLANK;
		return true;
	}

	public boolean isNumber(char value) {
		// Alte optiuni de a testa ca s-a primit o comanda de forma a3a4 etc.
		// idei ?

		int number = 0;
		try {
			number = Integer.parseInt(String.valueOf(value));
			return true;
		} catch (NumberFormatException er) {
			return false;
		}
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
