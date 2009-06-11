package net.bioclipse.ds.model.report;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.ds.model.TestRun;


public abstract class AbstractTestReportModel {

    private String name;
    private List<DSRow> rows;
    private TestRun testrun;

    public AbstractTestReportModel() {
    }

    public boolean existsRow(int record) {
        if (record < rows.size())
            return true;
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DSRow> getRows() {
        return rows;
    }

    public void setRows(List<DSRow> rows) {
        this.rows = rows;
    }

    public void addRow(DSRow row){
        if (rows==null)
            rows=new ArrayList<DSRow>();
        rows.add(row);
    }

    
    public TestRun getTestrun() {
    
        return testrun;
    }

    
    public void setTestrun( TestRun testrun ) {
        this.testrun = testrun;
        setRows( extractRows( testrun ) );
    }

    public abstract List<DSRow> extractRows( TestRun run );


}
