package com.myapp.fitnessapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;

public class QuoteRepository {
    // SharedPreferences file name and keys for caching daily quote
    private static final String PREFS_NAME   = "daily_quote_prefs";
    private static final String KEY_DATE     = "quote_date";
    private static final String KEY_QUOTE    = "quote_text";
    private static final String KEY_AUTHOR   = "quote_author";

    private final SharedPreferences prefs;
    // Service responsible for fetching quotes from network
    private final QuoteService       service;

    public QuoteRepository(Context ctx) {
        // Initialize SharedPreferences and network service
        prefs   = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        service = new QuoteService();
    }

    /**
     * Returns today's quote: if cached date matches today, use saved data;
     * otherwise fetch from network, cache it, and then return.
     */
    public void getDailyQuote(QuoteService.QuoteCallback callback) {
        String savedDate = prefs.getString(KEY_DATE, "");
        String today     = LocalDate.now().toString(); // e.g. "2025-05-14"

        if (today.equals(savedDate)) {
            // Use cached quote for today
            String content = prefs.getString(KEY_QUOTE, "");
            String author  = prefs.getString(KEY_AUTHOR, "");
            callback.onSuccess(content, author);
        } else {
            // Fetch a new quote since it's a new day
            service.fetchQuote(new QuoteService.QuoteCallback() {
                @Override
                public void onSuccess(String content, String author) {
                    // Cache the newly fetched quote and date
                    prefs.edit()
                            .putString(KEY_DATE,   today)
                            .putString(KEY_QUOTE,  content)
                            .putString(KEY_AUTHOR, author)
                            .apply();
                    callback.onSuccess(content, author);
                }

                @Override
                public void onError(Exception e) {
                    // Propagate error to caller
                    callback.onError(e);
                }
            });
        }
    }
}
