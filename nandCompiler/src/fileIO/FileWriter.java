package fileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileWriter implements Writer
{
	private PrintWriter writer;
	
	public boolean open(String path)
	{
		return open(new File(path));
	}
	
	public boolean open(File theFile)
	{
		try
		{
			writer = new PrintWriter(theFile);
			return true;
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
	}
	
	public void reset()
	{
		writer.close();
	}
	
	public void write(String input)
	{
		writer.print(input);
	}
}
