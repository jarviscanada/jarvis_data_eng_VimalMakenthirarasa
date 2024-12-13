package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.Position;
import ca.jrvs.apps.stockquote.dao.Quote;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import org.junit.*;

import java.sql.*;
import java.util.Optional;

import static org.junit.Assert.*;

public class PositionService_IntTest {

    private static Connection connection;
    private static QuoteDao quoteDao;
    private static PositionDao positionDao;
    private static QuoteHttpHelper httpHelper;
    private static PositionService positionService;

    // db connection parameters
    private static final String DB_URL_TEST = "jdbc:postgresql://localhost:5432/host_agent_test";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "password";

    // SQL Statements for table setup
    private static final String CREATE_QUOTE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS quote (\n" +
            "    symbol              VARCHAR(10) PRIMARY KEY,\n" +
            "    open                DECIMAL(10, 2) NOT NULL,\n" +
            "    high                DECIMAL(10, 2) NOT NULL,\n" +
            "    low                 DECIMAL(10, 2) NOT NULL,\n" +
            "    price               DECIMAL(10, 2) NOT NULL,\n" +
            "    volume              INT NOT NULL,\n" +
            "    latest_trading_day  DATE NOT NULL,\n" +
            "    previous_close      DECIMAL(10, 2) NOT NULL,\n" +
            "    change              DECIMAL(10, 2) NOT NULL,\n" +
            "    change_percent      VARCHAR(10) NOT NULL,\n" +
            "    timestamp           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL\n" +
            ");";

    private static final String CREATE_POSITION_TABLE_SQL = "CREATE TABLE IF NOT EXISTS position (\n" +
            "    symbol              VARCHAR(10) PRIMARY KEY,\n" +
            "    number_of_shares    INT NOT NULL,\n" +
            "    value_paid          DECIMAL(10, 2) NOT NULL\n" +
            ");";

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // connect to host_agent_)test database
        connection = DriverManager.getConnection(DB_URL_TEST, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(false);

        // create quote and position tables
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_QUOTE_TABLE_SQL);
            stmt.execute(CREATE_POSITION_TABLE_SQL);
        }

        // init quoteDao and PositionDao
        quoteDao = new QuoteDao(connection);
        positionDao = new PositionDao(connection);

        // init QuoteHttpHelper
        httpHelper = new QuoteHttpHelper();

        // init PositionService
        positionService = new PositionService(positionDao, quoteDao);
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // drop tables after tests, position is dependent on quote so drop it first
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS position;");
                stmt.execute("DROP TABLE IF EXISTS quote;");
            }
            connection.close();
        }
    }

    @Before
    public void setUp() throws SQLException {
        // clean quote and position tables before each test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM position;");
            stmt.execute("DELETE FROM quote;");
        }
    }

    @After
    public void tearDown() throws SQLException {
        // rollback any changes made during test
        connection.rollback();
    }

    /**
     * Integration Test: Test buy method with a valid ticker and no existing position.
     * Expects a new Position to be created and saved to the database.
     */
    @Test
    public void testBuy_NewPosition() {
        String ticker = "MSFT";
        int numberOfShares = 10;
        double price = 450.0;

        // check to see if quote exists in quote table
        Optional<Quote> quoteOpt = quoteDao.findById(ticker);
        if (!quoteOpt.isPresent()) {
            // not in quote table, get from API and save to db
            Quote fetchedQuote = httpHelper.fetchQuoteInfo(ticker);
            assertNotNull("Fetched Quote should not be null", fetchedQuote);
            quoteDao.save(fetchedQuote);
        }

        Position result = positionService.buy(ticker, numberOfShares, price);

        // check result
        assertNotNull("Result should not be null", result);
        assertEquals("Ticker should match", ticker, result.getTicker());
        assertEquals("Number of shares should match", numberOfShares, result.getNumOfShares());
        assertEquals("Value paid should match", numberOfShares * price, result.getValuePaid(), 0.001);

        // verify position saved in db
        Optional<Position> retrievedPosition = positionDao.findById(ticker);
        assertTrue("Position should be present in the database", retrievedPosition.isPresent());
        Position savedPosition = retrievedPosition.get();
        assertEquals("Saved Position ticker should match", ticker, savedPosition.getTicker());
        assertEquals("Saved Position number of shares should match", numberOfShares, savedPosition.getNumOfShares());
        assertEquals("Saved Position value paid should match", numberOfShares * price, savedPosition.getValuePaid(), 0.001);
    }

    /**
     * Integration Test: Test buy method with a valid ticker and existing position.
     * Expects the Position to be updated correctly in the database.
     */
    @Test
    public void testBuy_UpdatePosition() {
        String ticker = "MSFT";
        int initialShares = 20;
        double initialPrice = 1500.0;

        int additionalShares = 5;
        double additionalPrice = 1550.0;

        // check to see if quote exists in quote table
        Optional<Quote> quoteOpt = quoteDao.findById(ticker);
        if (!quoteOpt.isPresent()) {
            // not in quote table, get from API and save to db
            Quote fetchedQuote = httpHelper.fetchQuoteInfo(ticker);
            assertNotNull("Fetched Quote should not be null", fetchedQuote);
            quoteDao.save(fetchedQuote);
        }

        // create initial Position
        Position initialPosition = new Position();
        initialPosition.setTicker(ticker);
        initialPosition.setNumOfShares(initialShares);
        initialPosition.setValuePaid(initialShares * initialPrice);
        positionDao.save(initialPosition);

        Position result = positionService.buy(ticker, additionalShares, additionalPrice);

        // check result
        assertNotNull("Result should not be null", result);
        assertEquals("Ticker should match", ticker, result.getTicker());
        assertEquals("Number of shares should be updated", initialShares + additionalShares, result.getNumOfShares());
        assertEquals("Value paid should be updated", (initialShares * initialPrice) + (additionalShares * additionalPrice), result.getValuePaid(), 0.001);

        // verify position updated in db
        Optional<Position> retrievedPosition = positionDao.findById(ticker);
        assertTrue("Position should be present in the database", retrievedPosition.isPresent());
        Position savedPosition = retrievedPosition.get();
        assertEquals("Saved Position ticker should match", ticker, savedPosition.getTicker());
        assertEquals("Saved Position number of shares should match", initialShares + additionalShares, savedPosition.getNumOfShares());
        assertEquals("Saved Position value paid should match", (initialShares * initialPrice) + (additionalShares * additionalPrice), savedPosition.getValuePaid(), 0.001);
    }

    /**
     * Integration Test: Test sell method with a valid ticker and existing position.
     * Expects the Position to be deleted from the database.
     */
    @Test
    public void testSell() {
        String ticker = "MSFT";

        // check to see if quote exists in quote table
        Optional<Quote> quoteOpt = quoteDao.findById(ticker);
        if (!quoteOpt.isPresent()) {
            // not in quote table, get from API and save to db
            Quote fetchedQuote = httpHelper.fetchQuoteInfo(ticker);
            assertNotNull("Fetched Quote should not be null", fetchedQuote);
            quoteDao.save(fetchedQuote);
        }

        // create initial Position
        Position initialPosition = new Position();
        initialPosition.setTicker(ticker);
        initialPosition.setNumOfShares(50);
        initialPosition.setValuePaid(2500.0);
        positionDao.save(initialPosition);

        // make sure position exists before selling
        Optional<Position> beforeSell = positionDao.findById(ticker);
        assertTrue("Position should exist before selling", beforeSell.isPresent());
        positionService.sell(ticker);

        // verify position deleted from db
        Optional<Position> retrievedPosition = positionDao.findById(ticker);
        assertFalse("Position should be deleted from the database", retrievedPosition.isPresent());
    }
}