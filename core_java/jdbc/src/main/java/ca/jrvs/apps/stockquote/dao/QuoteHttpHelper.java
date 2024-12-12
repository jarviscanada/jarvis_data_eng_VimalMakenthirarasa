package ca.jrvs.apps.stockquote.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

public class QuoteHttpHelper {

    private String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
    private OkHttpClient client;

    public QuoteHttpHelper() {
        this.client = new OkHttpClient();
    }

    /**
     * Fetch latest quote data from Alpha Vantage endpoint
     * @param symbol
     * @return Quote with latest data
     * @throws IllegalArgumentException - if no data was found for the given symbol
     */
    public Quote fetchQuoteInfo(String symbol) throws IllegalArgumentException {
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol not set");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API Key not set");
        }

        Quote quote = new Quote();

        // Build the request using OkHttp
        Request request = new Request.Builder()
                .url("https://alpha-vantage.p.rapidapi.com/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&datatype=json")
                .addHeader("X-RapidAPI-Key", apiKey)
                .addHeader("X-RapidAPI-Host", "alpha-vantage.p.rapidapi.com")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            // get response body
            String responseBody = response.body().string();

            // parse response body
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode globalQuote = root.get("Global Quote");

            // check if response has expected data
            if (globalQuote == null || globalQuote.isEmpty()) {
                throw new IllegalArgumentException("No data found for given symbol: " + symbol);
            }

            quote = mapper.convertValue(globalQuote, Quote.class);
            quote.setTimestamp(Timestamp.from(Instant.now()));

            return quote;

        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch quote data: " + e.getMessage(), e);
        }
    }
}