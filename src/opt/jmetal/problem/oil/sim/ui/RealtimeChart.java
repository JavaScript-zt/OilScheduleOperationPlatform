package opt.jmetal.problem.oil.sim.ui;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Logarithmic Y-Axis
 *
 * <p>
 * Demonstrates the following:
 *
 * <ul>
 * <li>Logarithmic Y-Axis
 * <li>Building a Chart with ChartBuilder
 * <li>Place legend at Inside-NW position
 */
public class RealtimeChart {

    private SwingWrapper<XYChart> swingWrapper;
    private XYChart chart;
    private JFrame frame;

    private String title;// ����
    private String seriesName;// ϵ�У��˴�ֻ��һ��ϵ�С������ڶ������ݣ��������ö��ϵ��
    private List<Double> seriesData;// ϵ�е�����
    private int size = 1000;// �����ʾ�������ݣ�Ĭ����ʾ1000������

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * ʵʱ��ͼ
     *
     * @param seriesName
     * @param title
     */
    private RealtimeChart(String title, String seriesName) {
        super();
        this.seriesName = seriesName;
        this.title = title;
    }

    private static RealtimeChart _instance;

    public static RealtimeChart getInstance() {
        if (_instance == null) {
            _instance = new RealtimeChart("ʵʱ����", "ӲԼ������");
        }
        return _instance;
    }

    public synchronized void plot(double data) {
        if (seriesData == null) {
            seriesData = new LinkedList<>();
        }
        if (seriesData.size() == this.size) {
            seriesData.clear();
        }
        seriesData.add(data);

        if (swingWrapper == null) {
            // Create Chart
            chart = new XYChartBuilder().width(600).height(450).theme(ChartTheme.Matlab).title(title).build();
            chart.addSeries(seriesName, null, seriesData);
            chart.getStyler().setLegendPosition(LegendPosition.OutsideS);// ����legend��λ��Ϊ��ײ�
            chart.getStyler().setLegendLayout(LegendLayout.Horizontal);// ����legend�����з�ʽΪˮƽ����

            swingWrapper = new SwingWrapper<XYChart>(chart);
            frame = swingWrapper.displayChart();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// ��ֹ�رմ���ʱ�˳�����
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.setVisible(false);
                }
            });
        } else {
            // Update Chart
            chart.updateXYSeries(seriesName, null, seriesData, null);
            swingWrapper.repaintChart();
            frame.setVisible(true);
        }
    }
}
