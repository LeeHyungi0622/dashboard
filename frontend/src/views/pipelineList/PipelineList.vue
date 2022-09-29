<template>
  <v-app>
    <div class="pipelineListBox">
      <div class="pipelineListTitle">
        <p class="fsb16">파이프라인 목록</p>
        <div class="searchBox">
          <div class="activationFilter">
            <p>동작</p>
            <select @change="settingFilter('activationStatus', $event)">
              <option
                v-for="(item, index) in activationStatusList"
                :key="index"
                :value="item"
              >
                {{ item == "" ? "전체" : item }}
              </option>
            </select>
          </div>
          <div class="search">
            <select style="width: 10%">
              <option
                v-for="(item, index) in pipelineListFilterList"
                :key="index"
              >
                {{ item == "" ? "전체" : item }}
              </option>
            </select>
            <input type="text" class="mgL12" />
            <button class="mgL12">검색</button>
            <select name="" id="" class="mgL12" v-model="perPage">
              <option value="10">10개씩 표시</option>
              <option value="20">20개씩 표시</option>
            </select>
          </div>
        </div>

        <v-data-table
          :headers="pipelineListData.headers"
          :items="filteritems"
          :items-per-page="parseInt(perPage)"
          :page="currentPage"
          item-key="name"
          class="pipelineTable mgT12"
          :search="searchValue"
          :hide-default-footer="true"
          style="text-align: center"
        >
          <template v-slot:[`item.status`]="{ item }">
            <div class="activationStatusBox">
              <div class="acsTextBox">
                {{ item.status }}
              </div>
              <div class="acsBtnBox">
                <button
                  @click="
                    pipelineStatusAlertShows(
                      item.name,
                      item.status.toUpperCase() == 'STARING' ||
                        item.status.toUpperCase() == 'STOPPED'
                        ? 'STARTING'
                        : 'STARTING',
                      item.id
                    )
                  "
                  :disabled="item.status.toUpperCase().includes('ING')"
                >
                  {{ item.status }}
                </button>
              </div>
            </div>
          </template>
          <template v-slot:[`item.delete`]="{ item }">
            <button
              v-if="item.status.toUpperCase() == 'STOPPED'"
              @click="deletePipeline(item)"
            >
              삭제
            </button>
            <button v-else @click="deletePipeline(item)">중지 후 삭제</button>
          </template>
          <template v-slot:[`item.pipelineUpdate`]="{ item }">
            <button @click="goPipelineDetailEdit(item)">보기</button>
          </template>
        </v-data-table>
        <div class="paginationBox">
          <div class="firstPageBtnBox">
            <button
              class="v-pagination__navigation"
              @click="firstPage"
              :disabled="currentPage == 1"
            ></button>
          </div>
          <v-pagination
            v-model="currentPage"
            :length="totalPage"
            color="#2B4F8C"
          ></v-pagination>
          <div class="lastPageBtnBox">
            <button
              class="v-pagination__navigation"
              @click="lastPage"
              :disabled="currentPage == totalPage"
            ></button>
          </div>
        </div>

        <div class="pipelineBtnBox mgT12">
          <button class="pipelineButton" @click="tempPipelineShows">
            임시저장 파이프라인
          </button>
          <button class="pipelineButton mgL12" @click="goPipelineRegister">
            파이프라인 등록
          </button>
        </div>
      </div>
    </div>
  </v-app>
</template>
<script>
import pipelineListService from "../../js/api/pipelineList";
import EventBus from "@/eventBus/EventBus.js";
import pipelineListData from "../../json/pipelineList.json";
import tempPipeline from "../../json/tempPipeline.json";
export default {
  mounted() {
    console.log(this.$ws);
    this.connect();
    pipelineListService
      .getPipelineList()
      .then((res) => {
        this.$store.state.pipelineList = res;
      })
      .catch((err) => {
        console.log("PipelineList 조회에 실패하였습니다.", err);
      });
  },

  computed: {
    totalPage() {
      return Math.floor(
        (this.filteritems.length + parseInt(this.perPage)) / this.perPage
      );
    },
    filteritems() {
      return this.$store.state.pipelineList.filter((i) => {
        return (
          !this.selectedFilter || i[this.selectedFilter] === this.searchValue
        );
      });
    },
  },
  data() {
    return {
      perPage: 10,
      currentPage: 1,
      total: 15,
      tempPipeline: tempPipeline,
      activationStatusList: ["", "Run", "Starting", "Stopped", "Stopping"],
      pipelineListFilterList: ["", "파이프라인 이름", "적재Dataset"],
      searchValue: null,
      pipelineListData: pipelineListData,
    };
  },
  methods: {
    // API 사용법
    getpipelineList() {
      pipelineListService.getPipelineList();
    },

    firstPage() {
      this.currentPage = 1;
    },
    lastPage() {
      this.currentPage = this.totalPage;
    },
    tempPipelineShows() {
      let alertPayload = {
        contents: this.tempPipeline,
      };
      EventBus.$emit("show-temp-pipeline-popup", alertPayload);
    },
    pipelineStatusAlertShows(name, status, id) {
      let alertPayload = {
        title: "파이프라인 삭제",
        text:
          name +
          " 파이프라인의 " +
          "<br/>실행 상태가 " +
          status +
          "로 변경됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
        id: id,
      };
      EventBus.$emit("show-confirm-popup", alertPayload);
    },
    deletePipeline(item) {
      let alertPayload = {
        title: "파이프라인 Status 수정",
        text:
          item.name +
          " 파이프라인을 삭제하기 위해" +
          "<br/>프로세서를 중지하고 <br/>" +
          "잔여 Queue가 모두 삭제됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
      };
      console.log(item);
      EventBus.$emit("show-confirm-popup", alertPayload);
    },
    goPipelineDetailEdit(item) {
      this.$router.push({
        name: "pipelineUpdate",
        query: { id: item.id },
      });
    },
    goPipelineRegister() {
      this.$router.push({
        name: "pipelineCreate",
      });
    },

    connect() {
      this.$ws.onopen = () => {
        console.log("websocket Connected SUCCESS");
        this.getMessage();
      };
    },
    getMessage() {
      this.$ws.onmessage = ({ data }) => {
        this.$store.state.pipelineList = JSON.parse(data);
        console.log("메세지 수신", JSON.parse(data));
      };
    },
    disconnect() {
      this.$ws.close();
      console.log("websocket Disconnected SUCCESS");
    },
    sendMessage() {
      this.$ws.send(this.message);
      this.message = "";
    },
    settingFilter(filter, event) {
      console.log(filter, event.target.value);
      this.selectedFilter = filter;
      this.searchValue = event.target.value;
    },
  },
};
</script>
