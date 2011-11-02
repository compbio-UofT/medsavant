
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.ut.biolab.medsavant.server.log.ServerLogger;


/**
 *
 * @author mfiume
 */
public class Exec {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            sortFileByPosition("temp_proj1_ref1_update1","outfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private static void sortFileByPosition(String inFile, String outfile) throws IOException, InterruptedException {
        String sortCommand = "sort -t , -k 5,5 -k 6,6n -k 7 " + ((new File(inFile)).getAbsolutePath());
        
        ServerLogger.log(Exec.class, "Sorting file: " + ((new File(inFile)).getAbsolutePath()));
        
        if (!(new File(inFile)).exists()) {
            throw new IOException("File not found " + ((new File(inFile)).getAbsolutePath()));
        }

        Process p = Runtime.getRuntime().exec(sortCommand);
        //p.waitFor();
        
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        ServerLogger.log(Exec.class, "Writing results to file: " + ((new File(outfile)).getAbsolutePath()));
        
        boolean nothingWritten = true;
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        String s = null;
        // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                bw.write(s + "\n");
                //System.out.println(s);
                nothingWritten = false;
            }
        
            
        stdInput.close();
        bw.close();
        
        if (nothingWritten || !(new File(outfile)).exists()) {
            throw new IOException("Problem sorting file; no output");
        }
        
        ServerLogger.log(Exec.class, "Done sorting");
        
    }
}
