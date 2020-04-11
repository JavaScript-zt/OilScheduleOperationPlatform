package opt.jmetal.problem.oil.sim.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class JTableHelper {

    public static DefaultTableModel showTableWithNo(String filePath, boolean containColumeNames) throws IOException {
        return showTable(filePath, containColumeNames, true);
    }

    public static DefaultTableModel showTableWithnotNo(String filePath, boolean containColumeNames) throws IOException {
        return showTable(filePath, containColumeNames, false);
    }

    public static DefaultTableModel showTable(String filePath, boolean containColumeNames, boolean containNo)
            throws IOException {
        DefaultTableModel mm = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            // ���õ�Ԫ�񲻿ɱ༭
            public boolean isCellEditable(int rowIndex, int ColIndex) {
                return false;
            }
        };

        FileReader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
        String line;

        if ((line = br.readLine()) != null) {
            // ����
            int numOfColumes = 0;
            if (containNo) {
                numOfColumes = line.split(" |,").length + 1;
            } else {
                numOfColumes = line.split(" |,").length;
            }

            if (containColumeNames) {
                // ���������ڣ����ȡ����
                String[] columnNames = line.split(" |,");
                if (containNo) {
                    String[] newColumnNames = new String[columnNames.length + 1];
                    newColumnNames[0] = "No";
                    for (int i = 0; i < columnNames.length; i++) {
                        newColumnNames[i + 1] = columnNames[i];
                    }
                    mm.setColumnIdentifiers(newColumnNames);
                } else {
                    mm.setColumnIdentifiers(columnNames);
                }
                // ��ȡ��һ��
                line = br.readLine();
            } else {
                // �����������ڣ���������ʱ��������������
                if (containNo) {
                    String[] tmpColumnNames = new String[numOfColumes + 1];
                    tmpColumnNames[0] = "No";
                    for (int i = 0; i < numOfColumes; i++) {
                        tmpColumnNames[i + 1] = (i + 1) + "";
                    }
                    mm.setColumnIdentifiers(tmpColumnNames);
                } else {
                    String[] tmpColumnNames = new String[numOfColumes];
                    for (int i = 0; i < numOfColumes; i++) {
                        tmpColumnNames[i] = (i + 1) + "";
                    }
                    mm.setColumnIdentifiers(tmpColumnNames);
                }
            }

            do {
                String data[] = line.split(" |,");
                Vector<String> v = null;
                if (containNo) {
                    v = new Vector<>(numOfColumes);
                    for (int i = 0; i < numOfColumes; i++) {
                        if (i == 0) {
                            v.add(i, (mm.getDataVector().size() + 1) + "");
                        } else {
                            v.add(i, data[i - 1]);
                        }
                    }
                } else {
                    v = new Vector<>(data.length);
                    for (int i = 0; i < data.length; i++) {
                        v.add(i, data[i]);
                    }
                }
                mm.addRow(v);
            } while ((line = br.readLine()) != null);
        }
        // �رն�д��
        br.close();
        reader.close();

        return mm;
    }

    /**
     * ����ĳЩ�е���ɫ
     *
     * @param table ���
     * @param flags �Ƿ�������ʾ
     */
    public static void setRowsColor(JTable table, boolean[] flags) {
        try {
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                // ��дgetTableCellRendererComponent ����
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {

                    if (flags[row]) {
                        setBackground(Color.RED);
                        setForeground(Color.WHITE);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    }

                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            };
            // ��ÿ�е�ÿһ����Ԫ��
            int columnCount = table.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                table.getColumn(table.getColumnName(i)).setCellRenderer(dtcr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ������ݾ���
     *
     * @param table
     */
    public static void setTableColumnCenter(JTable table) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, r);
    }

    /**
     * ���ñ���ĳһ�еı���ɫ
     *
     * @param table
     */
    public static void setOneRowBackgroundColor(JTable table, int rowIndex, Color color) {
        try {
            // ��ȡ��ʼʱ�̹��͹޵�״̬
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DefaultTableCellRenderer tcr;

                    if (rowIndex != 0) {
                        tcr = new DefaultTableCellRenderer() {
                            private static final long serialVersionUID = 1L;

                            public Component getTableCellRendererComponent(JTable table, Object value,
                                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                                if (row == rowIndex - 1) {
                                    setBackground(color);
                                    setForeground(Color.WHITE);
                                } else if (row > rowIndex - 1) {
                                    setBackground(null);
                                    setForeground(null);
                                } else {
                                    setBackground(null);
                                    setForeground(null);
                                }

                                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                        column);
                            }
                        };
                    } else {
                        tcr = new DefaultTableCellRenderer() {
                            private static final long serialVersionUID = 1L;

                            public Component getTableCellRendererComponent(JTable table, Object value,
                                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                                setBackground(Color.WHITE);
                                setForeground(Color.BLACK);

                                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                        column);
                            }
                        };
                    }

                    int columnCount = table.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
                    }

                    table.repaint();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ����swing�е�jtable
     *
     * @param table
     * @param mm
     */
    public static void showTableInSwing(JTable table, TableModel mm) {
        // ��Ҫ��UI�߳�����²���UI������SwingUtilities���ҵ�UI�̲߳�ִ�и���UI����
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                table.setModel(mm);
            }
        });
    }
}
