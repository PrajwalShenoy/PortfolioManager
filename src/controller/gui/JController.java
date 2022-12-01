package controller.gui;

import model.User;
import view.gui.JView;

/**
 * Interface for the main controller of the GUI for the stock program.
 */
public interface JController {
  void createUser(String firstName, String lastName);

  void loginUser(String userId);

  void setUser(User user);

  void loadUser(String pathToUserFile);

  /**
   * Method to set the view.
   *
   * @param jView an object of type JView.
   */
  void setView(JView jView);

  void transactInPortfolio();

  void performanceOfPortfolio();

  void viewPortfolio();

  void loadPortfolio();

  void costBasisPortfolio();

  void valueOfPortfolio();

  void createPortfolio();

  void setCommission();

  void portfolioMenuBack();
}
