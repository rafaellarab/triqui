package edu.harding.tictactoe;

import java.util.Random;

public class TicTacToeGame {

	public static final int BOARD_SIZE = 9;

	public enum DifficultyLevel {
		Easy, Harder, Expert
	};

	private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

	public static final char HUMAN_PLAYER = 'X';
	public static final char COMPUTER_PLAYER = 'O';
	public static final char OPEN_SPOT = ' ';

	private Random mRand;

	private char mBoard[];

	public DifficultyLevel getDifficultyLevel() {
		return mDifficultyLevel;
	}

	public char[] getBoardState() {
		return mBoard;
	}

	public void setBoardState(char[] board) {
		mBoard = board.clone();
	}

	public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
		mDifficultyLevel = difficultyLevel;
	}

	public char getBoardOccupant(int location) {
		if (location >= 0 && location < BOARD_SIZE)
			return mBoard[location];
		return '?';
	}

	public TicTacToeGame() {
		mBoard = new char[BOARD_SIZE];
		mRand = new Random();
	}

	public void clearBoard() {

		for (int i = 0; i < BOARD_SIZE; i++) {
			mBoard[i] = OPEN_SPOT;
		}
	}

	public boolean setMove(char player, int location) {
		if (location >= 0 && location < BOARD_SIZE
				&& mBoard[location] == OPEN_SPOT) {
			mBoard[location] = player;
			return true;
		}
		return false;
	}

	public int checkForWinner() {

		for (int i = 0; i <= 6; i += 3) {
			if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 1] == HUMAN_PLAYER
					&& mBoard[i + 2] == HUMAN_PLAYER)
				return 2;
			if (mBoard[i] == COMPUTER_PLAYER
					&& mBoard[i + 1] == COMPUTER_PLAYER
					&& mBoard[i + 2] == COMPUTER_PLAYER)
				return 3;
		}

		for (int i = 0; i <= 2; i++) {
			if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 3] == HUMAN_PLAYER
					&& mBoard[i + 6] == HUMAN_PLAYER)
				return 2;
			if (mBoard[i] == COMPUTER_PLAYER
					&& mBoard[i + 3] == COMPUTER_PLAYER
					&& mBoard[i + 6] == COMPUTER_PLAYER)
				return 3;
		}

		if ((mBoard[0] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[8] == HUMAN_PLAYER)
				|| (mBoard[2] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[6] == HUMAN_PLAYER))
			return 2;
		if ((mBoard[0] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[8] == COMPUTER_PLAYER)
				|| (mBoard[2] == COMPUTER_PLAYER
						&& mBoard[4] == COMPUTER_PLAYER && mBoard[6] == COMPUTER_PLAYER))
			return 3;

		for (int i = 0; i < BOARD_SIZE; i++) {

			if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
				return 0;
		}

		return 1;
	}

	public int getComputerMove() {

		int move = -1;

		if (mDifficultyLevel == DifficultyLevel.Easy) {
			move = getRandomMove();
		} else if (mDifficultyLevel == DifficultyLevel.Harder) {
			move = getWinningMove();
			if (move == -1)
				move = getRandomMove();
		} else if (mDifficultyLevel == DifficultyLevel.Expert) {

			move = getWinningMove();
			if (move == -1)
				move = getBlockingMove();
			if (move == -1)
				move = getRandomMove();
		}

		return move;
	}

	private int getRandomMove() {

		int move;
		do {
			move = mRand.nextInt(9);
		} while (mBoard[move] == HUMAN_PLAYER
				|| mBoard[move] == COMPUTER_PLAYER);
		return move;
	}

	private int getBlockingMove() {

		for (int i = 0; i < BOARD_SIZE; i++) {
			char curr = mBoard[i];

			if (curr != HUMAN_PLAYER && curr != COMPUTER_PLAYER) {

				mBoard[i] = HUMAN_PLAYER;
				if (checkForWinner() == 2) {
					mBoard[i] = OPEN_SPOT;
					return i;
				} else
					mBoard[i] = OPEN_SPOT;
			}
		}

		return -1;
	}

	private int getWinningMove() {

		for (int i = 0; i < BOARD_SIZE; i++) {
			char curr = mBoard[i];

			if (curr != HUMAN_PLAYER && curr != COMPUTER_PLAYER) {

				mBoard[i] = COMPUTER_PLAYER;
				if (checkForWinner() == 3) {
					mBoard[i] = OPEN_SPOT;
					return i;
				} else
					mBoard[i] = OPEN_SPOT;
			}
		}

		return -1;
	}

}
