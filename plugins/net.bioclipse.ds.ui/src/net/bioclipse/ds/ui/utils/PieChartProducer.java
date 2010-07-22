package net.bioclipse.ds.ui.utils;

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


/*
 * GC example snippet: create an icon (in memory)
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class PieChartProducer {

	public static void main (String [] args) {
		Display display = new Display ();

		int greens=3;
		int reds=2;
		int blue=1;
		int radius=44;

		Image icon=generatePieChart(display, greens, reds, blue, radius, 50);

		Shell shell = new Shell (display);
		Button button = new Button (shell, SWT.PUSH);
		button.setImage (icon);
		button.setSize (100, 100);
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		icon.dispose ();
		display.dispose ();
	}

	public static Image generatePieChart(Display display, int greens, int reds, int blues, int radius, int imagesize) {
		Color red = display.getSystemColor (SWT.COLOR_RED);
		Color green = display.getSystemColor (SWT.COLOR_GREEN);
		Color blue = display.getSystemColor (SWT.COLOR_BLUE);
		Color black = display.getSystemColor (SWT.COLOR_BLACK);
		Color white = display.getSystemColor (SWT.COLOR_WHITE);
		Color gray = new Color(display, 150,150,150);
		
		Color lineColor=gray;
		
		Image image = new Image (display, imagesize, imagesize);
		GC gc = new GC (image);
		
		int totals=greens + reds + blues;
		int greenDeg=(int)(360*((float)greens/(totals)));;
//		System.out.println("Green deg: " + greenDeg);

		int redDeg=(int)(360*((float)reds/(totals)));
//		System.out.println("Red deg: " + redDeg);

		int blueDeg=(int)(360*((float)blues/(totals)));
//		System.out.println("Blue deg: " + blueDeg);
		
		int currentDeg=90;	//12 o'clock
		if (blues>0){
			gc.setBackground (blue);
			gc.fillArc(0,0, radius, radius, currentDeg, blueDeg);
			currentDeg+=blueDeg;
			gc.setBackground (lineColor);
			gc.setForeground(lineColor);
			gc.fillArc(0,0, radius, radius, currentDeg, 1);
		}
		if (reds>0){
			gc.setBackground (red);
			gc.fillArc(0,0, radius, radius, currentDeg, redDeg);
			currentDeg+=redDeg;
			gc.setBackground (lineColor);
			gc.setForeground(lineColor);
			gc.fillArc(0,0, radius, radius, currentDeg, 1);
		}
		if (greens>0){
			gc.setBackground (green);
			gc.fillArc(0,0, radius, radius, currentDeg, greenDeg);
			currentDeg+=greenDeg;
			gc.setBackground (lineColor);
			gc.setForeground(lineColor);
			gc.fillArc(0,0, radius, radius, currentDeg, 1);
		}

		gc.setBackground (lineColor);
		gc.setForeground(lineColor);
		gc.drawOval(0, 0, radius, radius);


		gc.dispose ();
		
		return image;

//		ImageData imageData = image.getImageData ();
//
//		PaletteData palette = new PaletteData (
//				new RGB [] {
//						new RGB (0, 0, 0),
//						new RGB (0xFF, 0xFF, 0xFF),
//				});
//		ImageData maskData = new ImageData (20, 20, 1, palette);
//		Image mask = new Image (display, maskData);
//		gc = new GC (mask);
//		gc.setBackground (black);
//		gc.fillRectangle (0, 0, 20, 20);
//		gc.setBackground (white);
//		gc.fillOval(5,5,10,10);
//		gc.dispose ();
//		maskData = mask.getImageData ();
//
////		Image icon = new Image (display, imageData, maskData);
//		Image icon = new Image (display, imageData);
//		return icon;
	}
} 
