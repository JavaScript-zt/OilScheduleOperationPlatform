package opt.easyjmetal.statistics;

import opt.easyjmetal.util.JMetalLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WilcoxonSignedRankTest {
    private static final String DEFAULT_LATEX_DIRECTORY = "wilcoxonranktest";
    private String resultBaseDirectory_;
    private List<String> indicList_;
    private List<String> algorithmNameList_;
    private List<String> problemList_;
    // ���Ŷ�0.95
    private double alpha_ = 0.05;
    private int[][][] better;

    public WilcoxonSignedRankTest(String[] algorithmNameList_,
                                  String[] problemList_,
                                  String[] indicList_,
                                  String basePath) {
        if (algorithmNameList_.length < 2) {
            JMetalLogger.logger.info("���������������㷨");
        }
        this.indicList_ = Arrays.asList(indicList_);
        this.algorithmNameList_ = Arrays.asList(algorithmNameList_);
        this.problemList_ = Arrays.asList(problemList_);
        this.resultBaseDirectory_ = basePath;
    }

    public void run() {
        try {
            List<List<List<List<Double>>>> data = readDataFromFiles();
            computeDataStatistics(data);
            generateLatexScript(data);
        } catch (IOException ex) {
            JMetalLogger.logger.info("������ָ��ֵ����ִ�����ɱ�����: " + ex.getMessage());
        }
    }

    /**
     * ��ȡָ��ֵ
     *
     * @return
     * @throws IOException
     */
    private List<List<List<List<Double>>>> readDataFromFiles() throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<List<List<List<Double>>>>(indicList_.size());

        for (int indicator = 0; indicator < indicList_.size(); indicator++) {
            // A data vector per problem
            data.add(indicator, new ArrayList<>());
            for (int problem = 0; problem < problemList_.size(); problem++) {
                data.get(indicator).add(problem, new ArrayList<List<Double>>());

                for (int algorithm = 0; algorithm < algorithmNameList_.size(); algorithm++) {
                    data.get(indicator).get(problem).add(algorithm, new ArrayList<Double>());


                    // Ŀ¼�ṹ��basePath/indicator/problem/algorithm.indicator
                    String directory = resultBaseDirectory_ + "/indicator/" + problemList_.get(problem) + "/" + algorithmNameList_.get(algorithm) + "." + indicList_.get(indicator);
                    // Read values from data files
                    FileInputStream fis = new FileInputStream(directory);
                    InputStreamReader isr = new InputStreamReader(fis);
                    try (BufferedReader br = new BufferedReader(isr)) {
                        String aux = br.readLine();
                        while (aux != null) {
                            data.get(indicator).get(problem).get(algorithm).add(Double.parseDouble(aux));
                            aux = br.readLine();
                        }
                    }
                }
            }
        }

        return data;
    }

    private void computeDataStatistics(List<List<List<List<Double>>>> data) {
        int indicatorListSize = indicList_.size();
        better = new int[indicatorListSize][][];

        int problemListSize = problemList_.size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            better[indicator] = new int[problemListSize][];

            int algorithmListSize = algorithmNameList_.size();
            for (int problem = 0; problem < problemListSize; problem++) {
                better[indicator][problem] = new int[algorithmListSize];

                for (int algorithm = 1; algorithm < algorithmListSize; algorithm++) {
                    // ��algorithm[0]��algorithm[1],algorithm[2]...�Ա�
                    boolean xz = computeStatistics(data.get(indicator).get(problem).get(0),
                            data.get(indicator).get(problem).get(algorithm));

                    double mean_ours = Statistics.MeanValue(data.get(indicator).get(problem).get(0));
                    double mean_reference = Statistics.MeanValue(data.get(indicator).get(problem).get(algorithm));

                    // HVԽ��Խ�ã�����ָ��ԽСԽ��
                    if (!indicList_.get(indicator).equals("HV")) {
                        // �жϲ������Ƿ�����
                        if (xz) {
                            if (mean_ours < mean_reference) {
                                better[indicator][problem][algorithm] = 1;
                            } else if (mean_ours > mean_reference) {
                                better[indicator][problem][algorithm] = -1;
                            }
                        } else {
                            better[indicator][problem][algorithm] = 0;
                        }
                    } else {
                        // �жϲ������Ƿ�����
                        if (xz) {
                            if (mean_ours > mean_reference) {
                                better[indicator][problem][algorithm] = 1;
                            } else if (mean_ours < mean_reference) {
                                better[indicator][problem][algorithm] = -1;
                            }
                        } else {
                            better[indicator][problem][algorithm] = 0;
                        }
                    }
                }
            }
        }
    }

    private void generateLatexScript(List<List<List<List<Double>>>> data) throws IOException {
        String latexDirectoryName = resultBaseDirectory_ + "/" + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        for (int i = 0; i < indicList_.size(); i++) {
            String latexFile = latexDirectoryName + "/" + "WilcoxonSignedRankTest" + indicList_.get(i) + ".tex";
            printHeaderLatexCommands(latexFile);
            printData(latexFile, i, "WilcoxonSignedRankTest");
            printEndLatexCommands(latexFile);
        }
    }

    /**
     * �жϲ������Ƿ�����
     *
     * @param ours
     * @param reference
     * @return
     */
    private boolean computeStatistics(List<Double> ours, List<Double> reference) {
        Double[] ours_array = new Double[ours.size()];
        Double[] reference_array = new Double[reference.size()];
        ours.toArray(ours_array);
        reference.toArray(reference_array);

        boolean pValue = Statistics.WilcoxonSignedRankTest(ArrayUtils.toPrimitive(ours_array), ArrayUtils.toPrimitive(reference_array), alpha_);
        return pValue;
    }

    void printHeaderLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, false)) {
            os.write("\\documentclass{article}" + "\n");
            os.write("\\usepackage{colortbl}" + "\n");
            os.write("\\usepackage{geometry}" + "\n");
            os.write("\\usepackage{pdflscape}" + "\n");
            os.write("\\geometry{a4paper,left=2cm,right=2cm,top=1cm,bottom=1cm}" + "\n");
            os.write("\\begin{document}" + "\n");
            os.write("\\thispagestyle{empty}" + "\n");// ��ǰҳ����ʾҳ��
            os.write("\\begin{landscape}" + "\n");
        }
    }

    void printEndLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, true)) {
            os.write("\\end{landscape}" + "\n");
            os.write("\\end{document}" + "\n");
        }
    }

    private void printData(String latexFile, int indicatorIndex,
                           String caption) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(latexFile, true)) {
            os.write("\n");
            os.write("\\begin{table}" + "\n");
            os.write("\\caption{" + indicList_.get(indicatorIndex) + ". " + caption + "}"
                    + "\n");
            os.write("\\label{table: " + indicList_.get(indicatorIndex) + "}" + "\n");
            os.write("\\centering" + "\n");
            os.write("\\begin{scriptsize}" + "\n");
            os.write("\\begin{tabular}{l");

            // calculate the number of columns
            os.write(StringUtils.repeat("l", algorithmNameList_.size()));
            os.write("}\n");
            os.write("\\hline\n");

            // write table head
            for (int i = -1; i < algorithmNameList_.size(); i++) {
                if (i == -1) {
                    os.write(" & ");
                } else if (i == (algorithmNameList_.size() - 1)) {
                    os.write(" " + algorithmNameList_.get(i).replace("_", "\\_") + "\\\\" + "\n");
                } else {
                    os.write("" + algorithmNameList_.get(i).replace("_", "\\_") + " & ");
                }
            }
            os.write("\\hline \n");

            // write lines
            for (int i = 0; i < problemList_.size(); i++) {
                os.write(problemList_.get(i).replace("_", "\\_") + " & & ");
                // �Ƚϵ�һ���㷨�������㷨������
                for (int j = 1; j < algorithmNameList_.size(); j++) {
                    String m;
                    if (better[indicatorIndex][i][j] == 0) {
                        m = "=";
                    } else if (better[indicatorIndex][i][j] > 0) {
                        m = "+";
                    } else {
                        m = "-";
                    }

                    os.write("$" + m + "$");
                    if (j < algorithmNameList_.size() - 1) {
                        os.write(" & ");
                    } else {
                        os.write(" \\\\\n");
                    }
                }
            }

            // close table
            os.write("\\hline" + "\n");
            os.write("\\end{tabular}" + "\n");
            os.write("\\end{scriptsize}" + "\n");
            os.write("\\end{table}" + "\n");
        }
    }
}
