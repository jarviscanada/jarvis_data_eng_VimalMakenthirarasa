package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.Quote;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import okhttp3.OkHttpClient;
import org.junit.*;
import java.sql.*;
import java.util.Optional;

import static org.junit.Assert.*;

public class QuoteService_IntTest {

    private static Connection connection;
    private static QuoteDao quoteDao;
    private static QuoteHttpHelper httpHelper;
    private static QuoteService quoteService;

    // db connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/host_agent_test";
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

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // connect to host_agent_)test database
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(false); // Begin transaction

        // create quote table
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_QUOTE_TABLE_SQL);
        }

        // init QuoteDao
        quoteDao = new QuoteDao(connection);

        // init QuoteHttpHelper
        String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
        OkHttpClient client = new OkHttpClient();
        httpHelper = new QuoteHttpHelper(apiKey, client);

        // init QuoteService
        quoteService = new QuoteService(quoteDao, httpHelper);
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // drop tables after tests, position is dependent on quote so drop it first
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS position");
                statement.execute("DROP TABLE IF EXISTS quote;");
            }
            connection.close();
        }
    }

    @Before
    public void setUp() throws SQLException {
        // clean quote table before each test
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM quote;");
        }
    }

    @After
    public void tearDown() throws SQLException {
        // rollback any changes made during test
        connection.rollback();
    }

    @Test
    public void testFetchQuoteDataFromAPI() {
        String ticker = "MSFT";

        // get quote data
        Optional<Quote> result = quoteService.fetchQuoteDataFromAPI(ticker);

        // check result
        assertTrue("Result should be present", result.isPresent());
        Quote quote = result.get();
        assertEquals("Ticker should match", "MSFT", quote.getTicker());

        // check to see if result has correct format
        assertNotNull(quote);
        assertEquals("MSFT", quote.getTicker());
        assertTrue(String.format("%.4f", quote.getOpen()).matches("\\d+\\.\\d{4}"));
        assertTrue(String.format("%.4f", quote.getHigh()).matches("\\d+\\.\\d{4}"));
        assertTrue(String.format("%.4f", quote.getLow()).matches("\\d+\\.\\d{4}"));
        assertTrue(String.format("%.4f", quote.getPrice()).matches("\\d+\\.\\d{4}"));
        assertTrue(String.valueOf(quote.getVolume()).matches("\\d+"));
        assertNotNull(quote.getLatestTradingDay());
        assertTrue(String.format("%.4f", quote.getPreviousClose()).matches("\\d+\\.\\d{4}"));
        assertTrue(String.format("%.4f", quote.getChange()).matches("-?\\d+\\.\\d{4}"));
        assertTrue(quote.getChangePercent().matches("-?\\d+\\.\\d+%"));
        assertNotNull(quote.getTimestamp());

        // verify quote saved in db
        Optional<Quote> retrievedQuote = quoteDao.findById(ticker);
        assertTrue("Quote should be present in the database", retrievedQuote.isPresent());
        Quote savedQuote = retrievedQuote.get();
        assertEquals("Saved quote ticker should match", quote.getTicker(), savedQuote.getTicker());
        assertEquals("Saved quote price should match", quote.getPrice(), savedQuote.getPrice(), 0.001);
        assertEquals("Saved quote volume should match", quote.getVolume(), savedQuote.getVolume());
        assertEquals("Saved quote change should match", quote.getChange(), savedQuote.getChange(), 0.001);
        assertEquals("Saved quote changePercent should match", quote.getChangePercent(), savedQuote.getChangePercent());
    }
}
