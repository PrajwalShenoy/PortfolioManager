package controller.gui;

import java.io.IOException;

import model.Model;
import model.User;
import model.utils.StatusObject;
import view.gui.JCalculateCostBasisView;
import view.gui.JCalculateValueView;
import view.gui.JCreatePortfolioView;
import view.gui.JCreateUserView;
import view.gui.JLoadPortfolioView;
import view.gui.JPerformanceView;
import view.gui.JPortfolioMenuView;
import view.gui.JSetCommissionForUserView;
import view.gui.JTransactionView;
import view.gui.JView;
import view.gui.JViewPortfolioView;

import static controller.Utils.listFlexiblePortfolios;

/**
 * Class that implements the JController Interface which is the main controller of the GUI
 * for the stock program.
 */
public class JControllerImpl implements JController {

  private final Model model;

  private JCreateUserView createUserView;
  private JPortfolioMenuView portfolioMenuView;
  private JCreatePortfolioView createPortfolioView;
  private JCalculateValueView calculateValueView;
  private JCalculateCostBasisView calculateCostBasisView;
  private JViewPortfolioView viewPortfolioView;
  private JLoadPortfolioView loadPortfolioView;
  private JTransactionView transactionView;
  private JSetCommissionForUserView commissionView;
  private JPerformanceView performanceView;
  private JCreatePortfolioController createPortfolioController;
  private JCalculateValueController calculateValueController;
  private JCalculateCostBasisController calculateCostBasisController;
  private JViewPortfolioController viewPortfolioController;
  private JLoadPortfolioController loadPortfolioController;
  private JTransactionController transactionController;
  private JSetCommissionController commissionController;
  private JPerformanceController performanceController;
  private User user;

  /**
   * Constructor to initialize model.
   *
   * @param model an object of type model.
   */
  public JControllerImpl(Model model) {
    this.model = model;
    createPortfolioController = new JCreatePortfolioControllerImpl(model);
    calculateValueController = new JCalculateValueControllerImpl(model);
    calculateCostBasisController = new JCalculateCostBasisControllerImpl(model);
    viewPortfolioController = new JViewPortfolioControllerImpl(model);
    loadPortfolioController = new JLoadPortfolioControllerImpl(model);
    transactionController = new JTransactionControllerImpl(model);
    commissionController = new JSetCommissionControllerImpl(model);
    performanceController = new JPerformanceControllerImpl(model);
  }

  @Override
  public void createUser(String firstName, String lastName) {
    if (firstName.equals("") | lastName.equals("")) {
      this.createUserView.setFailureOutput("First Name and/or Last Name cannot be"
              + " left blank. Please try again");
      return;
    }
    try {
      StatusObject<User> response = model.createUser(firstName, lastName, 1);
      if (response.statusCode > 0) {
        model.updateUserFile(response.returnedObject);
        this.createUserView.setSuccessOutput(response.statusMessage);
        this.createUserView.clearAllUserInputs();
      } else {
        this.createUserView.setFailureOutput(response.statusMessage);
      }
    } catch (IOException e) {
      this.createUserView.setFailureOutput(e.getMessage());
    }
  }

  @Override
  public void loginUser(String userId) {
    if (userId.equals("")) {
      this.createUserView.setFailureOutput("UserId cannot be blank.");
      return;
    }
    try {
      User user;
      StatusObject<User> selectedUser = model.getUser(userId);
      if (selectedUser.statusCode > 0) {
        user = selectedUser.returnedObject;
        this.setUser(user);
        this.createUserView.clearAllUserInputs();
        this.createUserView.isVisible(false);
        this.portfolioMenuView.isVisible(true);
      } else {
        this.createUserView.setFailureOutput(selectedUser.statusMessage);
      }
    } catch (Exception e) {
      this.createUserView.setFailureOutput(e.getMessage());
    }
  }

  @Override
  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void loadUser(String pathToUserFile) {
    try {
      StatusObject<User> createdUser = model.createUserFromXML(pathToUserFile);
      if (createdUser.statusCode > 0) {
        model.updateUserFile(createdUser.returnedObject);
        this.createUserView.setSuccessOutput("Successfully loaded the User:\n"
                + ((createdUser.returnedObject)).getFirstName() + " "
                + ((createdUser.returnedObject)).getLastName());
        this.createUserView.clearAllUserInputs();
      } else {
        this.createUserView.setFailureOutput(createdUser.statusMessage);
      }
    } catch (IOException e) {
      this.createUserView.setFailureOutput(e.getMessage());
    }
  }

  @Override
  public void setView(JView jView) {
    this.createUserView = jView.getCreateUserView();
    this.portfolioMenuView = jView.getPortfolioMenuView();
    this.createUserView.addFeatures(this);
    this.createPortfolioView = jView.getCreatePortfolioView();
    this.calculateValueView = jView.getCalculateValueView();
    this.calculateCostBasisView = jView.getCalculateCostBasisView();
    this.loadPortfolioView = jView.getLoadPortfolioView();
    this.viewPortfolioView = jView.getViewPortfolioView();
    this.transactionView = jView.getTransactionView();
    this.commissionView = jView.getCommissionView();
    this.performanceView = jView.getPerformanceView();
    this.portfolioMenuView.addFeatures(this);
    this.createPortfolioController.setView(jView);
    this.calculateValueController.setView(jView);
    this.calculateCostBasisController.setView(jView);
    this.viewPortfolioController.setView(jView);
    this.loadPortfolioController.setView(jView);
    this.transactionController.setView(jView);
    this.commissionController.setView(jView);
    this.performanceController.setView(jView);
  }

  @Override
  public void transactInPortfolio() {
    transactionController.setUser(user);
    this.transactionView.displayRadioButtonsForPortfolio(listFlexiblePortfolios(user, model));
    this.portfolioMenuView.isVisible(false);
    this.transactionView.isVisible(true);
  }

  @Override
  public void performanceOfPortfolio() {
    performanceController.setUser(user);
    this.performanceView.displayRadioButtonsForPortfolio(listFlexiblePortfolios(user, model));
    this.portfolioMenuView.isVisible(false);
    this.performanceView.clearUserInputs();
    this.performanceView.isVisible(true);
  }

  @Override
  public void viewPortfolio() {
    viewPortfolioController.setUser(user);
    this.viewPortfolioView.displayRadioButtonsForPortfolio(listFlexiblePortfolios(user, model));
    this.portfolioMenuView.isVisible(false);
    this.viewPortfolioView.clearUserInputs();
    this.viewPortfolioView.isVisible(true);
  }

  @Override
  public void loadPortfolio() {
    loadPortfolioController.setUser(user);
    this.portfolioMenuView.isVisible(false);
    this.loadPortfolioView.isVisible(true);
  }

  @Override
  public void costBasisPortfolio() {
    calculateCostBasisController.setUser(user);
    this.calculateCostBasisView.displayRadioButtonsForPortfolio(listFlexiblePortfolios(user,
            model));
    this.portfolioMenuView.isVisible(false);
    this.calculateCostBasisView.isVisible(true);
  }

  @Override
  public void valueOfPortfolio() {
    calculateValueController.setUser(user);
    this.calculateValueView.displayRadioButtonsForPortfolio(listFlexiblePortfolios(user, model));
    this.portfolioMenuView.isVisible(false);
    this.calculateValueView.isVisible(true);
  }

  @Override
  public void createPortfolio() {
    createPortfolioController.setUser(user);
    createPortfolioController.resetStockList();
    this.createPortfolioView.clearUserInputs();
    this.portfolioMenuView.isVisible(false);
    this.createPortfolioView.isVisible(true);
  }

  @Override
  public void setCommission() {
    commissionController.setUser(user);
    this.portfolioMenuView.isVisible(false);
    this.commissionView.isVisible(true);
    this.commissionView.setInitialMessage(user.getCommission());
  }

  @Override
  public void portfolioMenuBack() {
    this.portfolioMenuView.isVisible(false);
    this.createUserView.isVisible(true);
  }


}
