package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.problem.onlinemix.Oilschdule;
import opt.easyjmetal.problem.onlinemix.OnlineMixOIL;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.List;

/**
 * ����
 */
public class OnlineMix_MOEAs_decode {

    public static void main(String[] args) throws Exception {
        OnlineMixOIL problem = new OnlineMixOIL("Real");
        String dbName = problem.getName();
        String tableName = "NSGAII_1";

        SolutionSet solutionSet = SqlUtils.SelectData(dbName, tableName);
        int count = 0;
        for (int i = 0; i < solutionSet.size(); i++) {
            boolean flag = evaluate(solutionSet.get(i));
            if (flag) {
                count++;
            }
        }

        // ������һ�±���
        System.out.println("����" + solutionSet.size() + "����֧��⣬����" + count + "�����Ԥ�ڽ��һ��");
    }

    /**
     * ���۽����Ӧ�ȣ������п��ӻ�
     *
     * @param solution
     * @throws JMException
     */
    public static boolean evaluate(Solution solution) throws JMException {

        Variable[] decisionVariables = solution.getDecisionVariables();
        double[][] x = new double[1][decisionVariables.length];
        for (int i = 0; i < x.length; i++) {
            x[0][i] = decisionVariables[i].getValue();
        }

        List<List<Double>> eff = Oilschdule.fat(x, false);

        // �Ա�Ŀ��ֵ�Ƿ�ǰ��һ��
        if (solution.getObjective(0) == eff.get(0).get(x.length + 0)
                && solution.getObjective(1) == eff.get(0).get(x.length + 1)
                && solution.getObjective(2) == eff.get(0).get(x.length + 2)
                && solution.getObjective(3) == eff.get(0).get(x.length + 3)) {
            return true;
        }
        return false;
    }
}
