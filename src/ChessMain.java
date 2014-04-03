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
	public String[] king_side_castling = { "e1g1", "e8g8" };
	public String[] queen_side_castling = { "e1c1", "e8c8" };

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
				allmoves = new ArrayList<Moves>();
				allmoves = chess.generateAllMoves();
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
				System.out.println("resign");
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

		if (moveCommand.endsWith("q")) {
			makeMove(move, 1);
			return true;
		}

		if (king_side_castling[0].equals(moveCommand)
				|| king_side_castling[1].equals(moveCommand)) {
			makeKingCastling(move);
			return true;
		}

		if (queen_side_castling[0].equals(moveCommand)
				|| queen_side_castling[1].equals(moveCommand)) {
			makeQueenCastling(move);
			return true;
		}

		makeMove(move, 0);
		return true;

	}

	/*
	 * Metoda genereaza lista de mutari posibile ale tuturor pieselor
	 * engine-ului, obtinand lista de "allmoves".
	 */

	public ArrayList<Moves> generateAllMoves() {

		ArrayList<Moves> allmoves = new ArrayList<Moves>();

		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLUMNS; j++) {
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_PAWN * (-colorState))
					allmoves.addAll(generateAllMovesPaws(new Position(i, j)));
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_HORSE * (-colorState))
					allmoves.addAll(generateAllMovesHorse(new Position(i, j)));
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_BISHOP * (-colorState))
					allmoves.addAll(generateAllMovesBishop(new Position(i, j)));
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_ROOK * (-colorState))
					allmoves.addAll(generateAllMovesRook(new Position(i, j)));
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_QUEEN * (-colorState))
					allmoves.addAll(generateAllMovesQueen(new Position(i, j)));
				if ((-colorState) * table[i][j] < 0
						&& table[i][j] == Pieces.BLACK_KING * (-colorState))
					allmoves.addAll(generateAllMovesKing(new Position(i, j)));
			}

		return allmoves;
	}

	/*
	 * Metoda returneaza mutarile posibile pentru un pion printre care si
	 * deschiderea pionului cu doua pozitii in fata. (-colorState) *
	 * table[initialposition.line + colorState][initialposition.column - 1] > 0
	 * se traduce prin faptul ca pe acea pozitie din tabla se afla o piesa a
	 * adversarului, < 0 inseamna ca este piesa proprie engine-ului.
	 * initialposition.line + colorState - linia creste sau scade in functie de
	 * culoarea engine-ului.
	 */

	public ArrayList<Moves> generateAllMovesPaws(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();

		if (initialposition.line + colorState < ROWS
				&& initialposition.line + colorState >= 0)
			if (table[initialposition.line + colorState][initialposition.column] == 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line
								+ colorState, initialposition.column));

		if (initialposition.line + colorState < ROWS
				&& initialposition.column - 1 >= 0
				&& initialposition.line + colorState >= 0)
			if ((-colorState)
					* table[initialposition.line + colorState][initialposition.column - 1] > 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line
								+ colorState, initialposition.column - 1));

		if (initialposition.line + colorState < ROWS
				&& initialposition.column + 1 < COLUMNS
				&& initialposition.line + colorState >= 0)
			if ((-colorState)
					* table[initialposition.line + colorState][initialposition.column + 1] > 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line
								+ colorState, initialposition.column + 1));

		if (colorState == 1)
			if (initialposition.line == 1)
				if (table[initialposition.line + 1][initialposition.column] == 0)
					if (table[initialposition.line + 2][initialposition.column] == 0)
						moves.add(new Moves(initialposition.line,
								initialposition.column,
								initialposition.line + 2,
								initialposition.column));

		if (colorState == -1)
			if (initialposition.line == 6)
				if (table[initialposition.line - 1][initialposition.column] == 0)
					if (table[initialposition.line - 2][initialposition.column] == 0)
						moves.add(new Moves(initialposition.line,
								initialposition.column,
								initialposition.line - 2,
								initialposition.column));

		return moves;
	}

	/*
	 * Metoda returneaza mutarile posibile pentru cal, avand maxim opt mutari
	 * valide. Mutarea este valida daca pe pozitia viitoare se gaseste o piesa a
	 * adversarului sau este libera.
	 */

	public ArrayList<Moves> generateAllMovesHorse(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();

		if (initialposition.line + 2 < ROWS
				&& initialposition.column + 1 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line + 2][initialposition.column + 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 2,
						initialposition.column + 1));

		if (initialposition.line + 2 < ROWS && initialposition.column - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line + 2][initialposition.column - 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 2,
						initialposition.column - 1));

		if (initialposition.line + 1 < ROWS
				&& initialposition.column + 2 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line + 1][initialposition.column + 2] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 1,
						initialposition.column + 2));

		if (initialposition.line + 1 < ROWS && initialposition.column - 2 >= 0)
			if ((-colorState)
					* table[initialposition.line + 1][initialposition.column - 2] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 1,
						initialposition.column - 2));

		if (initialposition.line - 2 >= 0
				&& initialposition.column + 1 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line - 2][initialposition.column + 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 2,
						initialposition.column + 1));

		if (initialposition.line - 2 >= 0 && initialposition.column - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line - 2][initialposition.column - 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 2,
						initialposition.column - 1));

		if (initialposition.line - 1 >= 0
				&& initialposition.column + 2 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line - 1][initialposition.column + 2] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 1,
						initialposition.column + 2));

		if (initialposition.line - 1 >= 0 && initialposition.column - 2 >= 0)
			if ((-colorState)
					* table[initialposition.line - 1][initialposition.column - 2] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 1,
						initialposition.column - 2));

		return moves;
	}

	/*
	 * Metoda returneaza toate mutarile valide pentru nebun. (More in Readme)
	 */

	public ArrayList<Moves> generateAllMovesBishop(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();
		int line, column;
		line = initialposition.line;
		column = initialposition.column;

		while ((line + 1) < ROWS && (column + 1) < COLUMNS) {
			if (table[line + 1][column + 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column + 1));
				line++;
				column++;
			} else if ((-colorState) * table[line + 1][column + 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column + 1));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((line + 1) < ROWS && (column - 1) >= 0) {
			if (table[line + 1][column - 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column - 1));
				line++;
				column--;
			} else if ((-colorState) * table[line + 1][column - 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column - 1));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((line - 1) >= 0 && (column + 1) < COLUMNS) {
			if (table[line - 1][column + 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column + 1));
				line--;
				column++;
			} else if ((-colorState) * table[line - 1][column + 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column + 1));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((line - 1) >= 0 && (column - 1) >= 0) {
			if (table[line - 1][column - 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column - 1));
				line--;
				column--;
			} else if ((-colorState) * table[line - 1][column - 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column - 1));
				break;
			} else
				break;
		}

		return moves;
	}

	/*
	 * Metoda returneaza toate mutarile valide pentru tura. (More in Readme)
	 */

	public ArrayList<Moves> generateAllMovesRook(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();
		int line, column;
		line = initialposition.line;
		column = initialposition.column;

		while ((line + 1) < ROWS) {
			if (table[line + 1][column] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column));
				line++;
			} else if ((-colorState) * table[line + 1][column] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line + 1, column));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((line - 1) >= 0) {
			if (table[line - 1][column] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column));
				line--;
			} else if ((-colorState) * table[line - 1][column] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line - 1, column));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((column + 1) < COLUMNS) {
			if (table[line][column + 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line, column + 1));
				column++;
			} else if ((-colorState) * table[line][column + 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line, column + 1));
				break;
			} else
				break;
		}

		line = initialposition.line;
		column = initialposition.column;

		while ((column - 1) >= 0) {
			if (table[line][column - 1] == 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line, column - 1));
				column--;
			} else if ((-colorState) * table[line][column - 1] > 0) {
				moves.add(new Moves(initialposition.line,
						initialposition.column, line, column - 1));
				break;
			} else
				break;
		}

		return moves;
	}

	/*
	 * Metoda returneaza toate mutarile valide pentru regina, combinand mutarile
	 * posibile pentru comportamentul asemanator unei ture cu cele posibile
	 * pentru un comportament asemenator cu al unui nebun.
	 */
	public ArrayList<Moves> generateAllMovesQueen(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();

		moves.addAll(generateAllMovesBishop(initialposition));
		moves.addAll(generateAllMovesRook(initialposition));

		return moves;
	}

	/*
	 * Metoda genereaza toate mutarile valide pentru rege, avand maxim opt
	 * mutari valide.
	 */
	public ArrayList<Moves> generateAllMovesKing(Position initialposition) {

		ArrayList<Moves> moves = new ArrayList<Moves>();

		if (initialposition.line + 1 < ROWS)
			if ((-colorState)
					* table[initialposition.line + 1][initialposition.column] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 1,
						initialposition.column));

		if (initialposition.line - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line - 1][initialposition.column] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 1,
						initialposition.column));

		if (initialposition.column + 1 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line][initialposition.column + 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line,
						initialposition.column + 1));

		if (initialposition.column - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line][initialposition.column - 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line,
						initialposition.column - 1));

		if (initialposition.line + 1 < ROWS
				&& initialposition.column + 1 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line + 1][initialposition.column + 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 1,
						initialposition.column + 1));

		if (initialposition.line - 1 >= 0
				&& initialposition.column + 1 < COLUMNS)
			if ((-colorState)
					* table[initialposition.line - 1][initialposition.column + 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 1,
						initialposition.column + 1));

		if (initialposition.line + 1 < ROWS && initialposition.column - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line + 1][initialposition.column - 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line + 1,
						initialposition.column - 1));

		if (initialposition.line - 1 >= 0 && initialposition.column - 1 >= 0)
			if ((-colorState)
					* table[initialposition.line - 1][initialposition.column - 1] >= 0)
				moves.add(new Moves(initialposition.line,
						initialposition.column, initialposition.line - 1,
						initialposition.column - 1));

		return moves;
	}

	/*
	 * Metoda creeaza si returneaza o copia a tablei(matrice) de joc.
	 */

	public int[][] tableCopy(int[][] table) {

		int[][] table_copy = new int[ROWS][COLUMNS];

		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLUMNS; j++)
				table_copy[i][j] = table[i][j];

		return table_copy;
	}

	/*
	 * Metoda construieste lista de mutari valide, prin mutari valide
	 * intelegandu-se acele mutari care scot regele din pozitia de sah in cazul
	 * in care se afla in aceasta situatie sau nu lasa regele in pozitie de sah
	 * prin mutarea facuta. (More in Readme)
	 */
	public ArrayList<Moves> checkMoves() {

		ArrayList<Moves> moves = new ArrayList<Moves>();
		int[][] table_copy;

		for (Moves move : allmoves) {
			table_copy = tableCopy(table);
			table_copy[move.futureLine][move.futureColumn] = table_copy[move.currentLine][move.currentColumn];
			table_copy[move.currentLine][move.currentColumn] = Pieces.BLANK;
			if (!check(table_copy))
				moves.add(move);
		}

		return moves;
	}

	/*
	 * Metoda alege random din mutarile trecute prin "filtrul" de "check" si o
	 * transmite catre xboard.
	 */

	public boolean moveEngine() {

		ArrayList<Moves> moves = new ArrayList<Moves>();

		moves = checkMoves();
		if (moves.size() == 0)
			return false;
		Moves move = moves.get(new Random().nextInt(moves.size()));
		StringBuffer moveEngine = encodeMove(move);
		if (move.futureLine == 0 || move.futureLine == 7)
			makeMove(move, 1);
		else
			makeMove(move, 0);
		System.out.println(moveEngine);
		System.out.flush();

		return true;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
	 * coloana si situat deasupra. (More in Readme)
	 */

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

		if (ownPiece == 0)
			if (oppPiece == 1)
				if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
						|| table_copy[i][kingColumn] == Pieces.BLACK_QUEEN
								* colorState)
					return true;

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
	 * coloana si situat dedesubt. (More in Readme)
	 */

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

		if (ownPiece == 0)
			if (oppPiece == 1)
				if (table_copy[i][kingColumn] == Pieces.BLACK_ROOK * colorState
						|| table_copy[i][kingColumn] == Pieces.BLACK_QUEEN
								* colorState)
					return true;

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
	 * linie si situat la dreapta. (More in Readme)
	 */

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

		if (ownPiece == 0)
			if (oppPiece == 1)
				if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
						|| table_copy[kingLine][i] == Pieces.BLACK_QUEEN
								* colorState)
					return true;

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata pe aceeasi
	 * linie si situat la stanga. (More in Readme)
	 */

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

		if (ownPiece == 0)
			if (oppPiece == 1)
				if (table_copy[kingLine][i] == Pieces.BLACK_ROOK * colorState
						|| table_copy[kingLine][i] == Pieces.BLACK_QUEEN
								* colorState)
					return true;

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata in
	 * diagonala sa si anume in partea dreapta deasupra. (More in Readme)
	 */

	public boolean check_rightUp(int[][] table_copy, int kingLine,
			int kingColumn) {

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

		if (ownPiece == 0)
			if (oppPiece == 1) {
				if (table_copy[line][column] == Pieces.BLACK_BISHOP
						* colorState
						|| table_copy[line][column] == Pieces.BLACK_QUEEN
								* colorState)
					return true;
				if (colorState == 1)
					if (table_copy[line][column] == Pieces.BLACK_PAWN)
						if (line == kingLine + 1 && column == kingColumn + 1)
							return true;
			}

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata in
	 * diagonala sa si anume in partea dreapta dedesubt. (More in Readme)
	 */

	public boolean check_rightDown(int[][] table_copy, int kingLine,
			int kingColumn) {

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

		if (ownPiece == 0)
			if (oppPiece == 1) {
				if (table_copy[line][column] == Pieces.BLACK_BISHOP
						* colorState
						|| table_copy[line][column] == Pieces.BLACK_QUEEN
								* colorState)
					return true;
				if (colorState == -1)
					if (table_copy[line][column] == Pieces.WHITE_PAWN)
						if (line == kingLine - 1 && column == kingColumn + 1)
							return true;
			}

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata in
	 * diagonala sa si anume in partea stanga deasupra. (More in Readme)
	 */

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

		if (ownPiece == 0)
			if (oppPiece == 1) {
				if (table_copy[line][column] == Pieces.BLACK_BISHOP
						* colorState
						|| table_copy[line][column] == Pieces.BLACK_QUEEN
								* colorState)
					return true;
				if (colorState == 1)
					if (table_copy[line][column] == Pieces.BLACK_PAWN)
						if (line == kingLine + 1 && column == kingColumn - 1)
							return true;
			}

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de o piesa aflata in
	 * diagonala sa si anume in partea stanga dedesubt. (More in Readme)
	 */

	public boolean check_leftDown(int[][] table_copy, int kingLine,
			int kingColumn) {

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

		if (ownPiece == 0)
			if (oppPiece == 1) {
				if (table_copy[line][column] == Pieces.BLACK_BISHOP
						* colorState
						|| table_copy[line][column] == Pieces.BLACK_QUEEN
								* colorState)
					return true;
				if (colorState == -1)
					if (table_copy[line][column] == Pieces.WHITE_PAWN)
						if (line == kingLine - 1 && column == kingColumn - 1)
							return true;
			}

		return false;
	}

	/*
	 * Metoda verifica daca regele este pus in sah de un cal, ce se poate afla
	 * intr-un din cele opt pozitii posibile.
	 */

	public boolean check_Horse(int[][] table_copy, int kingLine, int kingColumn) {

		if (kingLine + 2 < ROWS && kingColumn + 1 < COLUMNS)
			if (table_copy[kingLine + 2][kingColumn + 1] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine + 2 < ROWS && kingColumn - 1 >= 0)
			if (table_copy[kingLine + 2][kingColumn - 1] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine + 1 < ROWS && kingColumn + 2 < COLUMNS)
			if (table_copy[kingLine + 1][kingColumn + 2] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine + 1 < ROWS && kingColumn - 2 >= 0)
			if (table_copy[kingLine + 1][kingColumn - 2] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine - 2 >= 0 && kingColumn + 1 < COLUMNS)
			if (table_copy[kingLine - 2][kingColumn + 1] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine - 2 >= 0 && kingColumn - 1 >= 0)
			if (table_copy[kingLine - 2][kingColumn - 1] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine - 1 >= 0 && kingColumn - 2 >= 0)
			if (table_copy[kingLine - 1][kingColumn - 2] == Pieces.BLACK_HORSE
					* colorState)
				return true;
		if (kingLine - 1 >= 0 && kingColumn + 2 < COLUMNS)
			if (table_copy[kingLine - 1][kingColumn + 2] == Pieces.BLACK_HORSE
					* colorState)
				return true;

		return false;
	}

	/*
	 * Metoda verifica daca regele este mutat in apropierea regelui advers,
	 * mutarea fiind ilegala.
	 */

	public boolean check_King(int[][] table_copy, int kingLine, int kingColumn) {

		if (kingLine + 1 < ROWS)
			if (table_copy[kingLine + 1][kingColumn] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingLine - 1 >= 0)
			if (table_copy[kingLine - 1][kingColumn] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingColumn - 1 >= 0)
			if (table_copy[kingLine][kingColumn - 1] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingColumn + 1 < COLUMNS)
			if (table_copy[kingLine][kingColumn + 1] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingLine + 1 < ROWS && kingColumn + 1 < COLUMNS)
			if (table_copy[kingLine + 1][kingColumn + 1] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingLine - 1 >= 0 && kingColumn + 1 < COLUMNS)
			if (table_copy[kingLine - 1][kingColumn + 1] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingLine + 1 < ROWS && kingColumn - 1 >= 0)
			if (table_copy[kingLine + 1][kingColumn - 1] == Pieces.BLACK_KING
					* colorState)
				return true;
		if (kingLine - 1 >= 0 && kingColumn - 1 >= 0)
			if (table_copy[kingLine - 1][kingColumn - 1] == Pieces.BLACK_KING
					* colorState)
				return true;

		return false;
	}

	/*
	 * Metoda determina pozitia actuala a regelui pe tabla si apoi apeleaza
	 * toate variantele posibile prin care acesta s-ar putea afla in pozitie de
	 * sah, returnand true daca este in sah si false in caz contrar.
	 */

	public boolean check(int[][] table_copy) {

		int kingLine, kingColumn, i = 0, j = 0, found = 0;

		for (i = 0; i < ROWS; i++) {
			for (j = 0; j < COLUMNS; j++)
				if (table_copy[i][j] == Pieces.BLACK_KING * (-colorState)) {
					found = 1;
					break;
				}
			if (found == 1)
				break;
		}

		kingLine = i;
		kingColumn = j;

		if (check_up(table_copy, kingLine, kingColumn))
			return true;
		if (check_down(table_copy, kingLine, kingColumn))
			return true;
		if (check_left(table_copy, kingLine, kingColumn))
			return true;
		if (check_right(table_copy, kingLine, kingColumn))
			return true;
		if (check_rightUp(table_copy, kingLine, kingColumn))
			return true;
		if (check_rightDown(table_copy, kingLine, kingColumn))
			return true;
		if (check_leftUp(table_copy, kingLine, kingColumn))
			return true;
		if (check_leftDown(table_copy, kingLine, kingColumn))
			return true;
		if (check_Horse(table_copy, kingLine, kingColumn))
			return true;
		if (check_King(table_copy, kingLine, kingColumn))
			return true;

		return false;
	}

	/*
	 * Metodă ce realizeaza mutarea pe tablă (matrice), daca o piesa ajunge pe
	 * ultima linie din tabla atunci changePawn este egal cu 1, si atunci se
	 * verifica daca acea piesa este PAWN, deoarece acesta se schimba in regina
	 * odata ajuns pe ultima linie.
	 */

	public void makeMove(Moves move, int changePawn) {

		if (changePawn == 0) {
			table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
			table[move.currentLine][move.currentColumn] = Pieces.BLANK;
		} else {
			if (table[move.currentLine][move.currentColumn] == Pieces.WHITE_PAWN)
				table[move.futureLine][move.futureColumn] = Pieces.WHITE_QUEEN;
			else if (table[move.currentLine][move.currentColumn] == Pieces.BLACK_PAWN)
				table[move.futureLine][move.futureColumn] = Pieces.BLACK_QUEEN;
			else
				table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
			table[move.currentLine][move.currentColumn] = Pieces.BLANK;
		}
	}

	/*
	 * Implementare rocada.
	 */

	public void makeKingCastling(Moves move) {

		table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
		table[move.currentLine][move.currentColumn] = Pieces.BLANK;
		table[move.futureLine][move.futureColumn - 1] = table[move.currentLine][move.futureColumn + 1];
		table[move.futureLine][move.futureColumn + 1] = Pieces.BLANK;
	}

	/*
	 * Implementare rocada.
	 */

	public void makeQueenCastling(Moves move) {

		table[move.futureLine][move.futureColumn] = table[move.currentLine][move.currentColumn];
		table[move.currentLine][move.currentColumn] = Pieces.BLANK;
		table[move.futureLine][move.futureColumn + 1] = table[move.currentLine][move.futureColumn - 2];
		table[move.futureLine][move.futureColumn - 2] = Pieces.BLANK;
	}
}
