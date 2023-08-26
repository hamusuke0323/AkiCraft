package com.hamusuke.akicraft.util;

import com.luciad.imageio.webp.WebPReadParam;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ImageDataDeliverer {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private final URL url;
    private final MutableObject<InputStream> inputStream = new MutableObject<>();
    private final MutablePair<Integer, Integer> widthHeight = MutablePair.of(0, 0);
    private final MutableBoolean failed = new MutableBoolean();
    private boolean started;
    private final Type type;

    public ImageDataDeliverer(@Nullable URL url) {
        this(url, Type.NORMAL);
    }

    public ImageDataDeliverer(@Nullable URL url, Type type) {
        this.url = url;
        this.type = type;
        if (this.url == null) {
            this.failed.setTrue();
            this.started = true;
        }
    }

    public static void shutdown() {
        THREAD_POOL.shutdown();
    }

    public ImageDataDeliverer prepareAsync(Consumer<Exception> exceptionHandler, Consumer<ImageDataDeliverer> whenComplete) {
        this.startPreparingAsync(exceptionHandler, whenComplete);
        return this;
    }

    @Nullable
    public InputStream deliver() {
        return this.inputStream.getValue();
    }

    public int getWidth() {
        return this.widthHeight.getLeft();
    }

    public int getHeight() {
        return this.widthHeight.getRight();
    }

    public Dimension getDim() {
        return new Dimension(this.getWidth(), this.getHeight());
    }

    public boolean readyToRender() {
        return this.deliver() != null && !this.failed() && this.getWidth() != 0 && this.getHeight() != 0;
    }

    public boolean failed() {
        return this.failed.booleanValue();
    }

    private void startPreparingAsync(Consumer<Exception> exceptionHandler, Consumer<ImageDataDeliverer> whenCompleteCorrectly) {
        if (!this.started) {
            this.started = true;
            CompletableFuture.supplyAsync(() -> {
                try (var imageInputStream = ImageIO.createImageInputStream(this.url.openStream())) {
                    var it = ImageIO.getImageReaders(imageInputStream);
                    if (it.hasNext()) {
                        var imageReader = it.next();
                        imageReader.setInput(imageInputStream);
                        var bufferedImage = this.type.imageFactory.apply(imageReader);
                        var byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, this.type == Type.NORMAL ? imageReader.getFormatName() : "PNG", byteArrayOutputStream);
                        byteArrayOutputStream.flush();
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
                        this.widthHeight.setLeft(bufferedImage.getWidth());
                        this.widthHeight.setRight(bufferedImage.getHeight());
                        return new ByteArrayInputStream(bytes);
                    } else {
                        throw new IOException("Image not found.");
                    }
                } catch (Exception e) {
                    exceptionHandler.accept(e);
                    this.failed.setTrue();
                }
                return null;
            }, THREAD_POOL).whenComplete((inputStream, throwable) -> {
                this.inputStream.setValue(inputStream);
                this.failed.setValue(inputStream == null);
                if (this.readyToRender()) {
                    whenCompleteCorrectly.accept(this);
                }
            });
        }
    }

    public URL getURL() {
        return this.url;
    }

    public enum Type {
        NORMAL(imageReader -> imageReader.read(0)),
        WEBP(imageReader -> {
            var param = new WebPReadParam();
            param.setBypassFiltering(true);
            return imageReader.read(0, param);
        });

        private final ThrowableFunction<ImageReader, BufferedImage> imageFactory;

        Type(ThrowableFunction<ImageReader, BufferedImage> imageFactory) {
            this.imageFactory = imageFactory;
        }
    }

    private interface ThrowableFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
