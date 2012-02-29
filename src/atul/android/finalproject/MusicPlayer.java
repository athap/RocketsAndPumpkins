package atul.android.finalproject;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
* Class MusicPlayer
* 
* Brief This is a Singleton class. It is responsible for the playing, stopping all the background 
* 		sounds of the game. It utilizes a sound pool for this purpose.
*/
public class MusicPlayer 
{
	/**
	* Represents the static method which returns the objet of MusicPlayer class. If no object exists
	* then it creates a new object otherwise the existing object is returned.
	* 
	* @note - It is used to create the object for the first time. After the object has been created
	* 		 getInstance() is called to access the object
	* 
	* @param context - Context in which the MusicPlayer is called
	* 
	* @return MusicPlayer object
	*/
	public static synchronized MusicPlayer getInstance(Context context)
	{
		// Check if the object of MusicPlayer exists. If it does not exist then create a new object
		if(ms_instance == null)
			ms_instance = new MusicPlayer(context);
		
		// return the object
		return ms_instance;		
	}
	
	/**
	* This method is used to get access to the instance of the MusicPlayer where context object 
	* is not available. This was required in one of the classes in my game.
	*  
	* @note - This is not used to create the object of the MusicPlayer. It only returns the existing
	* 		  object. 
	*         To create the object use getInstance(Context c)
	* 
	* @return Object of the MusicPlayer
	*/
	public static synchronized MusicPlayer getInstance()
	{
		return ms_instance;	
	}
	
	/**
	* Represents the private constructor of the MusicPlayer.
	* 
	* @param context in which the MusicPlayer is called
	*/
	private MusicPlayer(Context context)
	{
		m_context = context;
		
		// Create a sound pool manager
		m_soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		
		// Create a play list of all the sounds
		m_playList = new HashMap<String, Integer>();
		
		// Get the audio manager service object
		m_audioManager = (AudioManager)m_context.getSystemService(Context.AUDIO_SERVICE);
	}
	
	/**
	* Adds a sound to the playlist
	* 
	* @param name - name of the sound
	* 
	* @param soundId - Resource id of the raw music file
	*/
	public void addSound(String name, int soundId)
	{
		if(name == null || m_soundPool == null)
			return;
		
		int id = m_soundPool.load(m_context, soundId, 1);
		
		m_playList.put(name, id);		
		
	}
	
	/**
	* This method plays the sound specified by the name if it exists in the playlist
	* 
	* @param name of the sound to be played
	*/
	public void play(String name)
	{
		if(name == null || m_audioManager == null || m_playList == null)
			return;
		
		// Check if the sound exists in the play list
		if(!m_playList.containsKey(name))
			return;
		
		// Get current volume
		float currentVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		// Adjust current volume 
		currentVolume = currentVolume / m_audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		// play the sound and get store the id of the song being played
		m_currentPlaying = m_soundPool.play(m_playList.get(name), currentVolume, currentVolume, 1, 0, 1);		
	}
	
	/**
	* This method stops the song specified by the songName.
	* 
	* @note - If the parameter (songName) is null then sound being played currently is stopped
	* 
	* @param songName - Represents the name of the sound which is to be stopped
	*/
	public void stop(String songName)
	{
		if(m_audioManager == null || m_playList == null)
			return;
		
		if(songName == null)
		{
			m_soundPool.stop(m_currentPlaying);
			return;
		}
		else
		{
			if(m_playList.containsKey(songName))
				m_soundPool.stop(m_playList.get(songName));
		}
	}
	
	/**
	* This is the clean up method for MusicPlayer. It releases all the resources which are being used
	* by the MusicPlayer. It is called when the main activity is destroyed
	*  
	*/
	public void closeMusicPlayer()
	{
		m_soundPool.release();
		m_soundPool = null;
		
		m_playList.clear();
		m_audioManager.unloadSoundEffects();
		ms_instance = null;
	}
	
	// Represents the MusicPlayer singleton instance
	private static MusicPlayer ms_instance;
	
	// Represents the sound pool object
	private SoundPool m_soundPool;
	
	/*
	* Represents the play list object
	* Hash map is not thread safe but here it is used by a single thread. 
	*/
	private HashMap<String, Integer> m_playList;
	
	// Represents the Audio manager. Used to access volume
	private AudioManager m_audioManager;
	
	private Context m_context;

	// Represents the id of song being played currently
	private int m_currentPlaying;
}