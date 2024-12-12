package ca.jrvs.apps.stockquote.dao;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QuoteHttpHelperTest {

    private QuoteHttpHelper quoteHttpHelper;

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // initialize QuoteHttpHelper
        quoteHttpHelper = new QuoteHttpHelper();

        // set API key
        java.lang.reflect.Field apiKeyField = QuoteHttpHelper.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(quoteHttpHelper, "00d0a67d07msh301533398d4f49ap191c5djsn6eacf2ab47d8");

        // using reflection to inject the mocked OkHttpClient
        java.lang.reflect.Field clientField = QuoteHttpHelper.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(quoteHttpHelper, mockOkHttpClient);
    }

    @Test
    public void testFetchQuoteInfo_ValidSymbol() throws Exception {
        // Prepare mock response JSON
        String mockResponseJson = "{" +
                "\"Global Quote\": {" +
                "\"01. symbol\": \"MSFT\"," +
                "\"02. open\": \"444.0500\"," +
                "\"03. high\": \"450.3500\"," +
                "\"04. low\": \"444.0500\"," +
                "\"05. price\": \"448.9900\"," +
                "\"06. volume\": \"17278689\"," +
                "\"07. latest trading day\": \"2024-12-11\"," +
                "\"08. previous close\": \"443.3300\"," +
                "\"09. change\": \"5.6600\"," +
                "\"10. change percent\": \"1.2767%\"" +
                "}" +
                "}";

        ResponseBody mockResponseBody = ResponseBody.create(
                MediaType.parse("application/json"),
                mockResponseJson
        );

        // setup mock chain
        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponse.isSuccessful()).thenReturn(true);

        Quote quote = quoteHttpHelper.fetchQuoteInfo("MSFT");

        // check if quote details follow correct format
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

        // check that the HTTP call was made
        verify(mockOkHttpClient).newCall(any(Request.class));
        verify(mockCall).execute();
    }
}