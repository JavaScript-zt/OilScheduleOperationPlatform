/**
 * Created by liwenji  Email: wenji_li@126.com
 */
package opt.easyjmetal.algorithm;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Operator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.problem.ProblemFactory;
import opt.easyjmetal.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CMOEAs_main {

    public static void main(String[] args) throws Exception {
        // 0 represents for DE, 1 represents for SBX
        int crossoverMethod = 1;
        // "NSGAII_CDP",
        // "ISDEPLUS_CDP",
        // "NSGAIII_CDP",
        // "MOEAD_CDP",
        // "MOEAD_IEpsilon",
        // "MOEAD_Epsilon",
        // "MOEAD_SR",
        // "C_MOEAD",
        // "PPS_MOEAD"
        batchRun(new String[]{"NSGAII_CDP",
                "ISDEPLUS_CDP",
                "NSGAIII_CDP",
                "MOEAD_CDP",
                "MOEAD_IEpsilon",
                "MOEAD_Epsilon",
                "MOEAD_SR",
                "C_MOEAD",
                "PPS_MOEAD"}, crossoverMethod);
    }

    private static void batchRun(String[] methods, int crossMethod) throws Exception {
        String[] algorithmSet = methods;
        int algorithmNo = algorithmSet.length;
        for (int i = 0; i < algorithmNo; i++) {
            System.out.println("The tested algorithm: " + algorithmSet[i]);
            System.out.println("The process: " + String.format("%.2f", (100.0 * i / algorithmNo)) + "%");
            singleRun(algorithmSet[i], crossMethod); // 0 represents for DE, 1 represents for SBX
        }
    }

    private static void singleRun(String algorithmName, int crossMethod) throws Exception {
        Problem problem;                // The problem to solve
        Algorithm algorithm;            // The algorithm to use
        Operator crossover;            // Crossover operator
        Operator mutation;             // Mutation operator
        Operator selection;            // Selection operator
        HashMap parameters;           // Operator parameters

/////////////////////////////////////////// parameter setting //////////////////////////////////

        int popSize = 100;
        int neighborSize = (int) (0.1 * popSize);
        int maxFES = 50000;
        int updateNumber = 2;
        double deDelta = 0.9;
        double DeCrossRate = 1.0;
        double DeFactor = 0.5;

        double tao = 0.1;
        double alpha = 0.9;
        double threshold = 1e-3;

        // IDEA parameter
        float infeasibleRatio = 0.1f;

        String AlgorithmName = algorithmName;

        String mainPath = System.getProperty("user.dir");
        String weightPath = "resources/MOEAD_Weights";// Ȩ���ļ�·��
        int runtime = 10;// �������д���
        Boolean isDisplay = false;
        int plotFlag = 0; // 0 for the working population; 1 for the external archive

        // MOEAD_SR parameters
        double srFactor = 0.05;

        String resultFile = mainPath + "/" + AlgorithmName + ".db";
        FileUtils.deleteFile(resultFile);

        Object[] params = {"Real"};
        String[] problemStrings = {"EDFPS", "EDFTSS"};

//////////////////////////////////////// End parameter setting //////////////////////////////////

        List<List<Long>> runtimes = new ArrayList<>();

        for (int i = 0; i < problemStrings.length; i++) {
            problem = (new ProblemFactory()).getProblem(problemStrings[i], params);
            //define algorithm
            Object[] algorithmParams = {problem};
            algorithm = (new Utils()).getAlgorithm(AlgorithmName, algorithmParams);

            //define pareto file path
            String paretoPath = mainPath + "/pf_data/" + problemStrings[i] + ".pf";
            // Algorithm parameters
            algorithm.setInputParameter("AlgorithmName", AlgorithmName);
            algorithm.setInputParameter("populationSize", popSize);
            algorithm.setInputParameter("maxEvaluations", maxFES);
            algorithm.setInputParameter("dataDirectory", weightPath);
            algorithm.setInputParameter("T", neighborSize);
            algorithm.setInputParameter("delta", deDelta);
            algorithm.setInputParameter("nr", updateNumber);
            algorithm.setInputParameter("isDisplay", isDisplay);
            algorithm.setInputParameter("plotFlag", plotFlag);
            algorithm.setInputParameter("paretoPath", paretoPath);
            algorithm.setInputParameter("srFactor", srFactor);
            algorithm.setInputParameter("tao", tao);
            algorithm.setInputParameter("alpha", alpha);
            algorithm.setInputParameter("threshold_change", threshold);
            algorithm.setInputParameter("infeasibleRatio", infeasibleRatio);

            // Crossover operator
            if (crossMethod == 0) {                      // DE operator
                parameters = new HashMap();
                parameters.put("CR", DeCrossRate);
                parameters.put("F", DeFactor);
                crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover", parameters);
                algorithm.addOperator("crossover", crossover);
            } else if (crossMethod == 1) {                // SBX operator
                parameters = new HashMap();
                parameters.put("probability", 1.0);
                parameters.put("distributionIndex", 20.0);
                crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
                algorithm.addOperator("crossover", crossover);
            }

            // Mutation operator
            parameters = new HashMap();
            parameters.put("probability", 1.0 / problem.getNumberOfVariables());// �������
            parameters.put("distributionIndex", 20.0);
            mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);
            algorithm.addOperator("mutation", mutation);

            // Selection Operator
            parameters = null;
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);// ѡ������
            algorithm.addOperator("selection", selection);
            List<Long> runtimeList = new ArrayList<>();
            // each problem runs runtime times
            for (int j = 0; j < runtime; j++) {
                System.out.println("==================================================================");
                algorithm.setInputParameter("runningTime", j);
                // Execute the Algorithm
                System.out.println("The " + j + " run of " + algorithmName);
                long initTime = System.currentTimeMillis();
                SolutionSet pop = algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;
                // Result messages
                System.out.println("Total execution time: " + estimatedTime + "ms");
                runtimeList.add(estimatedTime);
                System.out.println("Problem:  " + problemStrings[i] + "  running time:  " + j);
                System.out.println("==================================================================");
            }
            runtimes.add(runtimeList);
        }
        // ��������ʱ��
        String basePath = "result/easyjmetal/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //true = append file
        FileWriter fileWritter = new FileWriter(basePath + "runtimes.txt", false);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

        for (int i = 0; i < runtimes.size(); i++) {
            List<Long> algorithmRuntimes = runtimes.get(i);
            for (int j = 0; j < algorithmRuntimes.size(); j++) {
                bufferWritter.write(algorithmRuntimes.get(j).toString());
                if (j < algorithmRuntimes.size() - 1) {
                    bufferWritter.write(" ");
                }
            }
            bufferWritter.write("\n");
        }
        bufferWritter.flush();
        bufferWritter.close();
    }
}
