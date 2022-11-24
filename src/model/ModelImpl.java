package model;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import model.handler.UserHandler;
import model.utils.StatusObject;
import model.utils.Transaction;

import static model.utils.Utils.getListingDateForTicker;
import static model.utils.Utils.getParentDirPath;
import static model.utils.Utils.getValueOnDateForStock;
import static model.utils.Utils.makeCSVChronological;
import static model.utils.Utils.readCSVIntoQueue;
import static model.utils.Utils.validateForDateAfterListing;
import static model.utils.Utils.validateForLegalDate;
import static model.utils.Utils.validateForNonZeroInteger;
import static model.utils.Utils.validateTicker;
import static model.utils.Utils.writeToCSV;
import static utils.Utils.getConfigAndProgramState;

/**
 * This class is an implementation of the Model Interface.
 * The methods associated with the class help perform operations of
 * the model which are the functionalities offered by the program.
 */

public class ModelImpl implements Model {

  private final PortfolioManager portfolioManager;
  private final Map<String, String> config;

  /**
   * This is the constructor for ModelImpl. An Instance of ModelImpl
   * is created with the help of this constructor.
   * It creates a PortfolioImpl by taking the following parameters.
   */

  public ModelImpl() {
    this.config = getConfigAndProgramState();
    int userId = Integer.parseInt(config.get("userId"));
    this.portfolioManager = new PortfolioManager(userId);
  }

  @Override
  public StatusObject<User> createUser(String firstName, String lastName, double commission) {
    try {
      if (firstName == null | lastName == null) {
        throw new IllegalArgumentException("Null is not a valid input "
                + "for first/last name");
      }
      if (commission < 0) {
        throw new IllegalArgumentException("Commission cannot be negative, "
                + "enter a positive number.");
      }
      User createdUser = portfolioManager.createUser(firstName, lastName, commission,
              config.get("userData"));
      return new StatusObject<>("Created user successfully with UserId:"
              + createdUser.getUserId()
              + " with name as "
              + createdUser.getFirstName()
              + " " + createdUser.getLastName(), 1,
              createdUser);
    } catch (Exception e) {
      return new StatusObject<>("User could not be created due to error: "
              + e.getMessage(),
              -1, null);
    }
  }

  @Override
  public StatusObject<User> getUser(String userId) {
    try {
      String pathToUser =
              this.config.get("userData") + "/" + userId + "/" + userId + ".xml";
      File userFile = new File(pathToUser);
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      SAXParser saxParser = saxParserFactory.newSAXParser();
      UserHandler handler = new UserHandler(false);
      saxParser.parse(userFile, handler);
      return new StatusObject<User>("Retrieved user successfully",
              1, handler.getUserList().get(0));
    } catch (ParserConfigurationException e) {
      return new StatusObject<User>("Parser configuration failed, please "
              + "configure XML parser properly",
              -1, null);
    } catch (SAXException e) {
      return new StatusObject<User>("XML file seems to be"
              + " corrupted.", -1, null);
    } catch (IOException e) {
      return new StatusObject<User>("User ID does not exist "
              + "or there might be problems with "
              + "the config file", -1, null);
    }
  }


  @Override
  public StatusObject<User> createUserFromXML(String xmlPath) {
    try {
      File userFile = new File(xmlPath);
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      SAXParser saxParser = saxParserFactory.newSAXParser();
      UserHandler handler = new UserHandler(true);
      saxParser.parse(userFile, handler);
      User createdUser = handler.getUserList().get(0);
      if (! verifyAndLoadTransactionsForFlexiblePortfolios(createdUser, xmlPath)) {
        throw new ExceptionInInitializerError("The transaction files are not present "
                + "or have discrepancies with the portfolio (XML file)");
      }
      return new StatusObject<>("Successfully created the"
              + " User Object with User ID "
              + createdUser.getUserId(), 1,
              createdUser);
    } catch (ParserConfigurationException e) {
      return new StatusObject<>("Parser configuration failed,"
              + " please configure XML parser properly",
              -1, null);
    } catch (SAXException e) {
      return new StatusObject<>("XML file seems to be "
              + "corrupted.", -1, null);
    } catch (IOException e) {
      return new StatusObject<>("File directory does not "
              + "exist or there might be problems with "
              + "the config file", -1, null);
    } catch (ExceptionInInitializerError e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  private boolean verifyAndLoadTransactionsForFlexiblePortfolios(User user, String xmlPath) {
    String parentPath = getParentDirPath(xmlPath);
    try {
      for (FlexiblePortfolio portfolio : user.getListOfFlexiblePortfolios()) {
        String pathToTransactions = parentPath + "/" + user.getUserId() + "_"
                + portfolio.getPortfolioId() + ".csv";
        String userDirPath = getParentDirPath(portfolio.getPathToPortfolioTransactions());
        Files.createDirectories(Paths.get(userDirPath));
        (new File(portfolio.getPathToPortfolioTransactions())).createNewFile();
        StatusObject<PriorityQueue<Transaction>> validatedTransactions =
                portfolio.getValidatedTransactions(
                        readCSVIntoQueue(pathToTransactions, false));
        if (validatedTransactions.statusCode < 0) {
          return false;
        }
        writeToCSV(validatedTransactions.returnedObject,
                portfolio.getPathToPortfolioTransactions());
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public void updateUserFile(User user) throws IOException {
    String userDataDir = this.config.get("userData");
    try {
      File userDir = new File(userDataDir + "/" + user.getUserId());
      if (!userDir.exists()) {
        userDir.mkdirs();
      }
      File userFile = new File(userDataDir + "/" + user.getUserId() + "/"
              + user.getUserId() + ".xml");
      FileWriter programStateWriter = new FileWriter(userFile);
      userFile.createNewFile();
      programStateWriter.write(user.xml());
      programStateWriter.close();
    } catch (IOException e) {
      throw new IOException("File directory does not exist "
              + "or there might be problems with the config file");
    }
  }

  @Override
  public List<RigidPortfolio> getRigidPortfoliosForUser(User user) {
    return user.getListOfRigidPortfolios();
  }

  @Override
  public List<FlexiblePortfolio> getFlexiblePortfoliosForUser(User user) {
    return user.getListOfFlexiblePortfolios();
  }


  @Override
  public StatusObject<RigidPortfolio> getParticularRigidPortfolio(User user, int portfolioId) {
    try {
      RigidPortfolio toReturn = null;
      for (RigidPortfolio portfolio : user.getListOfRigidPortfolios()) {
        if (portfolio.getPortfolioId() == portfolioId) {
          toReturn = portfolio;
          break;
        }
      }
      if (toReturn != null) {
        return new StatusObject<>("Retrieved portfolio "
                + "successfully", 1, toReturn);
      } else {
        throw new IllegalArgumentException("Portfolio with ID "
                + portfolioId + " not found");
      }
    } catch (Exception e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  @Override
  public StatusObject<FlexiblePortfolio> getParticularFlexiblePortfolio(
          User user, int portfolioId) {
    try {
      FlexiblePortfolio toReturn = null;
      for (FlexiblePortfolio portfolio : user.getListOfFlexiblePortfolios()) {
        if (portfolio.getPortfolioId() == portfolioId) {
          toReturn = portfolio;
          break;
        }
      }
      if (toReturn != null) {
        return new StatusObject<>("Retrieved portfolio "
                + "successfully", 1, toReturn);
      } else {
        throw new IllegalArgumentException("Portfolio with ID "
                + portfolioId + " not found");
      }
    } catch (Exception e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  @Override
  public int getPortfolioId(Portfolio portfolio) {
    return portfolio.getPortfolioId();
  }

  @Override
  public String getPortfolioName(Portfolio portfolio) {
    return portfolio.getPortfolioName();
  }

  @Override
  public String getPortfolioType(Portfolio portfolio) {
    return portfolio.getType();
  }

  @Override
  public StatusObject<RigidPortfolio> createRigidPortfolio(User user, String portfolioName,
                                                           List<Stock> listOfStocks) {
    try {
      RigidPortfolio createdPortfolio = user.createRigidPortfolio(portfolioName, listOfStocks);
      return new StatusObject<>("Successfully created"
              + " portfolio " + portfolioName + " for User ID "
              + user.getUserId(), 1, createdPortfolio);
    } catch (Exception e) {
      return new StatusObject<>("Portfolio could not"
              + " be created " + e.getMessage(),
              -1, null);
    }
  }

  @Override
  public StatusObject<FlexiblePortfolio> createFlexiblePortfolio(User user, String portfolioName,
                                                                 List<Stock> listOfStocks) {
    try {
      String portfolioPath = this.config.get("userData") + "/" + user.getUserId() + "/"
              + user.getNextFlexiblePortfolioId() + ".csv";
      File newPortfolioTransactions = new File(portfolioPath);
      if (!newPortfolioTransactions.createNewFile()) {
        throw new RuntimeException("Portfolio file already exists. This is causing discrepancies");
      }
      FlexiblePortfolio createdPortfolio = user.createFlexiblePortfolio(portfolioName,
              listOfStocks, portfolioPath);
      if (createdPortfolio == null) {
        throw new IOException("Portfolio could not be created as the transaction.csv could "
                + "not be created or is not found");
      }
      return new StatusObject<>("Successfully created"
              + " portfolio " + portfolioName + " for User ID "
              + user.getUserId(), 1, createdPortfolio);
    } catch (Exception e) {
      return new StatusObject<>("Portfolio could not"
              + " be created " + e.getMessage(),
              -1, null);
    }
  }

  @Override
  public StatusObject<Double> getValueOfPortfolioForDate(Portfolio portfolio, String date) {
    try {
      validateForLegalDate(date);
      return new StatusObject<>("Successfully calculated the value of the portfolio",
              1, portfolio.getValueForDate(date));
    } catch (Exception e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  @Override
  public int getNextUserId() {
    return this.portfolioManager.getNextUserId();
  }

  @Override
  public Stock createStock(String ticker, double quantity, String date) {
    if (date != null) {
      validateForLegalDate(date);
    }
    return new StockImpl(ticker, quantity, date, config, true);
  }

  @Override
  public String getStockTicker(Stock stock) {
    return stock.getTicker();
  }

  @Override
  public StatusObject<Stock> addStockToUninitializedPortfolio(Stock stock, double quantity) {
    double newQuantity = stock.getStockQuantity() + quantity;
    stock.setStockQuantity(newQuantity);
    return new StatusObject<Stock>(quantity + " stocks has been bought "
            + "successfully!", 1, stock);
  }

  @Override
  public String getStockPurchaseDate(Stock stock) {
    return stock.getDate();
  }

  @Override
  public StatusObject<PriorityQueue<Transaction>> getValidatedTransactions(
          FlexiblePortfolio portfolio, PriorityQueue<Transaction> transactions) {
    return portfolio.getValidatedTransactions(transactions);
  }

  @Override
  public StatusObject<FlexiblePortfolio> buyStockInFlexiblePortfolio(
          FlexiblePortfolio portfolio, String ticker, String date, double quantity) {
    try {
      validateForNonZeroInteger(quantity);
      validateForLegalDate(date);
      String listingDate = getListingDateForTicker(ticker);
      // validateTicker(ticker);
      validateForDateAfterListing(date, listingDate);
      portfolio.buyStock(ticker, date, quantity);
      return new StatusObject<>("Bought "
              + quantity + " of " + ticker + " successfully.",
              1, portfolio);
    } catch (IOException e) {
      return new StatusObject<>("Transaction file for the portfolio does not exist.",
              -1, null);
    } catch (IllegalArgumentException e) {
      return new StatusObject<>(e.getMessage(),-1, null);
    }
  }

  @Override
  public StatusObject<FlexiblePortfolio> sellStockInFlexiblePortfolio(
          FlexiblePortfolio portfolio, String ticker, String date, double quantity) {
    try {
      validateForNonZeroInteger(quantity);
      validateForLegalDate(date);
      validateTicker(ticker);
      portfolio.sellStock(ticker, date, quantity);
      return new StatusObject<>("Sold "
              + quantity + " of " + ticker + " successfully.",
              1, portfolio);
    } catch (IOException | ParseException e) {
      return new StatusObject<>("Transaction file for the portfolio does not exist "
              + "or is corrupted.", -1, null);
    } catch (IllegalArgumentException e) {
      return new StatusObject<>(e.getMessage(),-1, null);
    }
  }

  @Override
  public StatusObject<PriorityQueue<Transaction>> getFlexiblePortfoliosPastTransactions(
          User user, FlexiblePortfolio portfolio) {
    try {
      return new StatusObject<>("Loaded previous transactions successfully",
              1,
              portfolio.loadPastTransactions(user));
    } catch (FileNotFoundException e) {
      return new StatusObject<>("Transactions file not found", -1,
              null);
    }
  }

  @Override
  public StatusObject<String> updateFlexiblePortfolioCsv(User user, FlexiblePortfolio portfolio) {
    try {
      makeCSVChronological(config.get("userData") + "/" + user.getUserId()
              + "/" + portfolio.getPortfolioId() + ".csv");
      return new StatusObject<String>("Successfully wrote to the CSV file",
              1, null);
    } catch (IOException e) {
      return new StatusObject<String>("Could not write to the CSV file",
              -1, null);
    }
  }

  @Override
  public StatusObject<String> viewCompositionOfFlexiblePortfolio(
          User user, FlexiblePortfolio portfolio, String date) {
    try {
      validateForLegalDate(date);
      StringBuilder toReturn = new StringBuilder();
      toReturn.append("Portfolio Name: ").append(portfolio.getPortfolioName()).append("\n");
      toReturn.append("Portfolio ID: ").append(portfolio.getPortfolioId()).append("\n");
      toReturn.append("These are the stocks present in the portfolio and their details:\n");
      List<Stock> listOfStocks = portfolio.getCompositionOfFlexiblePortfolio(date);
      for (Stock stock : listOfStocks) {
        toReturn.append("* ").append(stock.stockAsString()).append("\n");
      }
      return new StatusObject<>("Successfully computed the composition of the port"
              + "folio", 1, toReturn.toString());
    } catch (ParseException | FileNotFoundException | IllegalArgumentException e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  @Override
  public StatusObject<Double> getCostBasisOfFlexiblePortfolioForDate(
          User user, FlexiblePortfolio portfolio, String date) {
    try {
      validateForLegalDate(date);
      double costBasis = 0;
      List<String[]> csvLines = portfolio.getCSVUptoDate(date);
      for (String[] csvLine : csvLines) {
        if (csvLine[2].equalsIgnoreCase("buy")) {
          costBasis += (getValueOnDateForStock(csvLine[0], csvLine[3])
                  * Double.parseDouble(csvLine[1])) + Double.parseDouble(csvLine[4]);
        } else {
          costBasis += Double.parseDouble(csvLine[4]);
        }
      }
      return new StatusObject<>("Successfully calculated cost basis",
              1, costBasis);
    } catch (Exception e) {
      return new StatusObject<>(e.getMessage(), -1, null);
    }
  }

  @Override
  public StatusObject<String> performanceOfPortfolioOverTime(User user, Portfolio portfolio,
                                                             String startDate, String endDate) {
    try {
      validateForLegalDate(startDate);
      validateForLegalDate(endDate);
      SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
      long date1 = dateParser.parse(startDate).getTime();
      long date2 = dateParser.parse(endDate).getTime();

      long timeDiff = Math.abs(date2 - date1);
      double days = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
      double weeks = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 7;
      double biWeeks = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 14;
      double months = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 30;
      double quarter = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 90;
      double sixMonths = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 180;
      double years = (double) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) / 365;

      Map<Double, Integer> daysMappingAscending = new HashMap<>();
      daysMappingAscending.put(days, 1);
      daysMappingAscending.put(weeks, 7);
      daysMappingAscending.put(biWeeks, 14);
      daysMappingAscending.put(months, 30);
      daysMappingAscending.put(quarter, 90);
      daysMappingAscending.put(sixMonths, 180);
      daysMappingAscending.put(years, 365);

      Calendar c = Calendar.getInstance();
      c.setTime(dateParser.parse(startDate));
      double numberOfSticks = 0;
      int timeMultiple = 0;
      c.setTime(dateParser.parse(startDate));
      for (double timeQuant : daysMappingAscending.keySet()) {
        if (timeQuant > 5 & timeQuant < 30) {
          numberOfSticks = timeQuant;
          timeMultiple = daysMappingAscending.get(timeQuant);
          break;
        }
      }
      List<String> valuesForDates = new ArrayList<>();
      while (numberOfSticks > 0) {
        valuesForDates.add(dateParser.format(c.getTime()));
        c.add(Calendar.DATE, (int) timeMultiple);
        numberOfSticks -= 1;
      }
      if (numberOfSticks != 0) {
        valuesForDates.add(endDate);
      }
      List<Double> valuations = new ArrayList<>();
      for (String date : valuesForDates) {
        valuations.add(portfolio.getValueForDate(date));
      }
      double mini = Collections.min(valuations);
      double max = Collections.max(valuations);
      valuations = valuations.stream().map(i -> i - mini).collect(Collectors.toList());
      double scale = (max - mini) / 50;
      StringBuilder toReturn = new StringBuilder();
      toReturn.append("Performance of portfolio ").append(portfolio.getPortfolioId())
              .append(" from ").append(startDate).append(" to ").append(endDate).append("\n\n");
      for (int i = 0; i < valuations.size(); i++) {
        toReturn.append(valuesForDates.get(i)).append(" :").append(
                "*".repeat((int) (valuations.get(i) / scale))).append("\n");
      }
      toReturn.append("\n\n" + "Scale: * = $").append(scale).append(" and Base = $").append(mini);
      return new StatusObject<>("Successfully calculated"
              + " the performance of the portfolio",
              1, toReturn.toString());
    } catch (ParseException e) {
      return new StatusObject<>("Error parsing the given dates",
              -1, null);
    } catch (FileNotFoundException e) {
      return new StatusObject<>("Transaction file not found for the portfolio",
              -1, null);
    } catch (IllegalArgumentException e) {
      return new StatusObject<>(e.getMessage(),-1, null);
    }
  }
}