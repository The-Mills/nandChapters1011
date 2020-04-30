package fileIO;

public interface Writer extends Opener
{
	/**
	 * Writes the given text to the input file. No newline character is added at the end.
	 * @param input - The text to be written.
	 */
	void write(String input);
	
}