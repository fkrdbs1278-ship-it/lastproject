package com.young04.lastproject.salonevent.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 미용실 이벤트의 기본 정보와 진행 기간을 저장하는 Entity
@Entity
@Table(name = "SALON_EVENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalonEvent {

    // 이벤트 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_NO")
    private Long eventNo;

    // 이벤트 제목
    @Column(name = "EVENT_TITLE", nullable = false, length = 200)
    private String eventTitle;

    // 이벤트 상세 내용
    @Column(name = "EVENT_CONTENT", length = 2000)
    private String eventContent;

    // 이벤트 유형
    @Column(name = "EVENT_TYPE", nullable = false, length = 30)
    private String eventType;

    // 이벤트 이미지 주소
    @Column(name = "EVENT_IMAGE_URL", length = 500)
    private String eventImageUrl;

    // 이벤트 시작일
    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    // 이벤트 종료일
    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    // 이벤트 사용 여부를 Oracle CHAR(1) 컬럼과 연결
    @Column(
            name = "USE_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String useYn;

    // 이벤트 등록일
    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;

    // 이벤트 수정일
    @Column(name = "UPDATEDATE")
    private LocalDateTime updatedate;

    // 이벤트 등록에 사용하는 생성자
    @Builder
    public SalonEvent(
            String eventTitle,
            String eventContent,
            String eventType,
            String eventImageUrl,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String useYn
    ) {
        this.eventTitle = eventTitle;
        this.eventContent = eventContent;
        this.eventType = eventType;
        this.eventImageUrl = eventImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.useYn = useYn;
    }

    // 신규 이벤트 저장 전에 기본값과 등록일 설정
    @PrePersist
    public void prePersist() {
        if (useYn == null || useYn.isBlank()) {
            useYn = "Y";
        }

        regdate = LocalDateTime.now();
    }

    // 이벤트 수정 시 수정일 갱신
    @PreUpdate
    public void preUpdate() {
        updatedate = LocalDateTime.now();
    }

    // 이벤트 기본 정보 수정
    public void update(
            String eventTitle,
            String eventContent,
            String eventType,
            String eventImageUrl,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String useYn
    ) {
        this.eventTitle = eventTitle;
        this.eventContent = eventContent;
        this.eventType = eventType;
        this.eventImageUrl = eventImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.useYn = useYn;
    }

    // 이벤트를 사용자 화면에서 숨김 처리
    public void stopUsing() {
        this.useYn = "N";
    }

    // 중지된 이벤트를 다시 노출
    public void resumeUsing() {
        this.useYn = "Y";
    }
}