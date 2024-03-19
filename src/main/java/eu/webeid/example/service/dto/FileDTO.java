/*
 * Copyright (c) 2020-2024 Estonian Information System Authority
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.webeid.example.service.dto;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FileDTO {
    private static final String EXAMPLE_FILENAME = "example-for-signing.txt";

    private final String name;
    private String contentType;
    private byte[] contentBytes;

    public FileDTO(String name) {
        this.name = name;
    }

    public FileDTO(String name, String contentType, byte[] contentBytes) {
        this.name = name;
        this.contentType = contentType;
        this.contentBytes = contentBytes;
    }

    public static FileDTO fromMultipartFile(MultipartFile file) throws IOException {
        return new FileDTO(
                Objects.requireNonNull(file.getOriginalFilename()),
                Objects.requireNonNull(file.getContentType()),
                Objects.requireNonNull(file.getBytes())
        );
    }

    public static FileDTO getExampleForSigningFromResources() throws IOException {
        final URI resourceUri = new ClassPathResource("/static/files/" + EXAMPLE_FILENAME).getURI();
        return new FileDTO(
                EXAMPLE_FILENAME,
                MimeTypeUtils.TEXT_PLAIN_VALUE,
                Files.readAllBytes(Paths.get(resourceUri))
        );
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContentBytes() {
        return contentBytes;
    }
}
