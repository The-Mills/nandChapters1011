package fileIO;

import java.io.File;

public interface Opener 
{
	/**
	 * Opens the file
	 * @param path - The path to the file
	 * @return true if the file was opened without error. Else false.
	 */
	boolean open(String path);
	
	/**
	 * Opens the file
	 * @param theFile - The file
	 * @return true if the file was opened without error. Else false.
	 */
	boolean open(File theFile);
	
	/**
	 * Closes the IO stream and otherwise resets the Opener for reuse. 
	 */
	void reset();
}