package opt.easyjmetal.algorithm.moeas.entrence;

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

import java.util.HashMap;

/**
 *
 */
public class SJ_MOEAs_main {

    public static void main(String[] args) throws Exception {
        // 0 represents for DE, 1 represents for SBX
        // int crossoverMethod = 1;
        //batchRun(new String[]{"NSGAII_CDP"}, crossoverMethod);
        singleRun("NSGAII_CDP", 1);
    }

//    private static void batchRun(String[] methods, int crossMethod) throws Exception {
//        String[] algorithmSet = methods;
//        int algorithmNo = algorithmSet.length;
//
//        // �������ʱ��
//        String basePath = "result/easyjmetal/";
//        File dir = new File(basePath);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        //true = append file
//        FileWriter fileWritter = new FileWriter(basePath + "runtimes.txt", false);
//        StringBuilder stringBuilder = new StringBuilder();
//
//        for (int i = 0; i < algorithmNo; i++) {
//            System.out.println("The tested algorithm: " + algorithmSet[i]);
//            System.out.println("The process: " + String.format("%.2f", (100.0 * i / algorithmNo)) + "%");
//            stringBuilder.append(singleRun(algorithmSet[i], crossMethod)); // 0 represents for DE, 1 represents for SBX
//        }
//
//        fileWritter.write(stringBuilder.toString());
//        fileWritter.flush();
//        fileWritter.close();
//    }

    /**
     * ���
     * @param algorithmName
     * @param crossMethod
     * @return
     * @throws Exception
     */
    private static String singleRun(String algorithmName, int crossMethod) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        Problem problem;                // The problem to solve
        Algorithm algorithm;            // The algorithm to use
        Operator crossover;            // Crossover operator
        Operator mutation;             // Mutation operator
        Operator selection;            // Selection operator
        HashMap parameters;           // Operator parameters

/////////////////////////////////////////// parameter setting //////////////////////////////////

        int popSize = 50; // ��Ⱥ��С
        int maxFES = 500; // ���۴��� = ��Ⱥ��С * ��������
        int runtime = 10;// �������д���
        double DeCrossRate = 1.0;
        double DeFactor = 0.5;

        String AlgorithmName = algorithmName;

        String mainPath = System.getProperty("user.dir");
        String weightPath = "resources/MOEAD_Weights";// Ȩ���ļ�·��
        Boolean isDisplay = false;
        int plotFlag = 0; // 0 for the working population; 1 for the external archive

        String resultFile = mainPath + "/" + AlgorithmName + ".db";
        FileUtils.deleteFile(resultFile);

        Object[] params = {"Real"};
        String[] problemStrings = {"SJOIL"};

//////////////////////////////////////// End parameter setting //////////////////////////////////


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
            algorithm.setInputParameter("isDisplay", isDisplay);
            algorithm.setInputParameter("plotFlag", plotFlag);
            algorithm.setInputParameter("paretoPath", paretoPath);
            algorithm.setInputParameter("DBName", "sjoil");

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
                System.out.println("Problem:  " + problemStrings[i] + "  running time:  " + j);
                System.out.println("==================================================================");
                stringBuilder.append(algorithmName + "," + problemStrings[i] + "," + estimatedTime + "\n");
            }
        }
        return stringBuilder.toString();
    }
}
