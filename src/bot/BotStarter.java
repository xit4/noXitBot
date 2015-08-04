// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import field.Field;
import field.Shape;
import field.ShapeType;
import moves.MoveType;

import java.awt.*;
import java.util.ArrayList;

/**
 * BotStarter class
 * 
 * This class is where the main logic should be. Implement getMoves() to
 * return something better than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {

	public BotStarter() {}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

	/**
	 * Returns a random amount of random moves
	 * @param state : current state of the bot
	 * @param timeout : time to respond
	 * @return : a list of moves to execute
	 */
	public ArrayList<MoveType> getMoves(BotState state, long timeout) {

		ArrayList<MoveType> bestMoves = new ArrayList<>();

		// Get all the necessary data from the current game state
		Field grid = state.getMyField();
		ShapeType workingPiece = state.getCurrentShape();
		ShapeType workingNextPiece = state.getNextShape();
		int myCombo = state.getMyCombo();

		// Create the pieces that are going to be used to find the best set of moves
		Shape piece = new Shape(workingPiece, grid, state.getShapeLocation());
		Shape nextPiece = new Shape(workingNextPiece, grid, (workingNextPiece == ShapeType.O) ? new Point(4, -1) : new Point(3, -1));

		// Compute the best set of moves with 1 lookahead symbol
		BestScore best = getBestScoreLookahead(grid, piece, myCombo, nextPiece);

		int bestRotation = best.bestRotation;
		int bestLeft = best.bestLeft;

		// Fill the moves array with the computed moves
		for (; bestRotation > 0; bestRotation--)
			bestMoves.add(MoveType.TURNRIGHT);
		if (bestLeft < 0)
			for (; bestLeft < 0; bestLeft++)
				bestMoves.add(MoveType.RIGHT);
		else
			for (; bestLeft > 0; bestLeft--)
				bestMoves.add(MoveType.LEFT);

		bestMoves.add(MoveType.DROP);
		return bestMoves;

	}

	/**
	 * @param grid      : current field
	 * @param piece     : current piece
	 * @param combo     : current combo value
	 * @param nextPiece : next piece (can be null)
	 * @return : the best score, best left moves and best rotation
	 */

	BestScore getBestScoreLookahead(Field grid, Shape piece, int combo, Shape nextPiece) {

		BestScore bestScore = new BestScore();
		/*todo: initializing score to negative fixes moves blocking the "entrance" blocks as best moves. This is a
		* todo: temmporary fix and should be substituted by find a way to move the sample shape over blocking placed shapes
		*
		* todo: find a way to keep a combo streak going, maybe a buildup phase followed by a clearing phase
		*/
		bestScore.score = -1000;

		 // For every rotation
		for(int rotation = 0; rotation < 4; rotation++) {
			int left = 0;
			// Don't rotate the first time around
			if(rotation !=0) {
				piece.turnRight();
			}

			// Move the rotated shape all the way to the left (until it can be moved)
			Shape _piece = piece.clone();
			while(grid.canMoveLeft(_piece)){
				_piece.oneLeft();
				left++;
			}

			// Until the grid is valid (until the piece is moved all the way to the right)
			while(grid.isValid(_piece)){
				Shape _setPiece = _piece.clone();

				// Move the piece all the way down
				while(grid.canMoveDown(_setPiece)){
					_setPiece.oneDown();
				}

				if(grid.isValidTop(_setPiece)) {

					double score;
					int totalPoints;

					Field _grid = grid.clone();
					_grid.addPiece(_setPiece);

					// Compute the score for this composition
					//todo: remove *2
					score = _grid.evaluate(_setPiece, combo * 2);

					totalPoints = _grid.lines() + combo;

					// If a next piece is provided compute the best score and moves for both pieces
					if (nextPiece != null) {
/*						if(_grid.tooHigh(6))
							combo=2;*/
						int removed = _grid.removeLines();
						Shape next = nextPiece.clone();
						BestScore secondBest = getBestScoreLookahead(_grid, next, combo + removed, null);
						score += secondBest.score;
						totalPoints += secondBest.points;
/*
						System.out.println("score:" +score +" left:"+left +" rotation:"+rotation);
*/
					}


					// Save the new best score
					if ((/*totalPoints >= bestScore.points &&*/ score >= bestScore.score) || bestScore.score == 0.0) {

						bestScore.score = score;
						bestScore.bestLeft = left;
						bestScore.bestRotation = rotation;
					}
				}

				// Move the piece to the right before restarting the evaluation
				left--;
				_piece.oneRight();
			}
		}
		return bestScore;
	}

	/*
	* Private class used as a return value
	* */
	private class BestScore {
		double score;
		int bestLeft;
		int bestRotation;
		int points;

		public void BestScore() {
			this.score = 0.0;
			this.bestLeft = 0;
			this.bestRotation = 0;
			this.points = 0;
		}
	}
}
