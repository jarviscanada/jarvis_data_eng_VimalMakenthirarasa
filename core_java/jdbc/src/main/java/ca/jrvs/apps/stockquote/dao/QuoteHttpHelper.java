package ca.jrvs.apps.stockquote.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;

public class QuoteHttpHelper {

    private String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
    private OkHttpClient client;

    public void InitializeClient() {
        if(apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API Key not set");
        }
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
        Quote quote = new Quote();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://alpha-vantage.p.rapidapi.com/query?function=GLOBAL_QUOTE&symbol="+symbol+"&datatype=json"))
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", "alpha-vantage.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // parse response body
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode globalQuote = root.get("Global Quote");

            // check if response has expected data
            if (globalQuote == null || globalQuote.isEmpty()){
                throw new IllegalArgumentException("No data found for given symbol: " + symbol);
            }

            quote = mapper.convertValue(globalQuote, Quote.class);
            quote.setTimestamp(Timestamp.from(Instant.now()));
            return quote;


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quote;
    }

}
