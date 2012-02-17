package com.segmac.tetris.object;

import java.util.Random;

import android.graphics.Color;

public class I implements TetrisObject {

	//注意：Coordinate(列, 行)
	private final Coordinate[] stateOne = {new Coordinate(1, 0), new Coordinate(1, 1),
			new Coordinate(1, 2), new Coordinate(1, 3)};
	private final Coordinate[] stateTwo = {new Coordinate(0, 1), new Coordinate(1, 1), 
			new Coordinate(2, 1), new Coordinate(3, 1)};
	public Coordinate[] currentState = stateOne;
	public Coordinate[] prevState = stateOne;

	public I(){

	}
	
	@Override
	public void transform() {
		prevState = currentState;
		if(currentState == stateOne) currentState = stateTwo;
		else if(currentState == stateTwo) currentState = stateOne;
	}

	@Override
	public void randomState() {
		Random random = new Random();
		int i = random.nextInt(2);
		switch (i) {
		case 0:
			currentState = stateOne;
			break;
		case 1:
			currentState = stateTwo;
			break;
		default:
			break;
		}
	}

	@Override
	public Coordinate[] getNextTetrisState() {
		if(currentState == stateOne) return stateTwo;
		else if(currentState == stateTwo) return stateOne;
		return stateOne;
	}

	@Override
	public Coordinate[] getCurrentTetrisState() {
		return currentState;
	}

	@Override
	public Coordinate[] getPrevTetrisState() {
		return prevState;
	}

	@Override
	public int getTetrisColor() {
		return Color.RED;
	}

	@Override
	public int getRows() {
		if(currentState == stateOne) return 4;
		else if(currentState == stateTwo) return 2;
		return 0;
	}
}
