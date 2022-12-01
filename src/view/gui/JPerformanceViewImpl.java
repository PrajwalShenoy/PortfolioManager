package view.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

import controller.gui.JPerformanceController;

public class JPerformanceViewImpl extends JFrame implements JPerformanceView {
  private JPanel mainPanel;
  private JPanel displayPortfoliosPanel;
  private JButton loadPortfolio;
  private JButton backButton;
  private JButton plotGraph;
  private JRadioButton[] radioButtons;
  private JTextField startDate;
  private JTextField endDate;
  private JLabel outputLabel;



  public JPerformanceViewImpl() {
    super("Performance of portfolio");
    setSize(500, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
    this.placeTopPanel();
    displayPortfoliosPanel = new JPanel();
    mainPanel.add(displayPortfoliosPanel);
    loadPortfolio = new JButton();
    loadPortfolio.setText("Load Portfolio");
    mainPanel.add(loadPortfolio);
    this.placeDateSelectionPanel();
    this.placeLogOutput();
    add(mainPanel);
  }

  private void placeTopPanel() {
    JPanel topMostPanel = new JPanel();
    topMostPanel.setLayout(new FlowLayout());
    backButton = new JButton();
    backButton.setActionCommand("back");
    backButton.setText("Back");
    backButton.setHorizontalAlignment(SwingConstants.LEFT);
    JLabel topMostLabel = new JLabel();
    topMostLabel.setText("Select a portfolio from the selection and click \"Load Portfolio\"");
    topMostPanel.add(backButton);
    topMostPanel.add(topMostLabel);
    mainPanel.add(topMostPanel);
  }

  private String getSelectedButton() {
    for (JRadioButton r : radioButtons) {
      if (r.isSelected()) {
        return r.getText();
      }
    }
    return null;
  }

  private void placeDateSelectionPanel() {
    JPanel performancePlotter = new JPanel();
    performancePlotter.setBorder(BorderFactory.createTitledBorder("Performance graph details"));
    startDate = new JTextField(10);
    startDate.setText("YYYY-MM-DD");
    startDate.setBorder(BorderFactory.createTitledBorder("Start Date"));
    endDate = new JTextField(10);
    endDate.setText("YYYY-MM-DD");
    endDate.setBorder(BorderFactory.createTitledBorder("End Date"));
    plotGraph = new JButton();
    plotGraph.setText("Plot Graph");
    performancePlotter.add(startDate);
    performancePlotter.add(endDate);
    performancePlotter.add(plotGraph);
    mainPanel.add(performancePlotter);
  }

  private void placeLogOutput() {
    JPanel outputPanel = new JPanel();
    outputPanel.setBorder(BorderFactory.createTitledBorder("Output of operation performed"));
    outputLabel = new JLabel();
    outputPanel.add(outputLabel);
    mainPanel.add(outputPanel);
    add(mainPanel);
  }

  @Override
  public void addFeatures(JPerformanceController jPerformanceController) {
    backButton.addActionListener(evt -> jPerformanceController.back());
    loadPortfolio.addActionListener(evt -> jPerformanceController.selectPortfolio(getSelectedButton()));
    plotGraph.addActionListener(evt -> jPerformanceController.plotGraph(startDate.getText(), endDate.getText()));
  }

  @Override
  public void displayRadioButtonsForPortfolio(String portfolios) {
    this.selectPortfolioPanel(portfolios);
  }

  private void selectPortfolioPanel(String portfoliosText) {
    displayPortfoliosPanel.removeAll();
    ArrayList<String> portfolios = new ArrayList<>(Arrays.asList(portfoliosText.split("\n")));
    ButtonGroup rGroup = new ButtonGroup();
    JPanel radioPanel = new JPanel();
    radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.PAGE_AXIS));
    radioButtons = new JRadioButton[portfolios.size()];
    for (int i=0; i< portfolios.size(); i++) {
      radioButtons[i] = new JRadioButton(portfolios.get(i));
      rGroup.add(radioButtons[i]);
      radioPanel.add(radioButtons[i]);
    }
    displayPortfoliosPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    JScrollPane scrollableRadioPanel = new JScrollPane(radioPanel);
    displayPortfoliosPanel.add(scrollableRadioPanel);
  }

  @Override
  public void isVisible(boolean state) {
    this.setVisible(state);
    this.pack();
  }

  @Override
  public void clearUserInputs() {
//    startDate.setText("");
//    endDate.setText("");
  }

  @Override
  public void setLogOutput(String message) {
    this.outputLabel.setForeground(Color.BLACK);
    this.outputLabel.setText(message);
  }

  @Override
  public void setSuccessOutput(String message) {
    this.outputLabel.setForeground(new Color(0, 102, 0));
    this.outputLabel.setText(message);
  }

  @Override
  public void setFailureOutput(String message) {
    this.outputLabel.setForeground(Color.RED);
    this.outputLabel.setText(message);
  }
}