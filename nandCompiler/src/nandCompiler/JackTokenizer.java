package nandCompiler;

import java.util.List;
import java.util.ArrayList;

public class JackTokenizer 
{
	private static final char[] JACK_SYMBOLS = new char[] {'{', '}', '(', ')', '[', ']', '.', ',' , ';' , '+' , '-' , '*'
														  ,'/', '&', '|', '<', '>', '=', '~'};
	private static final String[] JACK_KEYWORDS = new String[] {"class", "constructor", "function", "method", "field", "static"
															   ,"var", "int", "char", "boolean", "void", "true", "false", "null"
															   , "this", "let", "do", "if", "else", "while", "return"}; 
	
	private List<String> tokens = new ArrayList<String>();
	private List<Integer> tokenTypes = new ArrayList<Integer>();
	private int numberTokens = 0;
	private int currentToken = 0;
	private boolean parsingComment = false;
	private String currentJackLine = "";
	
	public void inputJackLine(String jackLine)
	{
		currentJackLine = jackLine;
		trimString();
		if(currentJackLine.equals(""))
			return;
		tokenize();
	}
	
	public boolean hasMoreTokens()
	{
		if(currentToken < numberTokens)
			return true;
		return false;
	}
	
	public void advanceToken()
	{
		currentToken++;
	}
	
	public String getCurrentToken()
	{
		return tokens.get(currentToken);
	}
	
	public int getTokenType()
	{
		return tokenTypes.get(currentToken);
	}
	
	public void printTokens()
	{
		System.out.println(numberTokens);
		System.out.println(tokens.size());
		System.out.println(tokenTypes.size());
		for(int i = 0; i < numberTokens; i++)
		{
			System.out.println(tokenTypes.get(i) + "---" + tokens.get(i));
		}
	}
	
	public void reset()
	{
		tokens = new ArrayList<String>();
		tokenTypes = new ArrayList<Integer>();
		numberTokens = 0;
		currentToken = 0;
		parsingComment = false;
		currentJackLine = "";
	}

	/*
	 * Trims the current line of all comments and excess whitespace
	 */
	private void trimString()
	{
		// TO DO: Flesh out error detection for multi-line comments.
		if(parsingComment)
		{
			if(currentJackLine.contains("*/"))
			{
				currentJackLine = currentJackLine.replaceAll(".*\\*/", "");
				parsingComment = false;
			}
			else
			{
				currentJackLine = "";
				return;
			}
		}
		if(currentJackLine.contains("/**") || currentJackLine.contains("/*"))
		{
			if(!currentJackLine.contains("*/"))
				parsingComment = true;
			currentJackLine = currentJackLine.replaceAll("/\\*\\*?+.*+", "");
		}
		
		currentJackLine = currentJackLine.replaceAll("//.*+", "");
		currentJackLine = currentJackLine.trim();
		currentJackLine = currentJackLine.replaceAll("\\s++", " ");
	}
	
	/*
	 * Tokenizes the current String
	 */
	private void tokenize()
	{
		int currentCharIndex = 0, currentTokenStartIndex = 0;
		char currentChar;
		while(currentCharIndex < currentJackLine.length())
		{
			currentChar = currentJackLine.charAt(currentCharIndex);
			if(currentChar == ' ')
			{
				if(currentTokenStartIndex != currentCharIndex)
				{
					tokens.add(currentJackLine.substring(currentTokenStartIndex, currentCharIndex));
					determineTokenType();
					numberTokens++;
				}
				currentTokenStartIndex = currentCharIndex + 1;
			}
			else if(isSymbol(currentChar))
			{
				if(currentTokenStartIndex == currentCharIndex)
				{
					tokens.add(currentJackLine.substring(currentCharIndex, currentCharIndex + 1));
					tokenTypes.add(Compiler.TOKEN_SYMBOL);
					numberTokens++;
					currentTokenStartIndex++;
				}
				else
				{
					tokens.add(currentJackLine.substring(currentTokenStartIndex, currentCharIndex));
					determineTokenType();
					numberTokens++;
					tokens.add(currentJackLine.substring(currentCharIndex, currentCharIndex + 1));
					tokenTypes.add(Compiler.TOKEN_SYMBOL);
					numberTokens++;
					currentTokenStartIndex = currentCharIndex + 1;
				}
			}
			else if(currentChar == '"')
			{
				currentCharIndex = currentJackLine.indexOf("\"", currentCharIndex+1);
				tokens.add(currentJackLine.substring(currentTokenStartIndex + 1, currentCharIndex));
				tokenTypes.add(Compiler.TOKEN_STRING);
				numberTokens++;
				currentTokenStartIndex = currentCharIndex + 1;
			}
			else if(currentJackLine.equals("else"))
			{
				tokens.add(currentJackLine);
				determineTokenType();
				numberTokens++;
				break;
			}
			currentCharIndex++;
		}
	}
	
	/*
	 * Determines if a token is a keyword, identifier, or integer (by default).
	 */
	private void determineTokenType()
	{
		String token = tokens.get(numberTokens);
		if(isKeyword(token))
			tokenTypes.add(Compiler.TOKEN_KEYWORD);
		else
		{
			if(token.matches("\\d+?"))
				tokenTypes.add(Compiler.TOKEN_INTEGER);
			else
				tokenTypes.add(Compiler.TOKEN_IDENTIFIER);
		}	
	}
	
	private boolean isSymbol(char aChar)
	{
		for(char i : JACK_SYMBOLS)
		{
			if(i == aChar)
				return true;
		}
		return false;
	}
	
	private boolean isKeyword(String aString)
	{
		for(String i : JACK_KEYWORDS)
		{
			if(i.equals(aString))
				return true;
		}
		return false;
	}
}