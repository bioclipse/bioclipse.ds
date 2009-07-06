package net.bioclipse.ds.birt;

import java.util.HashMap;

import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;


public class Startup implements IStartup {

    public void earlyStartup() {

        //Start up a background job for starting BIRT
        Job loadBirtJob=new Job("Starting BIRT engine"){

        @Override
        protected IStatus run(IProgressMonitor monitor) {
                  monitor.beginTask( "Starting BIRT ", 3 );
                  monitor.worked( 1 );

                  WebViewer.startup();

                  return Status.OK_STATUS;
        }
          
        };
        loadBirtJob.setUser( false );
        loadBirtJob.schedule();
      

    }

}
