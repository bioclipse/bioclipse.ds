package net.bioclipse.ds.ui;

import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class DSContextProvider implements IContextProvider {

    public IContext getContext(Object target) {

      if (target instanceof Tree) {
        Tree tree=(Tree) target;
        if (tree.getSelection()!=null && tree.getSelection().length>0){
          TreeItem a = tree.getSelection()[0];
          if (a.getData() instanceof ITestResult) {
//              ITestResult tr= (ITestResult) a.getData();
//            return tr;
          }
          if (a.getData() instanceof IDSTest) {
              return (IDSTest)a.getData();
          }
          if (a.getData() instanceof TestRun) {
              return (TestRun)a.getData();
          }
        }
      }

      return null;
    }

    public int getContextChangeMask() {
      return SELECTION;
    }

    public String getSearchExpression(Object target) {
      return null;
    }

  }
