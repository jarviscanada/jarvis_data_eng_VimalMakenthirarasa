package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.Position;
import ca.jrvs.apps.stockquote.dao.Quote;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PositionService_UnitTest {

    private PositionDao mockPositionDao;
    private QuoteService mockQuoteService;
    private PositionService positionService;

    @Before
    public void setUp() {
        mockPositionDao = mock(PositionDao.class);
        mockQuoteService = mock(QuoteService.class);
        positionService = new PositionService(mockPositionDao, mockQuoteService);
    }

    // test buying new position
    @Test
    public void testBuy_NewPosition() {
        String ticker = "MSFT";
        int numberOfShares = 100;
        double price = 450.0;

        // mock QuoteService.fetchQuoteDataFromAPI to return a valid Quote
        Quote mockQuote = new Quote();
        mockQuote.setTicker(ticker);
        mockQuote.setVolume(200);
        when(mockQuoteService.fetchQuoteDataFromAPI(ticker)).thenReturn(Optional.of(mockQuote)); // Changed
        when(mockPositionDao.findById(ticker)).thenReturn(Optional.empty());
        ArgumentCaptor<Position> positionCaptor = ArgumentCaptor.forClass(Position.class);
        when(mockPositionDao.save(any(Position.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Position result = positionService.buy(ticker, numberOfShares, price);

        assertNotNull("Result should not be null", result);
        assertEquals("Ticker should match", ticker, result.getTicker());
        assertEquals("Number of shares should match", numberOfShares, result.getNumOfShares());
        assertEquals("Value paid should match", numberOfShares * price, result.getValuePaid(), 0.001);

        // verify interactions
        verify(mockQuoteService).fetchQuoteDataFromAPI(ticker); // Changed
        verify(mockPositionDao).findById(ticker);
        verify(mockPositionDao).save(positionCaptor.capture());

        // verify position object saved properly
        Position savedPosition = positionCaptor.getValue();
        assertEquals("Ticker should match", ticker, savedPosition.getTicker());
        assertEquals("Number of shares should match", numberOfShares, savedPosition.getNumOfShares());
        assertEquals("Value paid should match", numberOfShares * price, savedPosition.getValuePaid(), 0.001);
    }

    // test buy when updating existing position
    @Test
    public void testBuy_UpdatePosition() {
        String ticker = "MSFT";
        int numberOfShares = 50;
        double price = 450;

        // mock QuoteService.fetchQuoteDataFromAPI to return valid quote
        Quote mockQuote = new Quote();
        mockQuote.setTicker(ticker);
        mockQuote.setVolume(100);
        when(mockQuoteService.fetchQuoteDataFromAPI(ticker)).thenReturn(Optional.of(mockQuote)); // Changed

        // mock PositionDao.findById to return existing position
        Position existingPosition = new Position();
        existingPosition.setTicker(ticker);
        existingPosition.setNumOfShares(100);
        existingPosition.setValuePaid(5000.0);
        when(mockPositionDao.findById(ticker)).thenReturn(Optional.of(existingPosition));
        ArgumentCaptor<Position> positionCaptor = ArgumentCaptor.forClass(Position.class);
        when(mockPositionDao.save(any(Position.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Position result = positionService.buy(ticker, numberOfShares, price);

        assertNotNull("Result should not be null", result);
        assertEquals("Ticker should match", ticker, result.getTicker());
        assertEquals("Number of shares should be updated", 150, result.getNumOfShares());
        assertEquals("Value paid should be updated", 5000.0 + (numberOfShares * price), result.getValuePaid(), 0.001);

        // verify interactions
        verify(mockQuoteService).fetchQuoteDataFromAPI(ticker); // Changed
        verify(mockPositionDao).findById(ticker);
        verify(mockPositionDao).save(positionCaptor.capture());

        // verify position object saved properly
        Position savedPosition = positionCaptor.getValue();
        assertEquals("Ticker should match", ticker, savedPosition.getTicker());
        assertEquals("Number of shares should match", 150, savedPosition.getNumOfShares());
        assertEquals("Value paid should match", 5000.0 + (numberOfShares * price), savedPosition.getValuePaid(), 0.001);
    }

    // test sell position
    @Test
    public void testSell() {
        String ticker = "MSFT";

        // mock PositionDao.findById to return existing position
        Position existingPosition = new Position();
        existingPosition.setTicker(ticker);
        existingPosition.setNumOfShares(100);
        existingPosition.setValuePaid(45000.0);
        when(mockPositionDao.findById(ticker)).thenReturn(Optional.of(existingPosition));

        positionService.sell(ticker);

        // verify interactions
        verify(mockPositionDao).findById(ticker);
        verify(mockPositionDao).deleteById(ticker);
    }
}