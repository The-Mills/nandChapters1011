package fileIO;

import java.util.Scanner;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

public class FileReader implements Reader
{
	private Scanner reader;
	private String fileExtension = "";
	private FileFilter extensionFilter = new FileFilter()
			{
				public boolean accept(File file)
				{
					if(file.getName().endsWith(fileExtension))
						return true;
					return false;
				}
			};
	
	public boolean open(String path)
	{
		return open(new File(path));
	}
	
	public boolean open(File theFile)
	{
		try
		{
			reader = new Scanner(theFile);
			return true;
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
	}
	
	public void reset()
	{
		reader.close();
		// fileExtension is already reset in getFileList()
	}
	
	public boolean hasMoreLines()
	{
		return reader.hasNextLine();
	}
	
	public String readNextLine()
	{
		return reader.nextLine();
	}
	
	public File[] getFileList(String directory, String extension)
	{
		fileExtension = extension;
		File directoryFile = new File(directory); 
		File[] filePaths = directoryFile.listFiles(extensionFilter); // Returns null if directory does not exist.
		fileExtension = "";
		
		if(filePaths == null)
			filePaths = new File[0];
		
		return filePaths;
	}
}