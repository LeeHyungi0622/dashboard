<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="1400">
      <v-card>
        <v-card-title class="lighten-2 text-sm font-bold primary-color">
          Task History
        </v-card-title>

        <v-card-title
          class="lighten-2 text-xs font-bold ml-3 float-left"
        >
          {{ contents.subTitle }}
        </v-card-title>
        <v-card-text
          class="text-sm h-auto flex flex-col justify-center items-center text-center"
        >
          <v-data-table
            :headers="headers"
            :items="taskdatas"
            item-key="id"
            :items-per-page="parseInt(taskperPage)"
            :page="taskcurrentPage"
            class="pipelineTable mt-3 w-full text-center"
            :hide-default-footer="true"
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

        <v-card-actions class="flex justify-center">
          <button
            class="ml-3 text-xs w-1/6 p-1"
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
