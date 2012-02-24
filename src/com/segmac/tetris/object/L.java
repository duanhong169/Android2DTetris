package com.segmac.tetris.object;

import java.util.Random;

import android.graphics.Color;

public class L implements TetrisObject {

	//注意：Coordinate(列, 行)
	private final Coordinate[] stateOne = {new Coordinate(0, 0), new Coordinate(0, 1),
			new Coordinate(0, 2), new Coordinate(1, 2)};
	private final Coordinate[] stateTwo = {new Coordinate(0, 2), new Coordinate(1, 2), 
			new Coordinate(2, 2), new Coordinate(2, 1)};
	private final Coordinate[] stateThree = {new Coordinate(2, 2), new Coordinate(2, 1),
			new Coordinate(2, 0), new Coordinate(1, 0)};
	private final Coordinate[] stateFour = {new Coordinate(2, 0), new Coordinate(1, 0),
			new Coordinate(0, 0), new Coordinate(0, 1)};
	private Coordinate[] currentState = stateOne;

	public L(){

	}

	@Override
	public void transform() {
		if(currentState == stateOne) currentState = stateTwo;
		else if(currentState == stateTwo) currentState = stateThree;
		else if(currentState == stateThree) currentState = stateFour;
		else if(currentState == stateFour) currentState = stateOne;
	}

	@Override
	public Coordinate[] getCurrentTetrisState() {
		return currentState;
	}

	@Override
	public int getTetrisColor() {
		return Color.GREEN;
	}

	@Override
	public void randomState() {
		Random random = new Random();
		int i = random.nextInt(4);
		switch (i) {
		case 0:
			currentState = stateOne;
			break;
		case 1:
			currentState = stateTwo;
			break;
		case 2:
			currentState = stateThree;
			break;
		case 3:
			currentState = stateFour;
			break;
		default:
			break;
		}
	}

	@Override
	public Coordinate[] getNextTetrisState() {
		if(currentState == stateOne) return stateTwo;
		else if(currentState == stateTwo) return stateThree;
		else if(currentState == stateThree) return stateFour;
		else if(currentState == stateFour) return stateOne;
		return stateOne;
	}

	@Override
	public int getRows() {
		if(currentState == stateOne) return 3;
		else if(currentState == stateTwo) return 3;
		else if(currentState == stateThree) return 3;
		else if(currentState == stateFour) return 2;
		return 0;
	}
}