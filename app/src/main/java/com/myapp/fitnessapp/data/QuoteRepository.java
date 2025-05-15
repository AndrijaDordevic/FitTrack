package com.myapp.fitnessapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;

public class QuoteRepository {
    private static final String PREFS_NAME   = "daily_quote_prefs";
    private static final String KEY_DATE     = "quote_date";
    private static final String KEY_QUOTE    = "quote_text";
    private static final String KEY_AUTHOR   = "quote_author";

    private final SharedPreferences prefs;
    private final QuoteService       service;

    public QuoteRepository(Context ctx) {
        prefs   = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        service = new QuoteService();
    }

    /**
     * If we already fetched today’s quote, return it immediately.
     * Otherwise fetch from network, save it, then return it.
     */
    public void getDailyQuote(QuoteService.QuoteCallback callback) {
        String savedDate = prefs.getString(KEY_DATE, "");
        String today     = LocalDate.now().toString(); // e.g. "2025-05-14"

        if (today.equals(savedDate)) {
            // already have today’s quote
            String content = prefs.getString(KEY_QUOTE, "");
            String author  = prefs.getString(KEY_AUTHOR, "");
            callback.onSuccess(content, author);
        } else {
            // fetch fresh one
            service.fetchQuote(new QuoteService.QuoteCallback() {
                @Override
                public void onSuccess(String content, String author) {
                    // save for tomorrow
                    prefs.edit()
                            .putString(KEY_DATE,   today)
                            .putString(KEY_QUOTE,  content)
                            .putString(KEY_AUTHOR, author)
                            .apply();
                    callback.onSuccess(content, author);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        }
    }
}
