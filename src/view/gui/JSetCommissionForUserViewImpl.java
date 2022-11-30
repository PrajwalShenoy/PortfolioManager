package view.gui;

import java.awt.*;

import javax.swing.*;

import controller.gui.JSetCommissionController;

public class JSetCommissionForUserViewImpl extends JFrame implements JSetCommissionForUserView {

  private JPanel mainPanel;
  private JButton backButton;
  private JLabel outputLabel;
  private JButton setCommissionButton;

  private JTextArea commissionAmount;

  public JSetCommissionForUserViewImpl(){
    super();
    setTitle("Set Commission");
    setSize(400, 400);
    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
    this.setTopMostPanel();
    this.inputCommissionPanel();
    this.buttonPanel();
//    this.placeLogOutput();
    this.logOutputPanel();
  }

  private void setTopMostPanel() {
    JPanel topMostPanel = new JPanel();
    topMostPanel.setLayout(new FlowLayout());
    backButton = new JButton();
    backButton.setActionCommand("back");
    backButton.setText("Back");
    backButton.setHorizontalAlignment(SwingConstants.LEFT);
    JLabel topMostLabel = new JLabel();
    topMostLabel.setText("Enter the new commission amount for the user");
    topMostPanel.add(backButton);
    topMostPanel.add(topMostLabel);
    mainPanel.add(topMostPanel);
  }

  private void inputCommissionPanel(){
    JPanel inputCommissionPanel = new JPanel();
    inputCommissionPanel.setLayout(new GridLayout(1, 1, 2, 0));
    commissionAmount = new JTextArea(1, 20);
    commissionAmount.setLineWrap(true);
    commissionAmount.setBorder(BorderFactory.createTitledBorder("Enter commission"));
    inputCommissionPanel.add(commissionAmount);
    mainPanel.add(inputCommissionPanel);
  }

//  private void placeLogOutput(){
//    JPanel outputPanel = new JPanel();
//    outputPanel.setBorder(BorderFactory.createTitledBorder("Output of operation performed"));
//    outputLabel = new JLabel();
//    outputPanel.add(outputLabel);
//    mainPanel.add(outputPanel);
//    add(mainPanel);
//  }

  private void buttonPanel(){

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 1, 2, 2));
    JPanel setCommissionButtonPanel = new JPanel();
    setCommissionButton = new JButton();
    setCommissionButton.setText("Set Commission amount");
    setCommissionButtonPanel.add(setCommissionButton);
    buttonPanel.add(setCommissionButtonPanel);
    mainPanel.add(buttonPanel);
  }

  @Override
  public void addFeatures(JSetCommissionController jSetCommissionController) {
    this.backButton.addActionListener(evt -> jSetCommissionController.back());
    this.setCommissionButton.addActionListener(evt ->
            jSetCommissionController.setCommission(commissionAmount.getText()));

  }

  @Override
  public void isVisible(boolean state) {
    this.setVisible(state);

  }

  @Override
  public void clearAllUserInputs() {
    commissionAmount.setText("");
    outputLabel.setText("");
  }

  @Override
  public void setLogOutput(String message) {
    this.outputLabel.setForeground(Color.BLACK);
    this.outputLabel.setText(message);

  }

  private void logOutputPanel() {
    JPanel outputLog = new JPanel();
    outputLabel = new JLabel();
    outputLabel.setText("The current value for commission is set to $1." );
    outputLog.setBorder(BorderFactory.createTitledBorder("Output Logs"));
    outputLog.add(outputLabel);
    mainPanel.add(outputLog);
    add(mainPanel);
  }

  @Override
  public void setInitialMessage(double commission) {
    setLogOutput("The current value for commission is $" + commission );
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