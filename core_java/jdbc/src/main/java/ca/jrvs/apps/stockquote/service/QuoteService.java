package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.dao.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class QuoteService {

    private final QuoteDao dao;
    private final QuoteHttpHelper httpHelper;
    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

    public QuoteService(QuoteDao dao, QuoteHttpHelper httpHelper) {
        this.dao = dao;
        this.httpHelper = httpHelper;
    }

    /**
     * Fetches latest quote data from endpoint
     * @param ticker
     * @return Latest quote information or empty optional if ticker symbol not found
     */
    public Optional<Quote> fetchQuoteDataFromAPI(String ticker) {
        if (ticker == null || ticker.isEmpty()) {
            logger.error("Ticker symbol cannot be null or empty.");
            return Optional.empty();
        }

        try {
            // get quote data from API
            Quote quote = httpHelper.fetchQuoteInfo(ticker);

            if (quote == null) {
                logger.info("No quote data found for ticker: {}", ticker);
                return Optional.empty();
            }

            // save quote to db
            dao.save(quote);
            logger.info("Successfully fetched and saved quote for ticker: {}", ticker);
            return Optional.of(quote);
        } catch (Exception e) {
            logger.error("Error fetching quote data for ticker: {}", ticker, e);
            return Optional.empty();
        }
    }
}

