package net.bioclipse.ds.signatures.handlers;

import java.io.File;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class FileExistsValidator implements IValidator {

	@Override
	public IStatus validate(Object value) {
		
		//Should not happen
		if (!(value instanceof String)) return new Status(IStatus.ERROR, " ", "Wrong instance");
		String path = (String) value;
		
		if (path.length()<=0)
			return new Status(IStatus.INFO, " ", "Please specify a filename");
		
//		File file = new File(path);
//		if (file.getParentFile()==null)
//			return new Status(IStatus.ERROR, " ", "File must contain complete path");
//		
//		if (!(file.getParentFile().exists()))
//			return new Status(IStatus.ERROR, " ", "The folder '" + file.getParentFile() + "' does not exist");
//		
//		if (file.getName().length()<=0)
//			return new Status(IStatus.ERROR, " ", "Filename cannot be empty");

		return Status.OK_STATUS;
	}

}
