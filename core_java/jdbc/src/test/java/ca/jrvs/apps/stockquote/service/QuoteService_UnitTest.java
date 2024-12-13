package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.dao.Quote;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteService_UnitTest {

    private QuoteDao mockQuoteDao;
    private QuoteHttpHelper mockHttpHelper;
    private QuoteService quoteService;

    @Before
    public void setUp() {
        mockQuoteDao = mock(QuoteDao.class);
        mockHttpHelper = mock(QuoteHttpHelper.class);
        quoteService = new QuoteService(mockQuoteDao, mockHttpHelper);
    }

    @Test
    public void testFetchQuoteDataFromAPI() throws Exception {
        String ticker = "TEST1";
        Quote mockQuote = new Quote();
        mockQuote.setTicker(ticker);
        mockQuote.setOpen(100.00);
        mockQuote.setHigh(110.00);
        mockQuote.setLow(90.00);
        mockQuote.setPrice(105.00);
        mockQuote.setVolume(500000);
        mockQuote.setLatestTradingDay(Date.valueOf("2024-01-01"));
        mockQuote.setPreviousClose(95.00);
        mockQuote.setChange(10.00);
        mockQuote.setChangePercent("10.53%");
        mockQuote.setTimestamp(new Timestamp(System.currentTimeMillis()));

        when(mockHttpHelper.fetchQuoteInfo(ticker)).thenReturn(mockQuote);
        when(mockQuoteDao.save(mockQuote)).thenReturn(mockQuote);

        // assertions
        Optional<Quote> result = quoteService.fetchQuoteDataFromAPI(ticker);
        assertTrue("Result should be present", result.isPresent());
        assertEquals("Ticker should match", ticker, result.get().getTicker());
        assertEquals("Price should match", 105.00, result.get().getPrice(), 0.001);
        assertEquals("Volume should match", 500000, result.get().getVolume());
        assertEquals("Change should match", 10.00, result.get().getChange(), 0.001);
        assertEquals("ChangePercent should match", "10.53%", result.get().getChangePercent());

        // verify interactions
        verify(mockHttpHelper).fetchQuoteInfo(ticker);
        verify(mockQuoteDao).save(mockQuote);
    }
}
