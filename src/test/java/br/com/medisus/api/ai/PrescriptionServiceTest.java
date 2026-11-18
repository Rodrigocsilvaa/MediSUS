package br.com.medisus.api.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrescriptionServiceTest {
    @Test
    void returnsOnlyDistinctTextCandidatesFromTheMockedModel() {
        @SuppressWarnings("unchecked")
        ObjectProvider<PrescriptionModel> models = mock(ObjectProvider.class);
        PrescriptionModel model = mock(PrescriptionModel.class);
        when(models.getIfAvailable()).thenReturn(model);
        when(model.identify(any(), eq("image/png")))
                .thenReturn("[\"Paracetamol 500 mg\", 2, \"\", \"Paracetamol 500 mg\"]");
        byte[] image = {1, 2, 3};
        PrescriptionService service = new PrescriptionService(models, new ObjectMapper(), true);

        var result = service.identify(image, "image/png");

        assertThat(result.candidates()).containsExactly("Paracetamol 500 mg");
        assertThat(result.instruction()).contains("Review");
        verify(model).identify(image, "image/png");
    }

    @Test
    void reportsUnavailableWhenAiIsDisabled() {
        @SuppressWarnings("unchecked")
        ObjectProvider<PrescriptionModel> models = mock(ObjectProvider.class);
        PrescriptionService service = new PrescriptionService(models, new ObjectMapper(), false);

        assertThatThrownBy(() -> service.identify(new byte[]{1}, "image/jpeg"))
                .isInstanceOf(AiUnavailableException.class);
        verifyNoInteractions(models);
    }
}
