package edu.harding.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	private boolean mGameOver = false;

	private Button mBoardButtons[];
	MediaPlayer mHumanMediaPlayer;
	MediaPlayer mComputerMediaPlayer;
	
	private char mTurn = TicTacToeGame.COMPUTER_PLAYER;
	private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;
	static final int DIALOG_DIFFICULTY_ID = 0;
	static final int DIALOG_QUIT_ID = 1;
	static final int DIALOG_ABOUT = 2;
	private boolean mSoundOn;
	
	private SharedPreferences mPrefs;
	private int mHumanWins = 0;
	private int mComputerWins = 0;
	private int mTies = 0;
	private Button boton;
	private TicTacToeGame mGame;
	private BoardView mBoardView;

	private TextView mInfoTextView;
	private TextView mHumanScoreTextView;
	private TextView mComputerScoreTextView;
	private TextView mTieScoreTextView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                        
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        
        mInfoTextView = (TextView) findViewById(R.id.information); 
        mHumanScoreTextView = (TextView) findViewById(R.id.player_score);
        mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
        mTieScoreTextView = (TextView) findViewById(R.id.tie_score);
        
        // Restore the scores from the persistent preference data source
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);     
        mSoundOn = mPrefs.getBoolean(Settings.SOUND_PREFERENCE_KEY, true);
        mHumanWins = mPrefs.getInt("mHumanWins", 0);  
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);
        mBoardView.setBoardColor(mPrefs.getInt(Settings.BOARD_COLOR_PREFERENCE_KEY, Color.GRAY));
               
        String difficultyLevel = mPrefs.getString(Settings.DIFFICULTY_PREFERENCE_KEY, 
        		getResources().getString(R.string.difficulty_harder));
    	if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
    	else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
    	else
    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);  
    	
        if (savedInstanceState == null) {                           
        	nuevoJuego();
        }
        else {        	
        	// Restore the game's state
        	// The same thing can be accomplished with onRestoreInstanceState
        	mGame.setBoardState(savedInstanceState.getCharArray("board"));
        	mGameOver = savedInstanceState.getBoolean("mGameOver");        	
        	mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        	mTurn = savedInstanceState.getChar("mTurn");
        	mGoFirst = savedInstanceState.getChar("mGoFirst");
        	
        	// If it's the computer's turn, the previous turn did not take, so go again  
        	if (!mGameOver && mTurn == TicTacToeGame.COMPUTER_PLAYER) {        		
        		int move = mGame.getComputerMove();
        		setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        	}        	
        }       
        
        displayScores();
	}
	
	  private void displayScores() {
	    	mHumanScoreTextView.setText(Integer.toString(mHumanWins));
	    	mComputerScoreTextView.setText(Integer.toString(mComputerWins));
	    	mTieScoreTextView.setText(Integer.toString(mTies));
	    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		
		outState.putCharArray("board", mGame.getBoardState());		
		outState.putBoolean("mGameOver", mGameOver);
		outState.putCharSequence("info", mInfoTextView.getText());
		outState.putChar("mGoFirst", mGoFirst);
		outState.putChar("mTurn", mTurn);		
	}
	
	protected void onStop() {
	       super.onStop();
	              
	               
	       SharedPreferences.Editor ed = mPrefs.edit();
	       ed.putInt("mHumanWins", mHumanWins);
	       ed.putInt("mComputerWins", mComputerWins);
	       ed.putInt("mTies", mTies);
	       
	        
	       ed.putInt("difficultyLevel", mGame.getDifficultyLevel().ordinal());
	       
	       ed.commit(); 
		}
	
    @Override
	protected void onResume() {		
		super.onResume();
				
		mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound1);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound2);   
	}
    
	@Override
    protected void onPause() {
        super.onPause();
        
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }
	
    

    public boolean onCreateOptionsMenu(Menu menu) { 
        super.onCreateOptionsMenu(menu); 
        	    
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
   } 

	
    
    @SuppressWarnings("deprecation")
	public boolean onOptionsItemSelected(MenuItem item) {
    	 switch (item.getItemId()) {
         case R.id.new_game:
         	nuevoJuego();
             return true;
         case R.id.Settings: 
         	startActivityForResult(new Intent(this, Settings.class), 0);     	
         	return true;
         case R.id.reset:
         	mHumanWins = 0;
         	mComputerWins = 0;
             mTies = 0;
             displayScores();
             return true;
         case R.id.about:
         	showDialog(DIALOG_ABOUT);
         	return true;
        
         }
         return false;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	    	
    	// See if Back button was pressed on Settings activity
        if (requestCode == RESULT_CANCELED) {
        	// Apply potentially new settings      
        	
        	mSoundOn = mPrefs.getBoolean(Settings.SOUND_PREFERENCE_KEY, true);
        	
        	String difficultyLevel = mPrefs.getString(Settings.DIFFICULTY_PREFERENCE_KEY, 
        			getResources().getString(R.string.difficulty_harder));
        	if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
        		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        	else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
	    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        	else
	    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);    	
	    	
        	String goes_first = mPrefs.getString(Settings.GOES_FIRST_PREFERENCE_KEY, "Alternate");
        	if (!goes_first.equals("Alternate")) {
        		// See if any moves have been made.  If not, start a new game
        		// which will use the selected setting
        		for (int i = 0; i < 8; i++)
        			if (mGame.getBoardOccupant(i) != TicTacToeGame.OPEN_SPOT)
        				return;
        		
        		// All spots must be open
        		nuevoJuego();
        	}
        	
        	mBoardView.setBoardColor(mPrefs.getInt(Settings.BOARD_COLOR_PREFERENCE_KEY, Color.GRAY));
        }
    }
    
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
        switch(id) {
        case DIALOG_DIFFICULTY_ID:
            
        	        	
        	final CharSequence[] levels = {
        			getResources().getString(R.string.difficulty_easy),
        			getResources().getString(R.string.difficulty_harder), 
        			getResources().getString(R.string.difficulty_expert)};

        	builder.setTitle(R.string.difficulty_choose);
        	
        	
        	int selected = -1;
        	if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy)
        		selected = 0;
        	else if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder)
        		selected = 1;
        	else if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Expert)
        		selected = 2;
        	
        	
        	builder.setSingleChoiceItems(levels, selected, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
        	    	dialog.dismiss();   
        	    	
        	    	switch (item) {
        	    	case 0:
        	    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        	    		break;
        	    	case 1:
        	    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        	    		break;
        	    	case 2:
        	    		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
        	    		break;
        	    	}        	    	
        	    	
        	    	
        	    	Toast.makeText(getApplicationContext(), levels[item], Toast.LENGTH_SHORT).show();
        	    }
        	});
        	dialog = builder.create();
       	
            break;
        case DIALOG_QUIT_ID:
        	
        	builder.setMessage(R.string.quit_question)
        	       .setCancelable(false)
        	       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   Main.this.finish();
        	           }
        	       })
        	       .setNegativeButton(R.string.no, null);
        	dialog = builder.create();
        	
            break;
        case DIALOG_ABOUT:
        	
        	
        	Context context = getApplicationContext();
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout = inflater.inflate(R.layout.about_dialog, null); 		

			builder.setView(layout);
			builder.setPositiveButton("Listo", null);	
			dialog = builder.create();   
        	break;
        }
        
        return dialog;        
    }
	
	

	private void nuevoJuego() {

		mGame.clearBoard();    	
    	mBoardView.invalidate();       	


    	if (mGoFirst == TicTacToeGame.COMPUTER_PLAYER) {    		
    		mGoFirst = TicTacToeGame.HUMAN_PLAYER;
    		mTurn = TicTacToeGame.COMPUTER_PLAYER;
    		mInfoTextView.setText(R.string.first_computer);
    		int move = mGame.getComputerMove();
    		setMove(TicTacToeGame.COMPUTER_PLAYER, move);
    	}
    	else {
    		mGoFirst = TicTacToeGame.COMPUTER_PLAYER;
    		mTurn = TicTacToeGame.HUMAN_PLAYER;
    		mInfoTextView.setText(R.string.first_human); 
    	}	
    	
    	mGameOver = false;
	}

    private boolean setMove(char player, int location) {
    	
    	if (player == TicTacToeGame.COMPUTER_PLAYER) {    		
    		final int loc = location;
	    	Handler handler = new Handler();     		
    		handler.postDelayed(new Runnable() {
                public void run() {
                	mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, loc);
                	mBoardView.invalidate();   
                	
                	try {
                		if (mSoundOn) 
                			mComputerMediaPlayer.start();
                	}
                	catch (IllegalStateException e) {}; 
                	
                	int winner = mGame.checkForWinner();
                	if (winner == 0) {
                		mTurn = TicTacToeGame.HUMAN_PLAYER;	                                	
                		mInfoTextView.setText(R.string.turn_human);
                	}
                	else 
    	            	endGame(winner);                              	
                } 
     		}, 1000);     		
                
    		return true;
    	}
    	else if (mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location)) { 
    		mTurn = TicTacToeGame.COMPUTER_PLAYER;
        	mBoardView.invalidate();   // Redraw the board
    	   	if (mSoundOn) 
    	   		mHumanMediaPlayer.start();    	   	
    	   	return true;
    	}
    		   	    	
    	return false;
    }
	
	private void endGame(int winner) {
		if (winner == 1) {
    		mTies++;
    		mTieScoreTextView.setText(Integer.toString(mTies));
    		mInfoTextView.setText(R.string.result_tie); 
    	}
    	else if (winner == 2) {
    		mHumanWins++;
    		mHumanScoreTextView.setText(Integer.toString(mHumanWins));
    		String defaultMessage = getResources().getString(R.string.result_human_wins);
    		mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
    	}
    	else if (winner == 3) {
    		mComputerWins++;
    		mComputerScoreTextView.setText(Integer.toString(mComputerWins));
    		mInfoTextView.setText(R.string.result_computer_wins);
    	}
    	
    	mGameOver = true;
    }

	private class ButtonClickListener implements View.OnClickListener {
		int location;

		public ButtonClickListener(int location) {
			this.location = location;
		}

		public void onClick(View view) {
			
			boton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					nuevoJuego();
					
				}
			});
		
			
			if (!mGameOver && mBoardButtons[location].isEnabled()) {
				setMove(TicTacToeGame.HUMAN_PLAYER, location);

				int winner = mGame.checkForWinner();
				if (winner == 0) {
					mInfoTextView.setText(R.string.turn_computer);
					int move = mGame.getComputerMove();
					setMove(TicTacToeGame.COMPUTER_PLAYER, move);
					winner = mGame.checkForWinner();
				}

				if (winner == 0)
					mInfoTextView.setText(R.string.turn_human);
				else {

					if (winner == 1) {
						mTies++;
						mTieScoreTextView.setText(Integer.toString(mTies));
						mInfoTextView.setText(R.string.result_tie);
					} else if (winner == 2) {
						mHumanWins++;
						mHumanScoreTextView.setText(Integer
								.toString(mHumanWins));
						mInfoTextView.setText(R.string.result_human_wins);
						
					} else if (winner == 3) {
						mComputerWins++;
						mComputerScoreTextView.setText(Integer
								.toString(mComputerWins));
						mInfoTextView.setText(R.string.result_computer_wins);
					}

					mGameOver = true;
				}
			}
		}
	}
	
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

        	 	
	    	int col = (int) event.getX() / mBoardView.getBoardCellWidth();
	    	int row = (int) event.getY() / mBoardView.getBoardCellHeight();
	    	int pos = row * 3 + col;
	    		    	
	    	if (!mGameOver && mTurn == TicTacToeGame.HUMAN_PLAYER &&
	    			setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{        		
            	
            	
            	int winner = mGame.checkForWinner();
            	if (winner == 0) { 
            		mInfoTextView.setText(R.string.turn_computer); 
            		int move = mGame.getComputerMove();
            		setMove(TicTacToeGame.COMPUTER_PLAYER, move);            		
            	} 
            	else
            		endGame(winner);           	
            	
            } 
	    	
 
	    	return false;
        } 
    };
}