<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="1400">
      <v-card>
        <v-card-title class="lighten-2 text-sm font-bold" style="color: #2b4f8c">
          Task History
        </v-card-title>

        <v-card-title
          class="lighten-2 text-xs font-bold ml-3"
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
          class="text-sm"
        >
          <!-- <div class="search" style="height: 30px">
            <select style="width: 10%">
                <option>
                  title
                </option>
              </select>
            <input type="text" class="ml-3"/>
            <select name="" id="" class="ml-3">
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
            class="pipelineTable mt-3"
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
            class="ml-3 text-xs"
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
          data: null,
        };
      },
      required: true,
    },
  },
  mounted() {
    this.taskdatas = this.contents.data;
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
      taskdatas: []
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
