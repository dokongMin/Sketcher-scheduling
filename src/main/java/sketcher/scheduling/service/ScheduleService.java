package sketcher.scheduling.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import javafx.util.converter.LocalDateStringConverter;
import jdk.nashorn.internal.runtime.options.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sketcher.scheduling.domain.ManagerAssignSchedule;
import sketcher.scheduling.domain.Schedule;
import sketcher.scheduling.domain.User;
import sketcher.scheduling.dto.ScheduleDto;
import sketcher.scheduling.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ManagerAssignScheduleService managerAssignScheduleService;

    @Transactional
    public Integer saveSchedule(ScheduleDto scheduleDto) {
       return scheduleRepository.save(scheduleDto.toEntity()).getId();
    }

    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> findById(Integer id){
        return scheduleRepository.findById(id);
    }


    @Transactional
    public void deleteById(Integer Id){
        scheduleRepository.deleteById(Id);
    }

}
