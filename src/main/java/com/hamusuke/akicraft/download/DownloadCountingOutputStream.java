package com.hamusuke.akicraft.download;

import org.apache.commons.io.output.CountingOutputStream;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class DownloadCountingOutputStream extends CountingOutputStream {
    @Nullable
    private final Consumer<DownloadCountingOutputStream> listener;

    public DownloadCountingOutputStream(OutputStream out, @Nullable Consumer<DownloadCountingOutputStream> listener) {
        super(out);
        this.listener = listener;
    }

    @Override
    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);
        if (this.listener != null) {
            this.listener.accept(this);
        }
    }
}
