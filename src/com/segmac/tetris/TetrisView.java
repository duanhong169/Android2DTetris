package com.segmac.tetris;

import java.util.Random;

import com.segmac.tetris.object.*;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class TetrisView extends SurfaceView implements SurfaceHolder.Callback{

	private final String TAG = "Android2DTetris";
	
	//当前占有屏幕的高度和宽度
	private int mHeightOfTheView;
	private int mWidthOfTheView;
	
	//游戏主区域的左上角位置
	private static int mXOffset;
    private static int mYOffset;
    
    //方块预览区的左上角位置
    private static int mPreviewXOffset;
    private static int mPreviewYOffset;
    
    //游戏主区域的高度和宽度
	private final int NUM_COLUMNS = 10;
	private final int NUM_ROWS = 20;
	
	private final int INTERVAL_BETWEEN_TILES = 1;//方块之间的像素间隔（1像素）
	private static final int ACTION_TRANSFORM = 0;//操作代码，0为变换方块的方向
	private static final int ACTION_LEFTSHIFT = 1;//1为向左移动方块
	private static final int ACTION_RIGHTSHIFT = 2;//2为向右移动方块
	private static final int ACTION_DOWNSHIFT = 3;//3为向下移动方块
	
	//游戏的状态，参考Snake
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    
	private static int mTileSize;//每一格的尺寸
	//该数组代表了游戏主区域当前所有的方块
    private int[][] mTileArray = new int[NUM_ROWS][NUM_COLUMNS];
	//该数组代表了游戏主区域当前处于活动状态的方块
    private boolean[][] isTileActive = new boolean[NUM_ROWS][NUM_COLUMNS];
    
    private static int SIZE_OF_PREVIEW = 5;//预览区域的尺寸
	//该数组代表了方块预览区域的方块
    private int[][] mPreviewTileArray = new int[SIZE_OF_PREVIEW][SIZE_OF_PREVIEW];
    
    private static TetrisObject previewTetris;
    //预览方块的基准点坐标
	private static final int PREVIEW_TETRIS_COLUMN = 1;
	private static final int PREVIEW_TETRIS_ROW = 1;
	
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
	private Paint mPaint;
	
	private static TetrisObject newComingTetris;
	//新出现方块的初始坐标
	private static final int NEW_COMING_TETRIS_COLUMN = 4;
	private static final int NEW_COMING_TETRIS_ROW = 1;
	//活动方块的基准点，通过基准点和方块的相对坐标可以计算出方块的实际坐标
	private Coordinate basePoint = new Coordinate(NEW_COMING_TETRIS_COLUMN, NEW_COMING_TETRIS_ROW);
	
    private TextView mStatusText;
    private TextView mScoreText;
    private static long mScore = 0;
    private static long mLevel = 1;
	private static long intervalTime = 1000;//游戏的时钟频率（初始每1000毫秒一次）
	
    //用于驱动游戏
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            TetrisView.this.tick();
            TetrisView.this.invalidate();
        }

        //定时器，睡眠一定时间后发送消息，构成循环
        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };
    
	public TetrisView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setKeepScreenOn(true);
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		mPaint = new Paint();
		mPaint.setColor(Color.DKGRAY);
		mPaint.setAntiAlias(true);		
		setFocusable(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//得到当前屏幕的可用尺寸
		mHeightOfTheView = this.getHeight();
		mWidthOfTheView = this.getWidth();
		
		//得到每个方块的尺寸
		int tileMaxHeight = (int) Math.floor(mHeightOfTheView / NUM_ROWS);
		int tileMaxWidth = (int) Math.floor(mWidthOfTheView / (NUM_COLUMNS + SIZE_OF_PREVIEW));
		mTileSize = tileMaxHeight > tileMaxWidth ? tileMaxWidth : tileMaxHeight;

		//靠左显示游戏主区域
		mXOffset = 0;
		//用于在竖直方向上居中显示游戏主视图
        mYOffset = ((mHeightOfTheView - (mTileSize * NUM_ROWS)) / 2);
        
        //计算方块预览区的左上角位置
        mPreviewXOffset = mWidthOfTheView - SIZE_OF_PREVIEW*mTileSize;
        mPreviewYOffset = mYOffset;
	}

	private void drawTheGameView(){
		mCanvas = mSurfaceHolder.lockCanvas();
		mPaint.setColor(Color.WHITE);
		
		//绘制游戏主区域
		mCanvas.drawRect(mXOffset, mYOffset, mXOffset + NUM_COLUMNS*mTileSize,
				mYOffset + NUM_ROWS*mTileSize, mPaint);
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int column = 0; column < NUM_COLUMNS; column++) {
                if (mTileArray[row][column] != 0) {
            		mCanvas.save();
            		mCanvas.clipRect(mXOffset + column * mTileSize + INTERVAL_BETWEEN_TILES,
            				mYOffset + row * mTileSize + INTERVAL_BETWEEN_TILES,
            				mXOffset + (column+1) * mTileSize - INTERVAL_BETWEEN_TILES,
            				mYOffset + (row+1) * mTileSize - INTERVAL_BETWEEN_TILES);
            		mCanvas.drawColor(mTileArray[row][column]);
                    mCanvas.restore();
                }
            }
        }

        //绘制方块预览区域
		mCanvas.drawRect(mPreviewXOffset, mPreviewYOffset, mPreviewXOffset + SIZE_OF_PREVIEW * mTileSize,
				mPreviewYOffset + SIZE_OF_PREVIEW * mTileSize, mPaint);
        for (int row = 0; row < SIZE_OF_PREVIEW; row++) {
            for (int column = 0; column < SIZE_OF_PREVIEW; column++) {
                if (mPreviewTileArray[row][column] != 0) {
            		mCanvas.save();
            		mCanvas.clipRect(mPreviewXOffset + column * mTileSize + INTERVAL_BETWEEN_TILES,
            				mPreviewYOffset + row * mTileSize + INTERVAL_BETWEEN_TILES,
            				mPreviewXOffset + (column+1) * mTileSize - INTERVAL_BETWEEN_TILES,
            				mPreviewYOffset + (row+1) * mTileSize - INTERVAL_BETWEEN_TILES);
            		mCanvas.drawColor(mPreviewTileArray[row][column]);
                    mCanvas.restore();
                }
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
	}
	
	//方块按一定频率下落，这个频率由intervalTime决定
	private void tick(){
        if (mMode == RUNNING) {
        	
        	//当前方块是否已落地，若已经落地，则进行collapse（消除方块）
        	//和buildNewTetrisObject（产生新方块）操作
			boolean tetrisActive = downShiftActiveTileArray();
			if(!tetrisActive){
				unsetAllActiveTile();
				collapse(basePoint.row, newComingTetris.getRows());
				buildNewTetrisObject();
				basePoint = new Coordinate(NEW_COMING_TETRIS_COLUMN, NEW_COMING_TETRIS_ROW);
			}
			
			drawTheGameView();
			mRedrawHandler.sleep(intervalTime);
        }
	}

	//判断是否有行需要消除，计算消除的行数并计分，当分数达到一定值时加速方块下降速度
	private void collapse(int baseRow, int activeRows){
		Log.v(TAG, "baseRow = " + baseRow + "; activeRows = " + activeRows);
		int numCollapse = 0;
        for (int row = baseRow; row < (baseRow + activeRows < 20 ? baseRow + activeRows : 20); row++) {
            boolean collapseThisRow = true;
        	for (int column = 0; column < NUM_COLUMNS; column++) {
            	if(mTileArray[row][column] == Color.WHITE) collapseThisRow = false;
            }
        	if(collapseThisRow){
        		for(int shiftRows = row; shiftRows > 0; shiftRows--){
        			for (int column = 0; column < NUM_COLUMNS; column++) {
        				mTileArray[shiftRows][column] = mTileArray[shiftRows - 1][column];
        			}
        		}
        		numCollapse++;
        	}
        }
        
        //计分规则
        switch (numCollapse) {
        case 1:
        	mScore++;
        	break;
        case 2:
        	mScore = mScore + 3;
        	break;
        case 3:
        	mScore = mScore + 6;
        	break;
        case 4:
        	mScore = mScore + 10;
        	break;
        default:
        	break;
        }
        mScoreText.setText("已得分\n" + mScore);
        
        //加速游戏规则，20为加速间隔分数，1000为初始间隔，100为每升一级的增量
        mLevel = (mScore / 20) + 1;//每20分升一级
        //每升一级，间隔减少100，最快100
        if(intervalTime > 100) intervalTime = 1000 - (100 * mLevel);
	}
	
	//当transform（按键↑）事件发生时，改变活动格数组
	private void transformActiveTileArray(){
		if(testCollide(ACTION_TRANSFORM, 0, 0)){
			Log.v(TAG, "Transform failed!");
			return;
		}
		newComingTetris.transform();
		Coordinate[] prevCoordinates = newComingTetris.getPrevTetrisState();
		Coordinate[] currentCoordinates = newComingTetris.getCurrentTetrisState();
		for(int i = 0; i < prevCoordinates.length; i++){
			isTileActive[prevCoordinates[i].row + basePoint.row][prevCoordinates[i].column + basePoint.column] = false;
			unsetTile(prevCoordinates[i].row + basePoint.row, prevCoordinates[i].column + basePoint.column);
		}
		for(int i = 0; i < currentCoordinates.length; i++){
			isTileActive[currentCoordinates[i].row + basePoint.row][currentCoordinates[i].column + basePoint.column] = true;
			setTile(newComingTetris.getTetrisColor(), currentCoordinates[i].row + basePoint.row, currentCoordinates[i].column + basePoint.column);
		}
	}
	
	//下移活动格数组，在每一次tick()时被调用，可能是游戏自动调用tick()，
	//也可能在用户按下方向键时被调用
	private boolean downShiftActiveTileArray(){
        for (int row = NUM_ROWS - 1; row >= 0; row--) {
            for (int column = NUM_COLUMNS - 1; column >= 0; column--) {
            	if(isTileActive[row][column]){
            		if(testCollide(ACTION_DOWNSHIFT, column, row)) return false;
            	}
            }
        }
        for (int row = NUM_ROWS - 1; row >= 0; row--) {
            for (int column = NUM_COLUMNS - 1; column >= 0; column--) {
            	if(isTileActive[row][column]){
            		unsetActiveTile(row, column);
            		unsetTile(row, column);
            		setActiveTile(row + 1, column);
            		setTile(newComingTetris.getTetrisColor(), row + 1, column);
            	}
            }
        }
        basePoint.row++;
        return true;
	}
	
	//当左移事件发生时，改变活动格数组
	private void leftShiftActiveTileArray(){
        for (int column = 0; column < NUM_COLUMNS; column++) {
        	for (int row = 0; row < NUM_ROWS; row++) {
            	if(isTileActive[row][column]){
            		if(testCollide(ACTION_LEFTSHIFT, column, row)) return;
            	}
            }
        }
        for (int column = 0; column < NUM_COLUMNS; column++) {
        	for (int row = 0; row < NUM_ROWS; row++) {
            	if(isTileActive[row][column]){
            		unsetActiveTile(row, column);
            		unsetTile(row, column);
            		setActiveTile(row, column - 1);
            		setTile(newComingTetris.getTetrisColor(), row, column - 1);
            	}
            }
        }
        basePoint.column--;
	}
	
	//当右移事件发生时，改变活动格数组
	private void rightShiftActiveTileArray(){
        for (int column = NUM_COLUMNS - 1; column >= 0; column--) {
        	for (int row = 0; row < NUM_ROWS; row++) {
            	if(isTileActive[row][column]){
            		if(testCollide(ACTION_RIGHTSHIFT, column, row)) return;
            	}
            }
        }
        for (int column = NUM_COLUMNS - 1; column >= 0; column--) {
        	for (int row = 0; row < NUM_ROWS; row++) {
            	if(isTileActive[row][column]){
            		unsetActiveTile(row, column);
            		unsetTile(row, column);
            		setActiveTile(row, column + 1);
            		setTile(newComingTetris.getTetrisColor(), row, column + 1);
            	}
            }
        }
        basePoint.column++;
	}
	
	//方块的碰撞检测逻辑
	private boolean testCollide(int actionType, int column, int row){
		switch(actionType){
		case ACTION_TRANSFORM:
			Coordinate[] nextCoordinates = newComingTetris.getNextTetrisState();
			for(int i = 0; i < nextCoordinates.length; i++){
				if(nextCoordinates[i].row + basePoint.row >= NUM_ROWS) return true;
				if(nextCoordinates[i].column + basePoint.column >= NUM_COLUMNS) return true;
				if(!isTileActive[nextCoordinates[i].row][nextCoordinates[i].column] 
						&& mTileArray[nextCoordinates[i].row][nextCoordinates[i].column]
								!= Color.WHITE) return true;
			}
			break;
		case ACTION_LEFTSHIFT:
			if(column - 1 < 0) return true;
			if(!isTileActive[row][column - 1] && mTileArray[row][column - 1]
					!= Color.WHITE) return true;
			break;
		case ACTION_RIGHTSHIFT:
			if(column + 1 > NUM_COLUMNS - 1) return true;
			if(!isTileActive[row][column + 1] && mTileArray[row][column + 1]
					!= Color.WHITE) return true;
			break;
		case ACTION_DOWNSHIFT:
			if(row + 1 > NUM_ROWS - 1) return true;
			if(!isTileActive[row + 1][column] && mTileArray[row + 1][column]
					!= Color.WHITE) return true;
			break;
		default:
			break;
		}
		return false;
	}
	
	//清除所有方块 
    private void clearTiles() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int column = 0; column < NUM_COLUMNS; column++) {
                unsetTile(row, column);
            }
        }
    }
	
    //取消所有活动方块
    private void unsetAllActiveTile(){
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int column = 0; column < NUM_COLUMNS; column++) {
                unsetActiveTile(row, column);
            }
        }
    }
    
    //设置某个Tile为指定颜色的方块
    private void setTile(int tileColor, int row, int column) {
        mTileArray[row][column] = tileColor;
    }
    
    //清除某个Tile的颜色，即清除方块
    private void unsetTile(int row, int column) {
        mTileArray[row][column] = Color.WHITE;
    }
    
    //设置某个Tile为活动状态
    private void setActiveTile(int row, int column) {
    	isTileActive[row][column] = true;
    }
    
    //取消某个Tile的活动状态
    private void unsetActiveTile(int row, int column) {
    	isTileActive[row][column] = false;
    }
    
	//清除所有方块 
    private void clearPreviewTiles() {
        for (int row = 0; row < SIZE_OF_PREVIEW; row++) {
            for (int column = 0; column < SIZE_OF_PREVIEW; column++) {
                unsetPreviewTile(row, column);
            }
        }
    }
    
    //设置某个PreviewTile为指定颜色的方块
    private void setPreviewTile(int tileColor, int row, int column) {
        mPreviewTileArray[row][column] = tileColor;
    }
    
    //清除预览区域
    private void unsetPreviewTile(int row, int column) {
        mPreviewTileArray[row][column] = Color.WHITE;
    }
    
    //将新方块放入游戏
    private void buildNewTetrisObject(){
        newComingTetris = previewTetris;
        Coordinate[] newComingTetrisForm = newComingTetris.getCurrentTetrisState();
        for(int i = 0; i < newComingTetrisForm.length; i++){
        	if(mTileArray[newComingTetrisForm[i].row + NEW_COMING_TETRIS_ROW]
        			[newComingTetrisForm[i].column + NEW_COMING_TETRIS_COLUMN]
        					!= Color.WHITE){
        		setMode(LOSE);
        		return;
        	}
        }
        clearPreviewTiles();
        previewTetrisObject();
        for(int i = 0; i < newComingTetrisForm.length; i++){
        	setTile(newComingTetris.getTetrisColor(), 
        		newComingTetrisForm[i].row + NEW_COMING_TETRIS_ROW,
        		newComingTetrisForm[i].column + NEW_COMING_TETRIS_COLUMN);
        	setActiveTile(newComingTetrisForm[i].row + NEW_COMING_TETRIS_ROW, 
        		newComingTetrisForm[i].column + NEW_COMING_TETRIS_COLUMN);
        }
    }
    
    //在预览区域显示下一个方块
    private void previewTetrisObject(){
    	previewTetris = emergeOneTetrisObject();
    	Coordinate[] previewTetrisForm = previewTetris.getCurrentTetrisState();
        for(int i = 0; i < previewTetrisForm.length; i++){
        	setPreviewTile(previewTetris.getTetrisColor(), 
        		previewTetrisForm[i].row + PREVIEW_TETRIS_ROW,
        		previewTetrisForm[i].column + PREVIEW_TETRIS_COLUMN);
        }
    }
    
    //随机产生一个方块
    private TetrisObject emergeOneTetrisObject(){
    	TetrisObject newTetrisObject;
		Random random = new Random();
		int i = random.nextInt(7);
		switch (i) {
		case 0:
			newTetrisObject = new I();
			break;
		case 1:
			newTetrisObject = new J();
			break;
		case 2:
			newTetrisObject = new L();
			break;
		case 3:
			newTetrisObject = new O();
			break;
		case 4:
			newTetrisObject = new S();
			break;
		case 5:
			newTetrisObject = new T();
			break;
		case 6:
			newTetrisObject = new Z();
			break;
		default:
			newTetrisObject = new O();
			break;
		}
		return newTetrisObject;
    }
    
    //将二维数组转换为一位数组，供保存状态使用
    private int[] twoDimIntArrayToDimIntArray(int[][] tileArray) {
        int[] rawIntArray = new int[NUM_ROWS * NUM_COLUMNS];
    	for (int row = 0; row < NUM_ROWS; row++) {
            for (int column = 0; column < NUM_COLUMNS; column++) {
                rawIntArray[row * NUM_COLUMNS + column] = tileArray[row][column];
            }
        }
    	return rawIntArray;
    }
    
    private boolean[] twoDimBooleanArrayToDimBooleanArray(boolean[][] activeTileArray) {
        boolean[] rawBooleanArray = new boolean[NUM_ROWS * NUM_COLUMNS];
    	for (int row = 0; row < NUM_ROWS; row++) {
            for (int column = 0; column < NUM_COLUMNS; column++) {
                rawBooleanArray[row * NUM_COLUMNS + column] = activeTileArray[row][column];
            }
        }
    	return rawBooleanArray;
    }

    //保存游戏状态
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mTileArray", twoDimIntArrayToDimIntArray(mTileArray));
        map.putBooleanArray("isTileActive", twoDimBooleanArrayToDimBooleanArray(isTileActive));
        map.putLong("mScore", Long.valueOf(mScore));

        return map;
    }

    //将一维数组转换为二位数组，供恢复状态使用
    private int[][] DimIntArrayToTwoDimIntArray(int[] rawArray) {
    	int[][] parsedArray = new int[NUM_ROWS][NUM_COLUMNS];
    	for (int i = 0; i < NUM_ROWS * NUM_COLUMNS; i++) {
    		parsedArray[i / NUM_COLUMNS][i % NUM_COLUMNS] = rawArray[i];
    	}
    	return parsedArray;
    }
    
    private boolean[][] DimBooleanArrayToTwoDimBooleanArray(boolean[] rawArray) {
    	boolean[][] parsedArray = new boolean[NUM_ROWS][NUM_COLUMNS];
    	for (int i = 0; i < NUM_ROWS * NUM_COLUMNS; i++) {
    		parsedArray[i / NUM_COLUMNS][i % NUM_COLUMNS] = rawArray[i];
    	}
    	return parsedArray;
    }

    //恢复游戏状态
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);
        mTileArray = DimIntArrayToTwoDimIntArray(icicle.getIntArray("mTileArray"));
        isTileActive = DimBooleanArrayToTwoDimBooleanArray(icicle.getBooleanArray("isTileActive"));
        mScore = icicle.getLong("mScore");
        mScoreText.setText("已得分\n" + mScore);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mMode == READY || mMode == LOSE) {
                //初始化游戏
                clearTiles();
                unsetAllActiveTile();
                clearPreviewTiles();
                mScore = 0;
                previewTetrisObject();
                buildNewTetrisObject();
                setMode(RUNNING);
                mScoreText.setVisibility(View.VISIBLE);
                mScoreText.setText("已得分\n0");
                tick();
                return true;
            }

            if (mMode == PAUSE) {
                //继续游戏
            	setMode(RUNNING);
                mScoreText.setVisibility(View.VISIBLE);
                mScoreText.setText("已得分\n0");
                tick();
                return true;
            }
            
            //变换方块
			transformActiveTileArray();
			drawTheGameView();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			//加速下降
			tick();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			leftShiftActiveTileArray();
			drawTheGameView();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			rightShiftActiveTileArray();
			drawTheGameView();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//为TetrisView设置需要的TextView
    public void setStatusTextView(TextView newView) {
        mStatusText = newView;
    }
    
    public void setScoreTextView(TextView newView) {
        mScoreText = newView;
    }
	
    //设置游戏的状态
    //newMode:要将游戏设置成为的状态（RUNNING/PAUSE/READY/LOSE）
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            mScoreText.setVisibility(View.VISIBLE);
            tick();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            str = res.getString(R.string.mode_lose_prefix) + mScore
                  + res.getString(R.string.mode_lose_suffix);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
