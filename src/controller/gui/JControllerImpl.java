package controller.gui;

import model.Model;
import view.gui.JView;

/**
 * Class that implements the JController Interface which is the main controller of the GUI
 * for the stock program.
 */
public class JControllerImpl implements JController {

  private final JUserController userController;
  //  private JPortfolioController portfolioController;

  /**
   * Constructor to initialize model.
   *
   * @param model an object of type model.
   */
  public JControllerImpl(Model model) {
    userController = new JUserController(model);
  }

  @Override
  public void setView(JView jView) {
    userController.setView(jView);
  }

}
