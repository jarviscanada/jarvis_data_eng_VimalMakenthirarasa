package ca.jrvs.apps.stockquote.dao;

import org.junit.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class QuoteDao_Test {

    private static Connection connection;
    private static QuoteDao quoteDao;

    // db connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/host_agent";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "password";

    // SQL Statements for table setup
    private static final String CREATE_QUOTE_TABLE_SQL = "DROP TABLE IF EXISTS quote;\n" +
            "CREATE TABLE quote (\n" +
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
        // connect to db
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(false);

        // create quote table
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS position"); // drop dependent table first
            statement.execute("DROP TABLE IF EXISTS quote"); // then drop quote table
            statement.execute(CREATE_QUOTE_TABLE_SQL); // then recreate quote table
        }

        // init QuoteDao
        quoteDao = new QuoteDao(connection);
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        // if connection is open, drop tables and close connection
        if (connection != null && !connection.isClosed()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS position");
                statement.execute("DROP TABLE IF EXISTS quote");
            }
            connection.close();
        }
    }

    @Before
    public void setUp() throws SQLException {
        // clean quote table before each test
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM quote");
        }
    }

    @After
    public void tearDown() throws SQLException {
        // rollback any changes made during test
        connection.rollback();
    }

    @Test
    public void testSave_Insert() {
        // create new Quote
        Quote quote = new Quote();
        quote.setTicker("TEST1");
        quote.setOpen(100.00);
        quote.setHigh(110.00);
        quote.setLow(90.00);
        quote.setPrice(105.00);
        quote.setVolume(500000);
        quote.setLatestTradingDay(Date.valueOf("2024-01-01"));
        quote.setPreviousClose(95.00);
        quote.setChange(10.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));

        // save Quote
        Quote savedQuote = quoteDao.save(quote);

        // verify Quote is saved
        Optional<Quote> retrievedQuote = quoteDao.findById("TEST1");
        assertTrue("Quote should be present", retrievedQuote.isPresent());

        Quote q = retrievedQuote.get();
        assertEquals("TEST1", q.getTicker());
        assertEquals(100.00, q.getOpen(), 0.001);
        assertEquals(110.00, q.getHigh(), 0.001);
        assertEquals(90.00, q.getLow(), 0.001);
        assertEquals(105.00, q.getPrice(), 0.001);
        assertEquals(500000, q.getVolume());
        assertEquals(Date.valueOf("2024-01-01"), q.getLatestTradingDay());
        assertEquals(95.00, q.getPreviousClose(), 0.001);
        assertEquals(10.00, q.getChange(), 0.001);
        assertEquals("10.53%", q.getChangePercent());
        assertNotNull("Timestamp should not be null", q.getTimestamp());
    }

    /**
     * Test the save method for updating an existing Quote.
     */
    @Test
    public void testSave_Update() {
        // try inserting quote
        Quote quote = new Quote();
        quote.setTicker("TEST2");
        quote.setOpen(200.00);
        quote.setHigh(220.00);
        quote.setLow(180.00);
        quote.setPrice(210.00);
        quote.setVolume(800000);
        quote.setLatestTradingDay(Date.valueOf("2024-01-02"));
        quote.setPreviousClose(190.00);
        quote.setChange(20.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));
        quoteDao.save(quote);

        // update quote
        quote.setPrice(215.00);
        quote.setVolume(850000);
        quote.setChange(25.00);
        quote.setChangePercent("13.16%");
        quoteDao.save(quote);

        // verify Quote is updated
        Optional<Quote> retrievedQuote = quoteDao.findById("TEST2");
        assertTrue("Quote should be present after update", retrievedQuote.isPresent());

        Quote q = retrievedQuote.get();
        assertEquals(215.00, q.getPrice(), 0.001);
        assertEquals(850000, q.getVolume());
        assertEquals(25.00, q.getChange(), 0.001);
        assertEquals("13.16%", q.getChangePercent());
    }

    @Test
    public void testFindAll() {
        // insert multiple Quotes
        Quote quote1 = new Quote();
        quote1.setTicker("TEST3");
        quote1.setOpen(300.00);
        quote1.setHigh(330.00);
        quote1.setLow(270.00);
        quote1.setPrice(310.00);
        quote1.setVolume(600000);
        quote1.setLatestTradingDay(Date.valueOf("2024-01-03"));
        quote1.setPreviousClose(280.00);
        quote1.setChange(30.00);
        quote1.setChangePercent("10.71%");
        quote1.setTimestamp(new Timestamp(System.currentTimeMillis()));

        Quote quote2 = new Quote();
        quote2.setTicker("TEST4");
        quote2.setOpen(400.00);
        quote2.setHigh(440.00);
        quote2.setLow(360.00);
        quote2.setPrice(420.00);
        quote2.setVolume(700000);
        quote2.setLatestTradingDay(Date.valueOf("2024-01-04"));
        quote2.setPreviousClose(380.00);
        quote2.setChange(40.00);
        quote2.setChangePercent("10.53%");
        quote2.setTimestamp(new Timestamp(System.currentTimeMillis()));

        quoteDao.save(quote1);
        quoteDao.save(quote2);

        // retrieve all Quotes
        Iterable<Quote> allQuotes = quoteDao.findAll();
        List<Quote> quoteList = new ArrayList<>();
        allQuotes.forEach(quoteList::add);

        assertEquals("There should be 2 Quotes", 2, quoteList.size());
    }

    @Test
    public void testDeleteById() {
        // insert a Quote
        Quote quote = new Quote();
        quote.setTicker("TEST5");
        quote.setOpen(500.00);
        quote.setHigh(550.00);
        quote.setLow(450.00);
        quote.setPrice(525.00);
        quote.setVolume(900000);
        quote.setLatestTradingDay(Date.valueOf("2024-01-05"));
        quote.setPreviousClose(475.00);
        quote.setChange(50.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));
        quoteDao.save(quote);

        // delete Quote
        quoteDao.deleteById("TEST5");

        // verify deletion
        Optional<Quote> retrievedQuote = quoteDao.findById("TEST5");
        assertFalse("Quote should be deleted", retrievedQuote.isPresent());
    }

    @Test
    public void testDeleteAll() {
        // insert multiple Quotes
        Quote quote1 = new Quote();
        quote1.setTicker("TEST6");
        quote1.setOpen(600.00);
        quote1.setHigh(660.00);
        quote1.setLow(540.00);
        quote1.setPrice(630.00);
        quote1.setVolume(1000000);
        quote1.setLatestTradingDay(Date.valueOf("2024-01-06"));
        quote1.setPreviousClose(570.00);
        quote1.setChange(60.00);
        quote1.setChangePercent("10.53%");
        quote1.setTimestamp(new Timestamp(System.currentTimeMillis()));

        Quote quote2 = new Quote();
        quote2.setTicker("TEST7");
        quote2.setOpen(700.00);
        quote2.setHigh(770.00);
        quote2.setLow(630.00);
        quote2.setPrice(735.00);
        quote2.setVolume(1100000);
        quote2.setLatestTradingDay(Date.valueOf("2024-01-07"));
        quote2.setPreviousClose(675.00);
        quote2.setChange(60.00);
        quote2.setChangePercent("8.89%");
        quote2.setTimestamp(new Timestamp(System.currentTimeMillis()));

        quoteDao.save(quote1);
        quoteDao.save(quote2);

        // delete all Quotes
        quoteDao.deleteAll();

        // verify deletion
        Iterable<Quote> allQuotes = quoteDao.findAll();
        List<Quote> quoteList = new ArrayList<>();
        allQuotes.forEach(quoteList::add);
        assertEquals("All Quotes should be deleted", 0, quoteList.size());
    }
}
