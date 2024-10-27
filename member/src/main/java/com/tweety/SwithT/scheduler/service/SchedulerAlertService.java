package com.tweety.SwithT.scheduler.service;

import com.tweety.SwithT.common.configs.RabbitMqConfig;
import com.tweety.SwithT.common.service.TwilioService;
import com.tweety.SwithT.member.domain.Member;
import com.tweety.SwithT.member.repository.MemberRepository;
import com.tweety.SwithT.scheduler.domain.ScheduleAlert;
import com.tweety.SwithT.scheduler.domain.Scheduler;
import com.tweety.SwithT.scheduler.dto.ScheduleAlertCreateDto;
import com.tweety.SwithT.scheduler.dto.ScheduleAlertDto;
import com.tweety.SwithT.scheduler.dto.ScheduleAlertUpdateDto;
import com.tweety.SwithT.scheduler.repository.SchedulerAlertRepository;
import com.tweety.SwithT.scheduler.repository.SchedulerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulerAlertService {

    private final TwilioService twilioService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOperations;
    private final SchedulerAlertRepository schedulerAlertRepository;
    private final MemberRepository memberRepository;
    private final SchedulerRepository schedulerRepository;

    private static final String SCHEDULED_ALERTS_ZSET_KEY = "scheduled_alerts";

    @Autowired
    public SchedulerAlertService(TwilioService twilioService, @Qualifier("16") RedisTemplate<String, Object> redisTemplate,
                                 SchedulerAlertRepository schedulerAlertRepository, MemberRepository memberRepository,
                                 SchedulerRepository schedulerRepository) {
        this.twilioService = twilioService;
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet(); // Redis ZSetOperations 객체를 가져옴
        this.schedulerAlertRepository = schedulerAlertRepository;
        this.memberRepository = memberRepository;
        this.schedulerRepository = schedulerRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.SCHEDULER_ALERT_QUEUE)
    @Transactional
    public void receiveAndSendAlert(ScheduleAlertDto alertDto) {
        ScheduleAlert alert = schedulerAlertRepository.findById(alertDto.getId()).orElseThrow(
                ()-> new EntityNotFoundException("알림을 불러오는 데 실패했습니다"));
        if(alert.getSendYn()=='N'){
            try {
                String phoneNumber = alert.getScheduler().getMember().getPhoneNumber();  // 전화번호 가져오기
                String sendPhoneNumber = "+82" + phoneNumber.substring(1,11);
                System.out.println(sendPhoneNumber);
                String name = alert.getScheduler().getMember().getName();

                // 메시지 내용 생성
                String messageContent = name + "님, " + alert.getScheduler().getTitle() + " 가 곧 시작됩니다.";

                // Twilio를 통해 SMS 전송
                twilioService.sendSms(sendPhoneNumber, messageContent);

                // 알림이 성공적으로 전송된 경우, Redis에서 해당 알림 삭제
                Long alertId = alert.getId(); // 명시적으로 Long 타입으로 변환
                zSetOperations.remove(SCHEDULED_ALERTS_ZSET_KEY, alertId);

                // 알림 상태를 전송됨으로 업데이트
                alert.sended();

            } catch (Exception e) {
                // 처리 중 오류 발생 시 로깅
                System.err.println("MQ Listener에 문제 있음!! " + e.getMessage());
            }
        }
    }

    public ScheduleAlertDto getAlertInfo(Long id){
        ScheduleAlert scheduleAlert = schedulerAlertRepository.findBySchedulerId(id);

        return ScheduleAlertDto.builder()
                .id(scheduleAlert.getId())
                .schedulerId(scheduleAlert.getScheduler().getId())
                .reserveDay(scheduleAlert.getReserveDay())
                .reserveTime(scheduleAlert.getReserveTime())
                .build();
    }

    public void createAndSetAlert(ScheduleAlertCreateDto dto) {
        // 현재 인증된 사용자 정보를 통해 회원 조회
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원 정보입니다.")
        );

        // 스케줄 정보를 가져옴
        Scheduler scheduler = schedulerRepository.findById(dto.getScheduleId()).orElseThrow(
                () -> new EntityNotFoundException("스케줄 정보 불러오기에 실패했습니다.")
        );

        // 회원이 스케줄에 접근할 권한이 있는지 확인
        if (!scheduler.getMember().equals(member)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        // 이미 알림이 설정된 경우 예외 발생
        if (scheduler.getAlertYn() != 'N') {
            throw new IllegalStateException("이미 알림이 설정된 스케줄입니다.");
        }

        // 알림 엔티티를 생성하여 저장
        schedulerAlertRepository.save(dto.toEntity(scheduler));

        // 스케줄에 알림이 설정되었음을 표시
        scheduler.makingAlert();

        schedulerRepository.save(scheduler);
    }

    public void cancelAlert(Long alertId){
        ScheduleAlert scheduleAlert = schedulerAlertRepository.findById(alertId).orElseThrow(
                () -> new EntityNotFoundException("알람 정보 불러오기에 실패했습니다.")
        );

        // 스케줄 정보를 가져옴
        Scheduler scheduler = scheduleAlert.getScheduler();
        scheduler.cancelAlert();

        zSetOperations.remove(SCHEDULED_ALERTS_ZSET_KEY, scheduleAlert.getId());

        schedulerAlertRepository.delete(scheduleAlert);
    }

    public ScheduleAlert updateAlert(ScheduleAlertUpdateDto dto){
        Member member = memberRepository.findById(
                Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원 정보입니다.")
        );

        ScheduleAlert scheduleAlert = schedulerAlertRepository.findBySchedulerId(dto.getScheduleId());

        scheduleAlert.updateAlert(dto);

        schedulerAlertRepository.save(scheduleAlert);

        return scheduleAlert;
    }
}
