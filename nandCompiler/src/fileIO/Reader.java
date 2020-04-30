package fileIO;

import java.io.File;

public interface Reader extends Opener
{
	/**
	 * Determines whether the I/O stream has more lines to read.
	 * @return - True, if there are more lines to read. Else return false.
	 */
	boolean hasMoreLines();
	
	/**
	 * Reads the current line in the I/O stream.
	 * @return
	 */
	String readNextLine();
	
	/**
	 * Returns the list of all files in the specified directory that contain the specified file extension.
	 * @param directory - The directory of the files
	 * @param extension - The extension of the files. Must include the "."
	 */
	File[] getFileList(String directory, String extension);
}
