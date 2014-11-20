//package easyMock;

import java.util.ArrayList;

import junit.framework.*;
import static org.easymock.EasyMock.*;


public class TestMockMp3Player extends TestCase {
	
	  private Mp3Player mockMp3Player;
	  private Mp3Player mockMp3Player_control;
	  protected ArrayList list = new ArrayList();

	  @Override
	  protected void setUp() {
		System.out.println("Setup the Mock Object for the Mp3Player object..");
	    // Create a control handle to the Mock object
		mockMp3Player_control = createMock(Mp3Player.class);
		
	    list = new ArrayList();
	    list.add("Bill Chase -- Open Up Wide");
	    list.add("Jethro Tull -- Locomotive Breath");
	    list.add("The Boomtown Rats -- Monday");
	    list.add("Carl Orff -- O Fortuna");

	    // And create the mock object itself
		mockMp3Player = mockMp3Player_control;
	  }

	  
	public void testPlay_easyMock() {
		System.out.println("Executing the test case for Play with the EasyMock Objects...");
		mockMp3Player.loadSongs(list);
		    
		//Easy Mock - Start
		expect(mockMp3Player.isPlaying()).andReturn(false);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertFalse(mockMp3Player.isPlaying());
		
		//Easy Mock - Start
		reset(mockMp3Player_control);
		mockMp3Player.play();
		expect(mockMp3Player.isPlaying()).andReturn(true);
		expect(mockMp3Player.currentPosition()).andReturn(1.0);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertTrue(mockMp3Player.isPlaying());
		assertTrue(mockMp3Player.currentPosition() != 0.0);
		reset(mockMp3Player_control);
		
		mockMp3Player.pause();
		//Easy Mock - Start
		expect(mockMp3Player.currentPosition()).andReturn(1.0);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertTrue(mockMp3Player.currentPosition() != 0.0);
		reset(mockMp3Player_control);
		
		mockMp3Player.stop();
		//Easy Mock - Start
		expect(mockMp3Player.currentPosition()).andReturn(0.0);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertEquals(mockMp3Player.currentPosition(), 0.0, 0.1);
	}

	public void testPlayNoList_easyMock() {
		System.out.println("Executing the test case for Play with No List with the EasyMock Objects...");
	
		// Don't set the list up
		//Easy Mock - Start
		expect(mockMp3Player.isPlaying()).andReturn(false);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertFalse(mockMp3Player.isPlaying());
		reset(mockMp3Player_control);
		
		mockMp3Player.play();
		//Easy Mock - Start
		expect(mockMp3Player.isPlaying()).andReturn(false);
		expect(mockMp3Player.currentPosition()).andReturn(0.1);
		replay(mockMp3Player_control);
		//Easy Mock - End
		assertFalse(mockMp3Player.isPlaying());
		assertEquals(mockMp3Player.currentPosition(), 0.0, 0.1);
		reset(mockMp3Player_control);
		
		mockMp3Player.pause();
		//Easy Mock - Start
		expect(mockMp3Player.currentPosition()).andReturn(0.1);
		expect(mockMp3Player.isPlaying()).andReturn(false);
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentPosition(), 0.0, 0.1);
		assertFalse(mockMp3Player.isPlaying());
		reset(mockMp3Player_control);
		
		mockMp3Player.stop();
		//Easy Mock - Start
		expect(mockMp3Player.currentPosition()).andReturn(0.0);
		expect(mockMp3Player.isPlaying()).andReturn(false);
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentPosition(), 0.0, 0.1);
		assertFalse(mockMp3Player.isPlaying());
		reset(mockMp3Player_control);

	}

	public void testAdvance_easyMock() {
		System.out.println("Executing the test case for Advance with the EasyMock Objects...");
	
		mockMp3Player.loadSongs(list);
		mockMp3Player.play();
		
		//Easy Mock - Start
		expect(mockMp3Player.isPlaying()).andReturn(true);
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertTrue(mockMp3Player.isPlaying());
		reset(mockMp3Player_control);
		
		mockMp3Player.prev();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(0));
		expect(mockMp3Player.isPlaying()).andReturn(true);
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(0));
		assertTrue(mockMp3Player.isPlaying());
		reset(mockMp3Player_control);
		
		mockMp3Player.next();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(1));
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(1));
		reset(mockMp3Player_control);
		
		
		mockMp3Player.next();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(2));
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(2));
		reset(mockMp3Player_control);
		
		mockMp3Player.prev();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(1));
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(1));
		reset(mockMp3Player_control);
		
		mockMp3Player.next();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(2));
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(2));
		reset(mockMp3Player_control);
		
		mockMp3Player.next();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(3));
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(3));
		reset(mockMp3Player_control);
		
		mockMp3Player.next();
		//Easy Mock - Start
		expect(mockMp3Player.currentSong()).andReturn((String)list.get(3));
		expect(mockMp3Player.isPlaying()).andReturn(true);
		replay(mockMp3Player_control);	
		//Easy Mock - End
		assertEquals(mockMp3Player.currentSong(), list.get(3));
		assertTrue(mockMp3Player.isPlaying());
	}
}
