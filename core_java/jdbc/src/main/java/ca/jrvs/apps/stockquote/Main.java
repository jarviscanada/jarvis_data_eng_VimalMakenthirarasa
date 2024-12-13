package ca.jrvs.apps.stockquote;

import ca.jrvs.apps.stockquote.controller.StockQuoteController;
import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.dao.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.service.PositionService;
import ca.jrvs.apps.stockquote.service.QuoteService;
import okhttp3.OkHttpClient;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// -it flag keeps stdin open 
/**
 * docker run --rm -it \
 * -v "$(pwd)/log:/log" \
 * ${docker_user}/jdbc
 */

public class Main {

    public static void main(String[] args) {
        Map<String, String> properties = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("properties.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                properties.put(tokens[0], tokens[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class.forName(properties.get("db-class"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        String url = "jdbc:postgresql://"+properties.get("server")+":"+properties.get("port")+"/"+properties.get("database");
        try (Connection c = DriverManager.getConnection(url, properties.get("username"), properties.get("password"))) {
            QuoteDao qRepo = new QuoteDao(c);
            PositionDao pRepo = new PositionDao(c);
            QuoteHttpHelper rcon = new QuoteHttpHelper(properties.get("api-key"), client);
            QuoteService sQuote = new QuoteService(qRepo, rcon);
            PositionService sPos = new PositionService(pRepo, sQuote);
            StockQuoteController con = new StockQuoteController(sQuote, sPos);
            con.initClient();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}