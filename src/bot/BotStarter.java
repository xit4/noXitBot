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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import field.*;
import moves.MoveType;

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

	private final double heightWeight = 0.510066;
	private final double linesWeight =0.760666;
	private final double holesWeight =0.35663;
	private final double bumpinessWeight =0.184483;
	/**
	 * Returns a random amount of random moves
	 * @param state : current state of the bot
	 * @param timeout : time to respond
	 * @return : a list of moves to execute
	 */
	public ArrayList<MoveType> getMoves(BotState state, long timeout) {

		ArrayList<MoveType> bestMoves;
/*		if(state.getMyField().tooHigh())
*/
			bestMoves = getGreedyMoves(state,timeout);
/*		else*/
/*
			bestMoves = getBuildingMoves(state,timeout);
*/
		bestMoves.add(MoveType.DROP);
		return bestMoves;

	}

	public ArrayList<MoveType> getGreedyMoves(BotState state, long timeout) {

		/*final double heightWeight = 0.510066;
		final double linesWeight =0.760666;
		final double holesWeight =0.35663;
		final double bumpinessWeight =0.184483;*/

		ArrayList<MoveType> bestMoves = new ArrayList<>();
		int bestLeft=0,bestRotation=0;
		double bestScore = 0.0;
		int left;
		Field grid = state.getMyField();
		ShapeType workingPiece = state.getCurrentShape();
		Shape piece = new Shape(workingPiece,grid,state.getShapeLocation());
		for(int rotation = 0; rotation < 4; rotation++){
			left=0;
			if(rotation !=0) {
				piece.turnRight();
			}
			Shape _piece = piece.clone();
			while(grid.canMoveLeft(_piece)){
				_piece.oneLeft();
				left++;
			}

			while(grid.isValid(_piece)){
				Shape _setPiece = _piece.clone();

				while(grid.canMoveDown(_setPiece)){
					_setPiece.oneDown();
				}
				if(grid.isValidTop(_setPiece)) {
					double score;

					Field _grid = grid.clone();
					_grid.addPiece(_setPiece);

					score = -heightWeight * _grid.aggregateHeight() + linesWeight * _grid.lines() - holesWeight * _grid.holes() - bumpinessWeight * _grid.bumpiness();

					if (score > bestScore || bestScore == 0.0) {
						bestScore = score;
						bestLeft = left;
						bestRotation = rotation;
					}
				}
				left--;
				_piece.oneRight();
			}
		}
		for(;bestRotation>0;bestRotation--)
			bestMoves.add(MoveType.TURNRIGHT);
		if(bestLeft<0)
			for(;bestLeft<0;bestLeft++)
				bestMoves.add(MoveType.RIGHT);
		else
			for(;bestLeft>0;bestLeft--)
				bestMoves.add(MoveType.LEFT);
		return bestMoves;

	}

	public ArrayList<MoveType> getBuildingMoves(BotState state, long timeout) {

		ArrayList<MoveType> bestMoves = new ArrayList<>();
		int bestLeft=0,bestRotation=0;
		double bestScore = 0.0;
		int left;
		Field grid = state.getMyField();
		ShapeType workingPiece = state.getCurrentShape();
		int myCombo =state.getMyCombo();
		Shape piece = new Shape(workingPiece,grid,state.getShapeLocation());
/*		if(piece.getLocation().getY()<0)
			piece.oneDown();*/
		for(int rotation = 0; rotation < 4; rotation++){
			left=0;
			if(rotation !=0) {
				piece.turnRight();
			}

			Shape _piece = piece.clone();
			while(grid.canMoveLeft(_piece)){
				_piece.oneLeft();
				left++;
			}

			while(grid.isValid(_piece)){
				Shape _setPiece = _piece.clone();

				while(grid.canMoveDown(_setPiece)){
					_setPiece.oneDown();
				}

				if(grid.isValidTop(_setPiece)) {
					double score;

					Field _grid = grid.clone();
					_grid.addPiece(_setPiece);

					score = -heightWeight * _grid.aggregateHeight() + linesWeight * (myCombo==0 ?  _grid.lines() : myCombo+_grid.lines()) - holesWeight * _grid.holes() - bumpinessWeight * _grid.bumpiness();

					if (score > bestScore || bestScore == 0.0) {
						bestScore = score;
						bestLeft = left;
						bestRotation = rotation;
					}
				}
				left--;
				_piece.oneRight();
			}
		}
		for(;bestRotation>0;bestRotation--)
			bestMoves.add(MoveType.TURNRIGHT);
		if(bestLeft<0)
			for(;bestLeft<0;bestLeft++)
				bestMoves.add(MoveType.RIGHT);
		else
			for(;bestLeft>0;bestLeft--)
				bestMoves.add(MoveType.LEFT);
		return bestMoves;

	}
	
	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
