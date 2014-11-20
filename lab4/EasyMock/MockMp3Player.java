//package easyMock;

import java.util.ArrayList;
import java.util.List;


public class MockMp3Player implements Mp3Player{

	
	//Mp3 Player needs to play songs so creating a song name list of String
	List<String> listOfSongs = new ArrayList<String>();
	
	//Current Position is needed by the Interface of type Double
	double currentPosition = 0.0;
	
	int songPositionLocator = 0;
	boolean isSongPlaying = false;
	
	
	@Override
	public void play() {
		System.out.println("Play option selected");
		System.out.println("Verifying if a song can be played by verifying if a list of songs exists...");
		if(listOfSongs.size() > 0){
			System.out.println("List of Songs is of size: " + listOfSongs.size());
			//Set the Current Position for the Song since we are going to play it
			currentPosition = 1.0;
			//Set the value for isSongPlaying to TRUE to indicate that a song is playing
			System.out.println("Playing the song now..");
			isSongPlaying = true;
		}else{
			System.out.println("List of Songs is Empty. No song is available to play");
			//Indicates that there are no songs in the list, so nothing to play
		}
	}

	@Override
	public void pause() {
		System.out.println("Pause option selected");
		System.out.println("Pausing the current song playing");
		//Set the value for isSongPlaying to FALSE to indicate that the song has paused playing
		isSongPlaying = false;
		System.out.println("Paused successfully");
	}

	@Override
	public void stop() {
		System.out.println("Stop option selected");
		System.out.println("Stopping the current song playing");
		//Set the value for isSongPlaying to FALSE to indicate that the song has paused playing
		isSongPlaying = false;
		
		//Reset the position of the Song too
		currentPosition = 0.0;
		System.out.println("Stopped successfully");
	}

	@Override
	public double currentPosition() {
		System.out.println("Current position of Song is: " + currentPosition);
		return currentPosition;
	}

	@Override
	public String currentSong() {
		String currentSongPlaying = listOfSongs.get(songPositionLocator);
		System.out.println("Current Song being played is: " + currentSongPlaying);
		return currentSongPlaying;
	}

	@Override
	public void next() {
		System.out.println("Next option selected.");
		System.out.println("Moving the song position to the next point after verifying if there is next position");
		//Increment the Current Song Position Locator by 1 if the count is not more than the list available
		if(songPositionLocator < listOfSongs.size()-1){
			songPositionLocator = songPositionLocator + 1;
		}
	}

	@Override
	public void prev() {
		System.out.println("Prev option selected.");
		System.out.println("Moving the song position to the previous point after verifying if there is next position");
		//Decrement the Current Song Position Locator by 1 if the count is not 0
		if(songPositionLocator > 0){
			songPositionLocator = songPositionLocator - 1;
		}
	}

	@Override
	public boolean isPlaying() {
		System.out.println("Verifying if the song is playing. The result is: " + isSongPlaying);
		//Return the current boolean value for isSongPlaying. This will be set or reset in Play, Pause, Stop
		return isSongPlaying;
	}

	@Override
	public void loadSongs(ArrayList names) {
		System.out.println("Loading the list of Songs..");
		//Load the songs provided in the parameter into the Song List
		listOfSongs = names;
	}
}
