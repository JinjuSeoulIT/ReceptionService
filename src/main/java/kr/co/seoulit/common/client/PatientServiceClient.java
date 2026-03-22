package kr.co.seoulit.common.client;

import kr.co.seoulit.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PatientServiceClient {

    private static final String DEFAULT_PATIENT_SERVICE_BASE_URL = "http://192.168.1.55:8181";

    private final RestClient restClient;

    public PatientServiceClient(
            @Value("${patient-service.base-url:" + DEFAULT_PATIENT_SERVICE_BASE_URL + "}") String patientServiceBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(patientServiceBaseUrl)
                .build();
    }

    public PatientSummary requirePatientById(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }

        ApiResponse<Map<String, Object>> response;
        try {
            response = restClient.get()
                    .uri("/api/patients/{patientId}", patientId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            throw translateRestClientException("lookup patient by patientId=" + patientId, ex);
        }

        Map<String, Object> result = unwrapResult(response, "patient not found for patientId=" + patientId);
        PatientSummary candidate = toPatientSummary(result);
        if (candidate == null) {
            throw new IllegalArgumentException("patient data is invalid for patientId=" + patientId);
        }

        String resolvedName = trimToNull(candidate.patientName());
        if (resolvedName == null) {
            throw new IllegalArgumentException("patientName not found for patientId=" + patientId);
        }

        Long resolvedId = candidate.patientId() != null ? candidate.patientId() : patientId;
        return new PatientSummary(resolvedId, resolvedName);
    }

    public PatientSummary requirePatientByName(String patientName) {
        String normalizedName = trimToNull(patientName);
        if (normalizedName == null) {
            throw new IllegalArgumentException("patientName is required");
        }

        ApiResponse<List<Map<String, Object>>> response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/patients/search")
                            .queryParam("type", "name")
                            .queryParam("keyword", normalizedName)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            throw translateRestClientException("lookup patient by patientName=" + normalizedName, ex);
        }

        List<Map<String, Object>> result = unwrapResult(response, "patient not found for patientName=" + normalizedName);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("patient not found for patientName=" + normalizedName);
        }

        List<PatientSummary> candidates = result.stream()
                .map(this::toPatientSummary)
                .filter(Objects::nonNull)
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("patient data is invalid for patientName=" + normalizedName);
        }

        List<PatientSummary> exactMatches = candidates.stream()
                .filter(candidate -> normalizedName.equalsIgnoreCase(trimToNull(candidate.patientName())))
                .toList();

        if (exactMatches.size() == 1) {
            return exactMatches.get(0);
        }
        if (exactMatches.size() > 1) {
            throw new IllegalArgumentException("multiple patients found for patientName=" + normalizedName);
        }
        if (candidates.size() == 1) {
            PatientSummary candidate = candidates.get(0);
            return new PatientSummary(candidate.patientId(), firstNonBlank(candidate.patientName(), normalizedName));
        }
        throw new IllegalArgumentException("multiple patients found for patientName=" + normalizedName);
    }

    private <T> T unwrapResult(ApiResponse<T> response, String defaultMessage) {
        if (response == null) {
            throw new IllegalStateException("patient-service response is empty");
        }
        if (!response.isSuccess()) {
            throw new IllegalArgumentException(firstNonBlank(response.getMessage(), defaultMessage));
        }
        T result = response.getResult();
        if (result == null) {
            throw new IllegalArgumentException(defaultMessage);
        }
        return result;
    }

    private PatientSummary toPatientSummary(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        Long patientId = toLong(payload.get("patientId"));
        if (patientId == null) {
            patientId = toLong(payload.get("id"));
        }
        if (patientId == null) {
            return null;
        }

        String patientName = firstNonBlank(
                toString(payload.get("name")),
                toString(payload.get("patientName"))
        );
        return new PatientSummary(patientId, patientName);
    }

    private RuntimeException translateRestClientException(String action, RestClientException ex) {
        if (ex instanceof RestClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            String body = trimToNull(responseException.getResponseBodyAsString());
            String suffix = body == null ? "" : " body=" + body;
            if (status >= 400 && status < 500) {
                return new IllegalArgumentException("patient-service rejected request while " + action + suffix);
            }
            return new IllegalStateException("patient-service call failed (" + status + ") while " + action + suffix, ex);
        }
        return new IllegalStateException("patient-service call failed while " + action, ex);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue) {
            String normalized = trimToNull(stringValue);
            if (normalized == null) {
                return null;
            }
            try {
                return Long.valueOf(normalized);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String toString(Object value) {
        if (value == null) {
            return null;
        }
        return trimToNull(String.valueOf(value));
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PatientSummary(Long patientId, String patientName) {
    }
}
