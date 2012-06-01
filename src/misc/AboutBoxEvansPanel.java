package misc;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Generic About Panel.
 */
public class AboutBoxEvansPanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_NAME = "Java Application";
  private static final String DEFAULT_AUTHOR = "Written by Kenneth Evans, Jr.";
  private static final String DEFAULT_COPYRIGHT =
    "Copyright (c) 2012 Kenneth Evans";
  private static final String DEFAULT_COMPANY = "kenevans.net";


  private JLabel labelName = new JLabel();
  private JLabel labelAuthor = new JLabel();
  private JLabel labelCompany = new JLabel();
  private JLabel labelCopyright = new JLabel();
  
  private String name = null;
  private String author = null;
  private String company = null;
  private String copyright = null;

  /**
   * AboutBoxEvansPanel constructor.
   */
  public AboutBoxEvansPanel() {
    this(DEFAULT_NAME, DEFAULT_AUTHOR, DEFAULT_COMPANY, DEFAULT_COPYRIGHT);
  }

  /**
   * AboutBoxEvansPanel constructor.
   * @param name The name of the program.
   */
  public AboutBoxEvansPanel(String name) {
    this(name, DEFAULT_AUTHOR, DEFAULT_COMPANY, DEFAULT_COPYRIGHT);
  }

  /**
   * AboutBoxEvansPanel constructor.
   * @param name The name of the program.
   * @param author String identifying the author.
   * @param company Name of the company.
   * @param copyright Copyright statement.
   */
  public AboutBoxEvansPanel(String name, String author, String company,
    String copyright) {
    this.name = name;
    this.author = author;
    this.company = company;
    this.copyright = copyright;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Swing initialization
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    Border border = BorderFactory.createEtchedBorder();
    GridBagLayout layoutMain = new GridBagLayout();
    this.setLayout(layoutMain);
    this.setBorder(border);
    labelName.setText(name);
    labelAuthor.setText(author);
    labelCompany.setText(company);
    labelCopyright.setText(copyright);
    this.add(labelName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(5, 15, 2, 15), 0, 0));
    this.add(labelAuthor, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(5, 15, 2, 15), 0, 0));
    this.add(labelCompany, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(0, 15, 2, 15), 0, 0));
    this.add(labelCopyright, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(5, 15, 2, 15), 0, 0));
  }

  /**
   * @return Returns the author.
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param author The author to set.
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * @return Returns the company.
   */
  public String getCompany() {
    return company;
  }

  /**
   * @param company The company to set.
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * @return Returns the copyright.
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * @param copyright The copyright to set.
   */
  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }
  
}
