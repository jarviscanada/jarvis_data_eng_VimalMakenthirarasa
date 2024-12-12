package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.Position;
import ca.jrvs.apps.stockquote.dao.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PositionService {

    private final PositionDao dao;
    private final QuoteDao quoteDao;
    private static final Logger logger = LoggerFactory.getLogger(PositionService.class);

    public PositionService(PositionDao dao, QuoteDao quoteDao) {
        this.dao = dao;
        this.quoteDao = quoteDao;
    }

    /**
     * Processes a buy order and updates the database accordingly
     * @param ticker
     * @param numberOfShares
     * @param price
     * @return The position in our database after processing the buy
     */
    public Position buy(String ticker, int numberOfShares, double price) {
        // check if inputted symbol is valid
        if (ticker == null || ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be null or empty.");
            throw new IllegalArgumentException("Ticker symbol cannot be null or empty.");
        }
        if (numberOfShares <= 0) {
            logger.error("Number of shares to buy must be positive.");
            throw new IllegalArgumentException("Number of shares to buy must be positive.");
        }
        if (price <= 0) {
            logger.error("Price per share must be positive.");
            throw new IllegalArgumentException("Price per share must be positive.");
        }

        // get latest quote to check ticker and get available volume
        Optional<Quote> quoteOpt = quoteDao.findById(ticker);
        if (!quoteOpt.isPresent()) {
            logger.error("Invalid ticker symbol: {}", ticker);
            throw new IllegalArgumentException("Invalid ticker symbol: " + ticker);
        }

        Quote quote = quoteOpt.get();
        int availableVolume = quote.getVolume();

        // Business Logic: Cannot buy more than available volume
        if (numberOfShares > availableVolume) {
            logger.error("Cannot buy {} shares for ticker {}. Available volume: {}.", numberOfShares, ticker, availableVolume);
            throw new IllegalArgumentException("Cannot buy " + numberOfShares + " shares for ticker " + ticker + ". Available volume: " + availableVolume);
        }

        // Fetch existing position
        Optional<Position> positionOpt = dao.findById(ticker);
        Position position;

        if (positionOpt.isPresent()) {
            // update existing position
            position = positionOpt.get();
            position.setNumOfShares(position.getNumOfShares() + numberOfShares);
            position.setValuePaid(position.getValuePaid() + (numberOfShares * price));
            logger.info("Updating existing position for ticker {}: +{} shares at ${} each.", ticker, numberOfShares, price);
        } else {
            // create new position
            position = new Position();
            position.setTicker(ticker);
            position.setNumOfShares(numberOfShares);
            position.setValuePaid(numberOfShares * price);
            logger.info("Creating new position for ticker {}: {} shares at ${} each.", ticker, numberOfShares, price);
        }

        // save updated or new position
        dao.save(position);
        logger.info("Successfully processed buy order for ticker {}.", ticker);
        return position;
    }

    /**
     * Sells all shares of the given ticker symbol.
     * @param ticker
     */
    public void sell(String ticker) {
        // check if inputted symbol is valid
        if (ticker == null || ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be null or empty.");
            throw new IllegalArgumentException("Ticker symbol cannot be null or empty.");
        }

        // get existing position
        Optional<Position> positionOpt = dao.findById(ticker);
        if (!positionOpt.isPresent()) {
            logger.error("No existing position found for ticker: {}", ticker);
            throw new IllegalArgumentException("No existing position found for ticker: " + ticker);
        }

        // delete position
        dao.deleteById(ticker);
        logger.info("Successfully sold all shares for ticker {}.", ticker);
    }
}