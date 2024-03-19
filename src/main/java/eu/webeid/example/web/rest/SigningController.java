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

package eu.webeid.example.web.rest;

import eu.webeid.example.security.WebEidAuthentication;
import eu.webeid.example.service.SigningService;
import eu.webeid.example.service.dto.CertificateDTO;
import eu.webeid.example.service.dto.DigestDTO;
import eu.webeid.example.service.dto.FileDTO;
import eu.webeid.example.service.dto.SignatureDTO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@RestController
@RequestMapping("sign")
public class SigningController {

    private final SigningService signingService;

    public SigningController(SigningService signingService) {
        this.signingService = signingService;
    }

    @PostMapping("prepare")
    public DigestDTO prepare(@RequestBody CertificateDTO data, WebEidAuthentication authentication) throws CertificateException, NoSuchAlgorithmException, IOException {
        return signingService.prepareContainer(data, authentication);
    }

    @PostMapping("sign")
    public FileDTO sign(@RequestBody SignatureDTO data) {
        return signingService.signContainer(data);
    }

    @GetMapping(value = "download", produces = "application/vnd.etsi.asic-e+zip")
    public ResponseEntity<Resource> download() throws IOException {

        ByteArrayResource resource = signingService.getSignedContainerAsResource();
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + signingService.getContainerName())
                .body(resource);
    }

    /*  Here is an example endpoint that demonstrates how to handle file uploads.
        See also resources/templates/welcome-with-file-upload-support.html.

    @PostMapping(value = "upload", produces = "application/json")
    public FileDTO upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        return signingService.createContainer(FileDTO.fromMultipartFile(multipartFile));
    }
    */
}
