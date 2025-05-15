package com.myapp.fitnessapp.data;

import android.util.Log;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class QuoteService {
    // API endpoint for Forismatic quote service (HTTP)
    private static final String URL =
            "http://api.forismatic.com/api/1.0/"
                    + "?method=getQuote&format=json&lang=en";

    // HTTP client for network requests
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Callback interface to return quote data or errors
     */
    public interface QuoteCallback {
        void onSuccess(String content, String author);
        void onError(Exception e);
    }

    /**
     * Fetches a random quote from the Forismatic API asynchronously.
     * On success, parses JSON and returns the quote text and author.
     * On failure or a bad response, logs the error and invokes onError.
     */
    public void fetchQuote(QuoteCallback callback) {
        // Build HTTP GET request
        Request request = new Request.Builder()
                .url(URL)
                .build();

        // Enqueue asynchronous call
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                // Network-level failure
                Log.e("QuoteService", "Network failure", e);
                callback.onError(e);
            }
            @Override public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        // Handle non-2xx HTTP codes
                        String body = response.body() != null
                                ? response.body().string()
                                : "‹empty›";
                        IOException ex = new IOException(
                                "HTTP " + response.code() + ": " + body);
                        Log.e("QuoteService", "Bad response", ex);
                        callback.onError(ex);
                        return;
                    }
                    // Parse JSON body
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    String content = obj.optString("quoteText", "No quote");
                    String author  = obj.optString("quoteAuthor", "Unknown");
                    // Return parsed data
                    callback.onSuccess(content, author);
                } catch (Exception e) {
                    // Parsing or I/O error
                    Log.e("QuoteService", "Parsing error", e);
                    callback.onError(e);
                }
            }
        });
    }
}
