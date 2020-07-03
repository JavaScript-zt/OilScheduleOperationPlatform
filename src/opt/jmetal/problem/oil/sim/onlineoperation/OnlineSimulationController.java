package opt.jmetal.problem.oil.sim.onlineoperation;

import opt.jmetal.problem.oil.models.DSObject;
import opt.jmetal.problem.oil.models.FPObject;
import opt.jmetal.problem.oil.models.Fragment;
import opt.jmetal.problem.oil.models.TankObject;
import opt.jmetal.problem.oil.sim.common.MathHelper;
import opt.jmetal.problem.oil.sim.oil.Config;
import opt.jmetal.problem.oil.sim.operation.Operation;
import opt.jmetal.problem.oil.sim.operation.OperationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * �ؼ����ڳ�ͻ���
 *
 * @author Administrator
 */
public class OnlineSimulationController {

    // ��־��¼
    private Logger logger = LogManager.getLogger(OnlineSimulationController.class.getName());

    private double currentTime;

    private List<Operation> operations;

    public List<Operation> getOperations() {
        return operations;
    }

    public OnlineSimulationController() {

        operations = new LinkedList<>();
    }

    /**
     * �ж��Ƿ���Ƚ���
     *
     * @return
     */
    public boolean isFinished() {
        int count = 0;
        for (DSObject dsObject : Config.getInstance().getDSs()) {
            if (dsObject.getNextOilVolume() < 0) {
                count++;
            }
        }
        if (count < Config.getInstance().getDSs().size()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ��ȡ���й޵������Ҫע�͵Ľ�ֹʱ��
     *
     * @return
     */
    private Map<Integer, Double> getChargingDeadlineTimeOfAllTanks() {
        Operation.sortOperation(operations);

        Map<Integer, Double> times = new HashMap<>();
        for (Operation operation : operations) {
            if (operation.getType() == OperationType.Charging && operation.getStart() > currentTime) {
                if (!times.containsKey(operation.getTank())) {
                    times.put(operation.getTank(), operation.getStart());
                }
            }
        }

        return times;
    }

    /**
     * ��ȡ���й޵������Ҫ���͵�ʱ��
     *
     * @param currentTime
     * @return
     */
    private Map<Integer, Double> getFeedingDeadlineTimeOfAllTanks(double currentTime) {
        Operation.sortOperation(operations);

        Map<Integer, Double> times = new HashMap<>();
        for (Operation operation : operations) {
            if (operation.getType() == OperationType.Feeding && operation.getStart() > currentTime) {
                if (!times.containsKey(operation.getTank())) {
                    times.put(operation.getTank(), operation.getStart());
                }
            }
        }

        return times;
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
     * ��������ת������� ��֤����ͻ��������ٶ�ת�ˡ�
     *
     * @return
     */
    public Double getMaxVolumeWithMaxSpeed(int tank, int ds) {

        // ȷ��ת�˺������ٶ�
        double[] chargingSpeeds = Config.getInstance().getPipes().get(0).getChargingSpeed();
        double chargingSpeed = chargingSpeeds[chargingSpeeds.length - 1];// ��������ٶ�ת�ˡ�

        return getMaxVolume(tank, ds, chargingSpeed);
    }

    /**
     * ��������ת������� ��֤����ͻ������С�ٶ�ת�ˡ�
     *
     * @return
     */
    public Double getMaxVolumeWithMinSpeed(int tank, int ds) {

        // ȷ��ת�˺������ٶ�
        double[] chargingSpeeds = Config.getInstance().getPipes().get(0).getChargingSpeed();
        double chargingSpeed = chargingSpeeds[0];// ������С�ٶ�ת�ˡ�

        return getMaxVolume(tank, ds, chargingSpeed);
    }

    /**
     * ��������ת������� ��֤����ͻ��������������á�
     *
     * @return
     */
    public Double getMaxVolume(int tank, int ds, double chargingSpeed) {

        double vol = Double.MIN_VALUE;

        try {
            // ȷ�������ٶ�
            double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();

            // ���õ��Ƶķ���ȷ�����ת�����
            if (getChargingDeadlineTimeOfAllTanks().containsKey(tank)) {
                double deadline = getChargingDeadlineTimeOfAllTanks().get(tank);

                if (deadline - currentTime > Config.getInstance().RT) {

                    double vol1 = chargingSpeed * feedingSpeed * (deadline - currentTime - Config.getInstance().RT)
                            / (chargingSpeed + feedingSpeed);

                    // �ؼ����⣺��������´Ӻ���ǰ��û��̫�����⣬���ǣ���ʵ�ǣ�����ת�˹�ȥ��ԭ�Ͳ��������Ϳ�ʼ
                    // �������������ͣ���ˣ����ܻ��Ӻ�����Ӻ��ʱ����ã��ͻᵼ�µ�ǰ��feeding��������һ�����ߵ�
                    // charging�����໥�ص������յ��·����˳�ͻ����ˣ�����Ҫ���һ����������vol2��
                    double vol2 = 0.0;

                    if (getFeedingDeadlineTimeOfAllTanks(currentTime).containsKey(tank)) {
                        vol2 = (getFeedingDeadlineTimeOfAllTanks(currentTime).get(tank) - getFeedingEndTime()[ds - 1])
                                / Config.getInstance().getDSs().get(ds - 1).getSpeed();
                    } else {
                        vol2 = Double.MAX_VALUE;
                    }

                    vol = Math.min(vol1, vol2);

                } else {

                    // ����ת��ԭ��
                    vol = Double.MIN_VALUE;
                }
            } else {
                vol = Double.MAX_VALUE;
            }
        } catch (Exception e) {
            logger.fatal("calculate volume error.");
            System.exit(1);
        }

        return vol;
    }

    /**
     * ��ȡ�ܵ���ͣ��ʱ�䣬������۵�ܵ��͵��۵�ܵ�������ͣ�ˡ� tank=0��������һ��ͣ�˲���������ds�ж��Ǹ��۵�ܵ�ͣ�˻��ǵ��۵�ܵ�ͣ��
     *
     * @return
     */
    public double[] getChargingEndTime() {
        // �ܵ��ĸ���
        int numOfPipes = Config.getInstance().getPipes().size();
        double[] chargingEndTime = new double[numOfPipes];

        // �������������ע�ͽ���ʱ��
        for (Operation operation : operations) {
            if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
                // ���۵�ܵ�ת��
                if (operation.getDs() != Config.getInstance().HighOilDS) {
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
    private double[] getFeedingEndTime() {

        double[] feedEndTime = new double[Config.getInstance().getDSs().size()];

        // ����������������ͽ���ʱ��
        for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
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
        for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
            // ������ͼƻ������������ڿ��Ƿ�Χ
            if (Config.getInstance().getDSs().get(i).getNextOilVolume() == -1) {
                feedEndTime[i] = Double.MAX_VALUE;
                continue;
            }
        }
        return feedEndTime;
    }

    private enum TankState {
        empty, hotting, charging, feeding, waiting
    }

    /**
     * ��ȡ��ǰʱ�̹��͹޵�״̬���Ƿ����
     *
     * @param currentTime
     */
    private Map<Integer, TankState> getTankStatus(double currentTime) {
        // ���տ�ʼʱ������
        Operation.sortOperation(operations);

        Map<Integer, TankState> tankState = new HashMap<Integer, TankState>();
        TableModel model = getTankStateModel(currentTime);

        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        // ����޵�ȷ��״̬
        for (int i = 0; i < rowCount; i++) {

            int tank = i + 1;
            String state = model.getValueAt(i, colCount - 1).toString();// ��ȡ���ȡ�кŵ�ĳһ�е�ֵ��Ҳ�����ֶΣ�

            // ȷ���޵�ǰʱ�̵�״̬����ȷ����򵥵��������ͣ����Ÿ����ϴβ���������ȷ���ǵȴ����ǿ���
            if (state.equals("waiting")) {
                tankState.put(tank, TankState.waiting);
            } else if (state.equals("hotting")) {
                tankState.put(tank, TankState.hotting);
            } else if (state.equals("feeding")) {
                tankState.put(tank, TankState.feeding);
            } else if (state.equals("charging")) {
                tankState.put(tank, TankState.charging);
            } else if (state.equals("empty")) {
                tankState.put(tank, TankState.empty);
            }
        }

        return tankState;
    }

    /**
     * ��ȡ���͹޵������ͷ�ʱ�䣬Ϊͣ���ṩ�ο�
     *
     * @return
     */
    private double getEarliestAvailableTime(double currentTime) {
        Map<Integer, Double> availableTimes = new HashMap<Integer, Double>();

        // ����޵�ȷ��״̬
        for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
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
    public DefaultTableModel getTankStateModel(double currentTime) {
        Operation.sortOperation(operations);

        String[] columnNames = {"���͹�", "����", "ԭ������", "ԭ�����", "��ʼʱ��", "����ʱ��", "״̬"};

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);

        for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
            int tank = i + 1;
            Object[] data = {tank, Config.getInstance().getTanks().get(tank - 1).getCapacity(), 0, 0, 0, 0, "empty"};

            // ��¼ǰһ���ߺͺ�һ����
            Operation lastOperation = null;
            Operation nextOperation = null;

            for (Operation operation : operations) {
                // �ҵ���Ӧ�Ĺ�
                if (operation.getTank() == tank) {
                    if (operation.getStart() > currentTime) {
                        nextOperation = operation;
                        break;
                    } else {
                        // �����һ״̬
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
                            data[3] = MathHelper.precision(
                                    lastOperation.getVol()
                                            - (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    Config.getInstance().NumOfDivide);// ����ʣ�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "hoting";
                        } else if (lastOperation.getType() == OperationType.Charging) {
                            // ע��״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathHelper.precision(
                                    (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    Config.getInstance().NumOfDivide);// ����ע�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "charging";
                        } else if (lastOperation.getType() == OperationType.Feeding) {
                            // ����״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathHelper.precision(
                                    lastOperation.getVol()
                                            - (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
                                    Config.getInstance().NumOfDivide);// ����ʣ�����
                            data[4] = lastOperation.getStart();
                            data[5] = lastOperation.getEnd();
                            data[6] = "feeding";
                        }
                    } else {
                        if (lastOperation.getType() == OperationType.Charging) {

                            // �ȴ�״̬
                            data[2] = lastOperation.getOil();
                            data[3] = MathHelper.precision(lastOperation.getVol(), Config.getInstance().NumOfDivide);
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
                    data[3] = MathHelper.precision(nextOperation.getVol(), Config.getInstance().NumOfDivide);
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
     * ��ȡ�������������ϰ���С
     *
     * @return
     */
    public TableModel getFpModel() {
        String[] columnNames = {"������", "ԭ������", "ԭ�����", "����λ��"};

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
            int ds = i + 1;

            List<FPObject> fpObjects = Config.getInstance().getDSs().get(i).getFps();

            for (FPObject fpObject : fpObjects) {
                // �����Ѿ���ɵĽ��ϰ�
                if (fpObject.getVolume() > 0) {
                    Object[] data = new Object[columnNames.length];
                    data[0] = ds;
                    data[1] = fpObject.getOiltype();
                    data[2] = fpObject.getVolume();
                    data[3] = fpObject.getSite();
                    model.addRow(data);
                }
            }
        }
        return model;
    }

    /**
     * ��ȡ�ɱ�
     *
     * @return
     */
    public TableModel getCostModel() {
        String[] columnNames = {"Լ��Υ��ֵ", "�л�����", "�޵׻�ϳɱ�", "�ܵ���ϳɱ�", "�ܺĳɱ�", "�ù޸���"};

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        Object[] data = new Object[columnNames.length];
        double delayCost = Operation.getDelayCost(operations);
        double maintenanceCost = Operation.getTankMaintenanceTime(operations);
        double hardCost = delayCost + maintenanceCost;
        data[0] = hardCost;
        data[1] = Operation.getNumberOfChange(operations);
        data[2] = Operation.getTankMixingCost(operations);
        data[3] = Operation.getPipeMixingCost(operations);
        data[4] = Operation.getEnergyCost(operations);
        data[5] = Operation.getNumberOfTankUsed(operations);
        model.addRow(data);
        return model;
    }

    /**
     * ��ʼ������
     */
    public void initSimulation() {

        // ���س�ʼ������
        Config.getInstance().loadConfig();

        // ���ͽ���ʱ��
        double[] feedEndTime = new double[Config.getInstance().getDSs().size()];

        // ִ�г�ʼָ��(���۵���)
        for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
            TankObject tankObject = Config.getInstance().getTanks().get(i);
            double vol = MathHelper.precision(tankObject.getVolume(), Config.getInstance().Precision);
            int tank = i + 1;

            if (vol > 0) {
                if (Config.HotTank != tank) {

                    int oiltype = Config.getInstance().getTanks().get(i).getOiltype();
                    int ds = tankObject.getAssign();
                    double speed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
                    double feedTime = MathHelper.precision(vol / speed, Config.getInstance().Precision);

                    Operation feeding = new Operation(OperationType.Feeding, tank, ds,
                            MathHelper.precision(feedEndTime[ds - 1], Config.getInstance().Precision),
                            MathHelper.precision(feedEndTime[ds - 1] + feedTime, Config.getInstance().Precision), vol,
                            oiltype, speed, 0);// ��ʼ���������ͳ��ڣ���0��ʾ
                    operations.add(feeding);

                    // �������ͽ���ʱ��
                    feedEndTime[ds - 1] += feedTime;
                }
            }
        }
        // ִ�г�ʼָ��(���۵���)
        int tank = Config.HotTank;
        int ds = Config.getInstance().HighOilDS;
        TankObject tankObject = Config.getInstance().getTanks().get(tank - 1);
        double vol = MathHelper.precision(tankObject.getVolume(), Config.getInstance().Precision);
        double vPipe = Config.getInstance().getPipes().get(1).getVol();
        int oiltype = Config.getInstance().getTanks().get(tank - 1).getOiltype();
        double feedingSpeed = Config.getInstance().getDSs().get(Config.getInstance().HighOilDS - 1).getSpeed();
        double feedingTime = MathHelper.precision(vPipe / feedingSpeed, Config.getInstance().Precision);

        double paperSpeed = Config.getInstance().HotingSpeed;// Ϊ�˶Ա����Ķ�����
        double hotingTime = MathHelper.precision(vol / paperSpeed, Config.getInstance().Precision);
        double chargingTime = MathHelper.precision(vPipe / paperSpeed, Config.getInstance().Precision);
        Operation hoting = new Operation(OperationType.Hoting, tank, Config.getInstance().HighOilDS, 0, hotingTime, vol,
                oiltype, paperSpeed, 2);
        Operation charging = new Operation(OperationType.Charging, tank, Config.getInstance().HighOilDS,
                MathHelper.precision(hotingTime, Config.getInstance().Precision),
                MathHelper.precision(hotingTime + chargingTime, Config.getInstance().Precision), vPipe, oiltype,
                paperSpeed, 2);
        // ���ȹܵ���ĵ��۵�ԭ�ͻ����¹�������������
        ds = Config.getInstance().getTanks().get(tank - 1).getAssign();
        feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
        feedingTime = MathHelper.precision(vPipe / feedingSpeed, Config.getInstance().Precision);
        Operation feeding = new Operation(OperationType.Feeding, tank, ds,
                MathHelper.precision(feedEndTime[ds - 1], Config.getInstance().Precision),
                MathHelper.precision(feedEndTime[ds - 1] + feedingTime, Config.getInstance().Precision), vPipe, oiltype,
                feedingSpeed, 0);

        operations.add(hoting);
        operations.add(charging);
        operations.add(feeding);
    }

    /**
     * ��һ������
     *
     * @param fragment
     * @return
     * @throws Exception
     */
    public boolean doOperation(Fragment fragment) throws Exception {

        // ��ȡ��һ������
        int tank = fragment.getTank();
        int ds = fragment.getDs();
        double speed = fragment.getSpeed();
        double vol = fragment.getVolume();

        double[] feedEndTimes = getFeedingEndTime();
        double[] chargingEndTimes = getChargingEndTime();

        // �ж���Ҫת�˵Ĺܵ���ת�˽���ʱ��
        double currentTime = (ds != Config.getInstance().HighOilDS) ? chargingEndTimes[0] : chargingEndTimes[1];

        // �ж��Ƿ�ͣ��
        if (tank == 0) {
            double availableTime = getEarliestAvailableTime(currentTime);
            Operation stoping = new Operation(OperationType.Stop, tank, ds,
                    MathHelper.precision(currentTime, Config.getInstance().Precision),
                    MathHelper.precision(availableTime, Config.getInstance().Precision), vol, 0, 0, 0);

            // ���½��ϰ������ͽ���ʱ��
            operations.add(stoping);
        } else {

            // 1.�жϵ�ǰ���͹��Ƿ����ʹ��
            Map<Integer, TankState> tankState = getTankStatus(currentTime);
            if (tankState.containsKey(tank) && tankState.get(tank) != TankState.empty) {
                throw new Exception("ȷ����ѡ��Ĺ��͹�Ϊ�ա�");
            }

            // 2.�ж�ת�˵�ԭ���Ƿ񳬹����ͼƻ�
            if (vol > Config.getInstance().getDSs().get(ds - 1).getNextOilVolume()) {
                throw new Exception("���ͼƻ��У�������" + ds + "����Ҫת����ô�������ԭ�͡�");
            }

            // 3.�ж��Ƿ����㹩�͹�����Լ��
            if (vol > Config.getInstance().getTanks().get(tank - 1).getCapacity()) {
                throw new Exception(
                        "make sure the volume of opt.jmetal.problem.oil to translated is smaller than the capacity of the tank you selected.");
            }

            // ȷ��ԭ������
            int oiltype = Config.getInstance().getDSs().get(ds - 1).getNextOilType();
            // ȷ����һԭ����������һ���ۿ�
            int site = Config.getInstance().getDSs().get(ds - 1).getWhereNextOilFrom();
            // ȷ��ע��ʱ��
            double chargingTime = MathHelper.precision(vol / speed, Config.getInstance().Precision);

            // ȷ���������ʺ�����ʱ��
            double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
            double feedingTime = MathHelper.precision(vol / feedingSpeed, Config.getInstance().Precision);

            // ȷ����ʼ����ʱ��
            double feedingStartTime = feedEndTimes[ds - 1];

            // 4.�ж��Ƿ�����פ��ʱ��Լ��
            if (currentTime + chargingTime + Config.getInstance().RT > feedEndTimes[ds - 1]) {
                // ��������פ��ʱ��Լ��ʱ�����ȡ���˵ķ�ʽ
                feedingStartTime = currentTime + chargingTime + Config.getInstance().RT;
            }

            // 5.�жϱ���ע���Ƿ�ͱ�Ĳ����г�ͻ
            Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
            if (usingTimes.containsKey(tank) && usingTimes.get(tank) < feedingStartTime + feedingTime) {
                throw new Exception("���͹�ռ�ó�ͻ��");
            }

            // ȷ������
            Operation charging = new Operation(OperationType.Charging, tank, ds,
                    MathHelper.precision(currentTime, Config.getInstance().Precision),
                    MathHelper.precision(currentTime + chargingTime, Config.getInstance().Precision), vol, oiltype,
                    speed, site);
            Operation feeding = new Operation(OperationType.Feeding, tank, ds,
                    MathHelper.precision(feedingStartTime, Config.getInstance().Precision),
                    MathHelper.precision(feedingStartTime + feedingTime, Config.getInstance().Precision), vol, oiltype,
                    feedingSpeed, site);

            // ���½��ϰ������ͽ���ʱ��
            operations.add(charging);
            operations.add(feeding);
            Config.getInstance().getDSs().get(ds - 1).updateOilVolume(vol);
        }

        return true;
    }
}