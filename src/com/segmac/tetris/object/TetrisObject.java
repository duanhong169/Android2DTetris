package com.segmac.tetris.object;

public interface TetrisObject {
	
	public abstract void transform();
	
	public abstract void randomState();
	
	public abstract Coordinate[] getNextTetrisState();
	
	public abstract Coordinate[] getCurrentTetrisState();
	
	public abstract int getTetrisColor();
	
	/**
	 * 活跃行，相对于base point
	 * 用于得到方块落地时可能会发生collapse动作的行
	 * @return
	 */
	public abstract int getRows();
}
