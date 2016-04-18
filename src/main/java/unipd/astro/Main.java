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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
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
import java.awt.event.MouseListener;
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

@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
public class Main extends javax.swing.JPanel {
	private static Logger log = Logger.getLogger(Main.class.getName());
	private HashMap<String, Integer> jTable1Cols = new HashMap<>();
	private HashMap<String, Integer> jTable2Cols = new HashMap<>();
	private HashMap<String, String> tempStandards = new HashMap<>();
	private HashMap<String, String> tempLamps = new HashMap<>();
	private HashMap<String, List<String>> generatedList;
	private DataService dataService;

	private JComboBox comboStd, comboLinesList;
	private JComboBox comboLamps;
	private ActionListener autoHide = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			jTable1.clearSelection();
			((JComboBox) evt.getSource()).hidePopup();
			((JComboBox) evt.getSource()).setSelectedIndex(-1);
		}
	};
	private boolean sendRms;
	private int selectedAction = 0, runningCommand = -1, task = 0, groups = 0;
	private Style In, Out, Error;
	private List<String> scriptsList;
	private String basePath;
	private Execution process;
	private double rms = 0;
	private AsyncCallback callback = new AsyncCallback() {
		@Override
		public void OnResponseReceived(String response) {
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\t" + response + "\n", Out);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());

				if (response.startsWith("prered2"))
					task = 1;
				else if (response.startsWith("wlcal"))
					task = 2;
				else if (response.startsWith("fcal"))
					task = 3;
				else if (response.startsWith("background"))
					task = 4;
				else if (response.startsWith("apall"))
					task = 5;
				else if (response.startsWith("scombine"))
					task = 6;
				else if (response.startsWith("imcopy"))
					task = 7;

				switch (task) {
				case 1:
					if (response.startsWith("Dispersion axis (1=along lines, 2=along columns, 3=along z) (1:3)")
							|| (response.startsWith("Fit the normalization spectrum for")
									&& response.endsWith(".b interactively (yes):")))
						process.sendCommand(runningCommand, "\n");
					break;
				case 2:
					if (response.startsWith("Dispersion axis (1=along lines, 2=along columns, 3=along z) (1:3)")
							|| response.startsWith("Write coordinate map to the database"))
						process.sendCommand(runningCommand, "\n");
					else {
						String[] data = response.split(" ");
						if (data[0].contains(".b[*,")) {
							// Is RMS lower than the threshold?
							double actualRms = Double.parseDouble(data[data.length - 1]);
							if (actualRms < rms)
								// Yes: accept the interpolation
								if (sendRms)
									process.sendTextToGraphics("q");
							sendRms = !sendRms;
						}
					}
					break;
				case 3:
				case 5:
					if (response.startsWith("Find") || response.startsWith("Resize") || response.startsWith("Edit")
							|| response.startsWith("Trace") || response.startsWith("Fit")
							|| response.startsWith("Write") || response.startsWith("Extract")
							|| response.startsWith("Review"))
						process.sendKeyToGraphics("Return");
				}
			} catch (Exception e) {
				log.fatal(e);
			}
		}

		@Override
		public void OnScriptTerminated() {
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(), "◀\tterminated.\n",
						Out);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());
			} catch (BadLocationException e) {
				log.fatal(e);
			}
			if (scriptsList != null && scriptsList.size() > 0) {
				scriptsList.remove(0);
				if (scriptsList.size() > 0)
					runningCommand = process.startScript(Paths.get(basePath + "/" + scriptsList.get(0)).toString(),
							new String[] { ".exit" }, callback);
			}
		}

		@Override
		public void OnErrorReceived(String message) {
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\t" + message + "\n", Error);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());
			} catch (BadLocationException e) {
				log.fatal(e);
			}
		}

		@Override
		public void OnMessageSent(String message) {
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"▶\t" + message + "\n", In);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());
			} catch (BadLocationException e) {
				log.fatal(e);
			}
		}

	};

	public Main() {
		initComponents();
		finalInitComponents();
		startUp();
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
		this.jPath.setFont(new Font("Dialog", Font.BOLD, 12));
		jLoad = new javax.swing.JButton();
		jExplore = new javax.swing.JButton();
		jRadioFitsList = new javax.swing.JRadioButton();
		jRadioButton2 = new javax.swing.JRadioButton();
		jDirectoryPanel = new org.jdesktop.swingx.JXCollapsiblePane();
		jLabel16 = new javax.swing.JLabel();
		jPathDirectory = new javax.swing.JTextField();
		this.jPathDirectory.setFont(new Font("Dialog", Font.BOLD, 12));
		jLoadDirectory = new javax.swing.JButton();
		jExploreDirectory = new javax.swing.JButton();
		jShowStep1 = new javax.swing.JToggleButton();
		jShowStep1.setSelected(true);
		groupSteps.add(jShowStep1);
		jStep2 = new org.jdesktop.swingx.JXCollapsiblePane();
		this.jStep2.setCollapsed(true);
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jLabel2 = new javax.swing.JLabel();
		jStep2Next = new javax.swing.JButton();
		jOptionsPanel = new org.jdesktop.swingx.JXPanel();
		jLabel7 = new javax.swing.JLabel();
		jPanel10 = new javax.swing.JPanel();
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
		this.jIrafHome.setFont(new Font("Dialog", Font.BOLD, 12));
		this.jIrafHome.setEditable(false);
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

		jLoadDirectory.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLoadDirectory.setText("Load data");
		jLoadDirectory.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jLoadDirectoryActionPerformed(evt);
			}
		});

		jExploreDirectory.setText("Explore");
		jExploreDirectory.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jExploreDirectoryActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jDirectoryPanelLayout = new javax.swing.GroupLayout(jDirectoryPanel.getContentPane());
		jDirectoryPanel.getContentPane().setLayout(jDirectoryPanelLayout);
		jDirectoryPanelLayout.setHorizontalGroup(jDirectoryPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE)
				.addGroup(jDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
						jDirectoryPanelLayout.createSequentialGroup().addContainerGap().addGroup(jDirectoryPanelLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jDirectoryPanelLayout.createSequentialGroup().addGap(344, 344, 344)
										.addComponent(jLoadDirectory)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 514,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jDirectoryPanelLayout.createSequentialGroup().addComponent(jPathDirectory)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jExploreDirectory))
								.addGroup(jDirectoryPanelLayout.createSequentialGroup().addComponent(jLabel16).addGap(0,
										0, Short.MAX_VALUE)))
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
												.addComponent(jPathDirectory).addComponent(jExploreDirectory))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLoadDirectory).addGap(19, 19, 19))));

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

		this.popupMenu = new JPopupMenu();
		this.popupGroup = new JMenuItem();
		this.popupGroup.setText("Group together");
		this.popupGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popupGroupActionPerformed(e);
			}
		});
		this.popupDisband = new JMenuItem();
		this.popupDisband.setText("Disband group");
		this.popupDisband.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popupDisbandActionPerformed(e);
			}
		});
		this.popupMenu.add(this.popupGroup);
		this.popupMenu.add(this.popupDisband);

		jTable1.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Enabled", "Image", "Target name",
				"Type", "Is standard?", "Exp Time", "Lines list", "Lamp", "Standard" }) {
			Class[] columnTypes = new Class[] { Boolean.class, String.class, String.class, String.class, Boolean.class,
					Float.class, String.class, String.class, String.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		this.jTable1.getColumnModel().getColumn(0).setPreferredWidth(55);
		this.jTable1.getColumnModel().getColumn(1).setPreferredWidth(60);
		this.jTable1.getColumnModel().getColumn(2).setPreferredWidth(200);
		this.jTable1.getColumnModel().getColumn(3).setPreferredWidth(60);
		this.jTable1.getColumnModel().getColumn(4).setPreferredWidth(55);
		this.jTable1.getColumnModel().getColumn(5).setPreferredWidth(55);
		this.jTable1.getColumnModel().getColumn(6).setPreferredWidth(60);
		this.jTable1.getColumnModel().getColumn(7).setPreferredWidth(90);
		this.jTable1.getColumnModel().getColumn(8).setPreferredWidth(90);
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
		jTable1.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				jTable1MouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		});
		jScrollPane1.setViewportView(jTable1);

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
		jOptionsPanelLayout.setHorizontalGroup(jOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jOptionsPanelLayout.createSequentialGroup()
						.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.TRAILING, false)
								.addGroup(Alignment.LEADING,
										jOptionsPanelLayout.createSequentialGroup().addContainerGap().addComponent(
												this.jLabel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
								.addGroup(Alignment.LEADING,
										jOptionsPanelLayout.createSequentialGroup().addGap(20)
												.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.TRAILING)
														.addComponent(this.jLabel5).addComponent(this.jLabel6)
														.addComponent(this.jLabel7).addComponent(this.jLabel8)
														.addComponent(this.jLabel9))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(this.jPanel6, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(this.jPanel10, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(this.jPanel11, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(this.jPanel8, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(this.jPanel9, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addContainerGap(22, Short.MAX_VALUE)));
		jOptionsPanelLayout.setVerticalGroup(jOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(jOptionsPanelLayout.createSequentialGroup().addContainerGap().addComponent(this.jLabel4)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(jOptionsPanelLayout.createSequentialGroup().addGroup(jOptionsPanelLayout
										.createParallelGroup(Alignment.TRAILING)
										.addGroup(jOptionsPanelLayout.createSequentialGroup()
												.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.TRAILING)
														.addComponent(this.jLabel5).addComponent(this.jPanel6,
																GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jLabel6))
										.addComponent(this.jPanel8, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jLabel7))
								.addComponent(this.jPanel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(this.jPanel11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(this.jLabel8))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(jOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(this.jLabel9, Alignment.TRAILING).addComponent(this.jPanel10,
										Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));
		jOptionsPanel.setLayout(jOptionsPanelLayout);
		jSolve = new javax.swing.JButton();

		jSolve.setText("<html><center>Try to resolve<br>conflicts</center></html>");
		jSolve.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jSolveActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jStep2Layout = new javax.swing.GroupLayout(jStep2.getContentPane());
		jStep2Layout
				.setHorizontalGroup(
						jStep2Layout.createParallelGroup(Alignment.LEADING)
								.addGroup(
										jStep2Layout.createSequentialGroup().addContainerGap()
												.addGroup(jStep2Layout
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(jStep2Layout.createSequentialGroup()
																.addGroup(jStep2Layout
																		.createParallelGroup(Alignment.LEADING)
																		.addComponent(this.jLabel2)
																		.addComponent(this.jScrollPane1,
																				GroupLayout.DEFAULT_SIZE, 972,
																				Short.MAX_VALUE))
										.addGap(18)
										.addGroup(jStep2Layout.createParallelGroup(Alignment.TRAILING)
												.addComponent(this.jOptionsPanel, GroupLayout.PREFERRED_SIZE, 142,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(this.jSolve, GroupLayout.PREFERRED_SIZE, 175,
														GroupLayout.PREFERRED_SIZE))).addComponent(this.jStep2Next,
																Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 95,
																GroupLayout.PREFERRED_SIZE))
												.addContainerGap()));
		jStep2Layout.setVerticalGroup(jStep2Layout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jStep2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jStep2Layout.createParallelGroup(Alignment.TRAILING)
								.addGroup(jStep2Layout.createSequentialGroup().addComponent(this.jLabel2).addGap(18)
										.addComponent(this.jScrollPane1, GroupLayout.PREFERRED_SIZE, 167,
												GroupLayout.PREFERRED_SIZE))
								.addGroup(jStep2Layout.createSequentialGroup()
										.addComponent(this.jOptionsPanel, 0, 0, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(this.jSolve, GroupLayout.PREFERRED_SIZE, 54,
												GroupLayout.PREFERRED_SIZE)
										.addGap(6)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(this.jStep2Next, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
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
		jTabbedPane1.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				jTabbedPane1MouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
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
	}

	private void startUp() {
		try {
			rms = (double) (jWlcalThreshold.getValue() / 100d);
			// Start the console session under Linux
			String OS = System.getProperty("os.name").toLowerCase();
			if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
				this.runningCommand = process.startCommand("script -fqe /dev/null", new String[] { "exit" }, callback);
				Thread.sleep(5);
				process.sendCommand(this.runningCommand, "cd " + dataService.getProperty("iraf.home"));
				process.sendCommand(this.runningCommand, "pyraf", ".exit");
				process.sendCommands(this.runningCommand,
						new String[] {
								"pyexecute "
										+ Paths.get(dataService.getProperty("iraf.home"), "loadTasks.py").toString(),
								"?" });
			} else {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"◀\tThe terminal is available only on Linux systems.", Error);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());
			}
		} catch (Exception e) {
			log.fatal(e);
		}
	}

	private void finalInitComponents() {
		if (Runtime.getRuntime() != null)
			dataService = DataService.getInstance();

		this.process = new Execution();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				process.dispose();
			}
		}));

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

				if (row >= 0) {
					Color color = Color.WHITE;
					this.setForeground(Color.BLACK);
					// Lamps
					if (table.getValueAt(row, jTable1Cols.get("Type")).equals("LAMP"))
						color = new Color(255, 243, 115);
					// Conflicts
					else if (table.getValueAt(row, jTable1Cols.get("Type")).equals("IMAGE")
							&& !(tempLamps.containsKey(table.getValueAt(row, jTable1Cols.get("Image")))
									&& tempStandards.containsKey(table.getValueAt(row, jTable1Cols.get("Image"))))) {
						color = new Color(204, 0, 0);
						this.setForeground(Color.WHITE);
					} // Objects and standards
					else if (table.getValueAt(row, jTable1Cols.get("Type")).equals("IMAGE")
							&& tempLamps.containsKey(table.getValueAt(row, jTable1Cols.get("Image")))
							&& tempStandards.containsKey(table.getValueAt(row, jTable1Cols.get("Image")))) {
						if ((boolean) table.getValueAt(row, jTable1Cols.get("Is standard?")))
							// Standard
							color = new Color(114, 159, 207);
						else
							// Objects
							color = new Color(149, 226, 158);
					} // Flatfield
					else if (table.getValueAt(row, jTable1Cols.get("Type")).equals("FLATFIELD"))
						color = new Color(255, 252, 216);
					setBackground(color);
				}
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				super.setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}
		});

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
	}

	private void jRunScriptsActionPerformed(ActionEvent e) {
		if (this.generatedList.get("script") != null && this.generatedList.get("script").size() > 0) {
			log.info("Executing scripts...");
			this.jTabbedPane1.setSelectedIndex(1);
			try {
				jConsole.getStyledDocument().insertString(jConsole.getStyledDocument().getLength(),
						"Ready to execute: insert a command below to execute it...\n", In);
				jConsole.setCaretPosition(jConsole.getDocument().getLength());
			} catch (BadLocationException e1) {
				log.fatal(e1);
			}
			if (this.runningCommand != -1) {
				sendRms = true;
				process.sendCommand(runningCommand, "cd " + this.basePath);
				for (String script : this.generatedList.get("script"))
					process.sendCommand(runningCommand, "pyexecute " + script);
			}
		} else
			log.info("Nothing to do.");
		log.info("Done.");
	}

	/**
	 * Show the scripts using gEdit
	 */
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

	private void jSelectIrafActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jWlcalThresholdMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		this.jWlcalThreshold.setValue(this.jWlcalThreshold.getValue() - evt.getWheelRotation());
	}

	private void jWlcalThresholdStateChanged(javax.swing.event.ChangeEvent evt) {
		this.jLabel13.setText(String.valueOf(this.jWlcalThreshold.getValue()) + " %");
		rms = (double) (jWlcalThreshold.getValue() / 100d);
	}

	private void jBackgroundEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		if (((int) this.jBackgroundEnd.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jBackgroundEnd.getValue() < 2048 && evt.getWheelRotation() < 0))
			this.jBackgroundEnd.setValue((int) this.jBackgroundEnd.getValue() - evt.getWheelRotation());
	}

	private void jBackgroundStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		if (((int) this.jBackgroundStart.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jBackgroundStart.getValue() < 2048 && evt.getWheelRotation() < 0))
			this.jBackgroundStart.setValue((int) this.jBackgroundStart.getValue() - evt.getWheelRotation());
	}

	private void jImcopyStartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		if (((int) this.jImcopyStart.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jImcopyStart.getValue() < 2048 && evt.getWheelRotation() < 0))
			this.jImcopyStart.setValue((int) this.jImcopyStart.getValue() - evt.getWheelRotation());
	}

	private void jImcopyEndMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
		if (((int) this.jImcopyEnd.getValue() > 0 && evt.getWheelRotation() > 0)
				|| ((int) this.jImcopyEnd.getValue() < 2048 && evt.getWheelRotation() < 0))
			this.jImcopyEnd.setValue((int) this.jImcopyEnd.getValue() - evt.getWheelRotation());
	}

	private void jSendActionPerformed(java.awt.event.ActionEvent evt) {
		String message = this.jCommand.getText();
		if (message.equals("iraf"))
			if (JOptionPane.showConfirmDialog(this,
					"Running IRAF from AsgredLists is highly discouraged due to compatibility issues.\nMoreover AsgredLists won't be able to interact with IRAF.\nDo you want to run IRAF anyway?",
					"Wait", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				this.jCommand.setText("");
				return;
			}
		jConsole.setCaretPosition(jConsole.getDocument().getLength());
		if (runningCommand == -1) {
			process = new Execution();
			if (message.equals("clear"))
				this.jConsole.setText("");
			else if (message.equals("start pyraf")) {
				this.runningCommand = process.startCommand("script -fqe /dev/null", new String[] { "exit" }, callback);
				process.sendCommand(this.runningCommand, "cd " + dataService.getProperty("iraf.home"));
				process.sendCommand(this.runningCommand, "pyraf", ".exit");
			} else
				this.runningCommand = process.startCommand(this.jCommand.getText(), null, callback);
		} else
			process.sendCommand(this.runningCommand, message);
		this.jCommand.setText("");
	}

	private void jCommandKeyTyped(java.awt.event.KeyEvent evt) {
		if (evt.getKeyChar() == '\n')
			this.jSend.doClick();
	}

	private void jDoItActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			if (this.jCheckStartFromScrap.isSelected()) {
				if (this.runningCommand >= 0 && this.process.isPyraf(runningCommand))
					this.process.sendCommands(runningCommand,
							new String[] { "cd " + this.basePath, "!rm *.wc.fits", "!rm *.fc.fits", "!rm *.bg.fits",
									"!rm *.md.fits", "!rm *.py", "!rm wc*", "!rm fc*", "!rm md*", "!rm std*",
									"!rm list_*", "!rm *.b.fits", "!rm *.bf.fits", "!rm flat.*", "!rm *.std",
									"!rm *.sens.fits", "!rm -r database/", "!rm *.obj.fits" });
				Thread.sleep(500);
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
	}

	private void jRadioListsAndMultipleScriptsActionPerformed(java.awt.event.ActionEvent evt) {
		this.selectedAction = 2;
	}

	private void jRadioListsAndOneScriptActionPerformed(java.awt.event.ActionEvent evt) {
		this.selectedAction = 1;
	}

	private void jRadioListsOnlyActionPerformed(java.awt.event.ActionEvent evt) {
		this.selectedAction = 0;
	}

	private void jSolveActionPerformed(java.awt.event.ActionEvent evt) {
		log.info("Attempting to fix conflicts...");
		int lamp_fixed = 0, standard_fixed = 0;
		// FIX LAMPS FIRST
		String lampFileName = "";
		int rowLastLamp = 0;
		TableModel model = this.jTable1.getModel();
		for (int row = 0; row < model.getRowCount(); row++) {
			if ((boolean) model.getValueAt(row, jTable1Cols.get("Enabled"))
					&& ((String) model.getValueAt(row, jTable1Cols.get("Type"))).equals("LAMP")) {
				ImageEntity image = dataService.getImageRepository()
						.findByFileName((String) model.getValueAt(row, jTable1Cols.get("Image")));
				if (image.isGrouped()) {
					lampFileName = (String) model.getValueAt(row, jTable1Cols.get("Target name"));
					lampFileName = lampFileName.substring(lampFileName.indexOf("(") + 1, lampFileName.indexOf(")"));
				} else
					lampFileName = (String) model.getValueAt(row, jTable1Cols.get("Image"));
				for (int i = rowLastLamp; i < row; i++)
					if ("IMAGE".equals(model.getValueAt(i, jTable1Cols.get("Type"))))
						if ("".equals(model.getValueAt(i, jTable1Cols.get("Lamp")))) {
							model.setValueAt(lampFileName, i, jTable1Cols.get("Lamp"));
							tempLamps.put((String) model.getValueAt(i, jTable1Cols.get("Image")), lampFileName);
							lamp_fixed++;
						}
				rowLastLamp = row;
			}
		}
		// use the last lamp for all the remaining images without a lamp
		for (int i = rowLastLamp; i < model.getRowCount(); i++) {
			if (((boolean) model.getValueAt(i, jTable1Cols.get("Enabled")))
					&& ((String) model.getValueAt(i, jTable1Cols.get("Type"))).equals("IMAGE")) {
				if ("".equals(model.getValueAt(i, jTable1Cols.get("Lamp")))) {
					model.setValueAt(lampFileName, i, jTable1Cols.get("Lamp"));
					tempLamps.put((String) model.getValueAt(i, jTable1Cols.get("Image")), lampFileName);
					lamp_fixed++;
				}
			}
		}
		// FIX STANDARDS LAST
		String standardFileName = "";
		int rowLastStandard = 0;
		for (int row = 0; row < model.getRowCount(); row++) {
			if (((boolean) model.getValueAt(row, jTable1Cols.get("Enabled")))
					&& (Boolean) model.getValueAt(row, jTable1Cols.get("Is standard?"))) {
				standardFileName = (String) model.getValueAt(row, jTable1Cols.get("Image"));
				for (int i = rowLastStandard; i < row; i++) {
					if ("IMAGE".equals(model.getValueAt(i, jTable1Cols.get("Type")))
							&& (Boolean) model.getValueAt(i, jTable1Cols.get("Is standard?")) == false) {
						if ("".equals(model.getValueAt(i, jTable1Cols.get("Standard")))) {
							model.setValueAt(standardFileName, i, jTable1Cols.get("Standard"));
							tempStandards.put((String) model.getValueAt(i, jTable1Cols.get("Image")), standardFileName);
							standard_fixed++;
						}
					}
				}
				rowLastStandard = row;
			}
		}
		// use the last standard for all the remaining images without a standard
		for (int i = rowLastStandard; i < model.getRowCount(); i++) {
			if ("IMAGE".equals(model.getValueAt(i, jTable1Cols.get("Type")))
					&& (Boolean) model.getValueAt(i, jTable1Cols.get("Is standard?")) == false) {
				if ("".equals(model.getValueAt(i, jTable1Cols.get("Standard")))) {
					model.setValueAt(standardFileName, i, jTable1Cols.get("Standard"));
					tempStandards.put((String) model.getValueAt(i, jTable1Cols.get("Image")), standardFileName);
					standard_fixed++;
				}
			}
		}
		this.updateImagesTable();
		log.info("Done.");
		JOptionPane.showMessageDialog(this, String.valueOf(lamp_fixed + standard_fixed) + " conflicts solved ("
				+ String.valueOf(lamp_fixed) + " lamps; " + String.valueOf(standard_fixed) + " standards).");
	}

	private void jTable1PropertyChange(java.beans.PropertyChangeEvent evt) {
		int x = this.jTable1.getSelectedRow();
		int y = this.jTable1.getSelectedColumn();
		if (x != -1) {
			if (y == jTable1Cols.get("Type")) {
				ImageEntity image = dataService.getImageRepository()
						.findByFileName((String) this.jTable1.getValueAt(x, jTable1Cols.get("Image")));
				image.setType((String) this.jTable1.getValueAt(x, jTable1Cols.get("Type")));
				dataService.getImageRepository().save(image);
				updateComboStd();
				if (image.getType().equals("IMAGE"))
					updateComboLamps();
			} else if (y == jTable1Cols.get("Is standard?")) {
				ImageEntity image = dataService.getImageRepository()
						.findByFileName((String) this.jTable1.getValueAt(x, jTable1Cols.get("Image")));
				image.setIsStandard((boolean) this.jTable1.getValueAt(x, jTable1Cols.get("Is standard?")));
				dataService.getImageRepository().save(image);
				// Propagate consequences
				if (!image.isEnabled()) {
					TableModel model = this.jTable1.getModel();
					for (int i = 0; i < model.getRowCount(); i++)
						if (image.getFileName().equals(model.getValueAt(i, jTable1Cols.get("Standard")))) {
							String key = (String) this.jTable1.getValueAt(i, this.jTable1Cols.get("Image"));
							if (tempStandards.containsKey(key))
								tempStandards.remove(key);
							model.setValueAt("", i, jTable1Cols.get("Standard"));
						}
				} else {
					String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
					tempStandards.put(key, key);
					jTable1.setValueAt(jTable1.getValueAt(x, jTable1Cols.get("Image")), x, jTable1Cols.get("Standard"));
				}
				updateComboStd();
			} else if (y == jTable1Cols.get("Enabled")) {
				ImageEntity image = dataService.getImageRepository()
						.findByFileName((String) this.jTable1.getValueAt(x, jTable1Cols.get("Image")));
				image.setEnabled((boolean) jTable1.getValueAt(x, y));
				dataService.getImageRepository().save(image);
				// Propagate consequences for standards
				if (image.isStandard()) {
					if (!image.isEnabled()) {
						TableModel model = this.jTable1.getModel();
						for (int i = 0; i < model.getRowCount(); i++)
							if (model.getValueAt(i, jTable1Cols.get("Standard")) != null
									&& model.getValueAt(i, jTable1Cols.get("Standard")).equals(image.getFileName())) {
								String key = (String) this.jTable1.getValueAt(i, this.jTable1Cols.get("Image"));
								if (tempStandards.containsKey(key))
									tempStandards.remove(key);
								model.setValueAt("", i, jTable1Cols.get("Standard"));
							}
					} else {
						String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
						tempStandards.put(key, key);
						jTable1.setValueAt(jTable1.getValueAt(x, jTable1Cols.get("Image")), x,
								jTable1Cols.get("Standard"));
					}
					updateComboStd();
				}
				// Propagate consequences for lamps
				else if (image.getType().equals("LAMP")) {
					// HashSet<String> items = new HashSet<>();
					// for (int i = 0; i < comboLamps.getItemCount(); i++)
					// if (!image.getFileName().equals(comboLamps.getItemAt(i)))
					// items.add((String) comboLamps.getItemAt(i));
					// if it is grouped
					if (image.isGrouped()) {
						// disband the group and remove all associations
						DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
						String groupName = (String) model.getValueAt(x, jTable1Cols.get("Target name"));
						groupName = groupName.substring(groupName.indexOf("(") + 1, groupName.indexOf(")"));
						for (int i = 0; i < model.getRowCount(); i++) {
							if (groupName.equals(model.getValueAt(i, jTable1Cols.get("Lamp")))) {
								if (tempLamps.containsKey(model.getValueAt(i, jTable1Cols.get("Image"))))
									tempLamps.remove(model.getValueAt(i, jTable1Cols.get("Image")));
								model.setValueAt("", i, jTable1Cols.get("Lamp"));
							} else if (i != x && ((String) model.getValueAt(i, jTable1Cols.get("Target name")))
									.endsWith(groupName + ")")) {
								ImageEntity other = dataService.getImageRepository()
										.findByFileName((String) model.getValueAt(i, jTable1Cols.get("Image")));
								other.setGrouped(false);
								dataService.getImageRepository().save(other);
								model.setValueAt(other.getTargetName(), i, jTable1Cols.get("Target name"));
								// items.add(other.getFileName());
							} else if (i == x) {
								image.setGrouped(false);
								dataService.getImageRepository().save(image);
								model.setValueAt(image.getTargetName(), i, jTable1Cols.get("Target name"));
							}
						}
						// remove it from comboLamps
						// comboLamps.removeAllItems();
						// for (String item : items)
						// comboLamps.addItem(item);
					} else {
						// if it is not grouped
						// remove all associations
						DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
						for (int i = 0; i < model.getRowCount(); i++) {
							if (image.getFileName().equals(model.getValueAt(i, jTable1Cols.get("Lamp")))) {
								if (tempLamps.containsKey(model.getValueAt(i, jTable1Cols.get("Image"))))
									tempLamps.remove(model.getValueAt(i, jTable1Cols.get("Image")));
								model.setValueAt("", i, jTable1Cols.get("Lamp"));
							}
						}
						// remove it from comboLamps
						// comboLamps.removeAllItems();
						// for (String item : items)
						// comboLamps.addItem(item);
					}
					updateComboLamps();
				}
			} else if (y == jTable1Cols.get("Lamp")) {
				if ("".equals(this.jTable1.getValueAt(x, y))) {
					String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
					if (tempLamps.containsKey(key))
						tempLamps.remove(key);
				} else {
					String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
					tempLamps.put(key, (String) this.jTable1.getValueAt(x, y));
				}
			} else if (y == jTable1Cols.get("Standard")) {
				if ("".equals(this.jTable1.getValueAt(x, y))) {
					String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
					if (tempStandards.containsKey(key))
						tempStandards.remove(key);
				} else {
					String key = (String) this.jTable1.getValueAt(x, this.jTable1Cols.get("Image"));
					tempStandards.put(key, (String) this.jTable1.getValueAt(x, y));
				}
			}
			// else if (y == jTable1Cols.get("Lines list")) {
			// // change lineslist
			// // TODO: add support for different lines list for different
			// // lamps
			// if (((String) this.jTable1.getValueAt(x, y)) == null
			// || ((String) this.jTable1.getValueAt(x, y)).isEmpty()) {
			// if (((String) this.jTable1.getValueAt(x,
			// y)).toLowerCase().equals("fear"))
			// linelist = "FeAr";
			// else if (((String) this.jTable1.getValueAt(x,
			// y)).toLowerCase().equals("hefear"))
			// linelist = "HeFeAr";
			// else if (((String) this.jTable1.getValueAt(x,
			// y)).toLowerCase().equals("ne"))
			// linelist = "Ne";
			// else if (((String) this.jTable1.getValueAt(x,
			// y)).toLowerCase().equals("hgar"))
			// linelist = "HgAr";
			// else if (((String) this.jTable1.getValueAt(x,
			// y)).toLowerCase().equals("hgarne"))
			// linelist = "HgArNe";
			// }
			// }
		}
	}

	private void jExploreActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jLoadActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jShowStep1ActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jShowStep2ActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jShowStep3ActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jShowStep4ActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void jStep2NextActionPerformed(java.awt.event.ActionEvent evt) {
		this.populateDatabase();
		this.jShowStep3.doClick();
		this.updateTasksTable();
	}

	private void jTable2PropertyChange(java.beans.PropertyChangeEvent evt) {
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
	}

	private void jLoadDirectoryActionPerformed(java.awt.event.ActionEvent evt) {
		process.sendCommand(runningCommand, "cd " + this.jPathDirectory.getText());
		process.sendCommand(runningCommand,
				"iraf.hselect(images=\"????????.fits\", fields=\"$I,OBJECT,IMAGETYP,EXPTIME\", expr=\"yes\", Stdout=\"fits_list\")");
		this.jPath.setText(basePath + "/fits_list");
		this.jLoad.doClick();
	}

	private void jExploreDirectoryActionPerformed(java.awt.event.ActionEvent evt) {
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
			this.jPathDirectory.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void jRadioFitsListActionPerformed(java.awt.event.ActionEvent evt) {
		this.jFitsListPanel.setCollapsed(false);
		this.jDirectoryPanel.setCollapsed(true);
	}

	private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {
		this.jFitsListPanel.setCollapsed(true);
		this.jDirectoryPanel.setCollapsed(false);
	}

	private void jStep3NextActionPerformed(java.awt.event.ActionEvent evt) {
		this.jShowStep4.doClick();
	}

	private void jTable1MouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3 && jTable1.getSelectedRowCount() == 2) {
			popupMenu.setLocation(e.getLocationOnScreen());
			popupMenu.setVisible(true);
		} else
			popupMenu.setVisible(false);
	}

	private void jTabbedPane1MouseClicked(MouseEvent e) {
		if (popupMenu.isVisible()) {
			jTable1.clearSelection();
			popupMenu.setVisible(false);
		}
	}

	private void popupGroupActionPerformed(ActionEvent e) {
		groups++;
		popupMenu.setVisible(false);
		int rows[] = jTable1.getSelectedRows();
		TableModel model = jTable1.getModel();
		if (model.getValueAt(rows[0], jTable1Cols.get("Type")).equals("LAMP")
				&& model.getValueAt(rows[1], jTable1Cols.get("Type")).equals("LAMP")) {
			ImageEntity image1 = dataService.getImageRepository()
					.findByFileName((String) model.getValueAt(rows[0], jTable1Cols.get("Image")));
			ImageEntity image2 = dataService.getImageRepository()
					.findByFileName((String) model.getValueAt(rows[1], jTable1Cols.get("Image")));
			if (image1.isGrouped()) {
				// Disband the old group
				ImageEntity other = null;
				String key = (String) model.getValueAt(rows[0], jTable1Cols.get("Target name"));
				key = key.substring(key.indexOf("(") + 1, key.indexOf(")"));
				for (int i = 0; i < model.getRowCount(); i++)
					if (i != rows[0]
							&& ((String) model.getValueAt(i, jTable1Cols.get("Target name"))).endsWith(key + ")")) {
						other = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(i, jTable1Cols.get("Image")));
						model.setValueAt(other.getTargetName(), i, jTable1Cols.get("Target name"));
						other.setGrouped(false);
						dataService.getImageRepository().save(other);
						break;
					}
				// Delete old associations
				for (int i = 0; i < model.getRowCount(); i++) {
					if (i == rows[0] || key.equals(jTable1.getValueAt(i, jTable1Cols.get("Lamp")))) {
						jTable1.setValueAt("", i, jTable1Cols.get("Lamp"));
						if (tempLamps.containsKey(jTable1.getValueAt(i, jTable1Cols.get("Image"))))
							tempLamps.remove((String) jTable1.getValueAt(i, jTable1Cols.get("Image")));
					}
				}
			}
			if (image2.isGrouped()) {
				// Disband the old group
				ImageEntity other = null;
				String key = (String) model.getValueAt(rows[1], jTable1Cols.get("Target name"));
				key = key.substring(key.indexOf("(") + 1, key.indexOf(")"));
				for (int i = 0; i < model.getRowCount(); i++)
					if (i != rows[1]
							&& ((String) model.getValueAt(i, jTable1Cols.get("Target name"))).endsWith(key + ")")) {
						other = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(i, jTable1Cols.get("Image")));
						model.setValueAt(other.getTargetName(), i, jTable1Cols.get("Target name"));
						other.setGrouped(false);
						dataService.getImageRepository().save(other);
						break;
					}
				// Delete old associations
				for (int i = 0; i < model.getRowCount(); i++) {
					if (i == rows[1] || key.equals(jTable1.getValueAt(i, jTable1Cols.get("Lamp")))) {
						jTable1.setValueAt("", i, jTable1Cols.get("Lamp"));
						if (tempLamps.containsKey(jTable1.getValueAt(i, jTable1Cols.get("Image"))))
							tempLamps.remove((String) jTable1.getValueAt(i, jTable1Cols.get("Image")));
					}
				}
			}

			// create the new group
			image1.setGrouped(true);
			image2.setGrouped(true);
			dataService.getImageRepository().save(image1);
			dataService.getImageRepository().save(image2);
			// update the table
			model.setValueAt(image1.getTargetName() + " (LAMP" + groups + ")", rows[0], jTable1Cols.get("Target name"));
			model.setValueAt(image2.getTargetName() + " (LAMP" + groups + ")", rows[1], jTable1Cols.get("Target name"));
			model.setValueAt(true, rows[0], jTable1Cols.get("Enabled"));
			model.setValueAt(true, rows[1], jTable1Cols.get("Enabled"));
			model.setValueAt("HgArNe", rows[0], jTable1Cols.get("Lines list"));
			model.setValueAt("HgArNe", rows[1], jTable1Cols.get("Lines list"));
			// update the associations
			for (int i = 0; i < model.getRowCount(); i++) {
				if (image1.getFileName().equals(jTable1.getValueAt(i, jTable1Cols.get("Lamp")))
						|| image2.getFileName().equals(jTable1.getValueAt(i, jTable1Cols.get("Lamp")))) {
					jTable1.setValueAt("LAMP" + groups, i, jTable1Cols.get("Lamp"));
					if (tempLamps.containsKey(jTable1.getValueAt(i, jTable1Cols.get("Image"))))
						tempLamps.put((String) jTable1.getValueAt(i, jTable1Cols.get("Image")), "LAMP" + groups);
				}
			}
		}
		jTable1.clearSelection();
		updateComboLamps();
	}

	private void popupDisbandActionPerformed(ActionEvent e) {
		popupMenu.setVisible(false);
		int rows[] = jTable1.getSelectedRows();
		TableModel model = jTable1.getModel();
		for (int idx : rows) {
			ImageEntity image = dataService.getImageRepository()
					.findByFileName((String) model.getValueAt(idx, jTable1Cols.get("Image")));
			// Delete old group for image
			image.setGrouped(false);
			dataService.getImageRepository().save(image);
			String key = (String) model.getValueAt(idx, jTable1Cols.get("Target name"));
			model.setValueAt(image.getTargetName(), idx, jTable1Cols.get("Target name"));
			if (key.endsWith(")")) {
				key = key.substring(key.indexOf("(") + 1, key.indexOf(")"));
				// Delete old associations
				for (int i = 0; i < model.getRowCount(); i++) {
					if (((String) model.getValueAt(i, jTable1Cols.get("Target name"))).endsWith(key + ")")) {
						image = dataService.getImageRepository()
								.findByFileName((String) model.getValueAt(i, jTable1Cols.get("Image")));
						if (image.isGrouped()) {
							image.setGrouped(false);
							dataService.getImageRepository().save(image);
							model.setValueAt(image.getTargetName(), i, jTable1Cols.get("Target name"));
						}
					} else if (jTable1.getValueAt(i, jTable1Cols.get("Lamp")) != null
							&& jTable1.getValueAt(i, jTable1Cols.get("Lamp")).equals(key)) {
						if (tempLamps.containsKey(jTable1.getValueAt(i, jTable1Cols.get("Image"))))
							tempLamps.remove(jTable1.getValueAt(i, jTable1Cols.get("Image")));
						jTable1.setValueAt("", i, jTable1Cols.get("Lamp"));
					}
				}
			}
		}
		jTable1.clearSelection();
		updateComboLamps();
	}

	public void parseFile(String filePath) throws FileNotFoundException, IOException {
		log.info("Clearing jTable1...");
		DefaultTableModel model = ((DefaultTableModel) this.jTable1.getModel());
		for (int i = model.getRowCount() - 1; i > -1; i--)
			model.removeRow(i);
		log.info("Done.");
		log.info("Clearing temporary relationships...");
		tempLamps.clear();
		tempStandards.clear();
		log.info("Done.");
		log.info("Parsing " + filePath + "...");
		String line = "";
		BufferedReader fitsList = new BufferedReader(new FileReader(filePath));
		List<ImageEntity> tempImages = new ArrayList<>();
		log.info("Clearing database...");
		dataService.getObservationRepository().deleteAll();
		dataService.getScienceRepository().deleteAll();
		dataService.getStandardRepository().deleteAll();
		List<ImageEntity> images = dataService.getImageRepository().findByType("LAMP");
		for (ImageEntity image : images)
			image.setLamp(null);
		dataService.getImageRepository().save(images);
		dataService.getLampRepository().deleteAll();
		dataService.getFlatRepository().deleteAll();
		dataService.getImageRepository().deleteAll();
		log.info("Database cleared. Creating entities...");
		do {
			line = fitsList.readLine();
			log.info("Parsing \"" + line + "\"");
			// Ignore comments, empty lines and EOF
			if (line != null && !line.startsWith("#") && !line.equals("")) {
				ImageEntity item = ImageEntity.parseEntity(line);
				tempImages.add(item);
				log.info("Done. Creating the rows...");
				Object[] data = new Object[this.jTable1Cols.size()];
				data[this.jTable1Cols.get("Enabled")] = new Boolean(true);
				data[this.jTable1Cols.get("Image")] = item.getFileName();
				data[this.jTable1Cols.get("Target name")] = item.getTargetName();
				data[this.jTable1Cols.get("Exp Time")] = new Float(item.getExpTime());
				data[this.jTable1Cols.get("Is standard?")] = item.isStandard();
				data[this.jTable1Cols.get("Type")] = item.getType();
				data[this.jTable1Cols.get("Lines list")] = "";
				if (item.getType().equals("LAMP") && (item.getTargetName().toLowerCase().equals("fear")
						|| item.getTargetName().toLowerCase().equals("hefear")
						|| item.getTargetName().toLowerCase().equals("ne")
						|| item.getTargetName().toLowerCase().equals("hgar")
						|| item.getTargetName().toLowerCase().equals("hgarne"))) {
					String linelist = "";
					if (item.getTargetName().toLowerCase().equals("fear"))
						linelist = "FeAr";
					else if (item.getTargetName().toLowerCase().equals("hefear"))
						linelist = "HeFeAr";
					else if (item.getTargetName().toLowerCase().equals("ne"))
						linelist = "Ne";
					else if (item.getTargetName().toLowerCase().equals("hgar"))
						linelist = "HgAr";
					if (item.getTargetName().toLowerCase().equals("hgarne"))
						linelist = "HgArNe";

					data[this.jTable1Cols.get("Lines list")] = linelist;
				} else
					data[this.jTable1Cols.get("Lines list")] = "";
				data[this.jTable1Cols.get("Lamp")] = "";
				if (item.isStandard()) {
					data[this.jTable1Cols.get("Standard")] = item.getFileName();
					tempStandards.put(item.getFileName(), item.getFileName());
				}
				((DefaultTableModel) this.jTable1.getModel()).addRow(data);
			}
		} while (line != null);
		fitsList.close();
		log.info("Loading entities to the IMAGES table...");
		dataService.getImageRepository().save(tempImages);
		log.info("Done. File parsed.");
	}

	public void updateImagesTable() {
		log.info("Updating jTable1...");
		log.trace("Creating cell editors...");
		log.trace("Creating comboStd...");
		if (comboStd == null) {
			comboStd = new JComboBox();
			comboStd.addActionListener(autoHide);
			this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Standard"))
					.setCellEditor(new DefaultCellEditor(comboStd));
		}
		updateComboStd();
		log.trace("Done. Creating comboLamps...");
		if (comboLamps == null) {
			comboLamps = new JComboBox();
			comboLamps.addActionListener(autoHide);
			this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Lamp"))
					.setCellEditor(new DefaultCellEditor(comboLamps));
		}
		updateComboLamps();
		if (comboLinesList == null) {
			comboLinesList = new JComboBox();
			comboLinesList.addActionListener(autoHide);
			comboLinesList.addItem("FeAr");
			comboLinesList.addItem("HeFeAr");
			comboLinesList.addItem("Ne");
			comboLinesList.addItem("HgAr");
			comboLinesList.addItem("HgArNe");
			this.jTable1.getColumnModel().getColumn(jTable1Cols.get("Lines list"))
					.setCellEditor(new DefaultCellEditor(comboLinesList));
		}
		log.info("Done. Updating the table...");
		List<ImageEntity> images = new ArrayList<ImageEntity>();
		for (ImageEntity item : dataService.getImageRepository().findAll())
			images.add(item);
		if (((DefaultTableModel) this.jTable1.getModel()).getRowCount() > 0) {
			for (int i = 0; i < ((DefaultTableModel) this.jTable1.getModel()).getRowCount() - 1; i++) {
				this.jTable1.setValueAt(images.get(i).getType(), i, this.jTable1Cols.get("Type"));
				this.jTable1.setValueAt(images.get(i).isStandard(), i, this.jTable1Cols.get("Is standard?"));
				if (tempLamps.containsKey(images.get(i).getFileName()))
					this.jTable1.setValueAt(tempLamps.get(images.get(i).getFileName()), i,
							this.jTable1Cols.get("Lamp"));
				else
					this.jTable1.setValueAt("", i, this.jTable1Cols.get("Lamp"));
				if (tempStandards.containsKey(images.get(i).getFileName()))
					this.jTable1.setValueAt(tempStandards.get(images.get(i).getFileName()), i,
							this.jTable1Cols.get("Standard"));
				else if (images.get(i).isStandard()) {
					tempStandards.put(images.get(i).getFileName(), images.get(i).getFileName());
					this.jTable1.setValueAt(images.get(i).getFileName(), i, this.jTable1Cols.get("Standard"));
				} else
					this.jTable1.setValueAt("", i, this.jTable1Cols.get("Standard"));
			}
		}
		log.info("Done.");
		log.info("jTable1 updated.");
		jTable1.updateUI();
	}

	private void updateComboStd() {
		comboStd.removeAllItems();
		comboStd.addItem("");
		for (String entity : dataService.getImageRepository().getFileNameByIsStandardAndIsEnabled(true, true))
			comboStd.addItem(entity);
	}

	private void updateComboLamps() {
		comboLamps.removeAllItems();
		comboLamps.addItem("");
		HashSet<String> items = new HashSet<>();
		for (ImageEntity entity : dataService.getImageRepository().findByType("LAMP"))
			if (entity.isEnabled()) {
				if (!entity.isGrouped())
					items.add(entity.getFileName());
				else {
					TableModel model = jTable1.getModel();
					for (int i = 0; i < model.getRowCount(); i++)
						if (entity.getFileName().equals(model.getValueAt(i, jTable1Cols.get("Image")))) {
							String key = (String) model.getValueAt(i, jTable1Cols.get("Target name"));
							key = key.substring(key.indexOf("(") + 1, key.indexOf(")"));
							items.add(key);
							break;
						}
				}
			}
		for (String item : items)
			comboLamps.addItem(item);
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
		List<ImageEntity> images = dataService.getImageRepository().findByType("LAMP");
		for (ImageEntity image : images)
			image.setLamp(null);
		dataService.getImageRepository().save(images);
		dataService.getLampRepository().deleteAll();
		dataService.getFlatRepository().deleteAll();
		log.trace("Database cleared.");
		log.info("FLATFIELD");
		/*
		 * Populate the FLATFIELD
		 */
		FlatfieldImage flat = new FlatfieldImage();
		log.trace("Associating with the images of type \"FLATFIELD\"...");
		/* List<ImageEntity> */images = dataService.getImageRepository().findByType("FLATFIELD");
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
		HashSet<LampImage> lamps = new HashSet<>();
		for (ImageEntity image : images) {
			if (image.isEnabled()) {
				if (!image.isGrouped()) {
					LampImage lamp = new LampImage();
					lamp.setFlat(flat);
					if (lamp.getImages() == null)
						lamp.setImages(new ArrayList<ImageEntity>());
					lamp.getImages().add(image);
					lamp.setLampName(image.getFileName());
					DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
					for (int i = 0; i < model.getRowCount(); i++)
						if (model.getValueAt(i, jTable1Cols.get("Image")).equals(image.getFileName()))
							lamp.setLineList((String) model.getValueAt(i, jTable1Cols.get("Lines list")));
					image.setLamp(lamp);
					dataService.getLampRepository().save(lamp);
				} else {
					String groupName = "";
					DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
					for (int i = 0; i < model.getRowCount(); i++)
						if (model.getValueAt(i, jTable1Cols.get("Image")).equals(image.getFileName())) {
							groupName = (String) model.getValueAt(i, jTable1Cols.get("Target name"));
							break;
						}
					groupName = groupName.substring(groupName.indexOf("(") + 1, groupName.indexOf(")"));
					LampImage lamp = dataService.getLampRepository().findByLampName(groupName);
					if (lamp != null) {
						if (lamp.getImages().size() < 2) {
							image.setLamp(lamp);
							// lamp.setFlat(flat);
							lamp.getImages().add(image);
							// lamps.add(lamp);
						}
					} else {
						lamp = new LampImage();
						lamp.setFlat(flat);
						lamp.setImages(new ArrayList<ImageEntity>());
						lamp.getImages().add(image);
						lamp.setLampName(groupName);
						for (int i = 0; i < model.getRowCount(); i++)
							if (model.getValueAt(i, jTable1Cols.get("Image")).equals(image.getFileName())) {
								lamp.setLineList((String) model.getValueAt(i, jTable1Cols.get("Lines list")));
								break;
							}
						image.setLamp(lamp);
						dataService.getLampRepository().save(lamp);
						lamps.add(lamp);
					}
				}
			}
		}
		dataService.getLampRepository().save(lamps);
		dataService.getImageRepository().save(images);
		log.trace("Done and saved.");

		/*
		 * Populate the STANDARD
		 */
		log.info("STANDARD");
		images = dataService.getImageRepository().findByIsStandard(true);
		ArrayList<StandardImage> standards = new ArrayList<>();
		lamps.clear();
		for (ImageEntity image : images) {
			StandardImage standard = new StandardImage();
			standard.setImage(image);
			image.setStandard(standard);
			standard.setFlat(flat);
			if (this.tempLamps.containsKey(image.getFileName())) {
				LampImage lamp = dataService.getLampRepository()
						.findByLampName(this.tempLamps.get(image.getFileName()));
				standard.setLamp(lamp);
				if (lamp.getStandardImages() == null)
					lamp.setStandardImages(new ArrayList<StandardImage>());
				lamp.getStandardImages().add(standard);
				dataService.getLampRepository().save(lamp);
				lamps.add(lamp);
			}
			standards.add(standard);
		}
		dataService.getLampRepository().save(lamps);
		dataService.getStandardRepository().save(standards);

		/*
		 * Populate the SCIENCE
		 */
		log.info("SCIENCE");
		images = dataService.getImageRepository().findByType("IMAGE");
		lamps.clear();
		standards.clear();
		ArrayList<ScienceImage> sciences = new ArrayList<>();
		for (ImageEntity image : images) {
			ScienceImage science = new ScienceImage();
			science.setImage(image);
			science.setFlat(flat);
			if (this.tempLamps.containsKey(image.getFileName())) {
				LampImage lamp = dataService.getLampRepository()
						.findByLampName(this.tempLamps.get(image.getFileName()));
				science.setLamp(lamp);
				lamp.getScienceImages().add(science);
				lamps.add(lamp);
			}
			if (this.tempStandards.containsKey(image.getFileName())) {
				ImageEntity standardImage = dataService.getImageRepository()
						.findByFileName(this.tempStandards.get(image.getFileName()));
				science.setStandard(standardImage.getStandard());
				standardImage.getStandard().getScienceImages().add(science);
				standards.add(standardImage.getStandard());
			}
			sciences.add(science);
		}
		dataService.getLampRepository().save(lamps);
		dataService.getStandardRepository().save(standards);
		dataService.getScienceRepository().save(sciences);

		/*
		 * Populate the OBSERVATION
		 */
		log.info("OBSERVATION");
		List<Object[]> result = dataService.getImageRepository().getTargetNameAndStandardFileName();
		standards.clear();
		sciences.clear();
		ArrayList<Observation> observations = new ArrayList<>();
		for (Object[] data : result) {
			Observation observation = new Observation();
			StandardImage standard = dataService.getStandardRepository().findByFileName((String) data[1]);
			observation.setStandard(standard);
			standard.getObservations().add(observation);
			List<ScienceImage> science = dataService.getScienceRepository()
					.getScienceImageByTargetNameAndStandardFileName((String) data[0], (String) data[1]);
			observation.setScienceImages(science);
			for (ScienceImage entity : science) {
				entity.setObservation(observation);
				sciences.add(entity);
			}
			standards.add(standard);
			observations.add(observation);
		}
		dataService.getStandardRepository().save(standards);
		dataService.getObservationRepository().save(observations);
		dataService.getScienceRepository().save(sciences);
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
			if (this.generatedList.get("all") == null)
				this.generatedList.put("all", new ArrayList<>());
			this.generatedList.get("all").add("list_obj");
			writer = new PrintWriter(Paths.get(this.basePath, "list_obj").toFile());
			List<Observation> observations = dataService.getObservationRepository().findByIsEnabled(true);
			for (Observation observation : observations) {
				for (ScienceImage science : observation.getScienceImages()) {
					if (science.getImage().isEnabled()) {
						temp += science.getImage().getFileName() + "\t" + science.getLamp().getLampName() + "\n";
					}
				}
			}
			for (String item : dataService.getImageRepository().getFileNameByTypeAndIsEnabled("LAMP", true))
				tempLamp += item + "\n";

			writer.print(temp.trim());
			writer.close();

			temp = "";
			if (!"".equals(tempLamp)) {
				log.info("list_lamps");
				if (this.generatedList.get("all") == null)
					this.generatedList.put("all", new ArrayList<>());
				this.generatedList.get("all").add("list_lamps");
				writer = new PrintWriter(Paths.get(this.basePath, "list_lamps").toFile());
				writer.print(tempLamp.trim());
				writer.close();
			}
			log.info("list_flat");
			if (this.generatedList.get("all") == null)
				this.generatedList.put("all", new ArrayList<>());
			this.generatedList.get("all").add("list_flat");

			writer = new PrintWriter(Paths.get(this.basePath, "list_flat").toFile());
			for (FlatfieldImage flat : dataService.getFlatRepository().findAll()) {
				for (ImageEntity image : flat.getImages())
					temp += image.getFileName() + "\n";
			}
			writer.print(temp + "\n");
			writer.close();

			// Generate the list of IMA*.wc.fits
			for (Observation observation : observations) {
				if (observation.isEnabled() && observation.isDoWlcal()) {
					temp = normalizedTargetName(observation.getTargetName());
					log.info("wc" + temp);
					if (this.generatedList.get(observation.getTargetName()) == null)
						this.generatedList.put(observation.getTargetName(), new ArrayList<>());
					this.generatedList.get(observation.getTargetName()).add("wc" + temp);
					writer = new PrintWriter(Paths.get(this.basePath, "wc" + temp).toFile());
					temp = "";
					for (ScienceImage item : observation.getScienceImages())
						if (item.getImage().isEnabled())
							temp += item.getImage().getFileName() + "\n";
					writer.print(temp.trim());
					writer.close();
				}
			}

			// Generate the list of IMA*.fc.fits
			// This list is used as input for the background task
			// OBSOLETE
			// for (Observation observation : observations) {
			// if (observation.isDoFcal()) {
			// temp = normalizedTargetName(observation.getTargetName());
			// log.info("fc" + temp);
			//
			// if (this.generatedList.get(observation.getTargetName()) == null)
			// this.generatedList.put(observation.getTargetName(), new
			// ArrayList<>());
			// this.generatedList.get(observation.getTargetName()).add("fc" +
			// temp);
			// writer = new PrintWriter(Paths.get(this.basePath, "fc" +
			// temp).toFile());
			// temp = "";
			// for (ScienceImage item : observation.getScienceImages())
			// if (item.getImage().isEnabled())
			// temp += item.getImage().getFileName() + ".fc\n";
			// writer.print(temp + "\n");
			// writer.close();
			// }
			// }

			// Generate the list of IMA*.bg.fits
			// OBSOLETE
			// for (Observation observation : observations) {
			// if (observation.isDoBackground()) {
			// temp = normalizedTargetName(observation.getTargetName());
			// log.info("bg" + temp);
			// if (this.generatedList.get(observation.getTargetName()) != null)
			// this.generatedList.get(observation.getTargetName()).add("bg" +
			// temp);
			// else {
			// ArrayList<String> list = new ArrayList<>();
			// list.add("bg" + temp);
			// this.generatedList.put(observation.getTargetName(), list);
			// }
			// writer = new PrintWriter(Paths.get(this.basePath, "bg" +
			// temp).toFile());
			// temp = "";
			// for (ScienceImage item : observation.getScienceImages())
			// if (item.getImage().isEnabled())
			// temp += item.getImage().getFileName() + ".bg\n";
			// writer.print(temp + "\n");
			// writer.close();
			// }
			// }

			// Generate the list of IMA*.md.fits
			for (Observation observation : observations) {
				if (observation.isDoApall()) {
					temp = normalizedTargetName(observation.getTargetName());
					log.info("md" + temp);
					if (this.generatedList.get(observation.getTargetName()) == null)
						this.generatedList.put(observation.getTargetName(), new ArrayList<>());
					this.generatedList.get(observation.getTargetName()).add("md" + temp);

					writer = new PrintWriter(Paths.get(this.basePath, "md" + temp).toFile());
					temp = "";
					for (ScienceImage item : observation.getScienceImages())
						if (item.getImage().isEnabled())
							temp += item.getImage().getFileName() + ".md\n";
					writer.print(temp + "\n");
					writer.close();
				}
			}

			// Generate the list of IMA*.wc.fits
			// This list is used as input for standard parameter of fcal task
			for (StandardImage standard : dataService.getStandardRepository().findAll()) {
				temp = "";
				for (Observation observation : dataService.getObservationRepository()
						.findByStandardFileNameAndIsEnabledIsTrue(standard.getImage().getFileName())) {
					if (observation.isDoFcal()) {
						log.info("std" + standard.getImage().getFileName());
						if (this.generatedList.get(observation.getTargetName()) == null)
							this.generatedList.put(observation.getTargetName(), new ArrayList<>());
						this.generatedList.get(observation.getTargetName())
								.add("std" + standard.getImage().getFileName());

						for (ScienceImage item : observation.getScienceImages()) {
							/*
							 * Use file name instead of standard name to avoid
							 * confusion: the same standard used twice during
							 * the night MUST be treated as two different stars
							 */
							if (item.getImage().isEnabled())
								temp += item.getImage().getFileName() + "\n";
						}
					}
				}
				if (!temp.isEmpty()) {
					writer = new PrintWriter(
							Paths.get(this.basePath, "std" + standard.getImage().getFileName()).toFile());
					writer.write(temp + "\n");
					writer.close();
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
			String targetNormalized = normalizedTargetName(observation.getTargetName());
			PrintWriter writer = null;
			try {
				int multiples = 0;
				String name = targetNormalized;
				while (Paths.get(basePath, "exec" + name + ".py").toFile().exists()) {
					multiples++;
					name = targetNormalized + "." + multiples;
				}
				String temp = "", tempLamp = "";
				// Write them down
				log.info("list_obj_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) == null)
					this.generatedList.put(observation.getTargetName(), new ArrayList<>());
				this.generatedList.get(observation.getTargetName()).add("list_obj_" + name);
				writer = new PrintWriter(Paths.get(this.basePath, "list_obj_" + name).toFile());
				HashSet<String> lampName = new HashSet<String>();
				for (ScienceImage science : observation.getScienceImages()) {
					if (science.getImage().isEnabled()) {
						if (science.getLamp().getImages().get(0).isGrouped())
							temp += science.getImage().getFileName() + "\t" + science.getLamp().getLampName()
									+ normalizedTargetName(observation.getTargetName()) + "\n";
						else
							temp += science.getImage().getFileName() + "\t" + science.getLamp().getLampName() + "\n";
						for (ImageEntity image : science.getLamp().getImages())
							lampName.add(image.getFileName());
					}
				}
				// Always add the standard to the wavelength calibration
				if (observation.getStandard().getLamp().getImages().size() == 1)
					temp += observation.getStandard().getImage().getFileName() + "\t"
							+ observation.getStandard().getLamp().getLampName() + "\n";
				else
					temp += observation.getStandard().getImage().getFileName() + "\t"
							+ observation.getStandard().getLamp().getLampName()
							+ normalizedTargetName(observation.getTargetName()) + "\n";
				for (String item : lampName)
					tempLamp += item + "\n";

				writer.print(temp.trim());
				writer.close();

				temp = "";
				log.info("list_lamps_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) == null)
					this.generatedList.put(observation.getTargetName(), new ArrayList<>());
				this.generatedList.get(observation.getTargetName()).add("list_lamps_" + name);
				writer = new PrintWriter(Paths.get(this.basePath, "list_lamps_" + name).toFile());
				writer.print(tempLamp.trim());
				writer.close();

				log.info("list_flat_" + targetNormalized);
				if (this.generatedList.get(observation.getTargetName()) == null)
					this.generatedList.put(observation.getTargetName(), new ArrayList<>());
				this.generatedList.get(observation.getTargetName()).add("list_flat_" + name);
				writer = new PrintWriter(Paths.get(this.basePath, "list_flat_" + name).toFile());
				for (FlatfieldImage flat : dataService.getFlatRepository().findAll()) {
					for (ImageEntity image : flat.getImages())
						temp += image.getFileName() + "\n";
				}
				writer.print(temp.trim() + "\n");
				writer.close();

				// Generate the list of IMA*.wc.fits
				if (observation.isDoWlcal()) {
					log.info("wc" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) == null)
						this.generatedList.put(observation.getTargetName(), new ArrayList<>());
					this.generatedList.get(observation.getTargetName()).add("wc" + name);
					writer = new PrintWriter(Paths.get(this.basePath, "wc" + name).toFile());
					temp = "";
					for (ScienceImage item : observation.getScienceImages())
						if (item.getImage().isEnabled())
							temp += item.getImage().getFileName() + "\n";
					writer.print(temp.trim());
					writer.close();
				}

				// Generate the list of IMA*.fc.fits
				// This list is used as input for the background task
				// OBSOLETE
				// if (observation.isDoFcal()) {
				// log.info("fc" + targetNormalized);
				// if (this.generatedList.get(observation.getTargetName()) ==
				// null)
				// this.generatedList.put(observation.getTargetName(), new
				// ArrayList<>());
				// this.generatedList.get(observation.getTargetName()).add("fc"
				// + targetNormalized);
				// writer = new PrintWriter(Paths.get(this.basePath, "fc" +
				// targetNormalized).toFile());
				// temp = "";
				// for (ScienceImage item : observation.getScienceImages())
				// if (item.getImage().isEnabled())
				// temp += item.getImage().getFileName() + ".fc\n";
				// writer.print(temp.trim() + "\n");
				// writer.close();
				// }

				// Generate the list of IMA*.bg.fits
				if (observation.isDoBackground()) {
					// OBSOLETE
					// log.info("bg" + targetNormalized);
					// if (this.generatedList.get(observation.getTargetName())
					// != null)
					// this.generatedList.get(observation.getTargetName()).add("bg"
					// + targetNormalized);
					// else {
					// ArrayList<String> list = new ArrayList<>();
					// list.add("bg" + targetNormalized);
					// this.generatedList.put(observation.getTargetName(),
					// list);
					// }
					// writer = new PrintWriter(Paths.get(this.basePath, "bg" +
					// targetNormalized).toFile());
					// temp = "";
					// for (ScienceImage item : observation.getScienceImages())
					// if (item.getImage().isEnabled())
					// temp += item.getImage().getFileName() + ".bg\n";
					// writer.print(temp.trim() + "\n");
					// writer.close();

					// Generate background cursor file
					if (!Paths.get(this.basePath, "bgcols").toFile().exists()) {
						PrintWriter bgcols = new PrintWriter(Paths.get(this.basePath, "bgcols").toFile());
						bgcols.write(this.jBackgroundStart.getValue().toString() + " "
								+ this.jBackgroundEnd.getValue().toString());
						bgcols.close();
					}
				}

				// Generate the list of IMA*.md.fits
				if (observation.isDoApall()) {
					log.info("md" + targetNormalized);
					if (this.generatedList.get(observation.getTargetName()) == null)
						this.generatedList.put(observation.getTargetName(), new ArrayList<>());
					this.generatedList.get(observation.getTargetName()).add("md" + name);
					writer = new PrintWriter(Paths.get(this.basePath, "md" + name).toFile());
					temp = "";
					for (ScienceImage item : observation.getScienceImages())
						if (item.getImage().isEnabled())
							temp += item.getImage().getFileName() + ".md\n";
					writer.print(temp.trim() + "\n");
					writer.close();
				}

				// Let's generate the PyRAF script
				log.info(name);
				scriptsList.add(name);
				if (this.generatedList.get("script") == null)
					this.generatedList.put("script", new ArrayList<>());
				this.generatedList.get("script").add(name);
				writer = new PrintWriter(Paths.get(this.basePath, "exec" + name + ".py").toFile());
				writer.println("#!/usr/bin/env python");
				writer.println("import os");
				writer.println("import sys");
				writer.println("os.chdir(\"" + dataService.getProperty("iraf.home") + "\")");

				writer.println("from pyraf import iraf");
				writer.println("os.chdir(\"" + this.basePath + "\")");
				writer.println("iraf.asgred(_doprint=1)");
				if (observation.isDoPrered()) {
					// exec prered2
					writer.println("print(\"prered2\")");
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("prered2", 78));
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.println("iraf.prered2(flat=\"list_flat_" + name + "\", comp=\"list_lamps_" + name
							+ "\", object=\"list_obj_" + name + "\", outflat=\"flat." + name
							+ "\", trimsec=[1:2040,40:490], mode=\"ql\")");
				}

				if (observation.isDoWlcal()) {
					HashSet<LampImage> lamps = new HashSet<>();
					for (ScienceImage science : observation.getScienceImages()) {
						if (science.getLamp().getImages().get(0).isEnabled()
								&& science.getLamp().getImages().get(0).isGrouped())
							lamps.add(science.getLamp());
					}
					if (lamps.size() > 0) {
						// exec imarith
						writer.println("print(\"imarith\")");
						writer.println(new String(new char[80]).replace("\0", "#"));
						writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("imarith", 78));
						writer.println(new String(new char[80]).replace("\0", "#"));
						for (LampImage lamp : lamps)
							writer.println("iraf.imarith(operand1=\"" + lamp.getImages().get(0).getFileName() + ".b\","
									+ " op=\"+\",operand2=\"" + lamp.getImages().get(1).getFileName() + ".b\",result=\""
									+ lamp.getLampName() + normalizedTargetName(observation.getTargetName()) + ".b\")");

						// exec wlcal
						writer.println("print(\"wlcal\")");
						writer.println(new String(new char[80]).replace("\0", "#"));
						writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("wlcal", 78));
						writer.println(new String(new char[80]).replace("\0", "#"));
						writer.println("iraf.wlcal(input=\"list_obj_" + name + "\", refer=\""
								+ observation.getScienceImages().get(0).getLamp().getLampName()
								+ normalizedTargetName(observation.getTargetName()) + "\"," + " linelis=\""
								+ observation.getScienceImages().get(0).getLamp().getLineList().toLowerCase()
								+ "\", mode=\"ql\")");
					} else {
						// exec wlcal
						writer.println("print(\"wlcal\")");
						writer.println(new String(new char[80]).replace("\0", "#"));
						writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("wlcal", 78));
						writer.println(new String(new char[80]).replace("\0", "#"));
						writer.println("iraf.wlcal(input=\"list_obj_" + name + "\", refer=\""
								+ observation.getScienceImages().get(0).getLamp().getLampName() + "\"," + " linelis=\""
								+ observation.getScienceImages().get(0).getLamp().getLineList().toLowerCase()
								+ "\", mode=\"ql\")");
					}
				}
				if (observation.isDoFcal()) {
					// exec fcal
					writer.println("print(\"fcal\")");
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("fcal", 78));
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.println("iraf.fcal(obj=\"wl" + name + "\", stand=\""
							+ observation.getStandard().getImage().getFileName() + "\", dir=\"onedstds$"
							+ dataService.getStandardAtlas()
									.findByStandardName(observation.getStandard().getImage().getTargetName())
									.getCatalogueName()
							+ "/\", " + "star=\""
							+ dataService.getStandardAtlas()
									.findByStandardName(observation.getStandard().getImage().getTargetName())
									.getDatName()
							+ "\", mode=\"ql\")");
				}
				// exec background
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("background", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoBackground())
					for (ScienceImage image : observation.getScienceImages())
						if (image.getImage().isEnabled()) {
							writer.println("print(\"background\")");
							writer.println("iraf.background(input=\"" + image.getImage().getFileName()
									+ ".fc\", output=\"" + image.getImage().getFileName() + ".bg\", axis=2,"
									+ " mode=\"ql\", " + "Stdin=\"bgcols\")");
						}
				// exec apall
				writer.println("print(\"apall\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("apall", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoApall())
					for (ScienceImage image : observation.getScienceImages())
						if (image.getImage().isEnabled())
							writer.println("iraf.apall(input=\"" + image.getImage().getFileName() + ".bg\", output=\""
									+ image.getImage().getFileName() + ".md\", t_order=3, t_niter=5, mode=\"ql\")");
				// exec scombine
				writer.println("print(\"scombine\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("scombine", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoScombine())
					writer.println("iraf.scombine(input=\"@md" + name + "\", output=\"" + name
							+ ".md\", reject=\"minmax\", mode=\"ql\")");
				String start = this.jImcopyStart.getValue().toString();
				String end = this.jImcopyEnd.getValue().toString();
				// exec imcopy
				writer.println("print(\"imcopy\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("imcopy", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				if (observation.isDoImcopy())
					writer.println("iraf.imcopy(input=\"" + name + ".md[" + start + ":" + end + "]\", output=\"" + name
							+ ".obj\", mode=\"ql\")");
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
			if (this.generatedList.get("script") == null)
				this.generatedList.put("script", new ArrayList<>());
			this.generatedList.get("script").add("execAsgred.py");
			scriptsList = new ArrayList<String>();
			scriptsList.add("execAsgred.py");
			writer = new PrintWriter(Paths.get(this.basePath, "execAsgred.py").toFile());
			writer.println("#!/usr/bin/env python");
			writer.println("import os");
			writer.println("import sys");
			writer.println("os.chdir(\"" + this.jIrafHome.getText() + "\")");

			writer.println("from pyraf import iraf");
			writer.println("os.chdir(\"" + this.basePath + "\")");
			writer.println("iraf.asgred()");
			if (this.generatedList.containsKey("all") && this.generatedList.get("all").contains("list_flat")) {
				// exec prered2
				writer.println("print(\"prered2\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("prered2", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.println("iraf.prered2(flat=\"list_flat\", comp=\"list_lamps\", object=\"list_obj\","
						+ "trimsec=\"[1:2040,40:490]\", outflat=\"flat.all\", mode=\"ql\")");
			}
			List<ImageEntity> groups = dataService.getImageRepository().findByGrouped(true);
			if (groups != null && groups.size() > 0) {
				HashSet<LampImage> lamps = new HashSet<>();
				for (ImageEntity image : groups)
					if (image.isEnabled())
						lamps.add(image.getLamp());

				// exec imarith
				writer.println("print(\"imarith\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("imarith", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				for (LampImage lamp : lamps)
					writer.println("iraf.imarith(operand1=\"" + lamp.getImages().get(0).getFileName() + ".b\","
							+ " op=\"+\",operand2=\"" + lamp.getImages().get(1).getFileName() + ".b\",result=\""
							+ lamp.getLampName() + ".b\")");

			}
			if (this.generatedList.containsKey("all") && this.generatedList.get("all").contains("list_obj")) {
				// exec wlcal
				writer.println("print(\"wlcal\")");
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils.center("wlcal", 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				String firstLamp = dataService.getScienceRepository().getLamps().get(0);
				writer.println("iraf.wlcal(input=\"list_obj\", refer=\"" + firstLamp + "\", linelis=\""
						+ dataService.getLampRepository().findByLampName(firstLamp).getLineList().toLowerCase()
						+ "\", mode=\"ql\")");
			}
			// exec fcal
			writer.println("print(\"fcal\")");
			for (StandardImage standard : dataService.getStandardRepository().findAll()) {
				if (dataService.getObservationRepository()
						.findByStandardFileNameAndIsEnabledIsTrue(standard.getImage().getFileName()).size() > 0) {
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils
							.center("Flux calibration for " + standard.getImage().getTargetName(), 78));
					writer.println(new String(new char[80]).replace("\0", "#"));
					writer.println("iraf.fcal(obj=\"std" + standard.getImage().getFileName() + "\", stand=\""
							+ standard.getImage().getFileName() + "\", dir=\"onedstds$"
							+ dataService.getStandardAtlas().findByStandardName(standard.getImage().getTargetName())
									.getCatalogueName()
							+ "/\", " + "star=\"" + dataService.getStandardAtlas()
									.findByStandardName(standard.getImage().getTargetName()).getDatName()
							+ "\")");
				}
			}

			for (Observation observation : dataService.getObservationRepository().findByIsEnabled(true)) {
				writer.println(new String(new char[80]).replace("\0", "#"));
				writer.printf("#%78s#\n", org.apache.commons.lang3.StringUtils
						.center("reduction for " + observation.getTargetName(), 78));
				writer.println(new String(new char[80]).replace("\0", "#"));
				String targetNormalized = normalizedTargetName(observation.getTargetName());
				// exec background
				if (observation.isDoBackground())
					if (!Paths.get(this.basePath, "bgcols").toFile().exists()) {
						PrintWriter bgcols = new PrintWriter(Paths.get(this.basePath, "bgcols").toFile());
						bgcols.write(this.jBackgroundStart.getValue().toString() + " "
								+ this.jBackgroundEnd.getValue().toString());
						bgcols.close();
					}
				for (ScienceImage image : observation.getScienceImages())
					if (image.getImage().isEnabled()) {
						writer.println("print(\"background\")");
						writer.println("iraf.background(input=\"" + image.getImage().getFileName() + ".fc\", output=\""
								+ image.getImage().getFileName() + ".bg\", axis=2, "
								+ "mode=\"ql\", Stdin=\"bgcols\")");
					}
				// exec apall
				writer.println("print(\"apall\")");
				if (observation.isDoApall())
					for (ScienceImage image : observation.getScienceImages())
						if (image.getImage().isEnabled())
							writer.println("iraf.apall(input=\"" + image.getImage().getFileName() + ".bg\", output=\""
									+ image.getImage().getFileName() + ".md\", t_order = 3., t_niter = 5)");
				// exec scombine
				writer.println("print(\"scombine\")");
				if (observation.isDoScombine())
					writer.println("iraf.scombine(input=\"@md" + targetNormalized + "\", output=\"" + targetNormalized
							+ ".md\", reject=\"minmax\")");
				String start = this.jImcopyStart.getValue().toString();
				String end = this.jImcopyEnd.getValue().toString();
				// exec imcopy
				writer.println("print(\"imcopy\")");
				if (observation.isDoImcopy())
					writer.println("iraf.imcopy(input=\"" + targetNormalized + ".md[" + start + ":" + end
							+ "]\", output=\"" + targetNormalized + ".obj\")");
			}
			writer.close();
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
			pj.setPrintable(
					documentToPrint.getPrintable(new MessageFormat("TODO list"), new MessageFormat("AsgredLists")));
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

	public void dispose() {
		this.process.dispose();
	}

	public String normalizedTargetName(String targetName) {
		String tmp = targetName;
		if (tmp.contains("-"))
			tmp = tmp.replace("-", "m");
		if (tmp.contains("+"))
			tmp = tmp.replace("+", "p");
		if (tmp.contains(" "))
			tmp = tmp.replace(" ", "");
		if (tmp.contains("\""))
			tmp = tmp.replace("\"", "");
		return tmp;
	}

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
	private javax.swing.JButton jExploreDirectory;
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
	private javax.swing.JButton jLoadDirectory;
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
	private javax.swing.JTextField jPathDirectory;
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
	private JPopupMenu popupMenu;
	private JMenuItem popupGroup;
	private JMenuItem popupDisband;
}