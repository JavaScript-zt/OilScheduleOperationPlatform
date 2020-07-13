package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.problem.schedule.models.DSObject;
import opt.easyjmetal.problem.schedule.models.FactObject;
import opt.easyjmetal.problem.schedule.models.Fragment;
import opt.easyjmetal.problem.schedule.models.TankObject;
import opt.easyjmetal.problem.schedule.operation.Operation;
import opt.easyjmetal.problem.schedule.operation.OperationType;
import opt.easyjmetal.problem.schedule.rules.AbstractRule;
import opt.easyjmetal.problem.schedule.rules.RuleFactory;
import opt.easyjmetal.problem.schedule.util.ArrayHelper;
import opt.easyjmetal.problem.schedule.util.CloneUtils;
import opt.easyjmetal.problem.schedule.rules.ISimulationScheduler;
import opt.easyjmetal.problem.schedule.util.MathUtil;
import opt.easyjmetal.util.JMException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.*;

/**
 * �ؼ����ڳ�ͻ���
 *
 * @author Administrator
 */
public class COPScheduler implements ISimulationScheduler {

    private Logger logger = LogManager.getLogger(COPScheduler.class.getName());

    private List<Operation> operations = new LinkedList<>();// ���յľ�������
    private Solution solution;// ����ָ������
    private boolean plotEachStep;// �Ƿ����ÿһ���ĵ���
    private String ruleName = "";// ��������

    // ��ǰ�������
    private int loc = 0;
    private Config config;

    // ϵͳ״̬ջ
    private Stack<Fragment> fragmentStack = new Stack<>();// ����ջ
    public Stack<Integer[][]> policyStack = new Stack<>();// ����ջ
    private Stack<Operation> operationStack = new Stack<>();// ����ջ
    private Stack<Config> configStack = new Stack<>();// ����ջ

    public Config getConfig() {
        return config;
    }

    /**
     * �½�һ���͵�ǰ������ͬ������
     *
     * @return
     */
    private Config newConfig() {
        return CloneUtils.clone(config);
    }

    /**
     * �Ƴ�ջ�������ã�������config��ָ��
     */
    private void removeConfig() {
        configStack.pop();
        config = configStack.peek();
    }

    /**
     * ��ջ�в����µ����ã�������config��ָ��
     *
     * @param newConfig
     */
    private void addConfig(Config newConfig) {
        configStack.push(newConfig);
        config = configStack.peek();
    }

    /**
     * ��ȡ���յľ�������
     *
     * @return
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * ��������
     *
     * @param config   ���ȵ�������Ϣ
     * @param ruleName ��������
     */
    public COPScheduler(Config config, String ruleName) {
        this.config = config;
        this.plotEachStep = false;
        this.ruleName = ruleName;
    }

    /**
     * ��������
     *
     * @param config       ���ȵ�������Ϣ
     * @param showEachStep �Ƿ���ʾÿһ���Ľ��
     * @param ruleName     ��������
     */
    public COPScheduler(Config config, boolean showEachStep, String ruleName) {
        this.config = config;
        this.plotEachStep = showEachStep;
        this.ruleName = ruleName;
    }

    /**
     * ��ʼ
     */
    public void start(Solution solution) {
        // ��վ��߶���
        getOperations().clear();
        fragmentStack.clear();
        operationStack.clear();
        configStack.clear();
        policyStack.clear();
        loc = 0;

        initSimulation(solution);
        process();
    }

    /**
     * ��ȡ���������������ͳ���ʱ�䡾��ֵ�����Ѿ�����
     *
     * @return
     */
    public double[] getFeedingLastTime() {
        double[] deadlines = getFeedingEndTime();
        double tmp = Double.MAX_VALUE;
        for (int i = 0; i < deadlines.length; i++) {
            int ds = i + 1;
            int pipe = getCurrentPipe(ds);
            double currentTime = getCurrentTime(pipe);
            if (deadlines[i] - currentTime < tmp) {
                deadlines[i] = deadlines[i] - currentTime;
            }
        }
        return deadlines;
    }

    /**
     * ��ȡ����Ҫת��ԭ�͵�������
     *
     * @return
     */
    public int getMostEmergencyDS() {
        double[] oilEndTime = getFeedingEndTime();
        List<Double> oilEndTimeList = new ArrayList<>();
        for (int i = 0; i < oilEndTime.length; i++) {
            oilEndTimeList.add(oilEndTime[i]);
        }
        DoubleSummaryStatistics stat = oilEndTimeList.stream().mapToDouble(x -> x).summaryStatistics();
        double minTime = stat.getMin();
        int ds = oilEndTimeList.indexOf(minTime) + 1;
        return ds;
    }

    /**
     * ��ȡ�������������������ʱ�ο���
     *
     * @return
     */
    public int getMostNeedOilDS() {
        double[] oilEndTime = getFeedingEndTime();
        int ds = -1;
        double tmp = Double.MAX_VALUE;
        for (int i = 0; i < oilEndTime.length; i++) {
            int pipe = getCurrentPipe(i);
            double currentTime = getCurrentTime(pipe);
            if (oilEndTime[i] - currentTime < tmp) {
                tmp = oilEndTime[i] - currentTime;
                ds = i + 1;
            }
        }
        if (tmp <= 24) {
            return ds;
        } else {
            return -1;
        }
    }

    /**
     * �������ȫת�����������޵�ռ�ó�ͻ
     *
     * @param tank
     * @param ds
     * @param chargingSpeed
     * @return
     */
    public double getMaxSafeVolume(int tank, int ds, double chargingSpeed) {
        double vol = 0;

        // 1.��ǰʱ��T
        int pipe = getCurrentPipe(ds);
        double currentTime = getCurrentTime(pipe);

        // 2.���ͽ���ʱ��T1
        double[] feedEndTimes = getFeedingEndTime();
        double feedEndTime = feedEndTimes[ds - 1];

        // 3���͹޿�ʼ���õ���ʱ��T2
        Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
        double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
        if (usingTimes.containsKey(tank)) {
            vol = MathUtil.round(feedingSpeed * (usingTimes.get(tank) - feedEndTime), config.Precision);
        } else {
            vol = Double.MAX_VALUE;
        }

        return vol;
    }

    /**
     * ���㲻������������ȫת�����������޵�ռ�ó�ͻ
     *
     * @param tank
     * @param ds
     * @param chargingSpeed
     * @return
     */
    public double getMaxSafeVolumeUnnormal(int tank, int ds, double chargingSpeed) {
        double vol = 0;

        // 1.��ǰʱ��T
        int pipe = getCurrentPipe(ds);
        double currentTime = getCurrentTime(pipe);

        // 2.���͹޿�ʼ���õ���ʱ��T2
        Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
        double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();

        if (usingTimes.containsKey(tank)) {
            vol = MathUtil.round(
                    MathUtil.divide(feedingSpeed * chargingSpeed * (usingTimes.get(tank) - currentTime - config.RT),
                            feedingSpeed + chargingSpeed),
                    config.Precision);
        } else {
            vol = Double.MAX_VALUE;
        }

        return vol;
    }

    /**
     * �ھ������������������������ʱ���ͬʱ�����ʵ�ڸϲ��ϣ���ֱ����
     *
     * @param ds
     * @param chargingSpeed
     * @return
     */
    public double getRTVolume(int ds, double chargingSpeed) {
        double deadlineTime = getFeedingEndTime()[ds - 1];// ����ʱ��
        int pipe = getCurrentPipe(ds);
        double currentTime = getCurrentTime(pipe);
        double volume = MathUtil.round(chargingSpeed * (deadlineTime - currentTime - config.RT), config.Precision);
        return volume;
    }

    /**
     * ȷ������ת�����
     *
     * @param fp_vol
     * @param safe_vol
     * @param capacity
     * @return
     */
    public double getVolume(double fp_vol, double safe_vol, double capacity) {
        // ����Ҫ���ǵ�Լ�������������ϰ����Ż�������������ޡ�
        double limit = MathUtil.round(Math.min(fp_vol, Math.min(capacity, safe_vol)), config.Precision);// ������������
        return limit;
    }

    /**
     * ȷ������ת�����
     *
     * @param fp_vol
     * @param rt_vol
     * @param safe_vol
     * @param capacity
     * @return
     */
    public double getVolume(double fp_vol, double rt_vol, double safe_vol, double capacity) {
        // ����Ҫ���ǵ�Լ�������������ϰ����Ż�������������ޡ�
        double limit = MathUtil.round(Math.min(safe_vol, Math.min(fp_vol, Math.min(capacity, rt_vol))),
                config.Precision);// ������������
        return limit;
    }

    /**
     * ���˵��ϲ�Ĳ���
     *
     * @param vol
     * @param fp_vol
     * @return
     */
    public boolean filterCondition(double vol, double fp_vol) {
        // ����ʱ��Ĭ��ת�˼�¼���������С��һ����С�������ļ�ָ����
        if (vol < config.VolMin && fp_vol != vol) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ��ȡδ������ͼƻ���������
     *
     * @return
     */
    public List<Integer> getDSSet() {
        List<Integer> dss = new ArrayList<Integer>();

        for (int i = 0; i < config.getDSs().size(); i++) {
            int ds = i + 1;
            if (config.getDSs().get(ds - 1).getNextOilVolume() > 0) {
                dss.add(ds);
            }
        }
        return dss;
    }

    /**
     * ���ݹܵ�ע�ͽ���ʱ���Զ����㵱ǰ�ܵ�
     *
     * @param ds
     * @return
     */
    public int getCurrentPipe(int ds) {
        int pipe = -1;
        if (ds == config.HighOilDS) {
            pipe = 1;
        } else {
            pipe = 0;
        }
        return pipe;
    }

    /**
     * ����ܵ�ע�ͽ���ʱ�䵱ǰϵͳʱ��
     *
     * @param pipe
     * @return
     */
    public double getCurrentTime(int pipe) {
        double[] chargingEndTimes = getChargingEndTime();
        return chargingEndTimes[pipe];
    }

    /**
     * ��ȡ��ǰʱ�̹��͹޵�״̬���Ƿ����
     *
     * @param currentTime
     */
    public List<Integer> getTankSet(double currentTime) {
        // ���տ�ʼʱ������
        Operation.sortOperation(operations);

        List<Integer> tankSet = new LinkedList<>();
        TableModel model = getTankStateModel(currentTime);

        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        // ����޵�ȷ��״̬
        for (int i = 0; i < rowCount; i++) {
            int tank = i + 1;
            String state = model.getValueAt(i, colCount - 1).toString();// ��ȡ���ȡ�кŵ�ĳһ�е�ֵ��Ҳ�����ֶΣ�

            // �ҵ����п��еĹ��͹�
            if (state.equals("empty")) {
                tankSet.add(tank);
            }
        }

        return tankSet;
    }

    /**
     * ��ȡת���ٶ�
     *
     * @param ds
     * @return
     */
    public double[] getChargingSpeed(int ds) {
        int pipe = (ds == config.HighOilDS) ? 1 : 0;
        return config.getPipes().get(pipe).getChargingSpeed();
    }

    /**
     * ��ȡת���ٶ�
     *
     * @param pipe
     * @return
     */
    public double[] getCharingSpeed(int pipe) {
        return config.getPipes().get(pipe).getChargingSpeed();
    }

    /**
     * ���ν���
     *
     * @return
     */
    public Fragment getFragment() throws JMException {
        // �����������ص������ڴ�ռ䡾����Ҫ���������������ֱ�Ϊconfig/solution/loc��
        FactObject factObject = new FactObject();
        factObject.setConfig(config);
        factObject.setSolution(solution);
        factObject.setLoc(loc);

        AbstractRule rule = new RuleFactory().getRule(ruleName, this);
        Fragment nextFragment = rule.fireAllRule(factObject);
        return nextFragment;
    }

    /**
     * �����Ƽ����ԡ�Ӧ�Ը��۵�ܵ�ͣ�˵��µĻ��ݡ�
     */
    public Integer[][] generateRecommendPolicy() {

        int rows = config.getDSs().size();
        int cols = config.getTanks().size() + 1;
        Integer[][] policyMap = new Integer[rows][cols];

        // 1.��ʼ�����
        for (int i = 0; i < policyMap.length; i++) {
            for (int j = 0; j < policyMap[i].length; j++) {
                policyMap[i][j] = 0;// ��ǲ�����
            }
        }

        // 2.����δ�������������������Ĳ��ԡ�����ʱ��Ҫɸѡ��
        List<Integer> pipeOneList = getTankSet(getCurrentTime(0));
        List<Integer> pipeTwoList = getTankSet(getCurrentTime(1));
        List<Integer> dss = getDSSet();
        for (int i = 0; i < dss.size(); i++) {
            int ds = dss.get(i);

            if (ds != config.HighOilDS) {
                // �����еĹ��͹޶��ǿչ�ʱ������ͣ��
                if (pipeOneList.size() < config.getTanks().size()) {
                    policyMap[ds - 1][0] = 1;// ���ĳһ�е����ɸ�Ԫ��
                }
                // ���۵�ܵ�
                for (int j = 0; j < pipeOneList.size(); j++) {
                    int tank = pipeOneList.get(j);
                    policyMap[ds - 1][tank] = 1;
                }
            } else {
                // ���۵�ܵ�������ͣ�ˡ�
                for (int j = 0; j < pipeTwoList.size(); j++) {
                    int tank = pipeTwoList.get(j);
                    policyMap[ds - 1][tank] = 1;
                }
            }
        }

        return policyMap;
    }

    /**
     * �ܵ�ѡ�����
     *
     * @return
     */
    public int selectPipe() {
        List<Integer> dss = getDSSet();
        if (dss.contains(config.HighOilDS) && dss.size() == 1) {
            return 1;
        } else if (!dss.contains(config.HighOilDS)) {
            return 0;
        }
        return ArrayHelper.Arraysort(getChargingEndTime())[0];
    }

    /**
     * ѡ���µ�Ӧ�Բ���
     *
     * @return
     */
    private boolean existPolicies() {
        Integer[][] policies = policyStack.peek();

        // �ж��Ƿ����е��Ƽ����Զ��Ѿ����Թ�
        int sum = 0;
        for (int i = 0; i < policies.length; i++) {
            for (int j = policies[i].length - 1; j >= 0; j--) {
                sum += policies[i][j];
            }
        }

        if (sum == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ��������ľ���ָ��
     *
     * @return
     */
    private boolean process() {
        // �����ָ�����г���
        int steps = solution.getDecisionVariables().length / 2;

        // �����ʼ��ͼ��
        if (plotEachStep) {
            Operation.plotSchedule2(operations);
        }

        while (loc < steps && !isFinished()) {

            // ����ջ���ɲ��ԣ�����ջȻ�󽫿��еĲ�����ջ��ִ��֮ǰ��ǲ����Ѿ�ʹ�ù����мǣ�������
            if (policyStack.size() == fragmentStack.size()) {
                // �������п��ܵĲ��ԣ�����������ջ
                policyStack.push(generateRecommendPolicy());
            } else {
                // ���ݡ����۵�ܵ����ȣ���ռʽ���ȡ�
                while (!existPolicies()) {
                    last();
                    preemptiveScheduling();
                }
            }

            if (policyStack.size() != fragmentStack.size() + 1) {
                // ����ջ���������Ӧ�ñ��ִ�С���1
                logger.fatal("��������ϵͳջ�쳣");
                System.exit(1);
            }

            try {
                next();
            } catch (Exception e) {
                if (e.getMessage().equals("���۵�ܵ�������ͣ��")) {
                    // ��ǻ���
                    Integer[][] policies = policyStack.peek();
                    for (int i = 0; i < policies.length; i++) {
                        if (i + 1 != config.HighOilDS) {
                            for (int j = 0; j < policies[i].length; j++) {
                                policies[i][j] = 0;
                            }
                        }
                    }
                } else if (e.getMessage().equals("���͹�ռ�ó�ͻ")) {
                    logger.fatal("��Ӧ���ٴ��ڹ��͹�ռ�ó�ͻ����");
                    e.printStackTrace();
                } else {
                    logger.fatal("������������");
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * ��ռʽ���Ȳ���
     */
    private void preemptiveScheduling() {
        try {
            // ���ٿ��ǵ��۵���
            Integer[][] policyMap = policyStack.peek();
            for (int i = 0; i < policyMap.length; i++) {
                if (i + 1 != config.HighOilDS) {
                    for (int j = 0; j < policyMap[i].length; j++) {
                        policyMap[i][j] = 0;
                    }
                }
            }
        } catch (Exception e) {
            logger.fatal("���ش���");
            System.exit(1);
        }
    }

    /**
     * ������һ������
     *
     * @throws Exception
     */
    private void next() throws Exception {
        try {
            Fragment nextFragment = getFragment();
            if (nextFragment.getTank() != 0 && nextFragment.getVolume() == 0) {
                logger.fatal("������󣺷�ͣ��ָ��ǽ������Ϊ0");
                System.exit(1);
            }

            doOperation(nextFragment);

            // ���ִ������һ��������ĵ���ͼ
            if (plotEachStep) {
                Operation.plotSchedule2(operations);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());// ��Ϣ�������ϲ㴫��
        }
    }

    /**
     * ��ǰ����һ��
     *
     * @return
     */
    private void last() {
        // ����һ����loc==0ʱ���޷����ˡ�
        if (loc > 0) {
            loc--;
            policyStack.pop();
            fragmentStack.pop();
            Operation operation = null;
            do {
                operation = operationStack.pop();
                operations.remove(operation);// �Ƴ�����
            } while (operation.getType() == OperationType.Feeding);
            removeConfig();// �Ƴ�ջ�������ã�����configָ���µ�ջ������

            // ������غ�ĵ��ȼƻ�����ͼ
            if (plotEachStep) {
                Operation.plotSchedule2(operations);
            }
        }
    }

    /**
     * �ж��Ƿ���Ƚ���
     *
     * @return
     */
    private boolean isFinished() {
        int count = 0;
        for (DSObject dsObject : config.getDSs()) {
            if (dsObject.getNextOilVolume() < 0) {
                count++;
            }
        }
        if (count < config.getDSs().size()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ��ȡ���й޵������Ҫʹ�õ�ʱ��
     *
     * @param currentTime
     * @return
     */
    private Map<Integer, Double> getDeadlineTimeOfAllTanks(double currentTime) {
        Operation.sortOperation(operations);

        Map<Integer, Double> times = new HashMap<>();
        for (Operation operation : operations) {
            if (operation.getStart() > currentTime) {
                if (!times.containsKey(operation.getTank())) {
                    times.put(operation.getTank(), operation.getStart());
                }
            }
        }

        return times;
    }

    /**
     * ��ȡ�ܵ���ͣ��ʱ�䣬������۵�ܵ��͵��۵�ܵ�������ͣ�ˡ� tank=0��������һ��ͣ�˲���������ds�ж��Ǹ��۵�ܵ�ͣ�˻��ǵ��۵�ܵ�ͣ��
     *
     * @return
     */
    private double[] getChargingEndTime() {
        // �ܵ��ĸ���
        int numOfPipes = config.getPipes().size();
        double[] chargingEndTime = new double[numOfPipes];

        // �������������ע�ͽ���ʱ��
        for (Operation operation : operations) {
            if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
                // ���۵�ܵ�ת��
                if (operation.getDs() != config.HighOilDS) {
                    // ����ע�ͽ���ʱ��
                    if (chargingEndTime[0] < operation.getEnd()) {
                        chargingEndTime[0] = operation.getEnd();
                    }
                } else {
                    if (chargingEndTime[1] < operation.getEnd()) {
                        chargingEndTime[1] = operation.getEnd();
                    }
                }
            }
        }

        return chargingEndTime;
    }

    /**
     * ��ȡ�����������ͽ���ʱ��
     *
     * @return
     */
    public double[] getFeedingEndTime() {

        double[] feedEndTime = new double[config.getDSs().size()];

        // ����������������ͽ���ʱ��
        for (int i = 0; i < config.getDSs().size(); i++) {
            int ds = i + 1;
            for (Operation operation : operations) {
                if (operation.getDs() == ds && operation.getType() == OperationType.Feeding) {
                    // �������ͽ���ʱ��
                    if (feedEndTime[i] < operation.getEnd()) {
                        feedEndTime[i] = operation.getEnd();
                    }
                }
            }
        }

        // ����Ѿ�������ͼƻ�����
        for (int i = 0; i < config.getDSs().size(); i++) {
            // ������ͼƻ������������ڿ��Ƿ�Χ
            if (config.getDSs().get(i).getNextOilVolume() == -1) {
                feedEndTime[i] = Double.MAX_VALUE;
                continue;
            }
        }
        return feedEndTime;
    }

    /**
     * ��ȡ���͹޵������ͷ�ʱ�䣬Ϊͣ���ṩ�ο�
     *
     * @return
     */
    public double getEarliestAvailableTime(double currentTime) {
        Map<Integer, Double> availableTimes = new HashMap<Integer, Double>();

        // ����޵�ȷ��״̬
        for (int i = 0; i < config.getTanks().size(); i++) {
            int tank = i + 1;
            for (Operation operation : operations) {
                // �ҵ���Ӧ�Ĺ�
                if (operation.getTank() == tank && operation.getEnd() > currentTime) {

                    // ���Ȼ����ͽ��������
                    if (operation.getType() == OperationType.Hoting || operation.getType() == OperationType.Feeding) {

                        availableTimes.put(tank, operation.getEnd());
                        break;
                    }
                }
            }
        }

        double time = Double.MAX_VALUE;

        for (Double value : availableTimes.values()) {
            if (time > value) {
                time = value;
            }
        }
        return time;
    }

    /**
     * ��ȡ���͹޵�״̬
     *
     * @return
     */
    private DefaultTableModel getTankStateModel(double currentTime) {
        Operation.sortOperation(operations);

        DefaultTableModel model = new DefaultTableModel();
        Object[] columnNames = {"���͹�", "����", "ԭ������", "ԭ�����", "��ʼʱ��", "����ʱ��", "״̬"};
        model.setColumnIdentifiers(columnNames);

        for (int i = 0; i < config.getTanks().size(); i++) {
            int tank = i + 1;
            Object[] data = {tank, config.getTanks().get(tank - 1).getCapacity(), 0, 0, 0, 0, "empty"};

            // ��¼ǰһ���ߺͺ�һ����
            Operation lastOperation = null;
            Operation nextOperation = null;
            for (Operation operation : operations) {
                if (operation.getTank() == tank) {
                    if (operation.getStart() > currentTime) {
                        nextOperation = operation;
                        break;
                    } else {
                        lastOperation = operation;
                    }
                }
            }

            // û���κξ��߼�¼
            if (lastOperation == null && nextOperation == null) {
                // �չ޵����⴦��
                data[2] = 0;
                data[3] = 0;
                data[4] = 0;
                data[5] = 0;
                data[6] = "empty";
            } else {
                // ǰһ�����߷ǿ�
                if (lastOperation != null) {
                    // �����ϴβ���������ȷ���ǵȴ����ǿ���
                    if (lastOperation.getStart() <= currentTime && currentTime < lastOperation.getEnd()) {
                        if (lastOperation.getType() == OperationType.Hoting) {
                            // ע��״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathUtil.round(
                                    lastOperation.getVol()
                                            - (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    config.NumOfDivide);// ����ʣ�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "hoting";
                        } else if (lastOperation.getType() == OperationType.Charging) {
                            // ע��״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathUtil.round(
                                    (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    config.NumOfDivide);// ����ע�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "charging";
                        } else if (lastOperation.getType() == OperationType.Feeding) {
                            // ����״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathUtil.round(
                                    lastOperation.getVol()
                                            - (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    config.NumOfDivide);// ����ʣ�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "feeding";
                        }
                    } else {
                        if (lastOperation.getType() == OperationType.Charging) {

                            // �ȴ�״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathUtil.round(lastOperation.getVol(), config.NumOfDivide);
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "waiting";
                        } else {
                            // �ȴ�״̬
                            data[2] = 0;
                            data[3] = 0;
                            data[4] = 0;
                            data[5] = 0;
                            data[6] = "empty";
                        }
                    }
                } else if (nextOperation.getType() == OperationType.Feeding) {

                    // ǰһ����Ϊ�գ���ʼ�������⴦��
                    data[2] = nextOperation.getOil();
                    data[3] = MathUtil.round(nextOperation.getVol(), config.NumOfDivide);
                    data[4] = nextOperation.getStart();
                    data[5] = nextOperation.getEnd();
                    data[6] = "waiting";
                }
            }

            model.addRow(data);

        }
        return model;
    }

    /**
     * ��ʼ������
     */
    private void initSimulation(Solution solution) {
        this.solution = solution;

        // ��ʼ������ջ
        Config config_Clone = CloneUtils.clone(config);
        configStack.push(config_Clone);

        // ���ͽ���ʱ��
        double[] feedEndTime = new double[config.getDSs().size()];

        // ִ�г�ʼָ��(���۵���)
        for (int i = 0; i < config.getTanks().size(); i++) {
            TankObject tankObject = config.getTanks().get(i);
            double vol = MathUtil.round(tankObject.getVolume(), config.Precision);
            int tank = i + 1;

            if (vol > 0) {
                if (Config.HotTank != tank) {

                    int oiltype = config.getTanks().get(i).getOiltype();
                    int ds = tankObject.getAssign();
                    double speed = config.getDSs().get(ds - 1).getSpeed();
                    double feedTime = MathUtil.round(vol / speed, config.Precision);

                    Operation feeding = new Operation(OperationType.Feeding, tank, ds,
                            MathUtil.round(feedEndTime[ds - 1], config.Precision),
                            MathUtil.round(feedEndTime[ds - 1] + feedTime, config.Precision), vol, oiltype, speed, 0);// ��ʼ���������ͳ��ڣ���0��ʾ
                    operations.add(feeding);

                    // �������ͽ���ʱ��
                    feedEndTime[ds - 1] += feedTime;
                }
            }
        }
        // ִ�г�ʼָ��(���۵���)
        int tank = Config.HotTank;
        int ds = config.HighOilDS;
        TankObject tankObject = config.getTanks().get(tank - 1);
        double vol = MathUtil.round(tankObject.getVolume(), config.Precision);
        double vPipe = config.getPipes().get(1).getVol();
        int oiltype = config.getTanks().get(tank - 1).getOiltype();
        double feedingSpeed = config.getDSs().get(config.HighOilDS - 1).getSpeed();
        double hotingTime = MathUtil.divide(vol, Config.HotingSpeed);
        double chargingTime = MathUtil.divide(vPipe, Config.HotingSpeed);
        double feedingTime = MathUtil.divide(vPipe, feedingSpeed);

        int site = 2;// ���۵�ۿ�
        Operation hoting = new Operation(OperationType.Hoting,
                tank,
                ds,
                0,
                MathUtil.round(hotingTime, config.Precision),
                vol,
                oiltype,
                Config.HotingSpeed,
                site);
        Operation charging = new Operation(OperationType.Charging,
                tank,
                ds,
                MathUtil.round(hotingTime, config.Precision),
                MathUtil.round(hotingTime + chargingTime, config.Precision),
                vPipe,
                oiltype,
                Config.HotingSpeed,
                site);
        // ���ȹܵ���ĵ��۵�ԭ�ͻ����¹�������������
        ds = config.getTanks().get(tank - 1).getAssign();
        feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
        feedingTime = MathUtil.divide(vPipe, feedingSpeed);
        Operation feeding = new Operation(OperationType.Feeding,
                tank,
                ds,
                MathUtil.round(feedEndTime[ds - 1], config.Precision),
                MathUtil.round(MathUtil.add(feedEndTime[ds - 1], feedingTime), config.Precision),
                vPipe, oiltype,
                feedingSpeed,
                site);

        operations.add(hoting);
        operations.add(charging);
        operations.add(feeding);
    }

    /**
     * ��¼
     *
     * @param col
     * @param row
     */
    private void record(int col, int row) {
        try {
            policyStack.peek()[row - 1][col] = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ִ�о���
     *
     * @param fragment
     * @throws Exception
     */
    private void doOperation(Fragment fragment) throws Exception {

        // ��ȡ��һ������
        int tank = fragment.getTank();
        int ds = fragment.getDs();
        double speed = fragment.getSpeed();
        double vol = fragment.getVolume();
        // ����Ѿ�ִ�й�
        record(tank, ds);

        double[] feedEndTimes = getFeedingEndTime();

        // �ж���Ҫת�˵Ĺܵ���ת�˽���ʱ��
        int pipe = getCurrentPipe(ds);
        double currentTime = getCurrentTime(pipe);

        // ����һ��δ�仯�����á����²���ջ��ԭ����������Ϣ���ܱ仯��
        Config config_Clone = newConfig();

        // �ж��Ƿ�ͣ��
        if (tank == 0) {

            // 3.���۵�ܵ�������ͣ��
            if (ds == config.HighOilDS) {
                throw new Exception("���۵�ܵ�������ͣ��");// ��ע�⣺��ע�����ݲ��ɱ䡿
            }

            double availableTime = getEarliestAvailableTime(currentTime);
            Operation stoping = new Operation(OperationType.Stop, tank, ds,
                    MathUtil.round(currentTime, config.Precision), MathUtil.round(availableTime, config.Precision), vol,
                    0, 0, 0);

            // ���½��ϰ������ͽ���ʱ��
            operations.add(stoping);
            operationStack.push(stoping);
        } else {

            // ȷ��ԭ������
            int oiltype = config.getDSs().get(ds - 1).getNextOilType();
            // ȷ����һԭ����������һ���ۿ�
            int site = config.getDSs().get(ds - 1).getWhereNextOilFrom();
            // ȷ��ע��ʱ��
            double chargingTime = MathUtil.divide(vol, speed);

            // ȷ���������ʺ�����ʱ��
            double feedingSpeed = config.getDSs().get(ds - 1).getSpeed();
            double feedingTime = MathUtil.divide(vol, feedingSpeed);

            // ȷ����ʼ����ʱ��
            double feedingStartTime = Math.max(feedEndTimes[ds - 1], currentTime + chargingTime + config.RT);

            // 2.�жϱ���ע���Ƿ�ͱ�Ĳ����г�ͻ
            Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
            double feedEndTime = MathUtil.round(MathUtil.add(feedingStartTime, feedingTime), config.Precision);
            if (usingTimes.containsKey(tank) && usingTimes.get(tank) < feedEndTime) {
                Operation.plotSchedule2(operations);
                throw new Exception("���͹�ռ�ó�ͻ");// ��������
            }

            // ȷ������
            Operation charging = new Operation(OperationType.Charging, tank, ds,
                    MathUtil.round(currentTime, config.Precision), MathUtil.add(currentTime, chargingTime), vol,
                    oiltype, speed, site);
            Operation feeding = new Operation(OperationType.Feeding, tank, ds, feedingStartTime, feedEndTime, vol,
                    oiltype, feedingSpeed, site);

            // ���½��ϰ������ͽ���ʱ��
            operations.add(charging);
            operations.add(feeding);
            operationStack.push(charging);// ע��˳����charging��feeding
            operationStack.push(feeding);
            config_Clone.getDSs().get(ds - 1).updateOilVolume(vol);// �����µ�����
        }
        fragmentStack.push(fragment);// ������ջ
        addConfig(config_Clone);// ���µ�����ѹ������ջ�У�������config��ָ��

        loc++;
    }
}