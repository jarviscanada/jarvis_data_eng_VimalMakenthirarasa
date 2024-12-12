package ca.jrvs.apps.stockquote.dao;

import org.junit.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class PositionDao_Test {

    private static Connection connection;
    private static QuoteDao quoteDao;
    private static PositionDao positionDao;

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

    private static final String CREATE_POSITION_TABLE_SQL = "DROP TABLE IF EXISTS position;\n" +
            "CREATE TABLE position (\n" +
            "    symbol                VARCHAR(10) PRIMARY KEY,\n" +
            "    number_of_shares      INT NOT NULL,\n" +
            "    value_paid            DECIMAL(10, 2) NOT NULL,\n" +
            "    CONSTRAINT symbol_fk FOREIGN KEY (symbol) REFERENCES quote(symbol)\n" +
            ");";

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // connect to db
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(false); // Enable transaction management

        // create quote and position tables
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS position");
            statement.execute(CREATE_QUOTE_TABLE_SQL);
            statement.execute(CREATE_POSITION_TABLE_SQL);
        }

        // init quoteDao, positionDao
        quoteDao = new QuoteDao(connection);
        positionDao = new PositionDao(connection);
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        // if connection is open, drop tables and close connection
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS position");
                stmt.execute("DROP TABLE IF EXISTS quote");
            }
            connection.close();
        }
    }

    @Before
    public void setUp() throws SQLException {
        // clean tables before each test
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM position");
            statement.execute("DELETE FROM quote");
        }
    }

    @After
    public void tearDown() throws SQLException {
        // rollback any changes made during test
        connection.rollback();
    }

    private void insertQuote(Quote quote) {
        quoteDao.save(quote);
    }

    @Test
    public void testSave_Insert() {
        // insert quote
        Quote quote = new Quote();
        quote.setTicker("TEST_POS1");
        quote.setOpen(100.00);
        quote.setHigh(110.00);
        quote.setLow(90.00);
        quote.setPrice(105.00);
        quote.setVolume(500000);
        quote.setLatestTradingDay(Date.valueOf("2024-02-01"));
        quote.setPreviousClose(95.00);
        quote.setChange(10.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote);

        // create new Position
        Position position = new Position();
        position.setTicker("TEST_POS1");
        position.setNumOfShares(50);
        position.setValuePaid(7500.00);

        // save Position
        Position savedPosition = positionDao.save(position);

        // verify Position is saved
        Optional<Position> retrievedPosition = positionDao.findById("TEST_POS1");
        assertTrue("Position should be present", retrievedPosition.isPresent());

        Position p = retrievedPosition.get();
        assertEquals("TEST_POS1", p.getTicker());
        assertEquals(50, p.getNumOfShares());
        assertEquals(7500.00, p.getValuePaid(), 0.001);
    }

    @Test
    public void testSave_Update() {
        // insert Quote
        Quote quote = new Quote();
        quote.setTicker("TEST_POS2");
        quote.setOpen(200.00);
        quote.setHigh(220.00);
        quote.setLow(180.00);
        quote.setPrice(210.00);
        quote.setVolume(800000);
        quote.setLatestTradingDay(Date.valueOf("2024-02-02"));
        quote.setPreviousClose(190.00);
        quote.setChange(20.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote);

        // insert Position
        Position position = new Position();
        position.setTicker("TEST_POS2");
        position.setNumOfShares(50);
        position.setValuePaid(7500.00);
        positionDao.save(position);

        // update Position
        position.setNumOfShares(100);
        position.setValuePaid(15000.00);
        positionDao.save(position);

        // verify Position is updated
        Optional<Position> retrievedPosition = positionDao.findById("TEST_POS2");
        assertTrue("Position should be present after update", retrievedPosition.isPresent());

        Position p = retrievedPosition.get();
        assertEquals(100, p.getNumOfShares());
        assertEquals(15000.00, p.getValuePaid(), 0.001);
    }


    @Test
    public void testFindAll() {
        // insert multiple Quotes
        Quote quote1 = new Quote();
        quote1.setTicker("TEST_POS3");
        quote1.setOpen(300.00);
        quote1.setHigh(330.00);
        quote1.setLow(270.00);
        quote1.setPrice(310.00);
        quote1.setVolume(600000);
        quote1.setLatestTradingDay(Date.valueOf("2024-02-03"));
        quote1.setPreviousClose(280.00);
        quote1.setChange(30.00);
        quote1.setChangePercent("10.71%");
        quote1.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote1);

        Quote quote2 = new Quote();
        quote2.setTicker("TEST_POS4");
        quote2.setOpen(400.00);
        quote2.setHigh(440.00);
        quote2.setLow(360.00);
        quote2.setPrice(420.00);
        quote2.setVolume(700000);
        quote2.setLatestTradingDay(Date.valueOf("2024-02-04"));
        quote2.setPreviousClose(380.00);
        quote2.setChange(40.00);
        quote2.setChangePercent("10.53%");
        quote2.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote2);

        // insert Positions
        Position position1 = new Position();
        position1.setTicker("TEST_POS3");
        position1.setNumOfShares(50);
        position1.setValuePaid(7500.00);
        positionDao.save(position1);

        Position position2 = new Position();
        position2.setTicker("TEST_POS4");
        position2.setNumOfShares(30);
        position2.setValuePaid(8400.00);
        positionDao.save(position2);

        // retrieve all Positions
        Iterable<Position> allPositions = positionDao.findAll();
        List<Position> positionList = new ArrayList<>();
        allPositions.forEach(positionList::add);

        assertEquals("There should be 2 Positions", 2, positionList.size());
    }

    @Test
    public void testDeleteById() {
        // insert Quote and Position
        Quote quote = new Quote();
        quote.setTicker("TEST_POS5");
        quote.setOpen(500.00);
        quote.setHigh(550.00);
        quote.setLow(450.00);
        quote.setPrice(525.00);
        quote.setVolume(900000);
        quote.setLatestTradingDay(Date.valueOf("2024-02-05"));
        quote.setPreviousClose(475.00);
        quote.setChange(50.00);
        quote.setChangePercent("10.53%");
        quote.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote);

        Position position = new Position();
        position.setTicker("TEST_POS5");
        position.setNumOfShares(100);
        position.setValuePaid(15000.00);
        positionDao.save(position);

        // delete Position
        positionDao.deleteById("TEST_POS5");

        // verify deletion
        Optional<Position> retrievedPosition = positionDao.findById("TEST_POS5");
        assertFalse("Position should be deleted", retrievedPosition.isPresent());
    }

    @Test
    public void testDeleteAll() {
        // insert multiple Quotes
        Quote quote1 = new Quote();
        quote1.setTicker("TEST_POS6");
        quote1.setOpen(600.00);
        quote1.setHigh(660.00);
        quote1.setLow(540.00);
        quote1.setPrice(630.00);
        quote1.setVolume(1000000);
        quote1.setLatestTradingDay(Date.valueOf("2024-02-06"));
        quote1.setPreviousClose(570.00);
        quote1.setChange(60.00);
        quote1.setChangePercent("10.53%");
        quote1.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote1);

        Quote quote2 = new Quote();
        quote2.setTicker("TEST_POS7");
        quote2.setOpen(700.00);
        quote2.setHigh(770.00);
        quote2.setLow(630.00);
        quote2.setPrice(735.00);
        quote2.setVolume(1100000);
        quote2.setLatestTradingDay(Date.valueOf("2024-02-07"));
        quote2.setPreviousClose(675.00);
        quote2.setChange(60.00);
        quote2.setChangePercent("8.89%");
        quote2.setTimestamp(new Timestamp(System.currentTimeMillis()));
        insertQuote(quote2);

        // insert multiple Positions
        Position position1 = new Position();
        position1.setTicker("TEST_POS6");
        position1.setNumOfShares(50);
        position1.setValuePaid(7500.00);
        positionDao.save(position1);

        Position position2 = new Position();
        position2.setTicker("TEST_POS7");
        position2.setNumOfShares(30);
        position2.setValuePaid(8400.00);
        positionDao.save(position2);

        // delete all Positions
        positionDao.deleteAll();

        // verify deletion
        Iterable<Position> allPositions = positionDao.findAll();
        List<Position> positionList = new ArrayList<>();
        allPositions.forEach(positionList::add);
        assertEquals("All Positions should be deleted", 0, positionList.size());
    }
}
