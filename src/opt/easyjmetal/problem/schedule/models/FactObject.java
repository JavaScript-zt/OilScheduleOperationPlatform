package opt.easyjmetal.problem.schedule.models;

import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.core.Solution;

/**
 * 负责和规则引擎通信
 *
 * @author Administrator
 */
public class FactObject {
    // 规则输入
    private Solution solution;
    private int loc;
    private int pipe;// 区分管道1调度还是管道2调度
    private Config config;
    private double[] feedEndTime;
    private double time;// 系统时间

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double[] getFeedEndTime() {
        return feedEndTime;
    }

    public void setFeedEndTime(double[] feedEndTime) {
        this.feedEndTime = feedEndTime;
    }

    public int getPipe() {
        return pipe;
    }

    public void setPipe(int pipe) {
        this.pipe = pipe;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public FactObject() {
    }

    public FactObject(Solution solution, int loc, Config config) {
        this.solution = solution;
        this.loc = loc;
        this.config = config;
    }
}
