/**
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU General Public License as published by
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
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;

import javax.swing.filechooser.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import unipd.astro.entity.FlatfieldImage;
import unipd.astro.entity.ImageEntity;
import unipd.astro.entity.LampImage;
import unipd.astro.entity.Observation;
import unipd.astro.entity.ScienceImage;
import unipd.astro.entity.StandardEntity;
import unipd.astro.entity.StandardImage;
import unipd.astro.service.DataService;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JTextPane;

/**
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
public class Main extends javax.swing.JPanel {
	private static Logger log = Logger.getLogger(Main.class.getName());
	private int selectedAction = 0;
	private HashMap<String, Integer> jTable1Cols = new HashMap<>();
	private HashMap<String, Integer> jTable2Cols = new HashMap<>();
	private DataService dataService;
	private HashMap<String, List<String>> generatedList;
	private List<ImageEntity> images; // used just for jTable1 cell renderer
										// purposes
	private Style In, Out, Error;
	private List<String> scriptsList;
	private String basePath;
	private PythonRunnable process;
	private AsyncCallback callback = new AsyncCallback() {
		@Override
		public void OnResponseReceived(String response) {
			try {
				((DefaultCaret) jConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\t" + response + "\n", Out);
				if (response.contains("rms=")) {
					int index = response.lastIndexOf("rms=") + 4;
					int startIdx = index;
					while (response.charAt(index) != '.')
						index++;
					index++;
					while ((int) response.charAt(index) < 58 && (int) response.charAt(index) > 47)
						index++;
					double rms = Double.parseDouble(response.substring(startIdx, index));
					if (rms < (Double.parseDouble(dataService.getProperty("iraf.wlcal.rms_threshold")) / 100D)) {
						jCommand.setText("y");
						jSend.doClick();
					}
				}
			} catch (BadLocationException e) {
				log.fatal(e);
			}
		}

		@Override
		public void OnScriptTerminated() {
			try {
				((DefaultCaret) jConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(), "◀\tterminated.\n",
						Out);
			} catch (BadLocationException e) {
				log.fatal(e);
			}
			if (scriptsList != null && scriptsList.size() > 0) {
				scriptsList.remove(0);
				if (scriptsList.size() > 0)
					process.startScript(Paths.get(basePath + "/" + scriptsList.get(0)).toString(), callback);
			}
		}

		@Override
		public void OnErrorReceived(String message) {
			try {
				((DefaultCaret) jConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\t" + message + "\n", Error);
			} catch (BadLocationException e) {
				log.fatal(e);
			}
		}

		@Override
		public void OnMessageSent(String message) {
			try {
				((DefaultCaret) jConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"▶\t" + message + "\n", In);
			} catch (BadLocationException e) {
				log.fatal(e);
			}
		}
	};

	public Main() {
		initComponents();
		finalInitComponents();
	}

	public void initDatabase() {
		log.info("Initializing local dataService instance...");
		SplashScreen.getInstance().setProgress("Loading standard atlas...", 75);
		this.dataService = DataService.getInstance();
		log.info("Parsing standard.list...");
		String line = "";
		try {
			BufferedReader standardFile = new BufferedReader(
					new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("standard.list")));
			ArrayList<StandardEntity> entities = new ArrayList<>();
			do {
				line = standardFile.readLine();
				log.info("Parsing \"" + line + "\"");
				// Ignore comments, empty lines and EOF
				if (line != null && !line.startsWith("#") && !line.equals("")) {
					entities.add(StandardEntity.parseEntity(line));
				}
			} while (line != null);
			log.info("Loading the atlas to the database...");
			dataService.getStandardAtlas().save(entities);
			SplashScreen.getInstance().setProgress("Ready.", 100);
			log.info("StandardAtlas initialized.");
		} catch (Exception ex) {
			log.fatal(ex.getMessage(), ex);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		jPrintTODO = new JButton();
		jViewScripts = new JButton();
		jRunScripts = new JButton();
		jCheckStartFromScrap = new JCheckBox();
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
		jShowStep1.setSelected(true);
		groupSteps.add(jShowStep1);
		jStep2 = new org.jdesktop.swingx.JXCollapsiblePane();
		jStep2.setCollapsed(true);
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
		groupSteps.add(jShowStep2);
		jStep3 = new org.jdesktop.swingx.JXCollapsiblePane();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTable2 = new javax.swing.JTable();
		jLabel3 = new javax.swing.JLabel();
		jStep3Next = new org.jdesktop.swingx.JXButton();
		jShowStep3 = new javax.swing.JToggleButton();
		groupSteps.add(jShowStep3);
		jStep4 = new org.jdesktop.swingx.JXCollapsiblePane();
		this.jStep4.setCollapsed(true);
		jPanel12 = new javax.swing.JPanel();
		jRadioListsOnly = new javax.swing.JRadioButton();
		jRadioListsOnly.setSelected(true);
		jRadioListsAndOneScript = new javax.swing.JRadioButton();
		jRadioListsAndMultipleScripts = new javax.swing.JRadioButton();
		jDoIt = new javax.swing.JButton();
		jLabel10 = new javax.swing.JLabel();
		jShowStep4 = new javax.swing.JToggleButton();
		groupSteps.add(jShowStep4);
		jPythonPanel = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jConsole = new JTextPane();
		float[] background = Color.RGBtoHSB(76, 76, 76, null);
		jConsole.setBackground(Color.getHSBColor(background[0], background[1], background[2]));
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
		jSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				jSaveMouseClicked(e);
			}
		});

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
		jFitsListPanelLayout.setHorizontalGroup(jFitsListPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE)
				.addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jFitsListPanelLayout.createSequentialGroup().addContainerGap()
								.addGroup(jFitsListPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jFitsListPanelLayout.createSequentialGroup().addGap(344, 344, 344)
												.addComponent(jLoad)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
														514, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(jFitsListPanelLayout.createSequentialGroup().addComponent(jPath)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jExplore))
										.addGroup(jFitsListPanelLayout.createSequentialGroup().addComponent(jLabel1)
												.addGap(0, 0, Short.MAX_VALUE)))
								.addContainerGap())));
		jFitsListPanelLayout.setVerticalGroup(jFitsListPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 125, Short.MAX_VALUE)
				.addGroup(jFitsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jFitsListPanelLayout.createSequentialGroup().addGap(18, 18, 18).addComponent(jLabel1)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jFitsListPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jPath).addComponent(jExplore))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jLoad).addGap(19, 19, 19))));

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
		jDirectoryPanelLayout.setHorizontalGroup(jDirectoryPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE)
				.addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jDirectoryPanelLayout.createSequentialGroup().addContainerGap()
								.addGroup(jDirectoryPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jDirectoryPanelLayout.createSequentialGroup().addGap(344, 344, 344)
												.addComponent(jLoad1)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
														514, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(jDirectoryPanelLayout.createSequentialGroup().addComponent(jPath1)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jExplore1))
										.addGroup(jDirectoryPanelLayout.createSequentialGroup().addComponent(jLabel16)
												.addGap(0, 0, Short.MAX_VALUE)))
								.addContainerGap())));
		jDirectoryPanelLayout
				.setVerticalGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 125, Short.MAX_VALUE)
						.addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jDirectoryPanelLayout.createSequentialGroup().addGap(18, 18, 18)
										.addComponent(jLabel16)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jDirectoryPanelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jPath1).addComponent(jExplore1))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLoad1).addGap(19, 19, 19))));

		jLabel16.getAccessibleContext()
				.setAccessibleName("Select the directory which contains the images you want to reduce:");

		javax.swing.GroupLayout jStep1Layout = new javax.swing.GroupLayout(jStep1.getContentPane());
		jStep1.getContentPane().setLayout(jStep1Layout);
		jStep1Layout.setHorizontalGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jStep1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jFitsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(jStep1Layout.createSequentialGroup()
								.addGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jRadioFitsList).addComponent(jRadioButton2))
								.addGap(0, 0, Short.MAX_VALUE)).addComponent(jDirectoryPanel,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap()));
		jStep1Layout.setVerticalGroup(jStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jStep1Layout.createSequentialGroup().addComponent(jRadioFitsList)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jFitsListPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jRadioButton2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jDirectoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jShowStep1.setText("Showing step 1");
		this.jShowStep1
				.addActionListener(this.jStep1.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
		jShowStep1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jShowStep1ActionPerformed(evt);
			}
		});

		jStep2.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 2"));

		jTable1.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null, null, null, null, null } }, new String[] { "Enabled",
						"Image", "Target name", "Type", "Is standard?", "Exp Time", "Lamp", "Standard" }) {
			Class[] types = new Class[] { java.lang.Boolean.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class, java.lang.Boolean.class, java.lang.Float.class, java.lang.String.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { true, false, false, true, true, false, true, true };

			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		for (int i = 0; i < jTable1.getColumnCount(); i++)
			jTable1Cols.put(jTable1.getModel().getColumnName(i), i);
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
			jTable1.getColumnModel().getColumn(0).setPreferredWidth(60);
			jTable1.getColumnModel().getColumn(1).setPreferredWidth(60);
			jTable1.getColumnModel().getColumn(2).setPreferredWidth(200);
			jTable1.getColumnModel().getColumn(3).setPreferredWidth(60);
			jTable1.getColumnModel().getColumn(4).setPreferredWidth(60);
			jTable1.getColumnModel().getColumn(5).setPreferredWidth(55);
			jTable1.getColumnModel().getColumn(6).setPreferredWidth(60);
			jTable1.getColumnModel().getColumn(7).setPreferredWidth(60);
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

		jPanel10.setBackground(new java.awt.Color(240, 40, 34));
		jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel10.setPreferredSize(new java.awt.Dimension(14, 14));

		javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
		jPanel10.setLayout(jPanel10Layout);
		jPanel10Layout.setHorizontalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));
		jPanel10Layout.setVerticalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));

		jSolve.setText("<html><center>Try to resolve<br>conflicts</center></html>");
		jSolve.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jSolveActionPerformed(evt);
			}
		});

		jLabel5.setBackground(new java.awt.Color(255, 251, 216));
		jLabel5.setText("FLATFIELD");

		jLabel6.setBackground(new java.awt.Color(255, 243, 114));
		jLabel6.setText("LAMP");

		jPanel9.setBackground(new java.awt.Color(114, 159, 207));
		jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel9.setPreferredSize(new java.awt.Dimension(14, 14));

		javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
		jPanel9.setLayout(jPanel9Layout);
		jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));
		jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));

		jPanel6.setBackground(new java.awt.Color(255, 251, 216));
		jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel6.setPreferredSize(new java.awt.Dimension(14, 14));

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));

		jLabel9.setBackground(new java.awt.Color(255, 157, 0));
		jLabel9.setText("CONFLICT");

		jPanel11.setBackground(new java.awt.Color(149, 226, 158));
		jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel11.setPreferredSize(new java.awt.Dimension(14, 14));

		javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
		jPanel11.setLayout(jPanel11Layout);
		jPanel11Layout.setHorizontalGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));
		jPanel11Layout.setVerticalGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));

		jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		jLabel4.setText("Color legend:");

		jLabel8.setBackground(new java.awt.Color(20, 255, 59));
		jLabel8.setText("OBJECT");

		jPanel8.setBackground(new java.awt.Color(255, 243, 114));
		jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel8.setPreferredSize(new java.awt.Dimension(14, 14));

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));
		jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 12, Short.MAX_VALUE));

		javax.swing.GroupLayout jOptionsPanelLayout = new javax.swing.GroupLayout(jOptionsPanel);
		jOptionsPanel.setLayout(jOptionsPanelLayout);
		jOptionsPanelLayout
				.setHorizontalGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jOptionsPanelLayout.createSequentialGroup().addGroup(jOptionsPanelLayout
								.createParallelGroup(
										javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(
										jOptionsPanelLayout.createSequentialGroup().addGap(20, 20, 20)
												.addGroup(
														jOptionsPanelLayout
																.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.TRAILING)
																.addComponent(jLabel5).addComponent(jLabel6)
																.addComponent(jLabel7).addComponent(jLabel8)
																.addComponent(jLabel9))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jOptionsPanelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGroup(jOptionsPanelLayout.createSequentialGroup()
														.addGroup(jOptionsPanelLayout
																.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING)
																.addComponent(jPanel11,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addComponent(jPanel8,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addComponent(jPanel9,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGap(18, 18, 18).addComponent(jSolve))))
								.addGroup(jOptionsPanelLayout.createSequentialGroup().addContainerGap().addComponent(
										jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jOptionsPanelLayout.setVerticalGroup(jOptionsPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jOptionsPanelLayout.createSequentialGroup().addGap(0, 0, 0).addComponent(jLabel4)
						.addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jOptionsPanelLayout.createSequentialGroup()
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jOptionsPanelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
												.addGroup(jOptionsPanelLayout.createSequentialGroup()
														.addGroup(jOptionsPanelLayout
																.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.TRAILING)
																.addGroup(jOptionsPanelLayout.createSequentialGroup()
																		.addGroup(jOptionsPanelLayout
																				.createParallelGroup(
																						javax.swing.GroupLayout.Alignment.TRAILING)
																				.addComponent(jLabel5)
																				.addComponent(jPanel6,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(jLabel6))
																.addComponent(jPanel8,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel7))
												.addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jOptionsPanelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel8)))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jOptionsPanelLayout.createSequentialGroup().addGap(26, 26, 26).addComponent(
												jSolve, javax.swing.GroupLayout.PREFERRED_SIZE, 54,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));

		javax.swing.GroupLayout jStep2Layout = new javax.swing.GroupLayout(jStep2.getContentPane());
		jStep2Layout.setHorizontalGroup(jStep2Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jStep2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jStep2Layout.createParallelGroup(Alignment.LEADING)
								.addGroup(jStep2Layout.createSequentialGroup().addComponent(jLabel2).addGap(429)
										.addComponent(jOptionsPanel, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING,
								jStep2Layout.createSequentialGroup()
										.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 756,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 288, Short.MAX_VALUE)
										.addComponent(jStep2Next, GroupLayout.PREFERRED_SIZE, 95,
												GroupLayout.PREFERRED_SIZE)))
						.addContainerGap()));
		jStep2Layout.setVerticalGroup(jStep2Layout.createParallelGroup(Alignment.TRAILING).addGroup(jStep2Layout
				.createSequentialGroup().addContainerGap()
				.addGroup(jStep2Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(jStep2Layout.createSequentialGroup().addComponent(jLabel2).addGap(18).addComponent(
								jScrollPane1, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE))
						.addGroup(jStep2Layout.createSequentialGroup()
								.addComponent(jOptionsPanel, GroupLayout.PREFERRED_SIZE, 125,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
								.addComponent(jStep2Next, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));
		jStep2.getContentPane().setLayout(jStep2Layout);
		jShowStep2.setText("Go to step 2");
		this.jShowStep2
				.addActionListener(this.jStep2.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
		jShowStep2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jShowStep2ActionPerformed(evt);
			}
		});

		jStep3.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 3"));
		jStep3.setCollapsed(true);

		jTable2.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { new Boolean(true), null, new Boolean(true), new Boolean(true), new Boolean(true),
						new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true) } },
				new String[] { "Enabled", "Target", "prered2", "wlcal", "fcal", "background", "apall", "scombine",
						"imcopy" }) {
			Class[] types = new Class[] { java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class,
					java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class,
					java.lang.Boolean.class, java.lang.Boolean.class };
			boolean[] canEdit = new boolean[] { true, false, true, true, true, true, true, true, true };

			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		for (int i = 0; i < jTable2.getColumnCount(); i++)
			jTable2Cols.put(jTable2.getModel().getColumnName(i), i);
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
		jStep3Layout.setHorizontalGroup(jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jStep3Layout.createSequentialGroup().addContainerGap()
						.addGroup(jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jStep3Layout.createSequentialGroup().addComponent(jLabel3)
										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(jStep3Layout.createSequentialGroup().addComponent(jScrollPane2).addContainerGap())))
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jStep3Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jStep3Next, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		jStep3Layout.setVerticalGroup(jStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jStep3Layout.createSequentialGroup().addContainerGap().addComponent(jLabel3).addGap(18, 18, 18)
								.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 167,
										javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jStep3Next, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jShowStep3.setText("Go to step 3");
		jShowStep3
				.addActionListener(this.jStep3.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
		jShowStep3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jShowStep3ActionPerformed(evt);
			}
		});

		jStep4.setBorder(javax.swing.BorderFactory.createTitledBorder("STEP 4"));

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
		jRadioListsAndMultipleScripts.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jRadioListsAndMultipleScriptsActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
		jPanel12Layout.setHorizontalGroup(jPanel12Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel12Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel12Layout.createParallelGroup(Alignment.LEADING).addComponent(jRadioListsOnly)
								.addComponent(jRadioListsAndOneScript).addComponent(jRadioListsAndMultipleScripts))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel12Layout.setVerticalGroup(jPanel12Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel12Layout.createSequentialGroup().addContainerGap().addComponent(jRadioListsOnly)
						.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(jRadioListsAndOneScript)
						.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(jRadioListsAndMultipleScripts)
						.addContainerGap(35, Short.MAX_VALUE)));
		jPanel12.setLayout(jPanel12Layout);

		jDoIt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jDoIt.setText("Do it");
		jDoIt.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jDoItActionPerformed(evt);
			}
		});

		jLabel10.setText("Select what you want to obtain:");

		jViewScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jViewScriptsActionPerformed(e);
			}
		});
		jViewScripts.setText("View script(s)");
		jViewScripts.setFont(new Font("Dialog", Font.BOLD, 11));

		jRunScripts.setText("Run script(s)");
		jRunScripts.setFont(new Font("Dialog", Font.BOLD, 11));
		jRunScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jRunScriptsActionPerformed(e);
			}
		});

		jCheckStartFromScrap.setText("Start from scrap");

		jPrintTODO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jPrintTODOActionPerformed(e);
			}
		});
		jPrintTODO.setText("Print TODO list");
		jPrintTODO.setFont(new Font("Dialog", Font.BOLD, 11));

		this.jListGenerated = new JButton("Show generated files");
		this.jListGenerated.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jListGeneratedActionPerformed(e);
			}
		});

		javax.swing.GroupLayout jStep4Layout = new javax.swing.GroupLayout(jStep4.getContentPane());
		jStep4Layout.setHorizontalGroup(jStep4Layout.createParallelGroup(Alignment.LEADING).addGroup(jStep4Layout
				.createSequentialGroup().addContainerGap()
				.addGroup(jStep4Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(jStep4Layout.createSequentialGroup()
								.addComponent(this.jPanel12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addGroup(jStep4Layout.createParallelGroup(Alignment.LEADING)
										.addComponent(this.jDoIt, GroupLayout.PREFERRED_SIZE, 160,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(this.jCheckStartFromScrap))
								.addGap(18)
								.addGroup(jStep4Layout.createParallelGroup(Alignment.LEADING, false)
										.addComponent(this.jListGenerated, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(this.jViewScripts, GroupLayout.DEFAULT_SIZE, 160,
												Short.MAX_VALUE))
								.addGap(18)
								.addComponent(this.jRunScripts, GroupLayout.PREFERRED_SIZE, 160,
										GroupLayout.PREFERRED_SIZE)
								.addGap(18).addComponent(this.jPrintTODO, GroupLayout.PREFERRED_SIZE, 160,
										GroupLayout.PREFERRED_SIZE))
						.addComponent(this.jLabel10))
				.addGap(15)));
		jStep4Layout
				.setVerticalGroup(jStep4Layout.createParallelGroup(Alignment.LEADING)
						.addGroup(jStep4Layout.createSequentialGroup().addContainerGap().addComponent(
								this.jLabel10)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jStep4Layout.createParallelGroup(Alignment.LEADING).addComponent(this.jPrintTODO,
								GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.jRunScripts, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
						.addGroup(jStep4Layout.createSequentialGroup()
								.addComponent(this.jViewScripts, GroupLayout.PREFERRED_SIZE, 52,
										GroupLayout.PREFERRED_SIZE)
								.addGap(18).addComponent(this.jListGenerated))
						.addComponent(this.jPanel12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE).addGroup(
										jStep4Layout.createSequentialGroup()
												.addComponent(this.jDoIt, GroupLayout.PREFERRED_SIZE, 52,
														GroupLayout.PREFERRED_SIZE)
												.addGap(18).addComponent(this.jCheckStartFromScrap)))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jStep4.getContentPane().setLayout(jStep4Layout);
		jShowStep4.setText("Go to step 4");
		jShowStep4
				.addActionListener(this.jStep4.getActionMap().get(org.jdesktop.swingx.JXCollapsiblePane.TOGGLE_ACTION));
		jShowStep4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jShowStep4ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jInputPanelLayout = new javax.swing.GroupLayout(jInputPanel);
		jInputPanel.setLayout(jInputPanelLayout);
		jInputPanelLayout.setHorizontalGroup(jInputPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jInputPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jStep3, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jStep1, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jStep2, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jStep4, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jShowStep1, javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jShowStep2, javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jShowStep3, javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jShowStep4, javax.swing.GroupLayout.Alignment.TRAILING))
						.addContainerGap()));
		jInputPanelLayout.setVerticalGroup(jInputPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jInputPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(jStep1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jShowStep1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jStep2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jShowStep2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jStep3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jShowStep3)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jStep4, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jShowStep4)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jTabbedPane1.addTab("Data input and Control", jInputPanel);

		jConsole.setEditable(false);
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
		jPythonPanelLayout.setHorizontalGroup(jPythonPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPythonPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jScrollPane3).addGroup(jPythonPanelLayout.createSequentialGroup()
										.addComponent(jCommand).addGap(18, 18, 18).addComponent(jSend)))
						.addContainerGap()));
		jPythonPanelLayout.setVerticalGroup(jPythonPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPythonPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 421,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addGroup(jPythonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCommand, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jSend))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
		jPanel14Layout.setHorizontalGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel14Layout.createSequentialGroup().addContainerGap().addComponent(jLabel17)
						.addGap(18, 18, 18)
						.addComponent(jImcopyStart, javax.swing.GroupLayout.PREFERRED_SIZE, 123,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18).addComponent(jLabel18).addGap(18, 18, 18)
						.addComponent(jImcopyEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 123,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel14Layout.setVerticalGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel14Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel17)
								.addComponent(jImcopyStart, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabel18).addComponent(jImcopyEnd, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
		jPanel15Layout.setHorizontalGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel15Layout.createSequentialGroup().addContainerGap().addComponent(jLabel14)
						.addGap(18, 18, 18)
						.addComponent(jBackgroundStart, javax.swing.GroupLayout.PREFERRED_SIZE, 123,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18).addComponent(jLabel15).addGap(18, 18, 18)
						.addComponent(jBackgroundEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 123,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel15Layout.setVerticalGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel15Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jBackgroundStart, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(jBackgroundEnd, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabel14).addComponent(jLabel15)).addContainerGap()));

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

		jLabel13.setText("00");

		javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
		jPanel16.setLayout(jPanel16Layout);
		jPanel16Layout
				.setHorizontalGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel16Layout.createSequentialGroup().addContainerGap().addComponent(jLabel12)
								.addGap(30, 30, 30)
								.addComponent(jWlcalThreshold, javax.swing.GroupLayout.DEFAULT_SIZE, 892,
										Short.MAX_VALUE)
								.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel16Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel13).addGap(488, 488, 488)));
		jPanel16Layout.setVerticalGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel16Layout.createSequentialGroup().addContainerGap().addComponent(jLabel13)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jWlcalThreshold, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel12))
						.addContainerGap()));

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
		jPanel17Layout.setHorizontalGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel17Layout.createSequentialGroup().addContainerGap().addComponent(jLabel11)
						.addGap(18, 18, 18).addComponent(jIrafHome).addGap(18, 18, 18).addComponent(jSelectIraf)
						.addContainerGap()));
		jPanel17Layout.setVerticalGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel17Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel11)
								.addComponent(jIrafHome, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(jSelectIraf))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jSave.setText("Save as Default");

		JButton btnRestoreDefaults = new JButton("Restore Defaults");
		btnRestoreDefaults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				btnRestoreDefaultsMouseClicked(e);
			}
		});

		javax.swing.GroupLayout jAdvancedOptionsPanelLayout = new javax.swing.GroupLayout(jAdvancedOptionsPanel);
		jAdvancedOptionsPanelLayout
				.setHorizontalGroup(jAdvancedOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(jAdvancedOptionsPanelLayout.createSequentialGroup().addContainerGap()
								.addGroup(jAdvancedOptionsPanelLayout.createParallelGroup(Alignment.TRAILING)
										.addComponent(jPanel14, GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
										.addComponent(jPanel15, GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
										.addComponent(jPanel16, GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
										.addComponent(jPanel17, GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
										.addGroup(jAdvancedOptionsPanelLayout.createSequentialGroup()
												.addComponent(btnRestoreDefaults).addGap(18).addComponent(jSave)))
						.addContainerGap()));
		jAdvancedOptionsPanelLayout.setVerticalGroup(jAdvancedOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jAdvancedOptionsPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jPanel16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jPanel15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jPanel14, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
						.addGroup(jAdvancedOptionsPanelLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jSave).addComponent(btnRestoreDefaults))
						.addContainerGap()));
		jAdvancedOptionsPanel.setLayout(jAdvancedOptionsPanelLayout);

		jTabbedPane1.addTab("Advanced Options for PyRAF tasks", jAdvancedOptionsPanel);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap()
						.addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	}// </editor-fold>//GEN-END:initComponents

	private void jRunScriptsActionPerformed(ActionEvent e) {
		if (this.generatedList.get("script") != null && this.generatedList.get("script").size() > 0) {
			log.info("Executing scripts...");
			this.jTabbedPane1.setSelectedIndex(1);
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"Ready to execute: insert a command below to execute it...\n", In);
			} catch (BadLocationException e1) {
				log.fatal(e1);
			}
			if (process != null && process.isAlive())
				process.dispose();
			process = new PythonRunnable();
			this.jConsole.setText("");
			this.scriptsList = new ArrayList<>();
			for (String script : this.generatedList.get("script"))
				scriptsList.add(script);
			process.startScript(Paths.get(basePath + "/" + scriptsList.get(0)).toString(), callback);
		} else
			log.info("Nothing to do.");
		log.info("Done.");
	}

	private void jViewScriptsActionPerformed(ActionEvent e) {
		for (String script : scriptsList) {
			try {
				Desktop.getDesktop().open(Paths.get(this.basePath + "/" + script).toFile());
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

	private void finalInitComponents() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if (process != null && process.isAlive())
					process.dispose();
			}
		}));

		if (Runtime.getRuntime() != null)
			dataService = DataService.getInstance();

		this.jIrafHome.setText(dataService.getProperty("iraf.home"));
		this.jBackgroundStart.setValue(Integer.valueOf(dataService.getProperty("iraf.bg.col1")));
		this.jBackgroundEnd.setValue(Integer.valueOf(dataService.getProperty("iraf.bg.col2")));
		this.jImcopyStart.setValue(Integer.valueOf(dataService.getProperty("iraf.imcopy.start")));
		this.jImcopyEnd.setValue(Integer.valueOf(dataService.getProperty("iraf.imcopy.end")));
		this.jWlcalThreshold.setValue(Integer.valueOf(dataService.getProperty("iraf.wlcal.rms_threshold")));

		this.groupExplore.add(this.jRadioFitsList);
		this.groupExplore.add(this.jRadioButton2);
		this.jLabel13.setText(String.valueOf(this.jWlcalThreshold.getValue()) + "%");
		this.jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				// Color legend:
				// 1. standards: HSV=214,87,64; RGB=21,83,163
				// 2. objects: HSV=122,34,89; RGB=149,226,158
				// 3. lamps: HSV=55,75,100; RGB=255,239,64
				// 4. conflicts: HSV=0,100,80; RGB=204,0,0
				// 5. flat: HSV=55,15,100; RGB=255,252,216
				if (images != null && images.size() != 0) {
					Color color = new Color(0, 0, 0);
					ImageEntity image = images.get(row);
					this.setForeground(Color.BLACK);
					// Lamps
					if (image.getType().equals("LAMP"))
						color = new Color(255, 243, 115);
					// Conflicts
					else if (image.getType().equals("IMAGE") && table.getValueAt(row, jTable1Cols.get("Lamp")) != null
							&& table.getValueAt(row, jTable1Cols.get("Standard")) != null
							&& ("".equals(table.getValueAt(row, jTable1Cols.get("Lamp")))
									|| "".equals((String) table.getValueAt(row, jTable1Cols.get("Standard"))))) {
						color = new Color(204, 0, 0);
						this.setForeground(Color.WHITE);
					} // Objects and standards
					else if (image.getType().equals("IMAGE") && table.getValueAt(row, jTable1Cols.get("Lamp")) != null
							&& table.getValueAt(row, jTable1Cols.get("Standard")) != null
							&& !((String) table.getValueAt(row, jTable1Cols.get("Lamp"))).isEmpty()
							&& !((String) table.getValueAt(row, jTable1Cols.get("Standard"))).isEmpty()) {
						if (image.isStandard())
							// Standard
							color = new Color(114, 159, 207);
						else
							// Objects
							color = new Color(149, 226, 158);
					} // Flatfield
					else if (image.getType().equals("FLATFIELD"))
						color = new Color(255, 252, 216);
					setBackground(color);
				}
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				super.setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}
		});
		this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Type"))
				.setCellEditor(new DefaultCellEditor(new JComboBox(new String[] { "IMAGE", "FLATFIELD", "LAMP" })));

		this.jTable2.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				// Color legend:
				// 1. standards: HSV=214,87,64; RGB=21,83,163
				// 2. objects: HSV=122,34,89; RGB=149,226,158
				// 3. lamps: HSV=55,75,100; RGB=255,239,64
				// 4. conflicts: HSV=0,100,80; RGB=204,0,0
				// 5. flat: HSV=55,15,100; RGB=255,252,216
				Color color = new Color(0, 0, 0);
				TableModel model = table.getModel();
				String target = (String) model.getValueAt(row, jTable2Cols.get("Target"));
				if (target != null) {
					this.setForeground(Color.BLACK);
					target = target.substring(0, target.indexOf("(")).trim();
					if (dataService.getStandardAtlas().findByStandardName(target) != null)
						color = new Color(114, 159, 207);
					else
						color = new Color(149, 226, 158);
					setBackground(color);
				}
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				super.setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}
		});

		StyleContext sc = new StyleContext();
		In = sc.addStyle("In", null);
		In.addAttribute(StyleConstants.Foreground, new Color(114, 159, 207));
		In.addAttribute(StyleConstants.FontSize, new Integer(12));
		In.addAttribute(StyleConstants.FontFamily, "arial");
		In.addAttribute(StyleConstants.Bold, new Boolean(false));

		Out = sc.addStyle("Out", null);
		Out.addAttribute(StyleConstants.Foreground, new Color(149, 226, 158));
		Out.addAttribute(StyleConstants.FontSize, new Integer(12));
		Out.addAttribute(StyleConstants.FontFamily, "arial");
		Out.addAttribute(StyleConstants.Bold, new Boolean(true));

		Error = sc.addStyle("Error", null);
		Error.addAttribute(StyleConstants.Foreground, new Color(255, 156, 0));
		Error.addAttribute(StyleConstants.FontSize, new Integer(12));
		Error.addAttribute(StyleConstants.FontFamily, "arial");
		Error.addAttribute(StyleConstants.Bold, new Boolean(true));

		// Start the console session under Linux
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			this.process = new PythonRunnable();
			process.startCommand("script -fqe /dev/null", callback);
			process.toPython("cd " + dataService.getProperty("iraf.home"));
			process.toPython("pyraf");
		} else
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\tThe terminal is available only on Linux systems.", Error);
			} catch (BadLocationException e) {
				log.fatal(e);
			}
	}

	private void jSaveMouseClicked(MouseEvent e) {
		Properties properties = new Properties();
		try {
			properties.setProperty("iraf.home", this.jIrafHome.getText());
			properties.setProperty("iraf.bg.col1", this.jBackgroundStart.getValue().toString());
			properties.setProperty("iraf.bg.col2", this.jBackgroundEnd.getValue().toString());
			properties.setProperty("iraf.imcopy.start", this.jImcopyStart.getValue().toString());
			properties.setProperty("iraf.imcopy.end", this.jImcopyEnd.getValue().toString());
			properties.setProperty("iraf.wlcal.rms_threshold", String.valueOf(this.jWlcalThreshold.getValue()));
			properties.store(new FileOutputStream(
					Paths.get(System.getProperty("user.home"), "asgredLists.properties").toString()), "");
			JOptionPane.showMessageDialog(this, "Saved.", "All done", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ex) {
			log.error(ex);
			ex.printStackTrace();
		}
	}

	private void btnRestoreDefaultsMouseClicked(MouseEvent e) {
		dataService.restoreProperties();
		this.jIrafHome.setText(dataService.getProperty("iraf.home"));
		this.jBackgroundStart.setValue(Integer.valueOf(dataService.getProperty("iraf.bg.col1")));
		this.jBackgroundEnd.setValue(Integer.valueOf(dataService.getProperty("iraf.bg.col2")));
		this.jImcopyStart.setValue(Integer.valueOf(dataService.getProperty("iraf.imcopy.start")));
		this.jImcopyEnd.setValue(Integer.valueOf(dataService.getProperty("iraf.imcopy.end")));
		this.jWlcalThreshold.setValue(Integer.valueOf(dataService.getProperty("iraf.wlcal.rms_threshold")));
	}

	private void jSelectIrafActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jSelectIrafActionPerformed
		try {
			log.info("Modifing variable \"iraf.home\"...");
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
				dataService.setProperty("iraf.home", fileChooser.getSelectedFile().getParent());
				this.jIrafHome.setText(dataService.getProperty("iraf.home"));
			}
			log.info("iraf.home set.");
		} catch (Exception ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_jSelectIrafActionPerformed

	private void jWlcalThresholdMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_jWlcalThresholdMouseWheelMoved
		this.jWlcalThreshold.setValue(this.jWlcalThreshold.getValue() - evt.getWheelRotation());
	}// GEN-LAST:event_jWlcalThresholdMouseWheelMoved

	private void jWlcalThresholdStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jWlcalThresholdStateChanged
		this.jLabel13.setText(String.valueOf(this.jWlcalThreshold.getValue()) + " %");
	}// GEN-LAST:event_jWlcalThresholdStateChanged

	private void jBackgroundEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_jBackgroundEndMouseWheelMoved
		if (((int) this.jBackgroundEnd.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jBackgroundEnd.getValue() < 2048 && evt.getWheelRotation() < 0)) {
			this.jBackgroundEnd.setValue((int) this.jBackgroundEnd.getValue() - evt.getWheelRotation());
		}
	}// GEN-LAST:event_jBackgroundEndMouseWheelMoved

	private void jBackgroundStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_jBackgroundStartMouseWheelMoved
		if (((int) this.jBackgroundStart.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jBackgroundStart.getValue() < 2048 && evt.getWheelRotation() < 0)) {
			this.jBackgroundStart.setValue((int) this.jBackgroundStart.getValue() - evt.getWheelRotation());
		}
	}// GEN-LAST:event_jBackgroundStartMouseWheelMoved

	private void jImcopyStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_jImcopyStartMouseWheelMoved
		if (((int) this.jImcopyStart.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jImcopyStart.getValue() < 2048 && evt.getWheelRotation() < 0)) {
			this.jImcopyStart.setValue((int) this.jImcopyStart.getValue() - evt.getWheelRotation());
		}
	}// GEN-LAST:event_jImcopyStartMouseWheelMoved

	private void jImcopyEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {// GEN-FIRST:event_jImcopyEndMouseWheelMoved
		if (((int) this.jImcopyEnd.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jImcopyEnd.getValue() < 2048 && evt.getWheelRotation() < 0)) {
			this.jImcopyEnd.setValue((int) this.jImcopyEnd.getValue() - evt.getWheelRotation());
		}
	}// GEN-LAST:event_jImcopyEndMouseWheelMoved

	private void jSendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jSendActionPerformed
		String message = this.jCommand.getText();
		if (message.equals("iraf"))
			if (JOptionPane.showConfirmDialog(this,
					"Running IRAF from AsgredLists is highly discouraged due to compatibility issues.\nMoreover AsgredLists won't be able to interact with IRAF.\nDo you want to run IRAF anyway?",
					"Wait", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				this.jCommand.setText("");
				return;
			}
		((DefaultCaret) jConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		if (process == null || !process.isAlive()) {
			process = new PythonRunnable();
			if (message.equals("clear"))
				this.jConsole.setText("");
			else if (message.equals("start pyraf")) {
				process.startCommand("script -fqe /dev/null", callback);
				process.toPython("cd " + dataService.getProperty("iraf.home"));
				process.toPython("pyraf");
			} else if (message.equals("mime wlcal"))
				process.mimeWlcal(Paths.get("src/main/resources/mimeWlcal.py").toString(), callback);
			else
				process.startCommand(this.jCommand.getText(), callback);
		} else
			process.toPython(message);
		this.jCommand.setText("");
	}// GEN-LAST:event_jSendActionPerformed

	private void jCommandKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jCommandKeyTyped
		if (evt.getKeyChar() == '\n') {
			this.jSend.doClick();
		}
	}// GEN-LAST:event_jCommandKeyTyped

	private void jDoItActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jDoItActionPerformed
		try {
			if (this.jCheckStartFromScrap.isSelected())
				for (final File fileEntry : new File(this.basePath).listFiles()) {
					if (!fileEntry.isDirectory()) {
						if (!fileEntry.getName().equals("fits_list") && (fileEntry.getName().endsWith("bg.fits")
								|| fileEntry.getName().endsWith("fc.fits") || fileEntry.getName().endsWith("wl.fits")
								|| fileEntry.getName().endsWith("md.fits") || fileEntry.getName().endsWith("obj.fits")
								|| fileEntry.getName().endsWith(".py") || !fileEntry.getName().startsWith("IMA")
								|| !fileEntry.getName().startsWith("SCR")))
							fileEntry.delete();
					}
				}

			if (this.generatedList != null)
				this.generatedList.clear();
			else
				this.generatedList = new HashMap<String, List<String>>();

			switch (this.selectedAction) {
			case 0:
				log.info("Action selected: generate lists only...");
				this.generateAllLists();
				log.info("Done.");
				GeneratedList.showList(generatedList);
				break;
			case 1:
				log.info("Action selected: generate lists and one script...");
				this.generateAllLists();
				this.writeOneGiantScript();
				log.info("Done.");
				GeneratedList.showList(generatedList);
				break;
			case 2:
				log.info("Action selected: generate lists and a script for each target...");
				this.writeScriptForEachTarget();
				log.info("Done.");
				GeneratedList.showList(generatedList);
			}
		} catch (Exception ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_jDoItActionPerformed

	private void jRadioListsAndMultipleScriptsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jRadioListsAndMultipleScriptsActionPerformed
		this.selectedAction = 2;
	}// GEN-LAST:event_jRadioListsAndMultipleScriptsActionPerformed

	private void jRadioListsAndOneScriptActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jRadioListsAndOneScriptActionPerformed
		this.selectedAction = 1;
	}// GEN-LAST:event_jRadioListsAndOneScriptActionPerformed

	private void jRadioListsOnlyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jRadioListsOnlyActionPerformed
		this.selectedAction = 0;
	}// GEN-LAST:event_jRadioListsOnlyActionPerformed

	private void jSolveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jSolveActionPerformed
		log.info("Attempting to fix conflicts...");
		int lamp_fixed = 0, standard_fixed = 0;
		// FIX LAMPS FIRST
		String lampFileName = "";
		int rowLastLamp = 0;
		TableModel model = this.jTable1.getModel();
		for (int row = 0; row < model.getRowCount(); row++) {
			if (((String) model.getValueAt(row, jTable1Cols.get("Type"))).equals("LAMP")) {
				lampFileName = (String) model.getValueAt(row, jTable1Cols.get("Image"));
				for (int i = rowLastLamp + 1; i < row; i++) {
					if (((String) model.getValueAt(i, jTable1Cols.get("Type"))).equals("IMAGE")) {
						if (((String) model.getValueAt(i, jTable1Cols.get("Lamp"))).isEmpty()) {
							model.setValueAt(lampFileName, i, jTable1Cols.get("Lamp"));
							lamp_fixed++;
						}
					}
				}
				rowLastLamp = row;
			}
		}
		// use the last lamp for all the remaining images without a lamp
		for (int i = rowLastLamp + 1; i < model.getRowCount(); i++) {
			if (((String) model.getValueAt(i, jTable1Cols.get("Type"))).equals("IMAGE")) {
				if (((String) model.getValueAt(i, jTable1Cols.get("Lamp"))).isEmpty()) {
					model.setValueAt(lampFileName, i, jTable1Cols.get("Lamp"));
					lamp_fixed++;
				}
			}
		}
		// FIX STANDARDS LAST
		String standardFileName = "";
		int rowLastStandard = 0;
		for (int row = 0; row < model.getRowCount(); row++) {
			if ((Boolean) model.getValueAt(row, jTable1Cols.get("Is standard?")) == true) {
				standardFileName = (String) model.getValueAt(row, jTable1Cols.get("Image"));
				for (int i = rowLastStandard + 1; i < row; i++) {
					if ("IMAGE".equals(model.getValueAt(i, jTable1Cols.get("Type")))
							&& (Boolean) model.getValueAt(i, jTable1Cols.get("Is standard?")) == false) {
						if ("".equals(model.getValueAt(i, jTable1Cols.get("Standard")))) {
							model.setValueAt(standardFileName, i, jTable1Cols.get("Standard"));
							standard_fixed++;
						}
					}
				}
				rowLastStandard = row;
			}
		}
		// use the last standard for all the remaining images without a standard
		for (int i = rowLastStandard + 1; i < model.getRowCount(); i++) {
			if ("IMAGE".equals(model.getValueAt(i, jTable1Cols.get("Type")))
					&& (Boolean) model.getValueAt(i, jTable1Cols.get("Is standard?")) == false) {
				if ("".equals(model.getValueAt(i, jTable1Cols.get("Standard")))) {
					model.setValueAt(standardFileName, i, jTable1Cols.get("Standard"));
					standard_fixed++;
				}
			}
		}

		this.jTable1.updateUI();
		log.info("Done.");
		JOptionPane.showMessageDialog(this, String.valueOf(lamp_fixed + standard_fixed) + " conflicts solved ("
				+ String.valueOf(lamp_fixed) + " lamps; " + String.valueOf(standard_fixed) + " standards).");
	}// GEN-LAST:event_jSolveActionPerformed

	private void jTable1PropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_jTable1PropertyChange
		int x = this.jTable1.getSelectedRow();
		int y = this.jTable1.getSelectedColumn();
		if (x != -1) {
			ImageEntity image = DataService.getInstance().getImageRepository()
					.findByFileName((String) (this.jTable1.getValueAt(x, jTable1Cols.get("Image"))));

			if (y == jTable1Cols.get("Type"))
				image.setType((String) this.jTable1.getValueAt(x, y));
			else if (y == jTable1Cols.get("Is standard?")) {
				image.setStandard((Boolean) this.jTable1.getValueAt(x, y));
				this.jTable1.setValueAt(true, x, jTable1Cols.get("Enabled"));
			} else if (y == jTable1Cols.get("Enabled")
					&& (boolean) this.jTable1.getValueAt(x, jTable1Cols.get("Is standard?")) == true) {
				JOptionPane.showMessageDialog(this, "Images realtive to standard stars cannot be disabled.");
				this.jTable1.setValueAt(true, x, jTable1Cols.get("Enabled"));
			}

			this.jTable1.clearSelection();
			DataService.getInstance().getImageRepository().save(image);
		}
	}// GEN-LAST:event_jTable1PropertyChange

	private void jExploreActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jExploreActionPerformed
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
	}// GEN-LAST:event_jExploreActionPerformed

	private void jLoadActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jLoadActionPerformed
		try {
			if (this.jPath.getText().equals("")) {
				return;
			}

			if (this.jPath.getText().startsWith("file://")) {
				this.jPath.setText(this.jPath.getText().replace("file://", "").trim());
				this.basePath = this.jPath.getText().replace("fits_list", "");
			}
			parseFile(this.jPath.getText());
			this.updateImagesTable();
			this.jShowStep2.doClick();
		} catch (IOException ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_jLoadActionPerformed

	private void jShowStep1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jShowStep1ActionPerformed
		if (this.jShowStep1.isSelected()) {
			this.jShowStep1.setText("Showing step 1");
			this.jStep2.setCollapsed(true);
			this.jShowStep2.setSelected(false);
			this.jShowStep2.setText("Go to step 2");
			this.jStep3.setCollapsed(true);
			this.jShowStep3.setSelected(false);
			this.jShowStep3.setText("Go to step 3");
			this.jStep4.setCollapsed(true);
			this.jShowStep4.setSelected(false);
			this.jShowStep4.setText("Go to step 4");
		} else {
			this.jShowStep1.setText("Go to step 1");
		}
	}// GEN-LAST:event_jShowStep1ActionPerformed

	private void jShowStep2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jShowStep2ActionPerformed
		if (this.jShowStep2.isSelected()) {
			this.jShowStep2.setText("Showing step 2");
			this.jStep1.setCollapsed(true);
			this.jShowStep1.setSelected(false);
			this.jShowStep1.setText("Go to step 1");
			this.jStep3.setCollapsed(true);
			this.jShowStep3.setSelected(false);
			this.jShowStep3.setText("Go to step 3");
			this.jStep4.setCollapsed(true);
			this.jShowStep4.setSelected(false);
			this.jShowStep4.setText("Go to step 4");
		} else {
			this.jShowStep2.setText("Go to step 2");
		}
	}// GEN-LAST:event_jShowStep2ActionPerformed

	private void jShowStep3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jShowStep3ActionPerformed
		if (this.jShowStep3.isSelected()) {
			this.jShowStep3.setText("Showing step 3");
			this.jStep1.setCollapsed(true);
			this.jShowStep1.setSelected(false);
			this.jShowStep1.setText("Go to step 1");
			this.jStep2.setCollapsed(true);
			this.jShowStep2.setSelected(false);
			this.jShowStep2.setText("Go to step 2");
			this.jStep4.setCollapsed(true);
			this.jShowStep4.setSelected(false);
			this.jShowStep4.setText("Go to step 4");
		} else {
			this.jShowStep3.setText("Go to step 3");
		}
	}// GEN-LAST:event_jShowStep3ActionPerformed

	private void jShowStep4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jShowStep4ActionPerformed
		if (this.jShowStep4.isSelected()) {
			this.jShowStep4.setText("Showing step 4");
			this.jStep1.setCollapsed(true);
			this.jShowStep1.setSelected(false);
			this.jShowStep1.setText("Go to step 1");
			this.jStep2.setCollapsed(true);
			this.jShowStep2.setSelected(false);
			this.jShowStep2.setText("Go to step 2");
			this.jStep3.setCollapsed(true);
			this.jShowStep3.setSelected(false);
			this.jShowStep3.setText("Go to step 3");
		} else {
			this.jShowStep4.setText("Go to step 4");
		}
	}// GEN-LAST:event_jShowStep4ActionPerformed

	private void jStep2NextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jStep2NextActionPerformed
		this.populateDatabase();
		this.jShowStep3.doClick();
		this.updateTasksTable();
	}// GEN-LAST:event_jStep2NextActionPerformed

	private void jTable2PropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_jTable2PropertyChange
		int x = this.jTable2.getSelectedRow();
		if (x != -1) {
			int y = this.jTable2.getSelectedColumn();
			String args[] = String.valueOf(this.jTable2.getModel().getValueAt(x, jTable2Cols.get("Target"))).split(" ");
			args[1] = args[1].replace("(", "");
			args[1] = args[1].replace(")", "");
			String message = "";
			Observation observation = dataService.getObservationRepository()
					.findByTargetNameAndStandardFileName(args[0], args[1]);
			switch (y) {
			case 0:
				if (dataService.getScienceRepository().findConflictsForTargetName(args[0]) != 0) {
					message = "Some images relative to "
							+ (String) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("Target"))
							+ " contain conflicts: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave this target disabled.\nDo you want to enable it anyway?";
				}
				break;
			case 3:
				if (dataService.getScienceRepository().getIsLampMissingForTargetName(args[0]) != 0) {
					message = "Some images relative to "
							+ (String) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("Target"))
							+ " are missing lamps: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave wlcal for this target disabled.\nDo you want to enable it anyway?";
				}
				break;
			case 4:
				if (dataService.getScienceRepository().getIsStandardMissingForTargetName(args[0]) != 0) {
					message = "Some images relative to "
							+ (String) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("Target"))
							+ " are missing standard stars: this could lead to errors in the next step.\nWe suggest you either to return to STEP 2 and solve them or to leave fcal for this target disabled.\nDo you want to enable this target anyway?";
				}
			}

			if (!message.equals("") && (boolean) this.jTable2.getModel().getValueAt(x, y)) {
				if (JOptionPane.showConfirmDialog(this, message, "Wait!", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					if (y != 0) {
						this.jTable2.getModel().setValueAt(true, x, jTable2Cols.get("Image"));
					}
				} else {
					this.jTable2.getModel().setValueAt(false, x, y);
				}
			}
			observation.setEnabled((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("Enabled")));
			observation.setDoPrered((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("prered2")));
			observation.setDoWlcal((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("wlcal")));
			observation.setDoFcal((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("fcal")));
			observation.setDoBackground((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("background")));
			observation.setDoApall((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("apall")));
			observation.setDoScombine((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("scombine")));
			observation.setDoImcopy((boolean) this.jTable2.getModel().getValueAt(x, jTable2Cols.get("imcopy")));
			dataService.getObservationRepository().save(observation);
			this.jTable2.clearSelection();
		}
	}// GEN-LAST:event_jTable2PropertyChange

	private void jLoad1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jLoad1ActionPerformed
		PrintWriter writer = null;
		try {
			// Let's generate the PyRAF script
			writer = new PrintWriter(new FileWriter(Paths.get(this.basePath, "createFitsList.py").toString()));
			writer.println("#!/usr/bin/env python");
			writer.println("import os");
			writer.println("import sys");
			writer.println("os.chdir(\"" + dataService.getProperty("iraf.home") + "\")");
			writer.println("from pyraf import iraf");
			writer.println("os.chdir(\"" + this.basePath + "\")");
			writer.println(
					"iraf.hselect(images=\"?????????.fits\", fields=\"$I,target,typ,exptime\", expr=\"yes\", Stdout=\"fits_list\")");
		} catch (IOException ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (writer != null)
				writer.close();
		}
		process = new PythonRunnable();
		process.startScript(Paths.get(this.basePath + "/createFitsList.py").toString(), callback);
		this.jPath.setText(basePath + "/fits_list");
		this.jLoad.doClick();
	}// GEN-LAST:event_jLoad1ActionPerformed

	private void jExplore1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jExplore1ActionPerformed
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory());
			}

			@Override
			public String getDescription() {
				return "Select the directory containing the fits images";
			}
		});

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.basePath = fileChooser.getSelectedFile().getAbsolutePath();
			this.jPath1.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}// GEN-LAST:event_jExplore1ActionPerformed

	private void jRadioFitsListActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jRadioFitsListActionPerformed
		this.jFitsListPanel.setCollapsed(false);
		this.jDirectoryPanel.setCollapsed(true);
	}// GEN-LAST:event_jRadioFitsListActionPerformed

	private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jRadioButton2ActionPerformed
		this.jFitsListPanel.setCollapsed(true);
		this.jDirectoryPanel.setCollapsed(false);
	}// GEN-LAST:event_jRadioButton2ActionPerformed

	private void jStep3NextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jStep3NextActionPerformed
		this.jShowStep4.doClick();
	}// GEN-LAST:event_jStep3NextActionPerformed

	public void parseFile(String filePath) throws FileNotFoundException, IOException {
		log.info("Parsing " + filePath + "...");
		String line = "";
		BufferedReader fitsList = new BufferedReader(new FileReader(filePath));
		images = new ArrayList<>();
		log.info("Clearing database...");
		dataService.getObservationRepository().deleteAll();
		dataService.getScienceRepository().deleteAll();
		dataService.getStandardRepository().deleteAll();
		dataService.getLampRepository().deleteAll();
		dataService.getFlatRepository().deleteAll();
		dataService.getImageRepository().deleteAll();
		log.info("Database cleared. Creating entities...");
		do {
			line = fitsList.readLine();
			log.info("Parsing \"" + line + "\"");
			// Ignore comments, empty lines and EOF
			if (line != null && !line.startsWith("#") && !line.equals("")) {
				images.add(ImageEntity.parseEntity(line));
			}
		} while (line != null);
		fitsList.close();
		log.info("Loading entities to the IMAGES table...");
		dataService.getImageRepository().save(images);
		log.info("Done. File parsed.");
	}

	public void updateImagesTable() {
		log.info("Updating jTable1...");
		ActionListener autoHide = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				((JComboBox) evt.getSource()).hidePopup();
				((JComboBox) evt.getSource()).setSelectedIndex(-1);
			}
		};
		log.trace("Creating cell editors...");
		log.trace("Creating comboStd...");
		JComboBox comboStd = new JComboBox(dataService.getImageRepository().getFileNameByIsStandard(true).toArray());
		comboStd.addActionListener(autoHide);
		log.trace("Done. Creating comboLamps...");
		JComboBox comboLamps = new JComboBox(dataService.getImageRepository().getFileNameByType("LAMP").toArray());
		comboLamps.addActionListener(autoHide);
		this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Lamp"))
				.setCellEditor(new DefaultCellEditor(comboLamps));
		this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Standard"))
				.setCellEditor(new DefaultCellEditor(comboStd));
		log.info("Done. Clearing the table...");
		if (((DefaultTableModel) this.jTable1.getModel()).getRowCount() > 0) {
			for (int i = ((DefaultTableModel) this.jTable1.getModel()).getRowCount() - 1; i > -1; i--) {
				((DefaultTableModel) this.jTable1.getModel()).removeRow(i);
			}
		}
		log.info("Done. Creating the rows...");
		for (ImageEntity image : dataService.getImageRepository().findAll()) {
			if (image.isStandard()) {
				((DefaultTableModel) this.jTable1.getModel())
						.addRow(new Object[] { true, image.getFileName(), image.getTargetName(), image.getType(),
								image.isStandard(), image.getExpTime(), "", image.getFileName() });
			} else {
				((DefaultTableModel) this.jTable1.getModel()).addRow(new Object[] { true, image.getFileName(),
						image.getTargetName(), image.getType(), image.isStandard(), image.getExpTime(), "", "" });
			}
		}
		log.info("Done.");
		log.info("jTable1 updated.");
	}

	public void updateTasksTable() {
		log.info("Update table jTable2...");
		DefaultTableModel model = (DefaultTableModel) this.jTable2.getModel();
		log.info("Clearing the table...");
		if (model.getRowCount() > 0) {
			for (int i = model.getRowCount() - 1; i > -1; i--) {
				model.removeRow(i);
			}
		}
		log.info("Table cleared.");
		log.info("Populating the table...");
		List<Object[]> result = dataService.getImageRepository().getTargetNameAndStandardFileName();
		for (Object[] data : result) {
			boolean lampMissing = (dataService.getScienceRepository()
					.getIsLampMissingForTargetName((String) data[0]) != 0);
			boolean standardMissing = (dataService.getScienceRepository()
					.getIsStandardMissingForTargetName((String) data[0]) != 0);
			Object[] row = new Object[] { !(lampMissing || standardMissing),
					(String) data[0] + " (" + (String) data[1] + ")", true, !lampMissing, !standardMissing, true, true,
					true, true };
			model.addRow(row);
		}
		this.jTable2.setModel(model);
		this.jTable2.doLayout();
		log.info("jTable2 updated.");
	}

	public void populateDatabase() {
		log.info("Populating the database...");
		log.trace("Clearing the database...");
		dataService.getObservationRepository().deleteAll();
		dataService.getScienceRepository().deleteAll();
		dataService.getStandardRepository().deleteAll();
		dataService.getLampRepository().deleteAll();
		dataService.getFlatRepository().deleteAll();
		log.trace("Database cleared.");
		log.info("FLATFIELD");
		/*
		 * Populate the FLATFIELD
		 */
		FlatfieldImage flat = new FlatfieldImage();
		log.trace("Associating with the images of type \"FLATFIELD\"...");
		List<ImageEntity> images = dataService.getImageRepository().findByType("FLATFIELD");
		flat.setImages(images);
		for (ImageEntity image : images)
			image.setFlat(flat);
		log.trace("Done.");
		log.trace("Saving...");
		dataService.getFlatRepository().save(flat);
		dataService.getImageRepository().save(images);
		log.trace("Saved.");
		log.trace("FLATFIELD created.");

		/*
		 * Populate the LAMP
		 */
		log.info("LAMP");
		images = dataService.getImageRepository().findByType("LAMP");
		log.trace("Associating with the images of type \"LAMP\"...");
		for (ImageEntity image : images) {
			LampImage lamp = new LampImage();
			lamp.setFlat(flat);
			lamp.setImage(image);
			image.setLamp(lamp);
			dataService.getLampRepository().save(lamp);
			dataService.getImageRepository().save(image);
		}
		log.trace("Done and saved.");

		/*
		 * Populate the STANDARD
		 */
		log.info("STANDARD");
		images = dataService.getImageRepository().findByIsStandard(true);
		for (ImageEntity image : images) {
			StandardImage standard = new StandardImage();
			standard.setImage(image);
			image.setStandard(standard);
			TableModel model = this.jTable1.getModel();
			standard.setFlat(flat);
			for (int row = 0; row < model.getRowCount(); row++) {
				if (image.getFileName().equals(model.getValueAt(row, jTable1Cols.get("Image")))) {
					if (model.getValueAt(row, jTable1Cols.get("Lamp")) != null) {
						ImageEntity lampImage = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(row, jTable1Cols.get("Lamp")));
						standard.setLamp(lampImage.getLamp());
						lampImage.getLamp().getStandardImages().add(standard);
						dataService.getLampRepository().save(lampImage.getLamp());
					}
				}
			}
			dataService.getStandardRepository().save(standard);
		}

		/*
		 * Populate the IMAGE
		 */
		log.info("IMAGE");
		images = dataService.getImageRepository().findByType("IMAGE");
		for (ImageEntity image : images) {
			ScienceImage science = new ScienceImage();
			science.setImage(image);
			TableModel model = this.jTable1.getModel();
			science.setFlat(flat);
			for (int row = 0; row < model.getRowCount(); row++) {
				if (image.getFileName().equals(model.getValueAt(row, jTable1Cols.get("Image")))) {
					if (model.getValueAt(row, jTable1Cols.get("Lamp")) != null) {
						ImageEntity lampImage = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(row, jTable1Cols.get("Lamp")));
						science.setLamp(lampImage.getLamp());
						lampImage.getLamp().getScienceImages().add(science);
						dataService.getLampRepository().save(lampImage.getLamp());
					}
					if (model.getValueAt(row, jTable1Cols.get("Standard")) != null) {
						ImageEntity standardImage = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(row, jTable1Cols.get("Standard")));
						science.setStandard(standardImage.getStandard());
						standardImage.getStandard().getScienceImages().add(science);
						dataService.getStandardRepository().save(standardImage.getStandard());
					}
				}
			}
			dataService.getScienceRepository().save(science);
		}

		/*
		 * Populate the OBSERVATION
		 */
		log.info("OBSERVATION");
		List<Object[]> result = dataService.getImageRepository().getTargetNameAndStandardFileName();
		for (Object[] data : result) {
			Observation observation = new Observation();
			StandardImage standard = dataService.getStandardRepository().findByFileName((String) data[1]);
			observation.setStandard(standard);
			standard.getObservations().add(observation);
			List<ScienceImage> science = dataService.getScienceRepository()
					.getScienceImageByTargetNameAndStandardFileName((String) data[0], (String) data[1]);
			observation.setScienceImages(science);
			for (ScienceImage entity : science)
				entity.setObservation(observation);
			dataService.getStandardRepository().save(standard);
			dataService.getObservationRepository().save(observation);
			dataService.getScienceRepository().save(science);
		}
		log.info("Database populated.");
	}

	public void generateAllLists() {
		PrintWriter writer = null;
		this.generatedList = new HashMap<String, List<String>>();
		try {
			log.info("Generating all lists...");
			String temp = "", tempLamp = "";
			// Write them down
			log.info("list_obj");
			if (this.generatedList.get("all") != null)
				this.generatedList.get("all").add("list_obj");
			else {
				ArrayList<String> list = new ArrayList<>();
				list.add("list_obj");
				this.generatedList.put("all", list);
			}
			writer = new PrintWriter(Paths.get(this.basePath, "list_obj").toFile());
			List<Observation> observations = dataService.getObservationRepository().findByIsEnabled(true);
			HashSet<String> lampName = new HashSet<String>();
			for (Observation observation : observations) {
				int i = 0;
				for (ScienceImage science : observation.getScienceImages()) {
					TableModel model = jTable1.getModel();
					while (i < model.getRowCount()
							&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(science.getImage().getFileName()))
						i++;
					if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
						temp += science.getImage().getFileName() + "\t" + science.getLamp().getImage().getFileName()
								+ "\n";
						lampName.add(science.getLamp().getImage().getFileName());
						i++;
					}
				}
			}
			for (String item : lampName)
				tempLamp += item + "\n";

			writer.print(temp.trim());
			writer.close();

			temp = "";
			log.info("list_lamps");
			if (this.generatedList.get("all") != null)
				this.generatedList.get("all").add("list_lamps");
			else {
				ArrayList<String> list = new ArrayList<>();
				list.add("list_lamps");
				this.generatedList.put("all", list);
			}
			writer = new PrintWriter(Paths.get(this.basePath, "list_lamps").toFile());
			writer.print(tempLamp.trim());
			writer.close();

			log.info("list_flat");
			if (this.generatedList.get("all") != null)
				this.generatedList.get("all").add("list_flat");
			else {
				ArrayList<String> list = new ArrayList<>();
				list.add("list_flat");
				this.generatedList.put("all", list);
			}
			writer = new PrintWriter(Paths.get(this.basePath, "list_flat").toFile());
			for (FlatfieldImage flat : dataService.getFlatRepository().findAll()) {
				for (ImageEntity image : flat.getImages())
					temp += image.getFileName() + "\n";
			}
			writer.print(temp.trim());
			writer.close();

			// Generate the list of IMA*.wl.fits
			for (Observation observation : observations) {
				if (observation.isEnabled() && observation.isDoWlcal()) {
					temp = observation.getTargetName();
					if (temp.contains("+"))
						temp = temp.replaceAll("+", "p");
					if (temp.contains("-"))
						temp = temp.replaceAll("-", "m");
					log.info("wl" + temp);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("wl" + temp);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("wl" + temp);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "wl" + temp).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".wl\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}
			}

			// Generate the list of IMA*.fc.fits
			// This list is used as input for the background task
			for (Observation observation : observations) {
				if (observation.isDoFcal()) {
					temp = observation.getTargetName();
					if (temp.contains("+"))
						temp = temp.replaceAll("+", "p");
					if (temp.contains("-"))
						temp = temp.replaceAll("-", "m");
					log.info("fc" + temp);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("fc" + temp);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("fc" + temp);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "fc" + temp).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".fc\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}
			}

			// Generate the list of IMA*.bg.fits
			for (Observation observation : observations) {
				if (observation.isDoBackground()) {
					temp = observation.getTargetName();
					if (temp.contains("+"))
						temp = temp.replaceAll("+", "p");
					if (temp.contains("-"))
						temp = temp.replaceAll("-", "m");
					log.info("bg" + temp);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("bg" + temp);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("bg" + temp);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "bg" + temp).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".bg\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}
			}

			// Generate the list of IMA*.md.fits
			for (Observation observation : observations) {
				if (observation.isDoApall()) {
					temp = observation.getTargetName();
					if (temp.contains("+"))
						temp = temp.replaceAll("+", "p");
					if (temp.contains("-"))
						temp = temp.replaceAll("-", "m");
					log.info("md" + temp);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("md" + temp);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("md" + temp);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "md" + temp).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".md\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}
			}

			// Generate the list of IMA*.wl.fits
			// This list is used as input for standard parameter of fcal task
			for (StandardImage standard : dataService.getStandardRepository().findAll()) {
				for (Observation observation : standard.getObservations()) {
					if (observation.isEnabled() && observation.isDoFcal()) {
						temp = "";
						log.info("std" + standard.getImage().getFileName());
						if (this.generatedList.get(observation.getTargetName()) != null)
							this.generatedList.get(observation.getTargetName())
									.add("std" + standard.getImage().getFileName());
						else {
							ArrayList<String> list = new ArrayList<>();
							list.add("std" + standard.getImage().getFileName());
							this.generatedList.put(observation.getTargetName(), list);
						}
						writer = new PrintWriter(
								Paths.get(this.basePath, "std" + standard.getImage().getFileName()).toFile());
						int i = 0;
						for (ScienceImage item : standard.getScienceImages()) {
							/*
							 * Use file name instead of standard name to avoid
							 * confusion: the same standard used twice during
							 * the night MUST be treated as two different stars
							 */
							TableModel model = jTable1.getModel();
							while (i < model.getRowCount() && !model.getValueAt(i, jTable1Cols.get("Image"))
									.equals(item.getImage().getFileName()))
								i++;
							if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
								if (item.getObservation().isDoFcal())
									temp += item.getImage().getFileName() + ".wl\n";
								i++;
							}
						}
						writer.print(temp.trim());
						writer.close();
					}
				}
			}
		} catch (IOException ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (writer != null)
				writer.close();
		}
		log.info("Done.");
	}

	public void writeScriptForEachTarget() {
		log.info("Generating a script for each target...");
		scriptsList = new ArrayList<>();
		for (Observation observation : dataService.getObservationRepository().findByIsEnabled(true)) {
			String targetNormalized = observation.getTargetName();
			if (targetNormalized.contains("+"))
				targetNormalized = targetNormalized.replaceAll("+", "p");
			if (targetNormalized.contains("-"))
				targetNormalized = targetNormalized.replaceAll("-", "m");
			PrintWriter writer = null;
			try {
				String temp = "", tempLamp = "";
				// Write them down
				log.info("list_obj_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) != null)
					this.generatedList.get(observation.getTargetName()).add("list_obj_" + targetNormalized);
				else {
					ArrayList<String> list = new ArrayList<>();
					list.add("list_obj_" + targetNormalized);
					this.generatedList.put(observation.getTargetName(), list);
				}
				writer = new PrintWriter(Paths.get(this.basePath, "list_obj_" + targetNormalized).toFile());
				HashSet<String> lampName = new HashSet<String>();
				for (ScienceImage science : observation.getScienceImages()) {
					int i = 0;
					TableModel model = jTable1.getModel();
					while (i < model.getRowCount()
							&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(science.getImage().getFileName()))
						i++;
					if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
						temp += science.getImage().getFileName() + "\t" + science.getLamp().getImage().getFileName()
								+ "\n";
						lampName.add(science.getLamp().getImage().getFileName());
						i++;
					}
				}
				for (String item : lampName)
					tempLamp += item + "\n";

				writer.print(temp.trim());
				writer.close();

				temp = "";
				log.info("list_lamps_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) != null)
					this.generatedList.get(observation.getTargetName()).add("list_lamps_" + targetNormalized);
				else {
					ArrayList<String> list = new ArrayList<>();
					list.add("list_lamps_" + targetNormalized);
					this.generatedList.put(observation.getTargetName(), list);
				}
				writer = new PrintWriter(Paths.get(this.basePath, "list_lamps_" + targetNormalized).toFile());
				writer.print(tempLamp.trim());
				writer.close();

				log.info("list_flat_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) != null)
					this.generatedList.get(observation.getTargetName()).add("list_flat_" + targetNormalized);
				else {
					ArrayList<String> list = new ArrayList<>();
					list.add("list_flat_" + targetNormalized);
					this.generatedList.put(observation.getTargetName(), list);
				}
				writer = new PrintWriter(Paths.get(this.basePath, "list_flat_" + targetNormalized).toFile());
				for (FlatfieldImage flat : dataService.getFlatRepository().findAll()) {
					for (ImageEntity image : flat.getImages())
						temp += image.getFileName() + "\n";
				}
				writer.print(temp.trim());
				writer.close();

				// Generate the list of IMA*.wl.fits
				if (observation.isDoWlcal()) {
					log.info("wl" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("wl" + targetNormalized);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("wl" + targetNormalized);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "wl" + targetNormalized).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".wl\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}

				// Generate the list of IMA*.fc.fits
				// This list is used as input for the background task
				if (observation.isDoFcal()) {
					log.info("fc" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("fc" + targetNormalized);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("fc" + targetNormalized);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "fc" + targetNormalized).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".fc\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}

				// Generate the list of IMA*.bg.fits
				if (observation.isDoBackground()) {
					log.info("bg" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("bg" + targetNormalized);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("bg" + targetNormalized);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "bg" + targetNormalized).toFile());
					temp = "";
					int i = 0;
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".bg\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}

				// Generate the list of IMA*.md.fits
				if (observation.isDoApall()) {
					log.info("md" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) != null)
						this.generatedList.get(observation.getTargetName()).add("md" + targetNormalized);
					else {
						ArrayList<String> list = new ArrayList<>();
						list.add("md" + targetNormalized);
						this.generatedList.put(observation.getTargetName(), list);
					}
					writer = new PrintWriter(Paths.get(this.basePath, "md" + targetNormalized).toFile());
					temp = "";
					for (ScienceImage item : observation.getScienceImages()) {
						TableModel model = jTable1.getModel();
						int i = 0;
						while (i < model.getRowCount()
								&& !model.getValueAt(i, jTable1Cols.get("Image")).equals(item.getImage().getFileName()))
							i++;
						if ((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")) == true) {
							temp += item.getImage().getFileName() + ".md\n";
							i++;
						}
					}
					writer.print(temp.trim());
					writer.close();
				}

				// Let's generate the PyRAF script
				log.info("exec" + targetNormalized + ".py");
				scriptsList.add("exec" + targetNormalized + ".py");
				if (this.generatedList.get("script") != null)
					this.generatedList.get("script").add("exec" + targetNormalized + ".py");
				else {
					ArrayList<String> list = new ArrayList<>();
					list.add("exec" + targetNormalized + ".py");
					this.generatedList.put("script", list);
				}
				writer = new PrintWriter(Paths.get(this.basePath, "exec" + targetNormalized + ".py").toFile());
				writer.println("#!/usr/bin/env python");
				writer.println("import os");
				writer.println("import sys");
				writer.println("os.chdir(\"" + dataService.getProperty("iraf.home") + "\")");

				writer.println("from pyraf import iraf");
				writer.println("os.chdir(\"" + this.basePath + "\")");

				// exec prered2
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("prered2", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.println("iraf.asgred.prered2(flat=\"list_flat_" + targetNormalized + "\", comp=\"list_lamps_"
						+ targetNormalized + "\", object=\"list_obj_" + targetNormalized + "\", order=11)");
				// exec wlcal
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("wlcal", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				String firstLamp = dataService.getLampRepository().findAll().iterator().next().getImage().getFileName();
				writer.println(
						"iraf.asgred.wlcal(input=\"list_obj_" + targetNormalized + "\", refer=\"" + firstLamp + "\")");

				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("fcal", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.println("iraf.asgred.fcal(obj=\"wl" + targetNormalized + "\", stand=\""
						+ observation.getStandard().getImage().getFileName() + "\", dir=\"onedstds$"
						+ dataService.getStandardAtlas()
								.findByStandardName(observation.getStandard().getImage().getTargetName())
								.getCatalogueName()
						+ "\", " + "star=\""
						+ dataService.getStandardAtlas()
								.findByStandardName(observation.getStandard().getImage().getTargetName()).getDatName()
						+ "\")");

				// exec background
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("background", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoBackground())
					writer.println("iraf.asgred.background(input=\"fc" + targetNormalized + "\", output=\"bg"
							+ targetNormalized + "\")");

				// exec apall
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("apall", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoApall())
					writer.println("iraf.asgred.apall(input=\"@bg" + targetNormalized + "\", output=\"@md"
							+ targetNormalized + "\")");

				// exec scombine
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("scombine", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoScombine())
					writer.println("iraf.asgred.scombine(input=\"md" + targetNormalized + "\", output=\""
							+ targetNormalized + ".md\")");

				String start = this.jImcopyStart.getValue().toString();
				String end = this.jImcopyEnd.getValue().toString();

				// exec imcopy
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("imcopy", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoImcopy())
					writer.println("iraf.asgred.imcopy(input=\"" + targetNormalized + ".md[" + start + ":" + end
							+ "]\", output=\"" + targetNormalized + ".obj\")");
			} catch (IOException ex) {
				log.fatal(ex.getMessage(), ex);
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				if (writer != null)
					writer.close();
			}
			log.info("Done.");
		}
	}

	public void writeOneGiantScript() {
		PrintWriter writer = null;
		try {
			log.info("Generating one script...");
			// Let's generate the PyRAF script
			log.info("execAsgred.py");
			if (this.generatedList.get("script") != null)
				this.generatedList.get("script").add("execAsgred.py");
			else {
				ArrayList<String> list = new ArrayList<>();
				list.add("execAsgred.py");
				this.generatedList.put("script", list);
			}
			scriptsList = new ArrayList<String>();
			scriptsList.add("execAsgred.py");
			writer = new PrintWriter(Paths.get(this.basePath, "execAsgred.py").toFile());
			writer.println("#!/usr/bin/env python");
			writer.println("import os");
			writer.println("import sys");
			writer.println("os.chdir(\"" + dataService.getProperty("iraf.home") + "\")");

			writer.println("from pyraf import iraf");
			writer.println("os.chdir(\"" + this.basePath + "\")");
			// exec prered2
			writer.println("\n" + new String(new char[80]).replace("\0", "#"));
			writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("prered2", 78));
			writer.println(new String(new char[80]).replace("\0", "#"));
			writer.println(
					"iraf.asgred.prered2(flat=\"list_flat\", comp=\"list_lamps\", object=\"list_obj\", outflat=\"Flat\", order=11, mode=\"q\")");
			// exec wlcal
			writer.println("\n" + new String(new char[80]).replace("\0", "#"));
			writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("wlcal", 78));
			writer.println(new String(new char[80]).replace("\0", "#"));

			String firstLamp = dataService.getLampRepository().findAll().iterator().next().getImage().getFileName();
			writer.println("iraf.asgred.wlcal(input=\"list_obj\", refer=\"" + firstLamp + "\")");

			// exec fcal
			for (StandardImage standard : dataService.getStandardRepository().findAll()) {
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils
						.center("Flux calibration for " + standard.getImage().getTargetName(), 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (!standard.getObservations().isEmpty()) {
					writer.println("iraf.asgred.fcal(obj=\"std" + standard.getImage().getFileName() + "\", stand=\""
							+ standard.getImage().getFileName() + "\", dir=\"onedstds$"
							+ dataService.getStandardAtlas().findByStandardName(standard.getImage().getTargetName())
									.getCatalogueName()
							+ "\", " + "star=\"" + dataService.getStandardAtlas()
									.findByStandardName(standard.getImage().getTargetName()).getDatName()
							+ "\")");
				}
			}
			for (Observation observation : dataService.getObservationRepository().findByIsEnabled(true)) {
				writer.println("\n" + new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils
						.center("reduction for " + observation.getTargetName(), 78));
				writer.println(new String(new char[80]).replace("\0", "#"));

				String targetNormalized = observation.getTargetName();
				if (targetNormalized.contains("+"))
					targetNormalized = targetNormalized.replaceAll("+", "p");
				if (targetNormalized.contains("-"))
					targetNormalized = targetNormalized.replaceAll("-", "m");

				// exec background
				if (observation.isDoBackground())
					writer.println("iraf.asgred.background(input=\"fc" + targetNormalized + "\", output=\"bg"
							+ targetNormalized + "\")");

				// exec apall
				if (observation.isDoApall())
					writer.println("iraf.asgred.apall(input=\"@bg" + targetNormalized + "\", output=\"@md"
							+ targetNormalized + "\")");

				// exec scombine
				if (observation.isDoScombine())
					writer.println("iraf.asgred.scombine(input=\"md" + targetNormalized + "\", output=\""
							+ targetNormalized + ".md\")");

				String start = this.jImcopyStart.getValue().toString();
				String end = this.jImcopyEnd.getValue().toString();

				// exec imcopy
				if (observation.isDoImcopy())
					writer.println("iraf.asgred.imcopy(input=\"" + targetNormalized + ".md[" + start + ":" + end
							+ "]\", output=\"" + targetNormalized + ".obj\")");
			}
		} catch (IOException ex) {
			log.fatal(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (writer != null)
				writer.close();
		}
		log.info("Done.");
	}

	private void jPrintTODOActionPerformed(ActionEvent e) {
		generateTODOList();
	}

	private void generateTODOList() {
		try {
			JTextPane documentToPrint = new JTextPane();
			StyleContext sc = new StyleContext();
			Style text = sc.addStyle("text", null);
			text.addAttribute(StyleConstants.FontSize, new Integer(10));
			text.addAttribute(StyleConstants.FontFamily, "arial");
			text.addAttribute(StyleConstants.Bold, new Boolean(false));

			Style title = sc.addStyle("title", null);
			title.addAttribute(StyleConstants.FontSize, new Integer(12));
			title.addAttribute(StyleConstants.FontFamily, "arial");
			title.addAttribute(StyleConstants.Bold, new Boolean(true));
			String temp = "";
			for (Observation observation : dataService.getObservationRepository().findByIsEnabled(true)) {
				try {
					temp = observation.getTargetName();
					if (observation.getStandard().getImage().getTargetName().equals(observation.getTargetName()))
						temp += " (Standard)";
					else
						temp += " (Object)";
					temp += ":\n";
					documentToPrint.getStyledDocument().insertString(documentToPrint.getStyledDocument().getLength(),
							temp, title);
					temp = new String(new char[120]).replace("\0", "-") + "\n";
					documentToPrint.getStyledDocument().insertString(documentToPrint.getStyledDocument().getLength(),
							temp, text);
					temp = "";
					if (observation.isDoPrered())
						temp += "\t⬜ flatfield correction\n\r";
					if (observation.isDoWlcal())
						temp += "\t⬜ wavelength calibration\n\r";
					if (observation.isDoFcal())
						temp += "\t⬜ flux calibration\n\r";
					if (observation.isDoBackground())
						temp += "\t⬜ background subtraction\n\r";
					if (observation.isDoApall())
						temp += "\t⬜ spectrum extraction\n\r";
					if (observation.isDoScombine())
						temp += "\t⬜ spectrum combine\n\r";
					if (observation.isDoImcopy())
						temp += "\t⬜ cut final spectrum\n\r";
					temp += "\n\n";
					documentToPrint.getStyledDocument().insertString(documentToPrint.getStyledDocument().getLength(),
							temp, text);
					
				} catch (Exception ex) {

				}
			}

			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(documentToPrint.getPrintable(new MessageFormat("TODO list"),
					new MessageFormat("AsgredLists")));
			if (pj.printDialog()) {
				try {
					pj.print();
				} catch (PrinterException exc) {
					System.out.println(exc);
				}
			}
		} catch (Exception e) {
			log.error(e);
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {

		}
	}

	protected void jListGeneratedActionPerformed(ActionEvent e) {
		GeneratedList.showList(generatedList);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.ButtonGroup groupAction;
	private javax.swing.ButtonGroup groupExplore;
	private javax.swing.JPanel jAdvancedOptionsPanel;
	private javax.swing.JSpinner jBackgroundEnd;
	private javax.swing.JSpinner jBackgroundStart;
	private javax.swing.JTextField jCommand;
	private JTextPane jConsole;
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
	private javax.swing.JCheckBox jCheckStartFromScrap;
	private javax.swing.JButton jViewScripts;
	private javax.swing.JButton jRunScripts;
	private javax.swing.JButton jPrintTODO;
	private final ButtonGroup groupSteps = new ButtonGroup();
	private JButton jListGenerated;
}