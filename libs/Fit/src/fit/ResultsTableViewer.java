package fit;

import ij.measure.ResultsTable;

public interface ResultsTableViewer {
	void addResultsTableRecord(ResultsTable rt, double[] fit, String name);
}
