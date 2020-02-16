package opt.easyjmetal.algorithm.cmoeas.util.statistics;

import opt.easyjmetal.util.JMetalLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 计算C指标，分析不同算法或者不同策略下所得到结果的相对优劣
 */
public class CMetrics {

    private static final String DEFAULT_LATEX_DIRECTORY = "latex";
    private static final String resultBaseDirectory_ = "result/easyjmetal";

    private List<String> problemList_;

    private double[][] cvalues;

    public CMetrics(String[] problemNames) {
        this.problemList_ = Arrays.asList(problemNames);
    }

    public void run() {
        try {
            List<List<List<Double>>> data = readDataFromFiles();
            computeDataStatistics(data);
            generateLatexScript(data);
        } catch (IOException ex) {
            JMetalLogger.logger.info("请生成指标值后再执行生成表格操作: " + ex.getMessage());
        }
    }

    /**
     * 读取各个问题的pf
     *
     * @return
     * @throws IOException
     */
    private List<List<List<Double>>> readDataFromFiles() throws IOException {
        List<List<List<Double>>> data = new ArrayList<>();

        for (int problem = 0; problem < problemList_.size(); problem++) {
            data.add(readVectors(resultBaseDirectory_ + "/" + problemList_.get(problem) + ".pf"));
        }

        return data;
    }

    /**
     * @param filePath the file need to read
     * @return referenceVectors. referenceVectors[i][j] means the i-th vector's j-th
     * value
     */
    public static List<List<Double>> readVectors(String filePath) {
        List<List<Double>> referenceVectors = new ArrayList<>();
        String path = filePath;

        List<String> vectorStrList = null;
        try {
            vectorStrList = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < vectorStrList.size(); i++) {
            String vectorStr = vectorStrList.get(i);
            String[] objectArray = vectorStr.split("[\\s,]+");// 匹配空格或者逗号
            List<Double> line = new ArrayList<>();
            for (int j = 0; j < objectArray.length; j++) {
                line.add(Double.parseDouble(objectArray[j]));
            }
            referenceVectors.add(line);
        }

        return referenceVectors;
    }


    private void computeDataStatistics(List<List<List<Double>>> data) {

        int problemListSize = problemList_.size();
        cvalues = new double[problemListSize - 1][2];
        for (int problem = 1; problem < problemListSize; problem++) {
            // problem[0]problem[1],problem[2]...对比
            cvalues[problem - 1][0] = computeStatistics(data.get(0), data.get(problem));
            cvalues[problem - 1][1] = computeStatistics(data.get(problem), data.get(0));
        }
    }

    private void generateLatexScript(List<List<List<Double>>> data) throws IOException {
        String latexDirectoryName = resultBaseDirectory_ + "/" + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }

        String latexFile = latexDirectoryName + "/" + "CMetrics.tex";
        printHeaderLatexCommands(latexFile);
        printData(latexFile, "TTest");
        printEndLatexCommands(latexFile);
    }

    /**
     * Computes cValue
     *
     * @param ours
     * @param reference
     * @return
     */
    private double computeStatistics(List<List<Double>> ours, List<List<Double>> reference) {
        return C(ours, reference);
    }

    void printHeaderLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, false)) {
            os.write("\\documentclass{article}" + "\n");
            os.write("\\usepackage{colortbl}" + "\n");
            os.write("\\usepackage{geometry}" + "\n");
            os.write("\\usepackage{pdflscape}" + "\n");
            os.write("\\geometry{a4paper,left=2cm,right=2cm,top=1cm,bottom=1cm}" + "\n");
            os.write("\\begin{document}" + "\n");
            os.write("\\thispagestyle{empty}" + "\n");// 当前页不显示页码
            os.write("\\begin{landscape}" + "\n");
        }
    }

    void printEndLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, true)) {
            os.write("\\end{landscape}" + "\n");
            os.write("\\end{document}" + "\n");
        }
    }

    private void printData(String latexFile, String caption) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(latexFile, true)) {
            os.write("\n");
            os.write("\\begin{table}" + "\n");
            os.write("\\caption{" + "C" + ". " + caption + "}"
                    + "\n");
            os.write("\\label{table: " + "C" + "}" + "\n");
            os.write("\\centering" + "\n");
            os.write("\\begin{scriptsize}" + "\n");
            os.write("\\begin{tabular}{l");

            // calculate the number of columns
            os.write(StringUtils.repeat("l", 2));
            os.write("}\n");
            os.write("\\hline\n");

            // write table head
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    os.write(" C(" + problemList_.get(0).replace("_", "\\_") + ","
                            + problemList_.get(i).replace("_", "\\_") + ")" + " & ");
                } else {
                    os.write("C(" + problemList_.get(i).replace("_", "\\_") + "," +
                            problemList_.get(0).replace("_", "\\_") + ")" + "\\\\" + "\n");
                }
            }
            os.write("\\hline \n");

            // write lines
            for (int i = 0; i < problemList_.size() - 1; i++) {
                // 比较第一个算法与其他算法的性能
                for (int j = 0; j < 2; j++) {
                    os.write("$" + cvalues[i][j] + "$");
                    if (j == 0) {
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

    /**
     * 非支配比较：判断输入的解是否支配参考解
     *
     * @param x
     * @param y
     * @return
     */
    public boolean dominanceComparison(List<Double> x, List<Double> y) {

        int count1 = 0, count2 = 0;
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i).doubleValue() < y.get(i).doubleValue()) {
                count1++;
            } else if (x.get(i).doubleValue() == y.get(i).doubleValue()) {
                count2++;
            } else {
                return false;
            }
        }

        if (count1 + count2 == y.size() && count1 > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 非支配比较：判断集合中是否存在一个解支配当前解
     *
     * @param individual
     * @param solutionSet
     * @return
     */
    public boolean existDominanceSolution(List<Double> individual, List<List<Double>> solutionSet) {

        boolean flag = false;
        for (int i = 0; i < solutionSet.size(); i++) {
            // 若集合中任何一个解支配当前解，则返回真
            if (dominanceComparison(solutionSet.get(i), individual)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    /**
     * 非支配比较：判断集合之间的支配计数值
     *
     * @param A
     * @param B
     * @return
     */
    public int dominanceCount(List<List<Double>> A, List<List<Double>> B) {

        int count = 0;
        for (int i = 0; i < B.size(); i++) {
            if (existDominanceSolution(B.get(i), A)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 非支配比较：判断集合之间的支配计数值
     *
     * @param A
     * @param B
     * @return
     */
    public double C(List<List<Double>> A, List<List<Double>> B) {
        return 1.0 * dominanceCount(A, B) / B.size();
    }
}
