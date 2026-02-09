package kr.co.seoulit.reception.controller;

import kr.co.seoulit.reception.dto.MenuNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @GetMapping
    public List<MenuNode> getMenus() {
        MenuNode home = new MenuNode(
                1L,
                "HOME",
                "홈",
                "/",
                "Home",
                1,
                List.of()
        );

        MenuNode receptionRoot = new MenuNode(
                10L,
                "RECEPTION",
                "접수",
                null,
                "MedicalServices",
                2,
                List.of(
                        new MenuNode(
                                11L,
                                "RECEPTION_DASH",
                                "접수 관리",
                                "/reception",
                                "List",
                                1,
                                List.of()
                        ),
                        new MenuNode(
                                12L,
                                "RECEPTION_LIST",
                                "접수 목록",
                                "/receptions",
                                "List",
                                2,
                                List.of()
                        ),
                        new MenuNode(
                                13L,
                                "RECEPTION_NEW",
                                "신규 접수",
                                "/receptions/new",
                                "PersonAdd",
                                3,
                                List.of()
                        ),
                        new MenuNode(
                                14L,
                                "RECEPTION_CANCELED",
                                "접수 취소",
                                "/receptions/canceled",
                                "List",
                                4,
                                List.of()
                        ),
                        new MenuNode(
                                15L,
                                "RECEPTION_COMPLETED",
                                "접수 종결",
                                "/receptions/completed",
                                "List",
                                5,
                                List.of()
                        )
                )
        );

        MenuNode reservationRoot = new MenuNode(
                20L,
                "RESERVATION",
                "예약",
                null,
                "Description",
                3,
                List.of(
                        new MenuNode(
                                21L,
                                "RESERVATION_LIST",
                                "예약 목록",
                                "/reservations",
                                "List",
                                1,
                                List.of()
                        ),
                        new MenuNode(
                                22L,
                                "RESERVATION_NEW",
                                "신규 예약",
                                "/reservations/new",
                                "PersonAdd",
                                2,
                                List.of()
                        ),
                        new MenuNode(
                                23L,
                                "RESERVATION_CANCELED",
                                "예약 취소",
                                "/reservations/canceled",
                                "List",
                                3,
                                List.of()
                        ),
                        new MenuNode(
                                24L,
                                "RESERVATION_COMPLETED",
                                "진료 완료",
                                "/reservations/completed",
                                "List",
                                4,
                                List.of()
                        )
                )
        );

        MenuNode emergencyRoot = new MenuNode(
                30L,
                "EMERGENCY",
                "응급 접수",
                null,
                "MedicalServices",
                4,
                List.of(
                        new MenuNode(
                                31L,
                                "EMERGENCY_LIST",
                                "응급 목록",
                                "/emergency-receptions",
                                "List",
                                1,
                                List.of()
                        ),
                        new MenuNode(
                                32L,
                                "EMERGENCY_NEW",
                                "응급 신규",
                                "/emergency-receptions/new",
                                "PersonAdd",
                                2,
                                List.of()
                        ),
                        new MenuNode(
                                33L,
                                "EMERGENCY_CANCELED",
                                "응급 취소",
                                "/emergency-receptions/canceled",
                                "List",
                                3,
                                List.of()
                        ),
                        new MenuNode(
                                34L,
                                "EMERGENCY_COMPLETED",
                                "응급 종결",
                                "/emergency-receptions/completed",
                                "List",
                                4,
                                List.of()
                        )
                )
        );

        MenuNode inpatientRoot = new MenuNode(
                40L,
                "INPATIENT",
                "입원 접수",
                null,
                "MedicalServices",
                5,
                List.of(
                        new MenuNode(
                                41L,
                                "INPATIENT_LIST",
                                "입원 목록",
                                "/inpatient-receptions",
                                "List",
                                1,
                                List.of()
                        ),
                        new MenuNode(
                                42L,
                                "INPATIENT_NEW",
                                "입원 신규",
                                "/inpatient-receptions/new",
                                "PersonAdd",
                                2,
                                List.of()
                        ),
                        new MenuNode(
                                43L,
                                "INPATIENT_DISCHARGED",
                                "퇴원 목록",
                                "/inpatient-receptions/discharged",
                                "List",
                                3,
                                List.of()
                        )
                )
        );

        return List.of(home, receptionRoot, reservationRoot, emergencyRoot, inpatientRoot);
    }
}