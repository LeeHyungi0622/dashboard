<template>
    <div class="text-center">
      <v-dialog v-model="dialog" persistent width="1400">
        <v-card>
          <v-card-title class="lighten-2 fsb14" style="color: #2b4f8c">
            Task History
          </v-card-title>
  
          <v-card-title
            class="lighten-2 fsb12 mgL12"
            style="float: left:  !important;"
          >
            {{ contents.subTitle }}
          </v-card-title>
          <v-card-text
            style="
              height: auto;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              text-align: center;
            " 
            class="fs14"
          >
            <!-- <div class="search" style="height: 30px">
              <select style="width: 10%">
                  <option>
                    title
                  </option>
                </select>
              <input type="text" class="mgL12"/>
              <select name="" id="" class="mgL12">
                <option value="5">5개씩 표시</option>
                <option value="10">10개씩 표시</option>
              </select>
            </div> -->
            <v-data-table
              :headers="headers"
              :items="taskdatas"
              item-key="id"
              :items-per-page="parseInt(taskperPage)"
              :page="taskcurrentPage"
              class="pipelineTable mgT12"
              :hide-default-footer="true"
              style="text-align: center; width: 100%"
            >
            </v-data-table>
            <div>
              <div class="paginationBox">
                <div class="firstPageBtnBox">
                  <button
                    class="v-pagination__navigation"
                    @click="taskfirstPage"
                    :disabled="taskcurrentPage == 1"
                  ></button>
                </div>
                <v-pagination
                  v-model="taskcurrentPage"
                  :length="tasktotalPage"
                  color="#2B4F8C"
                ></v-pagination>
                <div class="lastPageBtnBox">
                  <button
                    class="v-pagination__navigation"
                    @click="tasklastPage"
                    :disabled="taskcurrentPage == tasktotalPage"
                  ></button>
                </div>
              </div>
            </div>
          </v-card-text>
  
          <v-card-actions style="display: flex; justify-content: center">
            <button
              style="width: 15%; padding: 3px"
              class="mgL12 fs12"
              @click="close"
            >
              닫기
            </button>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </div>
  </template>
  
  <script>
 
  export default {
    props: {
      contents: {
        type: Object,
        default: () => {
          return {
            title: null,
            data: null,
            filterList: null,
          };
        },
        required: true,
      },
    },
    mounted() {
      this.taskdatas = this.$store.state.taskDataList;
    },
    computed: {
      tasktotalPage() {
        return Math.floor(
          (this.taskdatas.length + parseInt(this.taskperPage) - 1) / this.taskperPage
        );
      },
    },
    data() {
      return {
        headers: [
            { "text": "NO", "value": "id", "sortable": false },
            { "text": "동작 id", "value": "commandId", "sortable": false },
            { "text": "작업이름", "value": "taskName", "sortable": false },
            { "text": "동작 상태","value": "status","sortable": false},
            { "text": "동작 시작 시간", "value": "startedAt", "sortable": false },
            { "text": "동작 종료 시간", "value": "finishedAt", "sortable": false }
            
            ],
        taskdatas: [
            {
                "id": 1,
                "commandId": "11",
                "taskName": "DELETE",
                "parameter": "",
                "status": "SUCCEED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            },
            {
                "id": 2,
                "commandId": "12",
                "taskName": "UPDATE",
                "parameter": "",
                "status": "SUCCEED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            },
            {
                "id": 3,
                "commandId": "13",
                "taskName": "DELETE",
                "parameter": "",
                "status": "SUCCEED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            },
            {
                "id": 4,
                "commandId": "14",
                "taskName": "RUN",
                "parameter": "",
                "status": "SUCCEED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            },
            {
                "id": 5,
                "commandId": "15",
                "taskName": "DELETE",
                "parameter": "",
                "status": "SUCCEED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            },
            {
                "id": 6,
                "commandId": "16",
                "taskName": "STOP",
                "parameter": "",
                "status": "FAILED",
                "startedAt": "2022-09-15 05:54:48.185Z",
                "finishedAt": "2022-09-26 02:48:00.850Z"
            }
            ],
        // taskdatas: [],
        pipelineListFilterList: [["전체",""], ["Task Name","name"]],
        vuetifyData: [],
        searchValue: "",
        selectedFilter: "",
        dialog: true,
        taskperPage: 5,
        taskcurrentPage: 1,
        total: 0,
        imgSrc: {
          x: require("../../assets/img/x.svg"),
          check: require("../../assets/img/check.svg"),
        },
      };
    },
    methods: {
      getImg(value) {
        let imgSrc = "x";
        if (value) {
          imgSrc = "check";
        }
        return imgSrc;
      },
      close() {
        console.log('close');
        this.$emit("close-task-list-popup");
      },
      taskfirstPage() {
        this.taskcurrentPage = 1;
      },
      tasklastPage() {
        this.taskcurrentPage = this.tasktotalPage;
      },
    },
  };
  </script>
  