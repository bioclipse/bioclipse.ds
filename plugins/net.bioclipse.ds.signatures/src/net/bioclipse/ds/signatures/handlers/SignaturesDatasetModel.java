package net.bioclipse.ds.signatures.handlers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SignaturesDatasetModel implements PropertyChangeListener{

    private static final int DEFAULT_HEIGHT = 3;

    private int height = DEFAULT_HEIGHT;
	private String newFile;
	private String nameProperty;
	private String responseProperty;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
	}
	
	public SignaturesDatasetModel() {
	}

	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		propertyChangeSupport.firePropertyChange("height", this.height,
				this.height = height);
	}

	public String getNewFile() {
		return newFile;
	}
	public void setNewFile(String newFile) {
		propertyChangeSupport.firePropertyChange("newFile", this.newFile,
				this.newFile = newFile);
	}

	public String getNameProperty() {
		return nameProperty;
	}
	public void setNameProperty(String nameProperty) {
		propertyChangeSupport.firePropertyChange("nameProperty", this.nameProperty,
				this.nameProperty = nameProperty);
	}

	public String getResponseProperty() {
		return responseProperty;
	}
	public void setResponseProperty(String responseProperty) {
		propertyChangeSupport.firePropertyChange("responseProperty", this.responseProperty,
				this.responseProperty = responseProperty);
	}

}
