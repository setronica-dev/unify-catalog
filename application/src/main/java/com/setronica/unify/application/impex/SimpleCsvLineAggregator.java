package com.setronica.unify.application.impex;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.springframework.batch.item.file.transform.ExtractorLineAggregator;

/**
 * Despite appearances, {@link org.springframework.batch.item.file.transform.DelimitedLineAggregator} cannot properly
 * format CSV when quoting is involved; this class replaces that implementation with a CSV library.
 *
 * @param <T> subject entity that the core aggregator handles
 */
public class SimpleCsvLineAggregator<T> extends ExtractorLineAggregator<T> {

    protected final CSVFormat format;

    public SimpleCsvLineAggregator(String delimiter, char quoteCharacter) {
        format = CSVFormat.Builder.create()
                .setDelimiter(delimiter)
                .setQuote(quoteCharacter)
                .setQuoteMode(QuoteMode.MINIMAL)
                .get();
    }

    @Override
    protected String doAggregate(Object[] fields) {
        return format.format(fields);
    }
}
