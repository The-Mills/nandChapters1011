package nandCompiler;

import fileIO.Reader;
import fileIO.FileReader;
import java.io.File;

public class Compiler 
{
	private static final String USER_INPUT_FILEPATH = "D:\\3650 Projects\\nand2tetris\\projects\\11\\Seven";
	
	public static final int TOKEN_KEYWORD    = 0;
	public static final int TOKEN_IDENTIFIER = 1;
	public static final int TOKEN_SYMBOL     = 2;
	public static final int TOKEN_STRING     = 3;
	public static final int TOKEN_INTEGER    = 4;
	
	public static void main(String[] args)
	{
		Reader theFileReader = new FileReader();
		JackTokenizer theTokenizer = new JackTokenizer();
		CompilationEngine theEngine = new CompilationEngine(theTokenizer);
		File[] jackFiles;
		String currentJackLine;
		
		if(!USER_INPUT_FILEPATH.endsWith(".jack"))
			jackFiles = theFileReader.getFileList(USER_INPUT_FILEPATH, ".jack");
		else
			jackFiles = new File[]{new File(USER_INPUT_FILEPATH)};
		
		if(jackFiles.length == 0)
			System.out.println("ERROR: No .jack files found or directory does not exist. Terminating.");
		
		for(File f : jackFiles)
		{
			if(!theFileReader.open(f))
			{
				System.out.println("ERROR: Could not open " + f  + " - This .jack file does not exist.");
				continue;
			}
			
			while(theFileReader.hasMoreLines())
			{
				currentJackLine = theFileReader.readNextLine();
				theTokenizer.inputJackLine(currentJackLine);
			}
			
			//theTokenizer.printTokens();
			
			theFileReader.reset();
			
			theEngine.setJackFile(f);
			theEngine.compileClass();
			theEngine.reset();
			
			
			theTokenizer.reset();
			
		}
	}
}