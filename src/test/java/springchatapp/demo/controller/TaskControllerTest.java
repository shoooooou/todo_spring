package springchatapp.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import springchatapp.demo.model.entity.AddTaskEntityFactory;
import springchatapp.demo.model.entity.TaskEntity;
import springchatapp.demo.model.resource.AddTaskRequestResource;
import springchatapp.demo.model.value.object.StatusCd;
import springchatapp.demo.model.value.object.TaskName;
import springchatapp.demo.service.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
  @Mock
  TaskService taskService;
  private MockMvc mockMvc;
  @InjectMocks
  private TaskController target;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(target).build();
  }

  @DisplayName("タスク取得件数が0件の場合にレスポンスのステータスが200番台でデータに空のリストが含まれている")
  @Test
  void getTaskList_ok1() throws Exception {
    final String uid = "0000000001";
    when(taskService.getTaskList(uid)).thenReturn(Optional.empty());

    mockMvc.perform(MockMvcRequestBuilders.get("/todo/tasklist/" + uid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(taskService, times(1)).getTaskList(uid);
  }

  @DisplayName("1件以上タスク取得ができた場合にレスポンスのステータスが200番台でデータに取得したステータスが含まれている")
  @Test
  void getTaskList_ok2() throws Exception {
    final var uid = "0000000001";
    final var expected = createExpectedGetTaskList();

    when(taskService.getTaskList(uid)).thenReturn(Optional.of(expected));

    mockMvc.perform(MockMvcRequestBuilders.get("/todo/tasklist/" + uid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].taskName").value(expected.get(0).getTaskName().getValue()))
        .andExpect(jsonPath("$[0].statusCd").value(expected.get(0).getStatusCd().getValue()))
        .andExpect(jsonPath("$[0].sequenceNo").value(expected.get(0).getSequenceNo()))
        .andExpect(jsonPath("$[1].taskName").value(expected.get(1).getTaskName().getValue()))
        .andExpect(jsonPath("$[1].statusCd").value(expected.get(1).getStatusCd().getValue()))
        .andExpect(jsonPath("$[1].sequenceNo").value(expected.get(1).getSequenceNo()))
        .andExpect(jsonPath("$[2].taskName").value(expected.get(2).getTaskName().getValue()))
        .andExpect(jsonPath("$[2].statusCd").value(expected.get(2).getStatusCd().getValue()))
        .andExpect(jsonPath("$[2].sequenceNo").value(expected.get(2).getSequenceNo()));

    verify(taskService, times(1)).getTaskList(uid);
  }

  @Test
  @DisplayName("タスクの登録が成功した場合にレスポンスのステータスが200番台でデータに登録成功メッセージが含まれている")
  void addTask_ok1() throws Exception {
    final var uid = "0000000001";
    final var taskName = "task1";
    final var statusCd = "1";
    final var addTaskRequestResource =
        AddTaskRequestResource.builder().taskName(taskName).statusCd(statusCd).build();
    final var addTaskEntity = AddTaskEntityFactory.create(uid, addTaskRequestResource);
    when(taskService.addTask(any())).thenReturn(1);

    mockMvc.perform(MockMvcRequestBuilders.post("/todo/tasklist/" + uid)
            .contentType("application/json")
            .content("{\"taskName\":\"" + taskName + "\",\"statusCd\":\"" + statusCd + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Task added successfully."));

    verify(taskService, times(1)).addTask(eq(addTaskEntity));
  }

  @Test
  @DisplayName("タスクの登録が失敗した場合にレスポンスのステータスが500番台でデータに登録失敗メッセージが含まれている")
  void addTask_error1() throws Exception {
    final var uid = "0000000001";
    final var taskName = "task1";
    final var statusCd = "1";
    final var addTaskRequestResource =
        AddTaskRequestResource.builder().taskName(taskName).statusCd(statusCd).build();
    final var addTaskEntity = AddTaskEntityFactory.create(uid, addTaskRequestResource);
    when(taskService.addTask(any())).thenThrow(new RuntimeException());

    mockMvc.perform(MockMvcRequestBuilders.post("/todo/tasklist/" + uid)
            .contentType("application/json")
            .content("{\"taskName\":\"" + taskName + "\",\"statusCd\":\"" + statusCd + "\"}"))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$").value("Task added failed."));

    verify(taskService, times(1)).addTask(eq(addTaskEntity));
  }

  List<TaskEntity> createExpectedGetTaskList() {
    List<TaskEntity> taskList = new ArrayList<>();

    taskList.add(TaskEntity.builder().sequenceNo(1234).taskName(TaskName.noValidateOf("task1"))
        .statusCd(StatusCd.noValidateOf("0")).build());
    taskList.add(TaskEntity.builder().sequenceNo(5678).taskName(TaskName.noValidateOf("task2"))
        .statusCd(StatusCd.noValidateOf("1")).build());
    taskList.add(TaskEntity.builder().sequenceNo(9876).taskName(TaskName.noValidateOf("task3"))
        .statusCd(StatusCd.noValidateOf("2")).build());

    return taskList;
  }
}