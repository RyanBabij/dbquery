import java.io.*; // file operations
import java.util.Vector; // vectors obvs
import java.util.concurrent.TimeUnit; //code execution timing
import java.nio.ByteBuffer; // convert int to bytes

public class dbquery
{
	// returns true if this column should be a number
	// hardcoded because CSV
	static public boolean isNumericColumn(int iColumn)
	{
		return (iColumn==0 || iColumn==1 || iColumn==2 || iColumn==3 || iColumn==7 || iColumn==8 || iColumn==9 || iColumn==13);
	}

    public static void main(String[] args)
    {
    	System.out.println("Heap file query program.");
    	System.out.println("Usage: java dbquery <search text> <pagesize>");
    	
    	//Query will always be searching column BLD_NAME (column 4) so this will be hardcoded
    	//Pagesize is necessary to find the heap file, which is named heap.<pagesize>
    	String strSearch="";
    	int pageSize=0;
    	String file = "heap."; // append pagesize
    	
        if (args.length!=2)
        {
        	System.out.println("Error: Program must provide search text and page size.");
        }
        else
        {
           strSearch=args[0];

           if (strSearch.length()==0)
           {
        	   System.out.println("Error: Bad search query.");
        	   System.exit(1);
           }
           strSearch=strSearch.toUpperCase(); // make searches case-insensitive
           
           try
           {
                pageSize = Integer.parseInt(args[1]);
           }
           catch (NumberFormatException nfe)
           {
                System.out.println("Error: Pagesize must be an integer.");
                System.exit(1);
           }
           file = file + args[1];
           
           System.out.println("Using heapfile: "+file);

        }
        
        
        // load in the heap file
        try (InputStream fileIn = new FileInputStream(file);)
        {
        	System.out.println("Reading in file: "+file);
        	
        	// scan through the heap file looking for comma delimiters.
        	// if the column is column 4, then build a string and check if
        	// search string is subset of column.
        	
        	// start timer
        	long startTime = System.nanoTime();
        	
        	// ignore newlines. commas mean next column (nColumns is hardcoded)
        	// current field we are building to scan string.
        	String currentScan = "";
        	String currentRecord = "";
        	
        	int currentColumn = 0;
        	int SEARCH_COLUMN = 4;
        	int N_COLUMN = 19;
        	
        	boolean quotes=false; // flag to ignore commas in quotes
        	boolean intColumn; // if the column is an integer, we must ignore newlines and commas
        	// if they are part of the binary data
        	
        	boolean searchMatch=false; // flip to true if search string matches column.
        	
            int byteRead;
            while (true)
            {
            	byteRead = fileIn.read(); // -1 means EOF

            	if ((quotes==false && byteRead == ',') || (isNumericColumn(currentColumn)==false && byteRead=='\n') || byteRead == -1 ) // delimiter or end of file (process final entry)
            	{
            		if (currentColumn == SEARCH_COLUMN)
            		{
            			// this is a column we want to search
            			// do string match
            			String upperScan = currentScan.toUpperCase();
            			
            			if ( upperScan.contains(strSearch))
            			{
            				searchMatch=true;
            				// flag the match and wait until all data has been read in.	
            			}
            		}
            		
            		
            		// increment counter to next column or reset to first column
            		++currentColumn;
            		if (currentColumn>=N_COLUMN || byteRead=='\n')
            		{
            			if (searchMatch)
            			{
            				// this record is a match, so we print it and exit.
            				System.out.println("Match found: ");
            				
            				// tokenise the string by commas, strip page nulls, convert ints
            				Vector <String> vToken = new Vector();
            				// have to tokenise manually to account for commas in quotes
            				boolean quotes2=false;
            				String strToke="";
            				for (int i=0;i<currentRecord.length();++i)
            				{
            					if ( currentRecord.charAt(i)=='"')
            					{
            						quotes2=!quotes2;
            					}
            					else if (quotes2==false && currentRecord.charAt(i)==',')
            					{
            						//delimiter
            						vToken.add(strToke);
            						strToke="";
            					}
            					else
            					{
            						strToke+=currentRecord.charAt(i);
            					}
            				}
            				
            				// a string will have all nulls stripped because a string doesn't need them.
            				// an int will have leading nulls stripped until only the 4 bytes remain.
            				// using CSV the datatypes must be known ahead of time, so they will be hardcoded.
            				for (int i=0;i<vToken.size();++i)
            				{
            					if (i>0)
            					{
            						System.out.print(", ");
            					}
            					if (isNumericColumn(i))
            					{
            						String num = vToken.get(i);
            						
            						if (num.length()<4)
            						{
            							// null field
            							System.out.print("NULL");
            						}
            						else
            						{
	            						// pull the rightmost 4 bytes and convert to int
	            						int excessChars = num.length() - 4;
	            						String strippedNum = num.substring(excessChars);

	            						byte aByte[];
	            						aByte = new byte [4];
	            						aByte[0]=(byte)num.charAt(num.length()-4);
	            						aByte[1]=(byte)num.charAt(num.length()-3);
	            						aByte[2]=(byte)num.charAt(num.length()-2);
	            						aByte[3]=(byte)num.charAt(num.length()-1);
	            						
	            						// convert the bytes to int
	            						ByteBuffer wrapped = ByteBuffer.wrap(aByte);
	            						int num2 = wrapped.getInt();
	            						
	            						System.out.print(num2);
            						}
            					}
            					else
            					{
            						// strip all nulls from string and print as-is
            						String strOut = vToken.get(i).replace("\0", ""); // strip all nulls from string
            						System.out.print(strOut);
            					}
            				}
            				System.out.println("");

            	            // stop timer
            	    		long endTime = System.nanoTime();

            	    		long totalNanoseconds = endTime - startTime;
            	    		long totalMilliseconds = totalNanoseconds/1000000;

            	    		System.out.println("Search took "+totalMilliseconds+" milliseconds.");
            	    		System.out.println("Exiting program.");
            				return;
            			}
            			
            			currentColumn=0;

            			//wipe record and search strings
            			currentScan="";
            			currentRecord="";
            		}
            		else
            		{
            			currentRecord+=",";
            		}
            		
            		if (byteRead==-1)
            		{
            			break;
            		}
            		
            	}
            	else if (byteRead == 13 )
            	{
            		//Ignore errant \r newline
            	}
            	else
            	{
            		// do we need to read this column in?
            		// build the string
            		if (currentColumn==SEARCH_COLUMN)
            		{
            			currentScan+=(char)byteRead;
            		}
            		currentRecord+=(char)byteRead;
            		
            		if (byteRead=='"') // flip flag to ignore commas in quotes
            		{
            			quotes=!quotes;
            		}
            
            	}
            }
            // stop timer
    		long endTime = System.nanoTime();

    		long totalNanoseconds = endTime - startTime;
    		long totalMilliseconds = totalNanoseconds/1000000;

    		System.out.println("Building name not found.");
    		System.out.println("Search took "+totalMilliseconds+" milliseconds.");
    		System.out.println("Exiting program.");
        }
        catch (IOException ex)
        {
            System.out.println("Error loading heap file.");
        }
    }
}