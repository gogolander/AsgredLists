/**
 * @author enzo
 */
package unipd.astro;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTextPane;

/**
 * @author enzo
 *
 */
@SuppressWarnings("serial")
public class GeneratedList extends JFrame {

	public static void showList(HashMap<String, List<String>> list) {
		GeneratedList frame = new GeneratedList(list);
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JButton jClose;
	private JButton jPrint;
	private JTextPane jDocument;
	private Style text, title;

	/**
	 * Create the frame.
	 */
	public GeneratedList(HashMap<String, List<String>> list) {
		initComponents();
		myInitComponents(list);
	}

	/**
	 * 
	 */
	private void myInitComponents(HashMap<String, List<String>> list) {
		StyleContext sc = new StyleContext();
		text = sc.addStyle("text", null);
		text.addAttribute(StyleConstants.FontSize, new Integer(11));
		text.addAttribute(StyleConstants.FontFamily, "arial");
		text.addAttribute(StyleConstants.Bold, new Boolean(false));

		title = sc.addStyle("title", null);
		title.addAttribute(StyleConstants.FontSize, new Integer(12));
		title.addAttribute(StyleConstants.FontFamily, "arial");
		title.addAttribute(StyleConstants.Bold, new Boolean(true));
		try {
			for (String key : list.keySet()) {
				this.jDocument.getStyledDocument().insertString(jDocument.getStyledDocument().getLength(),
						"Target: " + key + "\n", title);
				for (String item : list.get(key))
					this.jDocument.getStyledDocument().insertString(jDocument.getStyledDocument().getLength(),
							item + "\n", text);
			}
		} catch (Exception ex) {

		}
	}

	private void initComponents() {
		setTitle("Generated Files");
		setBounds(100, 100, 660, 508);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		this.scrollPane = new JScrollPane();
		this.jClose = new JButton("Close");
		this.jClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jCloseActionPerformed(e);
			}
		});
		this.jPrint = new JButton("Print");
		this.jPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jPrintActionPerformed(e);
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(this.contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(this.scrollPane, GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
										.addComponent(this.jPrint).addGap(18).addComponent(this.jClose)))
				.addContainerGap()));
		gl_contentPane
				.setVerticalGroup(
						gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
										.addComponent(this.scrollPane, GroupLayout.PREFERRED_SIZE, 378,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
												.addComponent(this.jClose).addComponent(this.jPrint))
				.addContainerGap()));
		{
			this.jDocument = new JTextPane();
			this.scrollPane.setViewportView(this.jDocument);
		}
		this.contentPane.setLayout(gl_contentPane);
	}

	private void jCloseActionPerformed(ActionEvent e) {
		super.dispose();
	}

	private void jPrintActionPerformed(ActionEvent e) {
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(this.jDocument.getPrintable(new MessageFormat("Generated files"), new MessageFormat("AsgredLists")));
		if (pj.printDialog()) {
			try {
				pj.print();
			} catch (PrinterException exc) {
				System.out.println(exc);
			}
		}
	}
}
