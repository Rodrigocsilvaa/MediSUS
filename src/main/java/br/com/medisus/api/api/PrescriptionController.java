package br.com.medisus.api.api;

import br.com.medisus.api.ai.PrescriptionService;
import br.com.medisus.api.api.ApiDtos.PrescriptionCandidates;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private static final Set<String> ACCEPTED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final PrescriptionService prescriptions;
    public PrescriptionController(PrescriptionService prescriptions) { this.prescriptions = prescriptions; }

    @PostMapping(value = "/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public PrescriptionCandidates identify(@RequestPart("image") MultipartFile image) throws IOException {
        if (image.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Image must not be empty");
        String contentType = image.getContentType() == null ? "" : image.getContentType().toLowerCase(Locale.ROOT);
        if (!ACCEPTED_TYPES.contains(contentType)) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Supported image types are JPEG, PNG and WebP");
        return prescriptions.identify(image.getBytes(), contentType);
    }
}
