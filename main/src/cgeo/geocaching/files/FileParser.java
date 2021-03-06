package cgeo.geocaching.files;

import cgeo.geocaching.Geocache;
import cgeo.geocaching.utils.CancellableHandler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.CancellationException;

public abstract class FileParser {
    /**
     * Parses caches from input stream.
     *
     * @param stream
     *         the input stream
     * @param progressHandler
     *         for reporting parsing progress (in bytes read from input stream)
     * @return collection of caches
     * @throws IOException
     *         if the input stream can't be read
     * @throws ParserException
     *         if the input stream contains data not matching the file format of the parser
     */
    @NonNull
    public abstract Collection<Geocache> parse(@NonNull final InputStream stream, @Nullable final CancellableHandler progressHandler) throws IOException, ParserException;

    /**
     * Convenience method for parsing a file.
     */
    @NonNull
    public Collection<Geocache> parse(final File file, final CancellableHandler progressHandler) throws IOException, ParserException {
        final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        try {
            return parse(stream, progressHandler);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @NonNull
    protected static StringBuilder readStream(@NonNull final InputStream is, @Nullable final CancellableHandler progressHandler) throws IOException {
        final StringBuilder buffer = new StringBuilder();
        final ProgressInputStream progressInputStream = new ProgressInputStream(is);
        final BufferedReader input = new BufferedReader(new InputStreamReader(progressInputStream, CharEncoding.UTF_8));

        try {
            String line;
            while ((line = input.readLine()) != null) {
                buffer.append(line);
                showProgressMessage(progressHandler, progressInputStream.getProgress());
            }
            return buffer;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    protected static void showProgressMessage(@Nullable final CancellableHandler handler, final int bytesRead) {
        if (handler != null) {
            if (handler.isCancelled()) {
                throw new CancellationException();
            }
            handler.sendMessage(handler.obtainMessage(0, bytesRead, 0));
        }
    }

    protected static void fixCache(final Geocache cache) {
        if (cache.getInventory() != null) {
            cache.setInventoryItems(cache.getInventory().size());
        } else {
            cache.setInventoryItems(0);
        }
        final long time = System.currentTimeMillis();
        cache.setUpdated(time);
        cache.setDetailedUpdate(time);
    }
}
