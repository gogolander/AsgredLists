/*
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unipd.astro;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class Main extends javax.swing.JPanel {

    private int selectedAction = 0;
    private boolean conflicts = false;
    private boolean showStep1 = true;
    private ImageList imageList;
    private String basePath;
    private ImageList list_obj;
    private ImageList list_lamp;
    private ImageList wlcalList;
    private ImageList fcalList;
    private ImageList backgroundList;
    private ImageList apallList;
    private ImageList imcopyList;
    private ImageList scombineList;
    private String irafPath = "";
    private Process python;
    private BufferedReader fromPython;
    private BufferedWriter toPython;

    public Main() {
        initComponents();
        myInitComponents();
        this.groupExplore.add(this.jRadioFitsList);
        this.groupExplore.add(this.jRadioButton2);
        this.jIrafHome.setText(this.irafPath);
        this.jLabel13.setText(String.valueOf(this.jWlcalThreshold.getValue()));
        this.jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                /**
                 * Color legend: 1. standards: HSV=217,100,100; RGB=0,96,255 2.
                 * objects: HSV=130,92,100; RGB=20,255,59 3. lamps:
                 * HSV=55,75,100; RGB=255,239,64 4. conflicts: HSV=37,100,100;
                 * RGB=255,156,0 5. flat: HSV=55,15,100; RGB=255,252,216
                 */
                float[] hsv = new float[3];
                if (imageList != null && imageList.size() > 0) {
                    Image image = imageList.get(row);
                    this.setForeground(Color.BLACK);
                    //Lamps
                    if (image.getType().equals("LAMP")) {
                        hsv = Color.RGBtoHSB(255, 243, 115, hsv);
                    } //Conflicts
                    else if (image.getType().equals("IMAGE") && (image.getLampName().isEmpty() || image.getStandardName().isEmpty())) {
                        hsv = Color.RGBtoHSB(255, 54, 0, hsv);
                        this.setForeground(Color.WHITE);
                    } //Objects and standards
                    else if (image.getType().equals("IMAGE") && !image.getLampName().isEmpty() && !image.getStandardName().isEmpty()) {
                        if (image.isStandard()) {
                            hsv = Color.RGBtoHSB(102, 161, 255, hsv);
                        } else {
                            hsv = Color.RGBtoHSB(20, 255, 59, hsv);
                        }
                    } //Flatfield
                    else if (image.getType().equals("FLATFIELD")) {
                        hsv = Color.RGBtoHSB(255, 252, 216, hsv);
                    }

                    setBackground(Color.getHSBColor(hsv[0], hsv[1], hsv[2]));
                }
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                return this;
            }
        });
        this.jTable1.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(new String[]{"IMAGE", "FLATFIELD", "LAMP"})));
//        this.jTable2.setDefaultRenderer(Object.class, this.jTable1.getDefaultRenderer(Object.class));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupAction = new javax.swing.ButtonGroup();
        groupExplore = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jInputPanel = new org.jdesktop.swingx.JXPanel();
        jStep1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jFitsListPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPath = new javax.swing.JTextField();
        jLoad = new javax.swing.JButton();
        jExplore = new javax.swing.JButton();
        jRadioFitsList = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jDirectoryPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel16 = new javax.swing.JLabel();
        jPath1 = new javax.swing.JTextField();
        jLoad1 = new javax.swing.JButton();
        jExplore1 = new javax.swing.JButton();
        jShowStep1 = new javax.swing.JToggleButton();
        jStep2 = new org.jdesktop.swingx.JXCollapsiblePane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jStep2Next = new javax.swing.JButton();
        jOptionsPanel = new org.jdesktop.swingx.JXPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jSolve = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jShowStep2 = new javax.swing.JToggleButton();
        jStep3 = new org.jdesktop.swingx.JXCollapsiblePane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jStep3Next = new org.jdesktop.swingx.JXButton();
        jShowStep3 = new javax.swing.JToggleButton();
        jStep4 = new org.jdesktop.swingx.JXCollapsiblePane();
        jPanel12 = new javax.swing.JPanel();
        jRadioListsOnly = new javax.swing.JRadioButton();
        jRadioListsAndOneScript = new javax.swing.JRadioButton();
        jRadioListsAndMultipleScripts = new javax.swing.JRadioButton();
        jRadioAllAndExec = new javax.swing.JRadioButton();
        jDoIt = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jShowStep4 = new javax.swing.JToggleButton();
        jPythonPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jConsole = new javax.swing.JTextArea();
        jCommand = new javax.swing.JTextField();
        jSend = new javax.swing.JButton();
        jAdvancedOptionsPanel = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jImcopyEnd = new javax.swing.JSpinner();
        jImcopyStart = new javax.swing.JSpinner();
        jPanel15 = new javax.swing.JPanel();
        jBackgroundStart = new javax.swing.JSpinner();
        jBackgroundEnd = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jWlcalThreshold = new javax.swing.JSlider();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jIrafHome = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jSelectIraf = new javax.swing.JButton();
        jSave = new javax.swing.JButton();

        jStep1.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 1"));

        jLabel1.setText("Select the fits_list which contains the list of images you want to reduce:");

        jLoad.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLoad.setText("Load data");
        jLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadActionPerformed(evt);
            }
        });

        jExplore.setText("Explore");
        jExplore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExploreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFitsListPanelLayout = new javax.swing.GroupLayout(jFitsListPanel.getContentPane());
        jFitsListPanel.getContentPane().setLayout(jFitsListPanelLayout);
        jFitsListPanelLayout.setHorizontalGroup(
            jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 957, Short.MAX_VALUE)
            .addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFitsListPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jFitsListPanelLayout.createSequentialGroup()
                            .addGap(344, 344, 344)
                            .addComponent(jLoad)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 514, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jFitsListPanelLayout.createSequentialGroup()
                            .addComponent(jPath)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jExplore))
                        .addGroup(jFitsListPanelLayout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        jFitsListPanelLayout.setVerticalGroup(
            jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
            .addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFitsListPanelLayout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jPath)
                        .addComponent(jExplore))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLoad)
                    .addGap(19, 19, 19)))
        );

        jRadioFitsList.setSelected(true);
        jRadioFitsList.setText("Select the fits_list");
        jRadioFitsList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioFitsListActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Select the directory");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jDirectoryPanel.setCollapsed(true);

        jLabel16.setText("Select the directory witch contains the images you want to reduce:");

        jLoad1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLoad1.setText("Load data");
        jLoad1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoad1ActionPerformed(evt);
            }
        });

        jExplore1.setText("Explore");
        jExplore1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExplore1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDirectoryPanelLayout = new javax.swing.GroupLayout(jDirectoryPanel.getContentPane());
        jDirectoryPanel.getContentPane().setLayout(jDirectoryPanelLayout);
        jDirectoryPanelLayout.setHorizontalGroup(
            jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 957, Short.MAX_VALUE)
            .addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jDirectoryPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jDirectoryPanelLayout.createSequentialGroup()
                            .addGap(344, 344, 344)
                            .addComponent(jLoad1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 514, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jDirectoryPanelLayout.createSequentialGroup()
                            .addComponent(jPath1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jExplore1))
                        .addGroup(jDirectoryPanelLayout.createSequentialGroup()
                            .addComponent(jLabel16)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        jDirectoryPanelLayout.setVerticalGroup(
            jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
            .addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jDirectoryPanelLayout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addComponent(jLabel16)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jPath1)
                        .addComponent(jExplore1))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLoad1)
                    .addGap(19, 19, 19)))
        );

        jLabel16.getAccessibleContext().setAccessibleName("Select the directory which contains the images you want to reduce:");

        javax.swing.GroupLayout jStep1Layout = new javax.swing.GroupLayout(jStep1.getContentPane());
        jStep1.getContentPane().setLayout(jStep1Layout);
        jStep1Layout.setHorizontalGroup(
            jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStep1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFitsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jStep1Layout.createSequentialGroup()
                        .addGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioFitsList)
                            .addComponent(jRadioButton2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jDirectoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jStep1Layout.setVerticalGroup(
            jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStep1Layout.createSequentialGroup()
                .addComponent(jRadioFitsList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFitsListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDirectoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jShowStep1.setText("Hide Step 1");
        this.jShowStep1.addActionListener(this.jStep1.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
        jShowStep1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowStep1ActionPerformed(evt);
            }
        });

        jStep2.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 2"));
        jStep2.setCollapsed(true);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, "", null}
            },
            new String [] {
                "Image", "Target name", "Type", "Is standard?", "Exp Time", "Lamp", "Standard"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Float.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setShowVerticalLines(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTable1PropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(55);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(200);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(60);
            jTable1.getColumnModel().getColumn(3).setPreferredWidth(55);
            jTable1.getColumnModel().getColumn(4).setPreferredWidth(40);
            jTable1.getColumnModel().getColumn(5).setPreferredWidth(55);
            jTable1.getColumnModel().getColumn(6).setPreferredWidth(55);
        }

        jLabel2.setText("Verify the data association and correct all conflicts:");

        jStep2Next.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jStep2Next.setText("Next step");
        jStep2Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStep2NextActionPerformed(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(101, 160, 255));
        jLabel7.setText("STANDARD");

        jPanel10.setBackground(new java.awt.Color(255, 54, 0));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel10.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        jSolve.setText("Try to resolve conflicts");
        jSolve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSolveActionPerformed(evt);
            }
        });

        jLabel5.setBackground(new java.awt.Color(255, 251, 216));
        jLabel5.setText("FLATFIELD");

        jLabel6.setBackground(new java.awt.Color(255, 243, 114));
        jLabel6.setText("LAMP");

        jPanel9.setBackground(new java.awt.Color(101, 160, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel9.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(255, 251, 216));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        jLabel9.setBackground(new java.awt.Color(255, 157, 0));
        jLabel9.setText("CONFLICT");

        jPanel11.setBackground(new java.awt.Color(20, 255, 59));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel11.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Color legend:");

        jLabel8.setBackground(new java.awt.Color(20, 255, 59));
        jLabel8.setText("OBJECT");

        jPanel8.setBackground(new java.awt.Color(255, 243, 114));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel8.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jOptionsPanelLayout = new javax.swing.GroupLayout(jOptionsPanel);
        jOptionsPanel.setLayout(jOptionsPanelLayout);
        jOptionsPanelLayout.setHorizontalGroup(
            jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jOptionsPanelLayout.createSequentialGroup()
                .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jOptionsPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jOptionsPanelLayout.createSequentialGroup()
                                .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jSolve))))
                    .addGroup(jOptionsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jOptionsPanelLayout.setVerticalGroup(
            jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jOptionsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel4)
                .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jOptionsPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jOptionsPanelLayout.createSequentialGroup()
                                .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jOptionsPanelLayout.createSequentialGroup()
                                        .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel5)
                                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6))
                                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7))
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jOptionsPanelLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jSolve, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jStep2Layout = new javax.swing.GroupLayout(jStep2.getContentPane());
        jStep2.getContentPane().setLayout(jStep2Layout);
        jStep2Layout.setHorizontalGroup(
            jStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStep2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStep2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(429, 429, 429)
                        .addComponent(jOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jStep2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 668, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(90, 90, 90)
                        .addComponent(jStep2Next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jStep2Layout.setVerticalGroup(
            jStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jStep2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStep2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jStep2Layout.createSequentialGroup()
                        .addComponent(jOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jStep2Next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jShowStep2.setSelected(true);
        jShowStep2.setText("Show Step 2");
        this.jShowStep2.addActionListener(this.jStep2.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
        jShowStep2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowStep2ActionPerformed(evt);
            }
        });

        jStep3.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 3"));
        jStep3.setCollapsed(true);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                { new Boolean(true), null,  new Boolean(true),  new Boolean(true),  new Boolean(true),  new Boolean(true),  new Boolean(true),  new Boolean(true),  new Boolean(true)}
            },
            new String [] {
                "Enabled", "Target", "prered2", "wlcal", "flcal", "background", "apall", "scombine", "imcopy"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jTable2.setShowVerticalLines(false);
        jTable2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTable2PropertyChange(evt);
            }
        });
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(1).setPreferredWidth(350);
            jTable2.getColumnModel().getColumn(2).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(3).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(4).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(5).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(6).setPreferredWidth(50);
            jTable2.getColumnModel().getColumn(7).setPreferredWidth(50);
        }
        jScrollPane2.setViewportView(jTable2);

        jLabel3.setText("Set what task to apply to images:");

        jStep3Next.setText("Next step");
        jStep3Next.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jStep3Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStep3NextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jStep3Layout = new javax.swing.GroupLayout(jStep3.getContentPane());
        jStep3.getContentPane().setLayout(jStep3Layout);
        jStep3Layout.setHorizontalGroup(
            jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStep3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStep3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jStep3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jStep3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jStep3Next, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jStep3Layout.setVerticalGroup(
            jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jStep3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStep3Next, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jShowStep3.setSelected(true);
        jShowStep3.setText("Show Step 3");
        jShowStep3.addActionListener(this.jStep3.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
        jShowStep3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowStep3ActionPerformed(evt);
            }
        });

        jStep4.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 4"));
        jStep4.setCollapsed(true);

        groupAction.add(jRadioListsOnly);
        jRadioListsOnly.setText("Generate only lists to be used within IRAF");
        jRadioListsOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioListsOnlyActionPerformed(evt);
            }
        });

        groupAction.add(jRadioListsAndOneScript);
        jRadioListsAndOneScript.setText("Geneare lists and only one PyRAF script for all objects");
        jRadioListsAndOneScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioListsAndOneScriptActionPerformed(evt);
            }
        });

        groupAction.add(jRadioListsAndMultipleScripts);
        jRadioListsAndMultipleScripts.setText("Generate lists and one PyRAF script per object");
        jRadioListsAndMultipleScripts.setEnabled(false);
        jRadioListsAndMultipleScripts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioListsAndMultipleScriptsActionPerformed(evt);
            }
        });

        groupAction.add(jRadioAllAndExec);
        jRadioAllAndExec.setText("Generate lists, one PyRAF script and execute it");
        jRadioAllAndExec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioAllAndExecActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioListsOnly)
                    .addComponent(jRadioListsAndOneScript)
                    .addComponent(jRadioAllAndExec)
                    .addComponent(jRadioListsAndMultipleScripts))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioListsOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioListsAndOneScript)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioListsAndMultipleScripts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioAllAndExec)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jDoIt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jDoIt.setText("Do it");
        jDoIt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDoItActionPerformed(evt);
            }
        });

        jLabel10.setText("Select what you want to obtain:");

        javax.swing.GroupLayout jStep4Layout = new javax.swing.GroupLayout(jStep4.getContentPane());
        jStep4.getContentPane().setLayout(jStep4Layout);
        jStep4Layout.setHorizontalGroup(
            jStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStep4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStep4Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jDoIt, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel10))
                .addGap(456, 456, 456))
        );
        jStep4Layout.setVerticalGroup(
            jStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jStep4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDoIt, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jShowStep4.setSelected(true);
        jShowStep4.setText("Show Step 4");
        jShowStep4.addActionListener(this.jStep4.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
        jShowStep4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowStep4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jInputPanelLayout = new javax.swing.GroupLayout(jInputPanel);
        jInputPanel.setLayout(jInputPanelLayout);
        jInputPanelLayout.setHorizontalGroup(
            jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jStep3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStep1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStep2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStep4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jShowStep1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jShowStep2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jShowStep3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jShowStep4, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jInputPanelLayout.setVerticalGroup(
            jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jStep1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jShowStep1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStep2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jShowStep2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStep3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jShowStep3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jStep4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jShowStep4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Data input and Control", jInputPanel);

        jConsole.setEditable(false);
        jConsole.setColumns(20);
        jConsole.setRows(5);
        jScrollPane3.setViewportView(jConsole);

        jCommand.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jCommandKeyTyped(evt);
            }
        });

        jSend.setText("Send");
        jSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPythonPanelLayout = new javax.swing.GroupLayout(jPythonPanel);
        jPythonPanel.setLayout(jPythonPanelLayout);
        jPythonPanelLayout.setHorizontalGroup(
            jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPythonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPythonPanelLayout.createSequentialGroup()
                        .addComponent(jCommand)
                        .addGap(18, 18, 18)
                        .addComponent(jSend)))
                .addContainerGap())
        );
        jPythonPanelLayout.setVerticalGroup(
            jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPythonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSend))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Execution", jPythonPanel);

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("imcopy"));

        jLabel17.setText("Spectrum starts at column:");

        jLabel18.setText("ends at:");

        jImcopyEnd.setModel(new javax.swing.SpinnerNumberModel(2048, 0, 2048, 1));
        jImcopyEnd.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jImcopyEndMouseWheelMoved(evt);
            }
        });

        jImcopyStart.setModel(new javax.swing.SpinnerNumberModel(50, 0, 2048, 1));
        jImcopyStart.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jImcopyStartMouseWheelMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addGap(18, 18, 18)
                .addComponent(jImcopyStart, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(jImcopyEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jImcopyStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jImcopyEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("background"));

        jBackgroundStart.setModel(new javax.swing.SpinnerNumberModel(1000, 0, 2048, 1));
        jBackgroundStart.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jBackgroundStartMouseWheelMoved(evt);
            }
        });

        jBackgroundEnd.setModel(new javax.swing.SpinnerNumberModel(1010, 0, 2048, 1));
        jBackgroundEnd.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jBackgroundEndMouseWheelMoved(evt);
            }
        });

        jLabel15.setText("and column");

        jLabel14.setText("Get background from column");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addComponent(jBackgroundStart, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addComponent(jBackgroundEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBackgroundStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBackgroundEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addContainerGap())
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("wlcal"));

        jWlcalThreshold.setPaintLabels(true);
        jWlcalThreshold.setPaintTicks(true);
        jWlcalThreshold.setSnapToTicks(true);
        jWlcalThreshold.setValue(10);
        jWlcalThreshold.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jWlcalThreshold.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jWlcalThresholdStateChanged(evt);
            }
        });
        jWlcalThreshold.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jWlcalThresholdMouseWheelMoved(evt);
            }
        });

        jLabel12.setText("wlcal RMS threshold");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGap(30, 30, 30)
                .addComponent(jWlcalThreshold, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(376, 376, 376))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jWlcalThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap())
        );

        jWlcalThreshold.setMinorTickSpacing(1);
        jWlcalThreshold.setMajorTickSpacing(10);

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("IRAF home"));

        jLabel11.setText("IRAF home");

        jSelectIraf.setText("Change");
        jSelectIraf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelectIrafActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(jIrafHome)
                .addGap(18, 18, 18)
                .addComponent(jSelectIraf)
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jIrafHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSelectIraf))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSave.setText("Save as Default");

        javax.swing.GroupLayout jAdvancedOptionsPanelLayout = new javax.swing.GroupLayout(jAdvancedOptionsPanel);
        jAdvancedOptionsPanel.setLayout(jAdvancedOptionsPanelLayout);
        jAdvancedOptionsPanelLayout.setHorizontalGroup(
            jAdvancedOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAdvancedOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAdvancedOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAdvancedOptionsPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jSave)))
                .addContainerGap())
        );
        jAdvancedOptionsPanelLayout.setVerticalGroup(
            jAdvancedOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAdvancedOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSave)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Advanced Options for PyRAF tasks", jAdvancedOptionsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSelectIrafActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelectIrafActionPerformed
        BufferedReader irafPathRead;
        try {
            irafPathRead = new BufferedReader(new FileReader(System.getProperty("user.home") + File.separator + ".irafPath"));
            try {
                this.irafPath = irafPathRead.readLine();
                if (irafPath == null) {
                    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
                    fileChooser.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return (pathname.getName().equals("login.cl") || pathname.isDirectory());
                        }

                        @Override
                        public String getDescription() {
                            return "Select the login.cl generated by the command \"mkiraf\"";
                        }
                    });

                    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        PrintWriter write = new PrintWriter(new FileWriter(System.getProperty("user.home") + File.separator + ".irafPath"));
                        write.print(fileChooser.getSelectedFile().getParent());
                        write.close();
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (FileNotFoundException ex) {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().equals("login.cl") || pathname.isDirectory());
                }

                @Override
                public String getDescription() {
                    return "Select the login.cl generated by the command \"mkiraf\"";
                }
            });

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                PrintWriter write = null;
                try {
                    write = new PrintWriter(new FileWriter(System.getProperty("user.home") + File.separator + ".irafPath"));
                    write.print(fileChooser.getSelectedFile().getParent());
                    write.close();
                } catch (IOException ex1) {
                    write.close();
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
                    JOptionPane.showMessageDialog(this, ex1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_jSelectIrafActionPerformed

    private void jWlcalThresholdMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jWlcalThresholdMouseWheelMoved
        this.jWlcalThreshold.setValue(this.jWlcalThreshold.getValue() - evt.getWheelRotation());
    }//GEN-LAST:event_jWlcalThresholdMouseWheelMoved

    private void jWlcalThresholdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jWlcalThresholdStateChanged
        this.jLabel13.setText(String.valueOf(this.jWlcalThreshold.getValue()));
    }//GEN-LAST:event_jWlcalThresholdStateChanged

    private void jBackgroundEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jBackgroundEndMouseWheelMoved
        if (((int) this.jBackgroundEnd.getValue() > 0 && evt.getWheelRotation() > 0) || ((int) this.jBackgroundEnd.getValue() < 2048 && evt.getWheelRotation() < 0)) {
            this.jBackgroundEnd.setValue((int) this.jBackgroundEnd.getValue() - evt.getWheelRotation());
        }
    }//GEN-LAST:event_jBackgroundEndMouseWheelMoved

    private void jBackgroundStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jBackgroundStartMouseWheelMoved
        if (((int) this.jBackgroundStart.getValue() > 0 && evt.getWheelRotation() > 0) || ((int) this.jBackgroundStart.getValue() < 2048 && evt.getWheelRotation() < 0)) {
            this.jBackgroundStart.setValue((int) this.jBackgroundStart.getValue() - evt.getWheelRotation());
        }
    }//GEN-LAST:event_jBackgroundStartMouseWheelMoved

    private void jImcopyStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jImcopyStartMouseWheelMoved
        if (((int) this.jImcopyStart.getValue() > 0 && evt.getWheelRotation() > 0) || ((int) this.jImcopyStart.getValue() < 2048 && evt.getWheelRotation() < 0)) {
            this.jImcopyStart.setValue((int) this.jImcopyStart.getValue() - evt.getWheelRotation());
        }
    }//GEN-LAST:event_jImcopyStartMouseWheelMoved

    private void jImcopyEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jImcopyEndMouseWheelMoved
        if (((int) this.jImcopyEnd.getValue() > 0 && evt.getWheelRotation() > 0) || ((int) this.jImcopyEnd.getValue() < 2048 && evt.getWheelRotation() < 0)) {
            this.jImcopyEnd.setValue((int) this.jImcopyEnd.getValue() - evt.getWheelRotation());
        }
    }//GEN-LAST:event_jImcopyEndMouseWheelMoved

    private void jSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSendActionPerformed
        if (python.isAlive()) {
            try {
                String message = this.jCommand.getText() + "\n";
                this.jConsole.append("> " + message);
                toPython.write(message);
                toPython.flush();
                this.jConsole.append("> " + fromPython.readLine() + "\n");
                if (message.equals("quit\n") || message.equals("break\n")) {
                    toPython.close();
                    fromPython.close();
                }
                this.jCommand.setText("");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jSendActionPerformed

    private void jCommandKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCommandKeyTyped
        if (evt.getKeyChar() == '\n') {
            this.jSend.doClick();
        }
    }//GEN-LAST:event_jCommandKeyTyped

    private void jDoItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDoItActionPerformed
        try {
            this.generateAllLists();
            switch (this.selectedAction) {
                case 0:
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Lists created.");
                JOptionPane.showMessageDialog(this, "Lists have been created correctly.", "All done", JOptionPane.INFORMATION_MESSAGE);
                break;
                case 1:
                this.writeOneGiantScript();
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Lists and one script created.");
                JOptionPane.showMessageDialog(this, "Lists and the PyRAF script have been created correctly.", "All done", JOptionPane.INFORMATION_MESSAGE);
                break;
                case 2:
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Unauthorized access to option \"Create lists and one script for each object\"");
                JOptionPane.showMessageDialog(this, "Not implemented yet! How did you get here?!", "Nothing done", JOptionPane.WARNING_MESSAGE);
                break;
                case 3:
                this.writeOneGiantScript();
                this.jTabbedPane1.setSelectedIndex(1);
                this.jConsole.setText("C:\\python27\\python.exe " + this.basePath + File.separator + "execPython.py\n");

                if (python != null && python.isAlive()) {
                    python.destroy();
                }

                python = Runtime.getRuntime().exec("C:\\python27\\python.exe");// + this.basePath + File.separator + "execPython.py");
                fromPython = new BufferedReader(new InputStreamReader(python.getInputStream()));
                toPython = new BufferedWriter(new OutputStreamWriter(python.getOutputStream()));
                Thread.sleep(100);
                if (python.getInputStream().available() != 0) {
                    byte[] buffer = new byte[python.getInputStream().available()];
                    python.getInputStream().read(buffer);
                    this.jConsole.append("\n> " + (new String(buffer)).trim().replaceAll("\n", "\n> ") + "\n");
                }

                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Lists and one script created. The script was executed.");
                JOptionPane.showMessageDialog(this, "Lists and the PyRAF script have been created correctly. The script was executed.", "All done", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jDoItActionPerformed

    private void jRadioAllAndExecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioAllAndExecActionPerformed
        this.selectedAction = 3;
    }//GEN-LAST:event_jRadioAllAndExecActionPerformed

    private void jRadioListsAndMultipleScriptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioListsAndMultipleScriptsActionPerformed
        this.selectedAction = 2;
    }//GEN-LAST:event_jRadioListsAndMultipleScriptsActionPerformed

    private void jRadioListsAndOneScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioListsAndOneScriptActionPerformed
        this.selectedAction = 1;
    }//GEN-LAST:event_jRadioListsAndOneScriptActionPerformed

    private void jRadioListsOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioListsOnlyActionPerformed
        this.selectedAction = 0;
    }//GEN-LAST:event_jRadioListsOnlyActionPerformed
        
    private void jSolveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSolveActionPerformed
        this.imageList.fixLamps();
        this.imageList.fixStandards();
        this.updateImagesTable();
    }//GEN-LAST:event_jSolveActionPerformed

    private void jTable1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable1PropertyChange
        int x = this.jTable1.getSelectedRow();
        int y = this.jTable1.getSelectedColumn();
        switch (y) {
            case 2:
            this.imageList.get(x).setType((String) this.jTable1.getValueAt(x, y));
            break;
            case 3:
            this.imageList.get(x).setIsStandard((Boolean) this.jTable1.getValueAt(x, y));
            break;
            case 5:
            this.imageList.get(x).setLampName((String) this.jTable1.getValueAt(x, y));
            break;
            case 6:
            this.imageList.get(x).setStandardName((String) this.jTable1.getValueAt(x, y));
        }
        this.jTable1.clearSelection();
    }//GEN-LAST:event_jTable1PropertyChange

    private void jExploreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExploreActionPerformed
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.getName().equals("fits_list") || pathname.isDirectory());
            }

            @Override
            public String getDescription() {
                return "Select fits_list generated by imhead";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.basePath = fileChooser.getSelectedFile().getParent();
            this.jPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jExploreActionPerformed

    private void jLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadActionPerformed
        try {
            if(this.jPath.equals(""))
                return;
            
            if (this.jPath.getText().startsWith("file://")) {
                this.jPath.setText(this.jPath.getText().replace("file://", "").trim());
                this.basePath = this.jPath.getText().replace("fits_list", "");
            }

            parseFile(this.jPath.getText());
            imageList.fixLamps();
            imageList.fixStandards();
            this.updateImagesTable();
            this.jShowStep2.doClick();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jLoadActionPerformed

    private void jShowStep1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowStep1ActionPerformed
        if(!this.jShowStep1.isSelected()) {
            this.jShowStep1.setText("Hide Step 1");
            this.jStep2.setCollapsed(true);
            this.jShowStep2.setSelected(true);
            this.jShowStep2.setText("Show Step 2");
            this.jStep3.setCollapsed(true);
            this.jShowStep3.setSelected(true);
            this.jShowStep3.setText("Show Step 3");
            this.jStep4.setCollapsed(true);
            this.jShowStep4.setSelected(true);
            this.jShowStep4.setText("Show Step 4");
        }
        else {
            this.jShowStep1.setText("Show Step 1");
        }
    }//GEN-LAST:event_jShowStep1ActionPerformed

    private void jShowStep2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowStep2ActionPerformed
        if(!this.jShowStep2.isSelected()) {
            this.jShowStep2.setText("Hide Step 2");
            this.jStep1.setCollapsed(true);
            this.jShowStep1.setSelected(true);
            this.jShowStep1.setText("Show Step 1");
            this.jStep3.setCollapsed(true);
            this.jShowStep3.setSelected(true);
            this.jShowStep3.setText("Show Step 3");
            this.jStep4.setCollapsed(true);
            this.jShowStep4.setSelected(true);
            this.jShowStep4.setText("Show Step 4");
        }
        else {
            this.jShowStep2.setText("Show Step 1");
        }
    }//GEN-LAST:event_jShowStep2ActionPerformed

    private void jShowStep3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowStep3ActionPerformed
        if(!this.jShowStep3.isSelected()) {
            this.jShowStep3.setText("Hide Step 3");
            this.jStep1.setCollapsed(true);
            this.jShowStep1.setSelected(true);
            this.jShowStep1.setText("Show Step 1");
            this.jStep2.setCollapsed(true);
            this.jShowStep2.setSelected(true);
            this.jShowStep2.setText("Show Step 2");
            this.jStep4.setCollapsed(true);
            this.jShowStep4.setSelected(true);
            this.jShowStep4.setText("Show Step 4");
        }
        else {
            this.jShowStep3.setText("Show Step 1");
        }
    }//GEN-LAST:event_jShowStep3ActionPerformed

    private void jShowStep4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowStep4ActionPerformed
        if(!this.jShowStep4.isSelected()) {
            this.jShowStep4.setText("Hide Step 4");
            this.jStep1.setCollapsed(true);
            this.jShowStep1.setSelected(true);
            this.jShowStep1.setText("Show Step 1");
            this.jStep2.setCollapsed(true);
            this.jShowStep2.setSelected(true);
            this.jShowStep2.setText("Show Step 2");
            this.jStep3.setCollapsed(true);
            this.jShowStep3.setSelected(true);
            this.jShowStep3.setText("Show Step 3");
        }
        else {
            this.jShowStep4.setText("Show Step 1");
        }
    }//GEN-LAST:event_jShowStep4ActionPerformed

    private void jStep2NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStep2NextActionPerformed
        this.jShowStep3.doClick();
        this.updateTasksTable();
    }//GEN-LAST:event_jStep2NextActionPerformed

    private void jTable2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable2PropertyChange
        int x = this.jTable2.getSelectedRow();
        if (x != -1) {
            int y = this.jTable2.getSelectedColumn();
            ImageList selected = this.imageList.getImagesWhoseTargetIs((String) this.jTable2.getModel().getValueAt(x, 1));
            String message = "";
            switch (y) {
                case 0:
                if (selected.containsConflicts()) {
                    message = "Some images relative to " + (String) this.jTable2.getModel().getValueAt(x, 1) + " contain conflicts: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave this target disabled.\nDo you want to enable it anyway?";
                }
                break;
                case 3:
                if (selected.lampsMissing()) {
                    message = "Some images relative to " + (String) this.jTable2.getModel().getValueAt(x, 1) + " are missing lamps: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave wlcal for this target disabled.\nDo you want to enable it anyway?";
                }
                break;
                case 4:
                if (selected.standardsMissing()) {
                    message = "Some images relative to " + (String) this.jTable2.getModel().getValueAt(x, 1) + " are missing standard stars: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave fcal for this target disabled.\nDo you want to enable this target anyway?";
                }
            }
            if (!message.equals("") && (boolean) this.jTable2.getModel().getValueAt(x, y)) {
                if (JOptionPane.showConfirmDialog(this, message, "Wait!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    if (y != 0) {
                        this.jTable2.getModel().setValueAt(true, x, 0);
                    }
                } else {
                    this.jTable2.getModel().setValueAt(false, x, y);
                }
            }
            this.jTable2.clearSelection();
        }
    }//GEN-LAST:event_jTable2PropertyChange

    private void jLoad1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoad1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jLoad1ActionPerformed

    private void jExplore1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExplore1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jExplore1ActionPerformed

    private void jRadioFitsListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioFitsListActionPerformed
        this.jFitsListPanel.setCollapsed(false);
        this.jDirectoryPanel.setCollapsed(true);
    }//GEN-LAST:event_jRadioFitsListActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        this.jFitsListPanel.setCollapsed(true);
        this.jDirectoryPanel.setCollapsed(false);
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jStep3NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStep3NextActionPerformed
        this.jShowStep4.doClick();
    }//GEN-LAST:event_jStep3NextActionPerformed
    
    public void parseFile(String filePath) throws FileNotFoundException, IOException {
        String line = "";
        imageList = new ImageList();
        BufferedReader fitsList = new BufferedReader(new FileReader(filePath));
        do {
            line = fitsList.readLine();
            //Ignore comments, empty lines and EOF
            if (line != null && !line.startsWith("#") && !line.equals("")) {
                imageList.add(Image.parseImage(line));
            }
        } while (line != null);
        fitsList.close();
    }

    public void updateImagesTable() {
        if (imageList == null || imageList.size() == 0) {
            return;
        }

        // Prepare the list of standard stars
        ArrayList<String> listStds = new ArrayList<>();
        for (Image image : imageList.getStandards()) {
            listStds.add(image.getFileName());
        }

        ActionListener autoHide = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                ((JComboBox) evt.getSource()).hidePopup();
                ((JComboBox) evt.getSource()).setSelectedIndex(-1);
            }
        };

        JComboBox comboStd = new JComboBox(listStds.toArray());
        comboStd.addActionListener(autoHide);

        // Prepare the list of lamps
        ArrayList<String> listLamps = new ArrayList<>();
        for (Image image : imageList.getLamps()) {
            listLamps.add(image.getFileName());
        }
        JComboBox lamps = new JComboBox(listLamps.toArray());
        lamps.addActionListener(autoHide);

        this.jTable1.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(lamps));
        this.jTable1.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboStd));

        if (((DefaultTableModel) this.jTable1.getModel()).getRowCount() > 0) {
            for (int i = ((DefaultTableModel) this.jTable1.getModel()).getRowCount() - 1; i > -1; i--) {
                ((DefaultTableModel) this.jTable1.getModel()).removeRow(i);
            }
        }

        for (Image image : imageList) {
            ((DefaultTableModel) this.jTable1.getModel()).addRow(new Object[]{
                image.getFileName(), image.getTargetName(), image.getType(), image.isStandard(),
                image.getExpTime(), image.getLampName(), image.getStandardName()});
        }
    }

    public void updateTasksTable() {
        if (imageList == null || imageList.size() == 0) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel)this.jTable2.getModel();
        if (model.getRowCount() > 0) {
            for (int i = model.getRowCount() - 1; i > -1; i--) {
                model.removeRow(i);
            }
        }
        
        for (String target : imageList.generateTargetsList()) {
            ImageList temp = imageList.getImagesWhoseTargetIs(target);
            Object[] row = new Object[] { !temp.containsConflicts(), target, true, !temp.lampsMissing(), !temp.standardsMissing(), true, true, true, true};
            model.addRow(row);
        }
        this.jTable2.setModel(model);
        this.jTable2.doLayout();
    }

    public void generateAllLists() {
        PrintWriter writer = null;
        try {
            list_obj = new ImageList();
            list_lamp = new ImageList();
            wlcalList = new ImageList();
            fcalList = new ImageList();
            backgroundList = new ImageList();
            apallList = new ImageList();
            imcopyList = new ImageList();
            scombineList = new ImageList();

            String lastTarget = "";
            //Generate lists to write
            for (int i = 0; i < this.jTable2.getModel().getRowCount(); i++) {
                if ((boolean) this.jTable2.getModel().getValueAt(i, 0)) {
                    //Get all images for the target
                    for (Image image : this.imageList.getImagesWhoseTargetIs((String) this.jTable2.getModel().getValueAt(i, 1))) {
                        //Apply prered2?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 2)) {
                            list_obj.add(image);
                            if (!list_lamp.contains(imageList.getImageWhoseFileNameIs(image.getLampName()))) {
                                list_lamp.add(imageList.getImageWhoseFileNameIs(image.getLampName()));
                            }
                        }
                        //Apply wlcal?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 3)) {
                            wlcalList.add(image);
                        }
                        //Apply fcal?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 4)) {
                            fcalList.add(image);
                        }
                        //Apply background?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 5)) {
                            backgroundList.add(image);
                        }
                        //Apply apall?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 6)) {
                            apallList.add(image);
                        }
                        //Apply scombine?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 7) && !lastTarget.equals(image.getTargetName())) {
                            scombineList.add(image);
                        }
                        //Apply imcopy?
                        if ((boolean) this.jTable2.getModel().getValueAt(i, 8) && !lastTarget.equals(image.getTargetName())) {
                            imcopyList.add(image);
                        }
                    }
                }
            }
            String temp = "";
            //Write them down
            writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "list_obj"));
            for (Image image : list_obj) {
                temp += image.getFileName() + "\t" + image.getLampName() + "\n";
            }
            writer.print(temp.trim());
            writer.close();
            temp = "";
            writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "list_lamps"));
            for (Image image : list_lamp) {
                temp += image.getFileName() + "\n";
            }
            writer.print(temp.trim());
            writer.close();
            writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "list_flat"));
            writer.print(imageList.generateFlatList());
            writer.close();

            // Generate the list of IMA*.wl.fits
            for (String target : wlcalList.generateTargetsList()) {
                temp = target;
                if (target.contains("+")) {
                    temp = temp.replaceAll("+", "p");
                }
                if (target.contains("-")) {
                    temp = temp.replaceAll("-", "m");
                }
                writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "wl" + temp));
                temp = "";
                for (String imageName : wlcalList.getImagesFileNameWhoseTargetIs(target)) {
                    temp += wlcalList.appendExtension(imageName, "wl") + "\n";
                }
                writer.print(temp.trim());
                writer.close();
            }

            // Generate the list of IMA*.fc.fits
            // This list is used as input for the background task
            for (String target : fcalList.generateTargetsList()) {
                temp = target;
                if (target.contains("+")) {
                    temp = temp.replaceAll("+", "p");
                }
                if (target.contains("-")) {
                    temp = temp.replaceAll("-", "m");
                }
                writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "fc" + temp));
                temp = "";
                for (String imageName : fcalList.getImagesFileNameWhoseTargetIs(target)) {
                    temp += fcalList.appendExtension(imageName, "fc") + "\n";
                }
                writer.print(temp.trim());
                writer.close();
            }

            // Generate the list of IMA*.bg.fits
            for (String target : backgroundList.generateTargetsList()) {
                temp = target;
                if (target.contains("+")) {
                    temp = temp.replaceAll("+", "p");
                }
                if (target.contains("-")) {
                    temp = temp.replaceAll("-", "m");
                }
                writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "bg" + temp));
                temp = "";
                for (String imageName : backgroundList.getImagesFileNameWhoseTargetIs(target)) {
                    temp += backgroundList.appendExtension(imageName, "bg") + "\n";
                }
                writer.print(temp.trim());
                writer.close();
            }

            // Generate the list of IMA*.md.fits
            for (String target : apallList.generateTargetsList()) {
                temp = target;
                if (target.contains("+")) {
                    temp = temp.replaceAll("+", "p");
                }
                if (target.contains("-")) {
                    temp = temp.replaceAll("-", "m");
                }
                writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "md" + temp));
                temp = "";
                for (String imageName : apallList.getImagesFileNameWhoseTargetIs(target)) {
                    temp += apallList.appendExtension(imageName, "md") + "\n";
                }
                writer.print(temp.trim());
                writer.close();
            }

            // Generate the list of IMA*.fc.fits
            // This list is used as input for standard parameter of fcal task
            for (Image standard : fcalList.getStandards()) {
                // Use file name instead of standard name to avoid confusion: the
                // same standard used twice during the night MUST be treated as two
                // different stars
                temp = "";
                writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "std" + standard.getFileName()));
                for (Image imageName : fcalList.getImagesWhoseStandardIs(standard.getFileName())) {
                    temp += fcalList.appendExtension(imageName.getFileName(), "fc") + "\n";
                }
                writer.print(temp.trim());
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (writer != null) {
            writer.close();
        }
    }

    public void writeOneGiantScript() {
        PrintWriter writer = null;
        try {
            // Let's generate the PyRAF script
            writer = new PrintWriter(new FileWriter(this.basePath + File.separator + "execAsgred.py"));
            writer.println("#!/usr/bin/env python");
            writer.println("import os");
            writer.println("import sys");
            if (!this.irafPath.equals("")) {
                writer.println("os.chdir(\"" + this.irafPath + "\")");
            }
            writer.println("from pyraf import iraf");
            writer.println("iraf.epar(\"display\")");
            // exec prered2
            writer.println("iraf.asgred.prered2(flat=\"list_flat\", comp=\"list_lamps\", object=\"list_obj\", order=11)");
            // exec wlcal
            writer.println("iraf.asgred.wlcal(input=\"list_obj\", refer=\"" + imageList.getFirstLamp() + "\")");
            // exec fcal
            if (fcalList != null) {
                for (Image standard : fcalList.getStandards()) {
                    writer.println("iraf.asgred.fcal(obj=\"std" + standard.getFileName() + "\", stand=\"" + standard.getFileName() + "\", dir=\"onedstds$" + Image.standardList.getCatalogue(standard.getTargetName()) + "\")");
                }
            }

            // exec background
            if (backgroundList != null) {
                for (String target : backgroundList.generateTargetsList()) {
                    String temp = target;
                    if (target.contains("+")) {
                        temp = target.replaceAll("+", "p");
                    }
                    if (temp.contains("-")) {
                        temp = temp.replaceAll("-", "m");
                    }
                    writer.println("iraf.asgred.background(input=\"fc" + temp + "\", output=\"bg" + temp + "\")");
                }
            }
            // exec apall
            if (apallList != null) {
                for (String target : apallList.generateTargetsList()) {
                    String temp = target;
                    if (target.contains("+")) {
                        temp = target.replaceAll("+", "p");
                    }
                    if (temp.contains("-")) {
                        temp = temp.replaceAll("-", "m");
                    }
                    writer.println("iraf.asgred.apall(input=\"@bg" + temp + "\", output=\"@md" + temp + "\")");
                }
            }
            // exec scombine
            if (scombineList != null) {
                for (String target : scombineList.generateTargetsList()) {
                    String temp = target;
                    if (target.contains("+")) {
                        temp = target.replaceAll("+", "p");
                    }
                    if (temp.contains("-")) {
                        temp = temp.replaceAll("-", "m");
                    }
                    writer.println("iraf.asgred.scombine(input=\"md" + temp + "\", output=\"" + temp + ".md\")");
                }
            }
            String start = this.jImcopyStart.getValue().toString();
            String end = this.jImcopyEnd.getValue().toString();
            // exec imcopy
            if (imcopyList != null) {
                for (String target : imcopyList.generateTargetsList()) {
                    String temp = target;
                    if (target.contains("+")) {
                        temp = target.replaceAll("+", "p");
                    }
                    if (temp.contains("-")) {
                        temp = temp.replaceAll("-", "m");
                    }

                    writer.println("iraf.asgred.imcopy(input=\"" + temp + ".md[" + start + ":" + end + "]\", output=\"" + temp + ".obj\")");
                }
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (writer != null) {
            writer.close();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup groupAction;
    private javax.swing.ButtonGroup groupExplore;
    private javax.swing.JPanel jAdvancedOptionsPanel;
    private javax.swing.JSpinner jBackgroundEnd;
    private javax.swing.JSpinner jBackgroundStart;
    private javax.swing.JTextField jCommand;
    private javax.swing.JTextArea jConsole;
    private org.jdesktop.swingx.JXCollapsiblePane jDirectoryPanel;
    private javax.swing.JButton jDoIt;
    private javax.swing.JButton jExplore;
    private javax.swing.JButton jExplore1;
    private org.jdesktop.swingx.JXCollapsiblePane jFitsListPanel;
    private javax.swing.JSpinner jImcopyEnd;
    private javax.swing.JSpinner jImcopyStart;
    private org.jdesktop.swingx.JXPanel jInputPanel;
    private javax.swing.JTextField jIrafHome;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton jLoad;
    private javax.swing.JButton jLoad1;
    private org.jdesktop.swingx.JXPanel jOptionsPanel;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTextField jPath;
    private javax.swing.JTextField jPath1;
    private javax.swing.JPanel jPythonPanel;
    private javax.swing.JRadioButton jRadioAllAndExec;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioFitsList;
    private javax.swing.JRadioButton jRadioListsAndMultipleScripts;
    private javax.swing.JRadioButton jRadioListsAndOneScript;
    private javax.swing.JRadioButton jRadioListsOnly;
    private javax.swing.JButton jSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton jSelectIraf;
    private javax.swing.JButton jSend;
    private javax.swing.JToggleButton jShowStep1;
    private javax.swing.JToggleButton jShowStep2;
    private javax.swing.JToggleButton jShowStep3;
    private javax.swing.JToggleButton jShowStep4;
    private javax.swing.JButton jSolve;
    private org.jdesktop.swingx.JXCollapsiblePane jStep1;
    private org.jdesktop.swingx.JXCollapsiblePane jStep2;
    private javax.swing.JButton jStep2Next;
    private org.jdesktop.swingx.JXCollapsiblePane jStep3;
    private org.jdesktop.swingx.JXButton jStep3Next;
    private org.jdesktop.swingx.JXCollapsiblePane jStep4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JSlider jWlcalThreshold;
    // End of variables declaration//GEN-END:variables

    private void myInitComponents() {
        
    }
}
