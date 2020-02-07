package opt.jmetal.util.fileoutput;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import opt.jmetal.solution.Solution;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SolutionListOutput {
    private FileOutputContext varFileContext;
    private FileOutputContext funFileContext;
    private String varFileName = "VAR";
    private String funFileName = "FUN";
    private String separator = "\t";
    private List<? extends Solution<?>> solutionList;
    private List<Boolean> isObjectiveToBeMinimized;

    public SolutionListOutput(List<? extends Solution<?>> solutionList) {
        varFileContext = new DefaultFileOutputContext(varFileName);
        funFileContext = new DefaultFileOutputContext(funFileName);
        varFileContext.setSeparator(separator);
        funFileContext.setSeparator(separator);
        this.solutionList = solutionList;
        isObjectiveToBeMinimized = null;
    }

    public SolutionListOutput setVarFileOutputContext(FileOutputContext fileContext) {
        varFileContext = fileContext;

        return this;
    }

    public SolutionListOutput setFunFileOutputContext(FileOutputContext fileContext) {
        funFileContext = fileContext;

        return this;
    }

    public SolutionListOutput setObjectiveMinimizingObjectiveList(List<Boolean> isObjectiveToBeMinimized) {
        this.isObjectiveToBeMinimized = isObjectiveToBeMinimized;

        return this;
    }

    public SolutionListOutput setSeparator(String separator) {
        this.separator = separator;
        varFileContext.setSeparator(this.separator);
        funFileContext.setSeparator(this.separator);

        return this;
    }

    public void print() {
        if (isObjectiveToBeMinimized == null) {
            printObjectivesToFile(funFileContext, solutionList);
        } else {
            printObjectivesToFile(funFileContext, solutionList, isObjectiveToBeMinimized);
        }
        printVariablesToFile(varFileContext, solutionList);
    }

    public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (solutionList.size() > 0) {
                int numberOfVariables = solutionList.get(0).getNumberOfVariables();
                for (int i = 0; i < solutionList.size(); i++) {
                    for (int j = 0; j < numberOfVariables; j++) {
                        if (j < numberOfVariables - 1) {
                            bufferedWriter
                                    .write(solutionList.get(i).getVariableValueString(j) + context.getSeparator());
                        } else {
                            bufferedWriter.write(solutionList.get(i).getVariableValueString(j) + "");
                        }
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error writing data ", e);
        }

    }

    public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (solutionList.size() > 0) {
                int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
                for (int i = 0; i < solutionList.size(); i++) {
                    for (int j = 0; j < numberOfObjectives; j++) {
                        if (j < numberOfObjectives - 1) {
                            bufferedWriter.write(solutionList.get(i).getObjective(j) + context.getSeparator());
                        } else {
                            bufferedWriter.write(solutionList.get(i).getObjective(j) + "");
                        }
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing objecives to file: ", e);
        }
    }

    public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList,
                                      List<Boolean> minimizeObjective) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (solutionList.size() > 0) {
                int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
                if (numberOfObjectives != minimizeObjective.size()) {
                    throw new JMetalException(
                            "The size of list minimizeObjective is not correct: " + minimizeObjective.size());
                }
                for (int i = 0; i < solutionList.size(); i++) {
                    for (int j = 0; j < numberOfObjectives; j++) {
                        if (j < numberOfObjectives - 1) {
                            if (minimizeObjective.get(j)) {
                                bufferedWriter.write(solutionList.get(i).getObjective(j) + context.getSeparator());
                            } else {
                                bufferedWriter
                                        .write(-1.0 * solutionList.get(i).getObjective(j) + context.getSeparator());
                            }
                        } else {
                            if (minimizeObjective.get(j)) {
                                bufferedWriter.write(solutionList.get(i).getObjective(j) + "");
                            } else {
                                bufferedWriter.write(-1.0 * solutionList.get(i).getObjective(j) + "");
                            }
                        }
                    }
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing objecives to file: ", e);
        }
    }

    /*
     * Wrappers for printing with default configuration
     */
    public void printObjectivesToFile(String fileName) {
        printObjectivesToFile(new DefaultFileOutputContext(fileName), solutionList);
    }

    public void printObjectivesToFile(String fileName, List<Boolean> minimizeObjective) {
        printObjectivesToFile(new DefaultFileOutputContext(fileName), solutionList, minimizeObjective);
    }

    public void printVariablesToFile(String fileName) {
        printVariablesToFile(new DefaultFileOutputContext(fileName), solutionList);
    }

}
