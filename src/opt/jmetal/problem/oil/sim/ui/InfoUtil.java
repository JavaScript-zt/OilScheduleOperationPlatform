package opt.jmetal.problem.oil.sim.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * * @author Administrator �˹������÷���ʵ���������󣬵��� void show("����","����") ����. InfoUtil
 * tool * = new InfoUtil(); tool.show("����","����")
 */
public class InfoUtil {

    private TipWindow tw = null; // ��ʾ��

    private JPanel headPan = null;
    private JPanel feaPan = null;
    private JPanel btnPan = null;
    private JLabel title = null; // ��Ŀ����

    private JLabel head = null; // ��ɫ����

    private JLabel close = null; // �رհ�ť

    private JTextArea feature = null; // ����

    private JScrollPane jfeaPan = null;
    private JButton sure = null;
    private String titleT = null;
    private String word = null;
    private Desktop desktop = null;

    public void init() {
        // �½�300x180����Ϣ��ʾ��
        tw = new TipWindow(300, 180);
        headPan = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        feaPan = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPan = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        title = new JLabel("��ӭʹ�ñ�ϵͳ");
        head = new JLabel(titleT);
        close = new JLabel(" x");
        feature = new JTextArea(word);
        jfeaPan = new JScrollPane(feature);
        sure = new JButton("ȷ��");
        sure.setHorizontalAlignment(SwingConstants.CENTER);

        // ������ʾ��ı߿�,��Ⱥ���ɫ
        tw.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.white));
        title.setPreferredSize(new Dimension(260, 26));
        title.setVerticalTextPosition(JLabel.CENTER);
        title.setHorizontalTextPosition(JLabel.CENTER);
        title.setFont(new Font("����", Font.PLAIN, 12));
        title.setForeground(Color.black);

        close.setFont(new Font("Arial", Font.BOLD, 15));
        close.setPreferredSize(new Dimension(20, 20));
        close.setVerticalTextPosition(JLabel.CENTER);
        close.setHorizontalTextPosition(JLabel.CENTER);
        close.setCursor(new Cursor(12));
        close.setToolTipText("�ر�");

        head.setPreferredSize(new Dimension(250, 35));
        head.setVerticalTextPosition(JLabel.CENTER);
        head.setHorizontalTextPosition(JLabel.CENTER);
        head.setFont(new Font("����", Font.PLAIN, 14));
        head.setForeground(Color.black);

        feature.setEditable(false);
        feature.setForeground(Color.BLACK);
        feature.setFont(new Font("����", Font.PLAIN, 13));
        feature.setBackground(new Color(255, 255, 255));
        // �����ı����Զ�����
        feature.setLineWrap(true);

        jfeaPan.setPreferredSize(new Dimension(260, 100));
        jfeaPan.setBorder(null);
        jfeaPan.setBackground(Color.black);
        tw.setBackground(Color.white);

        // Ϊ�������ı��򣬼Ӹ��յ�JLabel������������ȥ
        JLabel jsp = new JLabel();
        jsp.setPreferredSize(new Dimension(300, 15));

        sure.setPreferredSize(new Dimension(60, 30));
        // ���ñ�ǩ�������
        sure.setCursor(new Cursor(12));
        // ����button���
        sure.setContentAreaFilled(false);
        sure.setBorder(BorderFactory.createRaisedBevelBorder());
        sure.setBackground(Color.gray);

        // headPan.add(title);
        headPan.add(head);
        headPan.add(close);

        feaPan.add(jsp);
        feaPan.add(jfeaPan);

        // feaPan.add(releaseLabel);

        btnPan.add(sure);

        headPan.setBackground(new Color(104, 141, 177));
        feaPan.setBackground(Color.white);
        btnPan.setBackground(Color.white);

        tw.add(headPan, BorderLayout.NORTH);
        tw.add(feaPan, BorderLayout.CENTER);
        tw.add(btnPan, BorderLayout.SOUTH);
    }

    public void handle() {
        // Ϊ���°�ť������Ӧ���¼�
        desktop = Desktop.getDesktop();
        sure.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                tw.close();
            }

            public void mouseEntered(MouseEvent e) {
                sure.setBorder(BorderFactory.createLineBorder(Color.gray));
            }

            public void mouseExited(MouseEvent e) {
                sure.setBorder(null);
            }
        });
        // ���Ͻǹرհ�ť�¼�
        close.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                tw.close();
            }

            public void mouseEntered(MouseEvent e) {
                close.setBorder(BorderFactory.createLineBorder(Color.gray));
            }

            public void mouseExited(MouseEvent e) {
                close.setBorder(null);
            }
        });
    }

    public void show(String titleT, String word) {

        this.titleT = titleT;
        this.word = word;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                init();
                handle();
                tw.setAlwaysOnTop(true);
                tw.setUndecorated(true);
                tw.setResizable(false);
                tw.setVisible(true);
                tw.run();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void close() {
        tw.close();
    }
}

class TipWindow extends JDialog {

    private static final long serialVersionUID = 8541659783234673950L;
    private static Dimension dim;
    private int x, y;
    private int width, height;
    private static Insets screenInsets;

    public TipWindow(int width, int height) {
        this.width = width;
        this.height = height;
        dim = Toolkit.getDefaultToolkit().getScreenSize();
        screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        x = (int) (dim.getWidth() - width - 3);
        y = (int) (dim.getHeight() - screenInsets.bottom - 3);
        initComponents();
    }

    public void run() {
        for (int i = 0; i <= height; i += 10) {
            try {
                this.setLocation(x, y - i);
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        // �˴���������ʵ������Ϣ��ʾ��6����Զ���ʧ
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        close();
    }

    private void initComponents() {
        this.setSize(width, height);
        this.setLocation(x, y);
        this.setBackground(Color.black);
    }

    public void close() {
        x = this.getX();
        y = this.getY();
        int ybottom = (int) dim.getHeight() - screenInsets.bottom;
        for (int i = 0; i <= ybottom - y; i += 10) {
            try {
                setLocation(x, y + i);
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        dispose();
    }

}