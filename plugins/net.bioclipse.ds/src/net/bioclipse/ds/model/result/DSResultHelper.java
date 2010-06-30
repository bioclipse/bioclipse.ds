/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth - ospjuth@users.sf.net
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.model.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ola
 *
 */
public class DSResultHelper {

    public static Map<Integer, Integer> getResultsFromProperty( String input ) {

//      Atom no -> Norm Value
      Map<Integer, Integer> reslist=new HashMap<Integer, Integer>();
      
//Should operate on
//    1,1;2,56;3,56;5,100
      
      String[] entries = input.split( ";" );
      for (String entry : entries){
          String[] esplit = entry.split( "," );
          int ano = java.lang.Integer.valueOf( esplit[0] );
          int res = java.lang.Integer.valueOf( esplit[1] );
          reslist.put( ano, res );
      }

      return reslist;
  }
    
    
}
