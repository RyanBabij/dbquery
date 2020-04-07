import java.io.*; // file operations
import java.util.Vector; // vectors obvs
import java.util.concurrent.TimeUnit; //code execution timing
import java.nio.ByteBuffer; // convert int to bytes

public class dbquery
{

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
        	
        	
        }
        catch (IOException ex)
        {
            System.out.println("Error loading heap file.");
        }
    }
}