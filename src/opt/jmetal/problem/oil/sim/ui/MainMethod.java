package opt.jmetal.problem.oil.sim.ui;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;

public class MainMethod {
    public static MainFrame frame = null;

    public static void main(String[] args) {

        // ָ��log4j2.xml�ļ���λ��
        ConfigurationSource source;
        String relativePath = "log4j2.xml";
        File log4jFile = new File(relativePath);
        try {
            if (log4jFile.exists()) {
                source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.initialize(null, source);
            } else {
                System.out.println("loginit failed");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // ��ʼ��GUI���棬����Ȩ�����û�
        frame = new MainFrame("Լ����Ŀ���Ż�");
    }
}
