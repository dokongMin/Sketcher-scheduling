package sketcher.scheduling.algorithm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sketcher.scheduling.domain.ManagerHopeTime;
import sketcher.scheduling.domain.User;
import sketcher.scheduling.dto.ManagerHopeTimeDto;
import sketcher.scheduling.dto.UserDto;
import sketcher.scheduling.repository.UserRepository;
import sketcher.scheduling.service.ManagerHopeTimeService;
import sketcher.scheduling.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
@Rollback(value = false)
public class AutoSchedulingTest {

    @Autowired
    ManagerHopeTimeService managerHopeTimeService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;


    int scheduleNodeSize = 21;
    int managerNodeSize = 0;   //근무가능 매니저 수

    List<Schedule> scheduleList = new ArrayList<>();//Integer id, Integer time, Integer weight, boolean managerWeightFlag
    /*
    필요 스케줄 노드 수
    * S1 - 1
    * S2 - 4
    * S3 - 16
    * */
    int scheduleSectionSize[] = {1, 4, 16};

    List<Manager> managerList = new ArrayList<>();

    @Before
    @Transactional
    public void setUp() {

//        settingUsers();

        List<User> allManager = userService.findAllManager();
        managerNodeSize = allManager.size();


        settingScheduleNodes();

        //case1. totalAssignTime을 고려x
        for (int i = 0; i < managerNodeSize; i++) {
            User user = allManager.get(i);
            int weight = 0;
            int monthValue = user.getUser_joinDate().getMonthValue();
            if (monthValue == 6 && monthValue == 5) {
                weight = 1;
            } else if (monthValue == 4) {
                weight = 2;
            } else {
                weight = 3;
            }
            List<ManagerHopeTime> managerHopeTimeList = managerHopeTimeService.findManagerHopeTimeByUser(user);
            managerList.add(new Manager(user.getCode(), managerHopeTimeList, managerHopeTimeList.size(),
                    0, 0, weight));
        }


    }

    private void settingScheduleNodes() {
        Integer tempTime = 13;

        Integer weightType[] = {1, 2, 3};
        boolean managerWeightFlag = false;

        int count = 0;      //변수값 세팅시에 scheduleSectionSize만큼 배정했는지 확인하기 위한 카운팅 변수
        int pointer = 0;    //scheduleSectionSize, weightType를 가리키는 포인터


        for (int i = 1; i <= scheduleNodeSize; i++) {
            if (weightType[pointer] == 3 && i > 10) {
                managerWeightFlag = true;
            }
            scheduleList.add(new Schedule(i, tempTime, weightType[pointer], managerWeightFlag));
            count++;
            if (scheduleSectionSize[pointer] == count) {
                tempTime++;
                pointer++;
                count = 0;
            }
            managerWeightFlag = false;
        }
    }

    private void settingUsers() {
        LocalDateTime date1 = LocalDateTime.of(2022, 3, 10, 1, 00);
        LocalDateTime date3 = LocalDateTime.of(2022, 4, 10, 20, 00);
        LocalDateTime date5 = LocalDateTime.of(2022, 5, 10, 17, 00);
        LocalDateTime date7 = LocalDateTime.of(2022, 6, 10, 7, 00);
        List<LocalDateTime> localDateTimeList = new ArrayList<>();
        localDateTimeList.add(date1);
        localDateTimeList.add(date3);
        localDateTimeList.add(date5);
        localDateTimeList.add(date7);

        String names[] = {"박태영", "정민환", "이혜원", "김희수", "김민준", "박서준", "임도윤", "정예준", "박시우", "정하준", "서주원", "유지호", "성지훈", "김준우", "박건우", "박서연", "이서윤", "이지우", "김서현", "박하은", "김하은", "김민서", "유민서", "박지민", "김희철", "김채원", "이도현", "김연우", "유다은", "김지원", "서지원", "이수빈", "김예린",
                "이준영", "박시후", "김진우", "정승우", "박채은", "채유나", "김가은", "박서영", "윤민지", "최예나", "최수민", "강수현", "이동현", "최한결", "김재원", "서민우", "김연서", "강다연", "정나윤", "김성현", "김우빈", "정지한", "최예성", "한나은", "홍예지"};


        for (int i = 0; i < names.length; i++) {
            UserDto userA = UserDto.builder()
                    .id("user" + (i + 1))
                    .authRole("MANAGER")
                    .password(new BCryptPasswordEncoder().encode("12345"))
                    .username(names[i])
                    .userTel("010-1234-5678")
                    .user_joinDate(localDateTimeList.get((i + 1) % 4))
                    .managerScore(5.0)
                    .build();
            String user1 = userService.saveUser(userA);
            User userT = userRepository.findById(user1).get();

            if (i < 5) {
                setHopeTime(userT, 0, 6);
            } else if (i < 10) {
                setHopeTime(userT, 6, 12);
            } else if (i < 15) {
                setHopeTime(userT, 0, 6);
                setHopeTime(userT, 6, 12);
            } else if (i < 20) {
                setHopeTime(userT, 12, 18);
                setHopeTime(userT, 18, 24);
            } else if (i < 30) {
                setHopeTime(userT, 6, 12);
                setHopeTime(userT, 12, 18);
                setHopeTime(userT, 18, 24);
            } else if (i >= 30) {
                setHopeTime(userT, 0, 6);
                setHopeTime(userT, 6, 12);
                setHopeTime(userT, 12, 18);
                setHopeTime(userT, 18, 24);
            }
        }

    }

    void setHopeTime(User userT, int i2, int i3) {
        ManagerHopeTimeDto hopeC = ManagerHopeTimeDto.builder()
                .start_time(i2)
                .finish_time(i3)
                .user(userT)
                .build();
        managerHopeTimeService.saveManagerHopeTime(hopeC);
    }

    @Test
    @Transactional
    public void dfsTest() {
        int count = 0;
        for (int i = 0; i < scheduleNodeSize; i++) {
            if (firstDFS(scheduleList.get(i))) count++;   //매칭 개수
        }
        boolean performSecondDFS = false;
        for (Schedule schedule : scheduleList) {
            if (schedule.getManager() == null) {
                performSecondDFS = true;
                break;
            }
        }
        if (performSecondDFS) {
//            for (int i = 0; i < scheduleNodeSize; i++) {
//                if (secondDFS(scheduleList.get(i))) count++;   //매칭 개수
//            }
        }

        System.out.println("matching count :" + count);
        for (Schedule schedule : scheduleList) {
            System.out.print(schedule.getId() + " : " + schedule.getTime() + "시, S" + schedule.getWeight() + ", M3매니저 필수 여부 " + schedule.isManagerWeightFlag() + ", ");
            if (schedule.getManager() != null) {
                System.out.println("배정매니저번호 " + schedule.getManager().getCode());
            } else {
                System.out.println("배정매니저 없음");
            }
        }

        for (Manager manager : managerList) {
            System.out.println(manager.getCode() + " : M" + manager.getWeight() + ", 하루배정 시간 : " + manager.getDayAssignTime());
        }
    }

    public boolean firstDFS(Schedule scheduleNode) {
//        int pointer = settingScheduleNodeTotalSize(scheduleNode);
//        for (int i = 0; i < scheduleSectionSize[pointer]; i++) {
        /* 매니저리스트 currentTime 오름차순 정렬 */
        Collections.sort(managerList);
//        for (Manager manager : managerList) {
//            System.out.println("매니저번호" + manager.getCode() + " : 현재 배정 시간 : " + manager.getTotalAssignTime());
//        }
        for (Manager manager : managerList) {
            Schedule alreadyExistingScheduleNode = manager.findScheduleByTime(scheduleNode.getTime());
            if (!manager.isContrainHopeTimes(scheduleNode.getTime())) { //조건1. 희망시간 포함 여부
                continue;
            }
            if (manager.getDayAssignTime() > 3) {   // 조건2. 하루 배정 시간이 3시간이 넘으면 제외
                continue;
            }
            if (alreadyExistingScheduleNode != null) {            // 조건3. 이미 해당 매니저가 동시간대에 배정되어 있음
                // 이미 배정된 매니저가 managerWeightFlag=false이고, 현재 스케줄이 managerWeightFlag=true인 경우
                if (checkSwapping(alreadyExistingScheduleNode, scheduleNode)) {
                    alreadyExistingScheduleNode.setManager(null);
                    manager.updateAssignScheduleList(alreadyExistingScheduleNode, scheduleNode);
                    scheduleNode.setManager(manager);
                    return true;    //s1, s2에 M3가 배정되는 경우 -->
                }
                continue;
            }
            //매니저가 해당 시간대에 아직 배정되지 않은 경우 (alreadyExistingScheduleNode == null인 경우)
            if (scheduleNode.isManagerWeightFlag() && manager.getWeight() != 3) {
                continue;                                   //조건4. managerWeightFlag가 true라면 매니저는 반드시 M3여야 함
            }
            if (alreadyExistingScheduleNode == null || firstDFS(alreadyExistingScheduleNode)) {
                manager.updateAssignScheduleList(alreadyExistingScheduleNode, scheduleNode);
                scheduleNode.setManager(manager);
                return true;
            }
//isPriorityScoreHigherThanExistingeManager
        }
        return false;
    }

    public boolean secondDFS(Schedule scheduleNode) {   //스와핑 때문에 스케줄 배정이 취소된 노드를 대상으로 다시 dfs 수행
//        int pointer = settingScheduleNodeTotalSize(scheduleNode);
//        for (int i = 0; i < scheduleSectionSize[pointer]; i++) {
        for (Manager manager : managerList) {
            Schedule alreadyExistingScheduleNode = manager.findScheduleByTime(scheduleNode.getTime());
            if (!manager.isContrainHopeTimes(scheduleNode.getTime())) { //조건1. 희망시간 포함 여부
                continue;
            }
            if (alreadyExistingScheduleNode != null) {            // 조건2. 이미 해당 매니저가 동시간대에 배정되어 있음
                // 이미 배정된 매니저가 managerWeightFlag=false이고, 현재 스케줄이 managerWeightFlag=true인 경우
                if (checkSwapping(alreadyExistingScheduleNode, scheduleNode)) {
                    alreadyExistingScheduleNode.setManager(null);
                    manager.updateAssignScheduleList(alreadyExistingScheduleNode, scheduleNode);
                    scheduleNode.setManager(manager);
                    return true;
                }
                continue;
            }
            //매니저가 해당 시간대에 아직 배정되지 않은 경우 (alreadyExistingScheduleNode == null인 경우)
            if (scheduleNode.isManagerWeightFlag() && manager.getWeight() != 3) {
                continue;                                   //조건3. managerWeightFlag가 true라면 매니저는 반드시 M3여야 함
            }
            if (alreadyExistingScheduleNode == null || firstDFS(alreadyExistingScheduleNode)) {
                manager.updateAssignScheduleList(alreadyExistingScheduleNode, scheduleNode);
                scheduleNode.setManager(manager);
                return true;
            }
//isPriorityScoreHigherThanExistingeManager
        }
        return false;
    }

    private boolean checkSwapping(Schedule alreadyExistingScheduleNode, Schedule scheduleNode) {
        if (alreadyExistingScheduleNode.isManagerWeightFlag() == false && scheduleNode.isManagerWeightFlag() == true)
            return true;
        return false;
    }


    private int settingScheduleNodeTotalSize(Schedule scheduleNode) {
        int pointer = -1;
        if (scheduleNode.getWeight() == 3) {
            pointer = 0;
        } else if (scheduleNode.getWeight() == 2) {
            pointer = 1;
        } else if (scheduleNode.getWeight() == 1) {
            pointer = 2;
        }
        return pointer;
    }
}