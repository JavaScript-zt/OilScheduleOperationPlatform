package opt.easyjmetal.problem.schedule.util;

import opt.jmetal.problem.oil.canvas.gante.CanvasGante;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ChartFrame extends JFrame {
    // ���ڴ�С
    private final static int width = 1100;
    private final static int height = 480;

    private CanvasGante canvas;// canvas��ͼ��
    private JMenuBar menubar;// �˵���

    public ChartFrame() {
        setTitle("ԭ�Ͷ�����ϸ����ͼ");
        canvas = new CanvasGante();

        // �����ڶ�λ����Ļ�м�
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
        add(canvas);

        // �˵���
        menubar = new JMenuBar();

        JMenu menu = new JMenu("�ļ�(F)");
        menu.setMnemonic(KeyEvent.VK_F);    //���ÿ��ٷ��ʷ�

        JMenuItem item = new JMenuItem("����(S)", KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));// ��ݼ�
        item.addActionListener((e) -> {
            //��ͼ�񱣴�Ϊ�ļ�
            JFileChooser chooser = new JFileChooser();//�ļ�����Ի���
            chooser.setCurrentDirectory(new File("."));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File oFile = chooser.getSelectedFile();
                try {
                    //����ͼ���ļ�
                    savePic(oFile.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        menu.add(item);

        menubar.add(menu);
        setJMenuBar(menubar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                // �����С�仯���Զ��ػ�
                canvas.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // �����ƶ����Զ��ػ�
                canvas.repaint();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                // ������ֺ��Զ��ػ�
                canvas.repaint();
            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        setVisible(true);
    }

    public void updateCanvas(double[][] data) {
        canvas.setData(data);
        canvas.repaint();
    }

    /**
     * ����ͼ��
     *
     * @param filepath
     */
    public void savePic(String filepath) {
        Dimension imagesize = canvas.getSize();
        BufferedImage myImage = new BufferedImage(imagesize.width, imagesize.height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics1 = myImage.createGraphics();
        graphics1.setClip(new Rectangle(0, 0, imagesize.width,imagesize.height));
        canvas.drawGante(graphics1);
        graphics1.dispose();

        try {
            // ImageIO.write(myImage, "jpg", new File(filepath));
            // �����������
            ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
            writer.setOutput(new FileImageOutputStream(new File(filepath)));
            writer.write(myImage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
