import java.util.*;
import java.io.*;

public class ChessMain {
	
	int[][] tabel = new int[8][8];
	Color color = Color.BLACK;
	Turn turn = Turn.PLAYER;
	
	boolean firstMove = true;
	int line, col = 4;
	
	public static void main(String[] args) {
		String command = new String();
		BufferedReader scanner = new BufferedReader(new InputStreamReader(
				System.in));
		Stare st;
	
		ChessMain chess = new ChessMain();
		
		while (true) {
			try {

				if (chess.turn == Turn.ENGINE) {
					if (!chess.moveOurPiece()) {
						System.out.println("resign");
						return;
					}
					chess.turn = Turn.PLAYER;
					System.out.println("ENGINE");
					chess.printMatrix(); ///////////////////////////
					continue;
				}

				command = scanner.readLine();
				command.toLowerCase();
				
				if (command.startsWith("usermove")) {
					chess.moveYourPiece(command.split(" ")[1]);
					chess.turn = Turn.ENGINE;
					System.out.println("PLAYER");
					chess.printMatrix(); //////////////////////////////
					continue;
				}
				
				switch (command) {
						case "xboard":
							st = Stare.ACTIV;
							break;
						case "new":
							st = Stare.NEW_GAME;
							chess.initTabel();
							break;
						case "force":
							st = Stare.FORCE;
							break;
						case "go":
							st = Stare.GO;
							chess.turn = Turn.ENGINE;
							break;
						case "white":
							chess.color = Color.BLACK;
							break;
						case "black":
							chess.color = Color.WHITE;
							chess.turn = Turn.ENGINE;
							break;
						case "quit":
							return;
						default:
							break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initTabel() {

		for (int line = 0; line < 8; ++line)
			for (int col = 0; col < 8; ++col)
				tabel[line][col] = 0;
		
		for (int i = 0; i < 8; ++i) {
			tabel[1][i] = Piese.WHITE_PAWN;
			tabel[6][i] = Piese.BLACK_PAWN;
		}
		
		tabel[0][1] = tabel[0][6] = Piese.WHITE_HORSE;
		tabel[7][1] = tabel[7][6] = Piese.BLACK_HORSE;

		tabel[0][2] = tabel[0][5] = Piese.WHITE_BISHOP;
		tabel[7][2] = tabel[7][5] = Piese.BLACK_BISHOP;
		
		tabel[0][0] = tabel[0][7] = Piese.WHITE_ROOK;
		tabel[7][0] = tabel[7][7] = Piese.BLACK_ROOK;
		
		tabel[0][3] = Piese.WHITE_QUEEN;
		tabel[7][3] = Piese.BLACK_QUEEN;
		
		tabel[0][4] = Piese.WHITE_KING;
		tabel[7][4] = Piese.BLACK_KING;
		
		System.out.println("initializare");
/*		for (int line = 0; line < 8; ++line) {
			for (int col = 0; col < 8; ++col)
				System.out.print(tabel[line][col] + " ");
			System.out.println();
		}*/
	}
	
	public boolean moveYourPiece(String move) {
		int fromL, fromC, toL, toC;
		
		fromC = move.charAt(0) - 'a';
		fromL = move.charAt(1) - '1';
		toC = move.charAt(2) - 'a';
		toL = move.charAt(3) - '1';

//		System.out.println("formL: " + fromL);
//		System.out.println("formC: " + fromC);
//		System.out.println("toL: " + toL);
//		System.out.println("toC: " + toC);
		
		tabel[toL][toC] = tabel[fromL][fromC];
		tabel[fromL][fromC] = Piese.BLANK;

//		printMatrix();

		return true;
	}
	
	public boolean moveOurPiece() {
		if (firstMove) {
			firstMove = false;
			if (color == color.WHITE) {
				line = 1;
			}
			else {
				line = 6;
			}
		}
		
		if (color == color.WHITE && tabel[line][col] == Piese.WHITE_PAWN) {
			if (tabel[line+1][col] == Piese.BLANK) {
				takePiece(line, col, line+1, col);
				giveCommand(line, col, line+1, col);
				line++;
				return true;
			} else {
				return false;
			}
		} 
		else if (color == color.BLACK && tabel[line][col] == Piese.WHITE_PAWN){
			if (tabel[line-1][col] == Piese.BLANK) {
				takePiece(line, col, line-1, col);
				giveCommand(line, col, line-1, col);
				line--;
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private void giveCommand(int fromL, int fromC, int toL, int toC) {
		StringBuffer command = new StringBuffer(10);
		
		command.append("move ");
		command.append(fromC + 'a');
		command.append(fromL + '1');
		command.append(toC + 'a');
		command.append(toL + '1');
		command.append('\n');
		
		System.out.println(command);
		System.out.flush();
	}

	public void takePiece(int fromL, int fromC, int toL, int toC) {
		tabel[toL][toC] = tabel[fromL][fromC];
		tabel[fromL][fromC] = Piese.BLANK;
	}
	
// TODO: remove after testing	
	public void printMatrix() {
		for (int line = 0; line < 8; ++line) {
			for (int col = 0; col < 8; ++col)
				System.out.print(tabel[line][col] + " ");
			System.out.println();
		}
	}
	
}

enum Stare {
	ACTIV, NEW_GAME, FORCE, GO;
}

enum Color {
	WHITE, BLACK;
}

enum Turn {
	ENGINE, PLAYER;
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