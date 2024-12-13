package ca.jrvs.apps.stockquote.controller;

import ca.jrvs.apps.stockquote.dao.Position;
import ca.jrvs.apps.stockquote.service.PositionService;
import ca.jrvs.apps.stockquote.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Scanner;

public class StockQuoteController {

    private final QuoteService quoteService;
    private final PositionService positionService;
    private static final Logger logger = LoggerFactory.getLogger(StockQuoteController.class);

    public StockQuoteController(QuoteService quoteService, PositionService positionService) {
        this.quoteService = quoteService;
        this.positionService = positionService;
    }

    public void initClient() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            printMenu();
            System.out.print("Enter your choice: ");
            int choice;

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                logger.error("Invalid input. Please enter a number between 0 and 4.");
                System.out.println("Invalid input. Please enter a number between 0 and 4.\n");
                continue;
            }

            switch (choice) {
                case 0: // get stock quote info from API
                    viewStockQuote(scanner);
                    break;
                case 1: // buy stock
                    buyStocks(scanner);
                    break;
                case 2: // sell stock
                    sellStocks(scanner);
                    break;
                case 3: // view all currently held positions
                    viewAllPositions();
                    break;
                case 4:
                    exit = true;
                    System.out.println("Exiting the application. Goodbye!");
                    logger.info("Application exited by user.");
                    break;
                default:
                    logger.error("Invalid choice: {}. Please select a number between 0 and 4.", choice);
                    System.out.println("Invalid choice. Please select a number between 0 and 4.\n");
            }
        }

        scanner.close();
    }

    private void printMenu() {
        System.out.println("===================================");
        System.out.println("||        Stock Quote App        ||");
        System.out.println("Please choose an option:");
        System.out.println("0. View Stock Quote");
        System.out.println("1. Buy Stock");
        System.out.println("2. Sell Stock");
        System.out.println("3. View All Held Positions");
        System.out.println("4. Exit");
        System.out.println("===================================");
    }

    private void viewStockQuote(Scanner scanner) {
        System.out.print("Enter ticker symbol: ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        if (ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be empty.");
            System.out.println("Ticker symbol cannot be empty.\n");
            return;
        }

        try {
            var quoteOpt = quoteService.fetchQuoteDataFromAPI(ticker);
            if (quoteOpt.isPresent()) {
                var quote = quoteOpt.get();
                String formattedQuote = formatQuote(quote);

                System.out.println("===================================");
                System.out.println("Stock Quote Information:");
                System.out.println(formattedQuote);
                System.out.println("===================================\n");
                logger.info("Displayed quote for ticker: {}", ticker);
            } else {
                System.out.println("No quote data found for ticker: " + ticker + "\n");
                logger.warn("No quote data found for ticker: {}", ticker);
            }
        } catch (Exception e) {
            logger.error("Error fetching quote data for ticker: {}", ticker, e);
            System.out.println("Error fetching quote data for ticker: " + ticker + "\n");
        }
    }

    private void buyStocks(Scanner scanner) {
        System.out.print("Enter ticker symbol to buy: ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        if (ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be empty.");
            System.out.println("Ticker symbol cannot be empty.\n");
            return;
        }

        int numberOfShares;
        double price;

        try {
            System.out.print("Enter number of shares to buy: ");
            numberOfShares = Integer.parseInt(scanner.nextLine().trim());
            if (numberOfShares <= 0) {
                logger.error("Number of shares must be positive.");
                System.out.println("Number of shares must be positive.\n");
                return;
            }

            var quoteOpt = quoteService.fetchQuoteDataFromAPI(ticker);
            if (!quoteOpt.isPresent()) {
                System.out.println("Cannot fetch price for ticker: " + ticker + "\n");
                logger.warn("Cannot fetch price for ticker: {}", ticker);
                return;
            }

            price = quoteOpt.get().getPrice();
            System.out.println("Current price for " + ticker + " is $" + price);

        } catch (NumberFormatException e) {
            logger.error("Invalid number format for shares.", e);
            System.out.println("Invalid number format for shares.\n");
            return;
        } catch (Exception e) {
            logger.error("Error fetching price for ticker: {}", ticker, e);
            System.out.println("Error fetching price for ticker: " + ticker + "\n");
            return;
        }

        try {
            var position = positionService.buy(ticker, numberOfShares, price);
            System.out.println("Successfully bought " + numberOfShares + " shares of " + ticker + ".");
            System.out.println("Updated Position:");
            System.out.println(formatPosition(position) + "\n");
            logger.info("Bought {} shares of {}.", numberOfShares, ticker);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to buy stocks: {}", e.getMessage());
            System.out.println("Failed to buy stocks: " + e.getMessage() + "\n");
        } catch (Exception e) {
            logger.error("Error processing buy order for ticker: {}", ticker, e);
            System.out.println("Error processing buy order for ticker: " + ticker + "\n");
        }
    }

    private void sellStocks(Scanner scanner) {
        System.out.print("Enter ticker symbol to sell: ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        if (ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be empty.");
            System.out.println("Ticker symbol cannot be empty.\n");
            return;
        }

        try {
            positionService.sell(ticker);
            System.out.println("Successfully sold all shares of " + ticker + ".\n");
            logger.info("Sold all shares of {}.", ticker);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to sell stocks: {}", e.getMessage());
            System.out.println("Failed to sell stocks: " + e.getMessage() + "\n");
        } catch (Exception e) {
            logger.error("Error processing sell order for ticker: {}", ticker, e);
            System.out.println("Error processing sell order for ticker: " + ticker + "\n");
        }
    }

    private void viewAllPositions() {
        try {
            Iterable<Position> positions = positionService.getAllPositions();
            Iterator<Position> iterator = positions.iterator();
            if (!iterator.hasNext()) {
                System.out.println("No positions found.\n");
                logger.info("No positions to display.");
                return;
            }
            System.out.println("------------------------------------");
            System.out.println("Your Currently held Positions:");
            while (iterator.hasNext()) {
                Position position = iterator.next();
                System.out.println(formatPosition(position));
            }
            System.out.println("-----------------------------------\n");
            logger.info("Displayed all positions.");
        } catch (Exception e) {
            logger.error("Error retrieving positions.", e);
            System.out.println("Error retrieving positions.\n");
        }
    }

    // helper method for formatting Quotes
    private String formatQuote(ca.jrvs.apps.stockquote.dao.Quote quote) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticker: ").append(quote.getTicker()).append("\n");
        sb.append("Price: $").append(String.format("%.2f", quote.getPrice())).append("\n");
        sb.append("Volume: ").append(quote.getVolume()).append("\n");
        return sb.toString();
    }

    // helper method for formatting Positions
    private String formatPosition(Position position) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticker: ").append(position.getTicker()).append("\n");
        sb.append("Number of Shares: ").append(position.getNumOfShares()).append("\n");
        sb.append("Value Paid: $").append(String.format("%.2f", position.getValuePaid())).append("\n");
        return sb.toString();
    }
}
