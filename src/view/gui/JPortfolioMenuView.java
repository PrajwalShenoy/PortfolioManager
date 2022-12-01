package view.gui;

import controller.gui.JController;

/**
 * This is a view interface for main portfolio menu.
 */
public interface JPortfolioMenuView {
  /**
   * Method to link the view to controller.
   *
   * @param jController controller for portfolio menu.
   */
  void addFeatures(JController jController);

  /**
   * Method to set the visibility of a view.
   */
  void isVisible(boolean state);
}
