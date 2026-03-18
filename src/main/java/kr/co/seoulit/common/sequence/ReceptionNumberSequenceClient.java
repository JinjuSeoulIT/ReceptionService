package kr.co.seoulit.common.sequence;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;

@Component
@RequiredArgsConstructor
public class ReceptionNumberSequenceClient {

    private static final String CALL_NEXT_RECEPTION_NO = "{ call HOSPITAL.SP_NEXT_RECEPTION_NO(?, ?) }";

    private final JdbcTemplate jdbcTemplate;

    public String nextReceptionNo(String seqType) {
        String normalizedType = normalizeSeqType(seqType);
        try {
            String generated = jdbcTemplate.execute(
                    (CallableStatementCreator) connection -> {
                        var callable = connection.prepareCall(CALL_NEXT_RECEPTION_NO);
                        callable.setString(1, normalizedType);
                        callable.registerOutParameter(2, Types.VARCHAR);
                        return callable;
                    },
                    (CallableStatementCallback<String>) callable -> {
                        callable.execute();
                        return callable.getString(2);
                    }
            );

            if (generated == null || generated.isBlank()) {
                throw new IllegalStateException("SP_NEXT_RECEPTION_NO returned empty value for seqType=" + normalizedType);
            }
            return generated.trim();
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Failed to call SP_NEXT_RECEPTION_NO for seqType=" + normalizedType, ex);
        }
    }

    private String normalizeSeqType(String seqType) {
        if (seqType == null || seqType.isBlank()) {
            throw new IllegalArgumentException("seqType is required");
        }
        return seqType.trim().toUpperCase();
    }
}
