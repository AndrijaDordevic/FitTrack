package com.myapp.fitnessapp.data;

import android.util.Log;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class QuoteService {
    // Forismatic over HTTP
    private static final String URL =
            "http://api.forismatic.com/api/1.0/"
                    + "?method=getQuote&format=json&lang=en";

    private final OkHttpClient client = new OkHttpClient();

    public interface QuoteCallback {
        void onSuccess(String content, String author);
        void onError(Exception e);
    }

    public void fetchQuote(QuoteCallback callback) {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("QuoteService", "Network failure", e);
                callback.onError(e);
            }
            @Override public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null
                                ? response.body().string()
                                : "‹empty›";
                        IOException ex = new IOException(
                                "HTTP " + response.code() + ": " + body);
                        Log.e("QuoteService", "Bad response", ex);
                        callback.onError(ex);
                        return;
                    }
                    String json = response.body().string();
                    // Forismatic returns a single object
                    JSONObject obj = new JSONObject(json);
                    String content = obj.optString("quoteText", "No quote");
                    String author  = obj.optString("quoteAuthor", "Unknown");
                    callback.onSuccess(content, author);
                } catch (Exception e) {
                    Log.e("QuoteService", "Parsing error", e);
                    callback.onError(e);
                }
            }
        });
    }
}
