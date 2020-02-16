package opt.easyjmetal.algorithm;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.algorithm.cmoeas.util.statistics.*;
import opt.easyjmetal.util.JMException;

public class CMOEAs_analysis {
    public static void main(String[] args) {
        try {
            String[] algorithmNames = {"NSGAII_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "MOEAD_CDP", "C_MOEAD", "PPS_MOEAD", "NSGAIII_CDP", "SPEA2_CDP"};
            String[] problemNames = {"EDF_PS", "EDF_TSS"};
            String[] indicatorNames = {"HV", "IGD"};
            int runtimes = 10;

            // 生成pareto前沿面
            Utils.generateParetoFront(algorithmNames, problemNames, runtimes);
            // 计算性能指标
            Utils.generateQualityIndicators(algorithmNames, problemNames, indicatorNames, runtimes);

            // Friedman测试
            Friedman.executeTest("HV", algorithmNames, problemNames);
            Friedman.executeTest("IGD", algorithmNames, problemNames);

            // 生成均值和方差
            MeanStandardDeviation generateLatexTables = new MeanStandardDeviation(algorithmNames, problemNames, indicatorNames);
            generateLatexTables.run();

            // 进行TTest
            TTest tTest = new TTest(algorithmNames, problemNames, indicatorNames);
            tTest.run();

            // 进行TTest
            WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest(algorithmNames, problemNames, indicatorNames);
            wilcoxonSignedRankTest.run();

            // 计算不同策略C指标
            CMetrics cMetrics = new CMetrics(problemNames);
            cMetrics.run();
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

}
