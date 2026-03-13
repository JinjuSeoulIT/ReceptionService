package kr.co.seoulit.reception.outpatient.config;

import kr.co.seoulit.reception.outpatient.entity.ReceptionClosureReasonEntity;
import kr.co.seoulit.reception.outpatient.repository.ReceptionClosureReasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceptionReferenceDataInitializer implements ApplicationRunner {

    private final ReceptionClosureReasonRepository closureReasonRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (closureReasonRepository.count() > 0) {
            return;
        }

        List<ReceptionClosureReasonEntity> defaults = List.of(
                createReason("USER_CANCEL", "User cancelled", 1),
                createReason("NO_SHOW", "Patient no-show", 2),
                createReason("SYSTEM_CANCEL", "System cancelled", 3),
                createReason("VISIT_END", "Visit completed", 4)
        );
        closureReasonRepository.saveAll(defaults);
        log.info("Initialized reception closure reasons: {}", defaults.size());
    }

    private ReceptionClosureReasonEntity createReason(String code, String name, int order) {
        ReceptionClosureReasonEntity reason = new ReceptionClosureReasonEntity();
        reason.setClosureReasonCd(code);
        reason.setClosureReasonName(name);
        reason.setReasonGroupCd("DEFAULT");
        reason.setUsableYn("Y");
        reason.setSortOrder(order);
        return reason;
    }
}
