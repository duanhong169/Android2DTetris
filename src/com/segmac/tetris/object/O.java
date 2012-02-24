package com.segmac.tetris.object;

import android.graphics.Color;

public class O implements TetrisObject {

	private final Coordinate[] stateOne = {new Coordinate(0, 0), new Coordinate(0, 1),
		new Coordinate(1, 0), new Coordinate(1, 1)};
	private Coordinate[] currentState = stateOne;
	
	public O(){
		
	}
	
	@Override
	public void transform() {
		currentState = stateOne;
	}

	@Override
	public Coordinate[] getCurrentTetrisState() {
		return currentState;
	}

	@Override
	public int getTetrisColor() {
		return Color.BLACK;
	}

	@Override
	public void randomState() {
		currentState = stateOne;		
	}

	@Override
	public Coordinate[] getNextTetrisState() {
		return stateOne;
	}

	@Override
	public int getRows() {
		return 3;
	}
}
