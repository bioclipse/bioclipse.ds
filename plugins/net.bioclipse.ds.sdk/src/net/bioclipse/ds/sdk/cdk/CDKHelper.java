package net.bioclipse.ds.sdk.cdk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

public class CDKHelper {
	

	public static int numberOfEntriesInSDF(String path, IProgressMonitor monitor) throws IOException {

		monitor.beginTask("Parsing size...", IProgressMonitor.UNKNOWN);

        long tStart = System.nanoTime();
        List<Long> values = new LinkedList<Long>();
        int num = 0;
        long pos = 0;
        long start = 0;
        int work = 0;
            BufferedInputStream counterStream
            = new BufferedInputStream( new FileInputStream(path) );
            int c = 0;
            while (c != -1) {
                c = counterStream.read();pos++;
                if (c == '$') {
                    c = counterStream.read();pos++;
                    if (c == '$') {
                        c = counterStream.read();pos++;
                        if (c == '$') {
                            c = counterStream.read();pos++;
                            if (c == '$') {
                                c = counterStream.read();pos++;
                                if ( c == '\r') {
                                    c = counterStream.read();// only CR or CR+LF
                                }else pos--;
                                if( c == '\n') {
                                    pos++;work = (int) start;
                                    start = pos;
                                    counterStream.read();pos++;
                                    num++;
                                }else { // next pos already read
                                    work = (int) start;
                                    start = pos;
                                    pos++;
                                }
                                values.add( start );
                                if ( monitor.isCanceled() ) {
                                    throw new OperationCanceledException();
                                }
                            }
                        }
                    }
                }
            }
            if( (pos-start)>3) {
                values.add(pos);
                num++;
            }
            counterStream.close();
        
        System.out.println("numberOfEntriesInSDF took %d to complete: "+
                          (int)((System.nanoTime()-tStart)/1e6));
        monitor.done();
        return values.size();
    }

	/**
	 * Just read the first mol and return its properties
	 * @param f
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<String> getAvailableProperties(String path) throws FileNotFoundException {
		
        BufferedInputStream is
        = new BufferedInputStream( new FileInputStream(path) );

		IteratingMDLReader ir = new IteratingMDLReader(is, NoNotificationChemObjectBuilder.getInstance());
		if (!ir.hasNext()) throw new IllegalArgumentException("File did not caontain any mols");
		IAtomContainer m = (IAtomContainer) ir.next();

		List<String> props = new ArrayList<String>();
		for (Object pname : m.getProperties().keySet()){
			props.add((String)pname);
		}
		
		if (props.contains("cdk:Title"))
			props.remove("cdk:Title");

		return props;
	}


}
