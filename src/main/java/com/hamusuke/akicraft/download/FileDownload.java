package com.hamusuke.akicraft.download;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

public class FileDownload {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
    private final String url;
    private final MutableLong totalBytes = new MutableLong();
    private final MutableLong bytesWritten = new MutableLong();
    private final MutableObject<HttpGet> httpRequest = new MutableObject<>();
    private final MutableBoolean dlStarted = new MutableBoolean();
    private final MutableBoolean cancelled = new MutableBoolean();
    private final MutableBoolean failed = new MutableBoolean();
    private final MutableBoolean finished = new MutableBoolean();
    private File saveTo;
    @Nullable
    private Thread currentThread;

    public FileDownload(String url) {
        this.url = url;
    }

    public void download(File saveTo, Consumer<Throwable> onError) {
        this.saveTo = saveTo;

        if (this.currentThread == null) {
            this.currentThread = new Thread(() -> {
                CloseableHttpResponse closeableHttpResponse = null;
                try (var closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build()) {
                    var httpGet = new HttpGet(this.url);
                    httpGet.setHeader("Accept-Encoding", "identity");
                    this.httpRequest.setValue(httpGet);
                    closeableHttpResponse = closeableHttpClient.execute(this.httpRequest.getValue());
                    this.totalBytes.setValue(Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue()));
                    this.dlStarted.setTrue();
                    if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        var countingOutputStream = new DownloadCountingOutputStream(new FileOutputStream(this.saveTo), downloadCountingOutputStream -> {
                            this.bytesWritten.setValue(downloadCountingOutputStream.getByteCount());
                        });
                        IOUtils.copy(closeableHttpResponse.getEntity().getContent(), countingOutputStream);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while downloading", e);
                    onError.accept(e);
                    this.failed.setTrue();
                    this.cancelled.setTrue();
                } finally {
                    if (this.httpRequest.getValue() != null) {
                        this.httpRequest.getValue().releaseConnection();
                    }
                    IOUtils.closeQuietly(closeableHttpResponse);
                    this.finished.setTrue();
                }
            }, "AkiCraft Download Thread");
            this.currentThread.setUncaughtExceptionHandler((t, e) -> {
                LOGGER.error("Error occurred while downloading", e);
                onError.accept(e);
            });
            this.currentThread.start();
        }
    }

    public void cancel() {
        if (this.httpRequest.getValue() != null) {
            this.httpRequest.getValue().abort();
        }

        if (this.saveTo != null) {
            this.saveTo.delete();
        }

        this.cancelled.setTrue();
    }

    public long bytesWritten() {
        return this.bytesWritten.longValue();
    }

    public long totalBytes() {
        return this.totalBytes.longValue();
    }

    public boolean isStarted() {
        return this.dlStarted.booleanValue();
    }

    public boolean cancelled() {
        return this.cancelled.booleanValue();
    }

    public boolean failed() {
        return this.failed.booleanValue();
    }

    public boolean finished() {
        return this.finished.booleanValue();
    }
}
