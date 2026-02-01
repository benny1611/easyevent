package com.benny1611.easyevent.util;

import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] content;

    public ByteArrayMultipartFile(byte[] content) {
        this.content = content;
    }


    @Override
    public String getName() {
        return "";
    }

    @Override
    public @Nullable String getOriginalFilename() {
        return null;
    }

    @Override
    public @Nullable String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}
