package br.com.medisus.api.ai;

import br.com.medisus.api.api.ApiDtos.PrescriptionCandidates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class PrescriptionService {
    private final ObjectProvider<PrescriptionModel> models;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public PrescriptionService(ObjectProvider<PrescriptionModel> models, ObjectMapper objectMapper,
                               @Value("${medisus.ai.enabled:false}") boolean enabled) {
        this.models = models; this.objectMapper = objectMapper; this.enabled = enabled;
    }

    public PrescriptionCandidates identify(byte[] image, String contentType) {
        if (!enabled) throw new AiUnavailableException();
        PrescriptionModel model = models.getIfAvailable();
        if (model == null) throw new AiUnavailableException();
        try {
            String answer = model.identify(image, contentType);
            return new PrescriptionCandidates(parseCandidates(answer),
                    "Review the suggested names before searching inventory. Availability comes only from saved stock.");
        } catch (AiUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiUnavailableException(ex);
        }
    }

    private List<String> parseCandidates(String response) throws Exception {
        if (response == null || response.isBlank()) return List.of();
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start < 0 || end < start) throw new IllegalArgumentException("AI response did not contain a JSON array");
        JsonNode node = objectMapper.readTree(response.substring(start, end + 1));
        if (!node.isArray()) throw new IllegalArgumentException("AI response was not a candidate array");
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        for (JsonNode candidate : node) {
            if (candidate.isTextual()) {
                String name = candidate.asText().trim();
                if (!name.isEmpty() && name.length() <= 160) candidates.add(name);
            }
        }
        return new ArrayList<>(candidates);
    }
}
