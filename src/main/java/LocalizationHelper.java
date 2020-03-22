import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Provides functionality to localize maximums in dSTORM localization algorithm.
 */
public class LocalizationHelper {
    private static final int borderWidth = 1;
    private int pixelsWidth;
    private int pixelsHeight;
    private int radiusForFitting;
    private int minThreshold;
    private int maxThreshold;

    /**
     * Creates localization helper instance with several parameters:
     * @param pixelsHeight image height in pixels
     * @param pixelsWidth image width in pixels
     * @param radiusForFitting fitting radius in pixels
     * @param threshold is used for thresholding (helps to avoid weak signals and noise)
     */
    public LocalizationHelper(int pixelsHeight, int pixelsWidth, int radiusForFitting, int minThreshold, int maxThreshold) {
        this.pixelsHeight = pixelsHeight;
        this.pixelsWidth = pixelsWidth;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        if (radiusForFitting > 1) {
            this.radiusForFitting = radiusForFitting;
        } else {
            this.radiusForFitting = 3;
        }
    }

    /**
     * Allows to get localized maximums with fitting squares for further fitting.
     * @param intensities image intensity matrix
     * @throws Exception if threshold is bigger than the biggest intensity of all maximums
     * @return fitting squares with intensities
     */
    public int[][][] getLocalizedFittingSquaresForSingleImage(int[][] intensities) throws Exception {
        ArrayList<LocalMaximum> localMaximums = getLocalMaximumsForSingleImage(intensities);
        return getFittingSquares(localMaximums, intensities);
    }

    /**
     * Allows to get localized maximums.
     * @param intensities image intensity matrix
     * @throws Exception if threshold is bigger than the biggest intensity of all maximums
     * @return list of maximums
     */
    public ArrayList<LocalMaximum> getLocalMaximumsForSingleImage(int[][] intensities) throws Exception {
        int[][] averagedIntensities = new int[pixelsHeight][pixelsWidth];
        for (int pixelRow = 0; pixelRow < pixelsHeight; pixelRow++) {
            for (int pixelColumn = 0; pixelColumn < pixelsWidth; pixelColumn++) {
                averagedIntensities[pixelRow][pixelColumn] = intensities[pixelRow][pixelColumn];
            }
        }
        sumNeighbourIntensities(averagedIntensities, intensities);
        boolean[][] mask = getLocalMaximumsMask(averagedIntensities);
        //Comment this to use initial non-averaged intensities for maximums
        //subtractBackground(averagedIntensities);
        //ArrayList<LocalMaximum> localMaximums = getLocalMaximumCandidates(mask, averagedIntensities);
        //Uncomment this to use initial non-averaged intensities for maximums
        //subtractBackground(intensities);
        ArrayList<LocalMaximum> localMaximums = getLocalMaximumCandidates(mask, intensities);
        return getThresholdAppliedMaximums(localMaximums);
    }

    /**
     * Creates averaged image pixels intensities for non-max suppression.
     * Neglects edge with 1 pixel width.
     * @param averagedIntensities matrix for averaging
     * @param intensities initial image intensity matrix
     */
    private void sumNeighbourIntensities(int[][] averagedIntensities, int[][] intensities) {
        for (int pixelRow = borderWidth; pixelRow < pixelsHeight - borderWidth; pixelRow++) {
            for (int pixelColumn = borderWidth; pixelColumn < pixelsWidth - borderWidth; pixelColumn++) {
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow - 1][pixelColumn];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow + 1][pixelColumn];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow][pixelColumn - 1];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow][pixelColumn + 1];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow - 1][pixelColumn - 1];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow - 1][pixelColumn + 1];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow + 1][pixelColumn - 1];
                averagedIntensities[pixelRow][pixelColumn] += intensities[pixelRow + 1][pixelColumn + 1];
            }
        }
    }
    
    /**
     * Subtracts intensity of less bright pixel from all image
     * @param intensities
     */
    private void subtractBackground(int[][] intensities) {
    	int min = Integer.MAX_VALUE;
    	for (int pixelRow = borderWidth; pixelRow < pixelsHeight - borderWidth; pixelRow++) {
            for (int pixelColumn = borderWidth; pixelColumn < pixelsWidth - borderWidth; pixelColumn++) {
            	if (intensities[pixelRow][pixelColumn] < min) {
            		min = intensities[pixelRow][pixelColumn];
            	}
            }
    	}
    	for (int pixelRow = borderWidth; pixelRow < pixelsHeight - borderWidth; pixelRow++) {
            for (int pixelColumn = borderWidth; pixelColumn < pixelsWidth - borderWidth; pixelColumn++) {
            	intensities[pixelRow][pixelColumn] -= min;
            }
    	}
    }

    /**
     * Provides non-max suppression for averaged image. Allows to get logical mask with true values at local maximums.
     * Neglects border.
     * @param intensities averaged image intensity matrix
     * @return local maximums logical mask
     */
    private boolean[][] getLocalMaximumsMask (int[][] intensities) {
        boolean[][] maximums = new boolean[pixelsHeight][pixelsWidth];
        setUpMaximumsMask(maximums);
        for (int pixelRow = radiusForFitting; pixelRow < pixelsHeight - radiusForFitting; pixelRow++) {
            for (int pixelColumn = radiusForFitting; pixelColumn < pixelsWidth - radiusForFitting; pixelColumn++) {
                maximums[pixelRow][pixelColumn] = true;
            }
        }
        for (int pixelRow = radiusForFitting; pixelRow < pixelsHeight - radiusForFitting; pixelRow++) {
            for (int pixelColumn = radiusForFitting; pixelColumn < pixelsWidth - radiusForFitting; pixelColumn++) {
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] >= intensities[pixelRow - 1][pixelColumn];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] > intensities[pixelRow + 1][pixelColumn];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] >= intensities[pixelRow][pixelColumn - 1];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] > intensities[pixelRow][pixelColumn + 1];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] >= intensities[pixelRow - 1][pixelColumn - 1];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] > intensities[pixelRow - 1][pixelColumn + 1];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] >= intensities[pixelRow + 1][pixelColumn - 1];
                maximums[pixelRow][pixelColumn] &= intensities[pixelRow][pixelColumn] > intensities[pixelRow + 1][pixelColumn + 1];
            }
        }
        return maximums;
    }

    /**
     * Sets up logical mask to neglect border.
     * @param maximums mask
     */
    private void setUpMaximumsMask(boolean maximums[][]) {
        for (int pixelRow = 0; pixelRow < radiusForFitting; pixelRow++) {
            for (int pixelColumn = 0; pixelColumn < pixelsWidth; pixelColumn++) {
                maximums[pixelRow][pixelColumn] = false;
            }
        }
        for (int pixelRow = pixelsHeight - radiusForFitting - 1; pixelRow < pixelsHeight; pixelRow++) {
            for (int pixelColumn = 0; pixelColumn < pixelsWidth; pixelColumn++) {
                maximums[pixelRow][pixelColumn] = false;
            }
        }
        for (int pixelRow = radiusForFitting; pixelRow < pixelsHeight - radiusForFitting; pixelRow++) {
            for (int pixelColumn = 0; pixelColumn < radiusForFitting; pixelColumn++) {
                maximums[pixelRow][pixelColumn] = false;
            }
            for (int pixelColumn = pixelsWidth - radiusForFitting - 1; pixelColumn < pixelsWidth - radiusForFitting; pixelColumn++) {
                maximums[pixelRow][pixelColumn] = false;
            }
        }
    }

    /**
     * Allows to get list of local maxim candidates.
     * @param maximumsMask logical local maximums mask
     * @param intensities image intensity (averaged or not)
     * @return list of maximums
     */
    private ArrayList<LocalMaximum> getLocalMaximumCandidates(boolean[][] maximumsMask, int[][] intensities) {
        ArrayList<LocalMaximum> localMaximums = new ArrayList<>();
        for (int pixelRow = radiusForFitting; pixelRow < pixelsHeight - radiusForFitting; pixelRow++) {
            for (int pixelColumn = radiusForFitting; pixelColumn < pixelsWidth - radiusForFitting; pixelColumn++) {
                if (maximumsMask[pixelRow][pixelColumn] == true) {
                    localMaximums.add(new LocalMaximum(intensities[pixelRow][pixelColumn], pixelRow, pixelColumn));
                }
            }
        }
        return localMaximums;
    }

    /**
     * Provides thresholding to remove noise maximums and weak signals.
     * @param candidates list of maximums
     * @throws Exception if threshold is bigger than the biggest intensity of all maximums
     * @return sieved list of maximums
     */
    private ArrayList<LocalMaximum> getThresholdAppliedMaximums(ArrayList<LocalMaximum> candidates) throws Exception {
    	Collections.sort(candidates, new Comparator<LocalMaximum>() {
            @Override
            public int compare(LocalMaximum o1, LocalMaximum o2) {
                return (o1.getIntensity() > o2.getIntensity()) ? -1 : ((o1.getIntensity() == o2.getIntensity()) ? 0 : 1);
            }
        });
    	/*
        if (candidates.get(0).getIntensity() < threshold) {
            throw new Exception("Threshold is too big, all maximums were sieved!");
        }
        */
        for (int i = candidates.size() - 1; i >= 0; i--) {
            if (candidates.get(i).getIntensity() < minThreshold || candidates.get(i).getIntensity() > maxThreshold) {
                candidates.remove(i);
            }
            else {
                break;
            }
        }
        candidates.trimToSize();
        return candidates;
    }

    /**
     * Allows to get array of fitting squares for further fitting.
     * @param maximums list of local maximums
     * @param intensities image intensity matrix
     * @return fitting squares
     */
    private int[][][] getFittingSquares(ArrayList<LocalMaximum> maximums, int[][] intensities) {
        int[][][] fittingSquares = new int[maximums.size()][radiusForFitting * 2 + 1][radiusForFitting * 2 + 1];
        int counter = 0;
        for (LocalMaximum maximum : maximums) {
            int centerRow = maximum.getRow();
            int centerColumn = maximum.getColumn();
            int upperFittingBorder = centerRow - radiusForFitting;
            //int bottomFittingBorder = centerRow + radiusForFitting;
            int leftFittingBorder = centerColumn - radiusForFitting;
            //int rightFittingBorder = centerColumn + radiusForFitting;
            for (int pixelRow = 0; pixelRow < radiusForFitting * 2 + 1; pixelRow++) {
                for (int pixelColumn = 0; pixelColumn < radiusForFitting * 2 + 1; pixelColumn++) {
                    fittingSquares[counter][pixelRow][pixelColumn] =
                            intensities[upperFittingBorder + pixelRow][leftFittingBorder + pixelColumn];
                }
            }
            counter++;
        }
        return fittingSquares;
    }
    
    /**
     * Getter for fitting radius
     * @return radiusForFitting
     */
    public int getRadiusForFitting() {
    	return radiusForFitting;
    }
}
