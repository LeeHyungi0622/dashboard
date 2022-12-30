<template>
  <v-app>
    <div class="pipelineListBox">
      <div class="pipelineListTitle">
        <p class="fsb16">파이프라인 목록</p>
        <div class="searchBox">
          <div class="activationFilter">
            <p>동작</p>
            <select @change="settingFilter('status', $event)">
              <option
                v-for="([title, val], index) in activationStatusList"
                :key="index"
                :value="val"
              >
                {{ title }}
              </option>
            </select>
          </div>
          <div class="search">
            <select style="width: 10%" @change="settingFilter('pipelineFilter', $event)">
              <option
                v-for="([title, val], index) in pipelineListFilterList"
                :key="index"
                :value="val"
              >
                {{ title }}
              </option>
            </select>
            <input type="text" class="mgL12" v-model="pipelineFilterInput" maxlength="300"/>
            <button class="mgL12" @click="actionFilter()">검색</button>
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
          class="pipelineTable mgT12"
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
                      item.status.toUpperCase(),
                      item.id
                    )
                  "
                  :disabled="item.status.toUpperCase().includes('ING')"
                >
                  {{ showStatusBtn(item.status.toUpperCase()) }}
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
    this.$store.state.overlay = true;
    this.connect();
    pipelineListService
      .getPipelineList()
      .then((res) => {
        this.$store.state.pipelineList = res;
        this.filteritems = res;
      })
      .catch((err) => {
        console.log("PipelineList 조회에 실패하였습니다.", err);
      });
    this.$store.state.tableShowMode = "";
    this.$store.state.registerPipeline = {};
    this.$store.state.completedPipeline = {};
    this.$store.state.overlay = false;
  },
  watch:{
    filteritems(){
      let idx = 0;
      for(let row of this.filteritems){
        row['idx'] = idx;
        idx ++;
      }
    },
  },
  computed: {
    totalPage() {
      return Math.floor(
        (this.filteritems.length + parseInt(this.perPage) - 1) / this.perPage
      );
    },

  },
  data() {
    return {
      filteritems: [],
      pipelineFilterInput: "",
      pipelineFilter: null,
      perPage: 10,
      currentPage: 1,
      total: 15,
      tempPipeline: tempPipeline,
      activationStatusList: [["전체",""], ["RUN","RUN"], ["STARTING","STARTING"], ["STOPPED","STOPPED"], ["STOPPING","STOPPING"]],
      pipelineListFilterList: [["전체",""], ["파이프라인 이름","name"], ["적재Dataset","dataSet"]],
      searchValue: null,
      pipelineListData: pipelineListData,
    };
  },
  methods: {
    // API 사용법
    getpipelineList() {
      pipelineListService.getPipelineList();
    },
    showStatusBtn(status){
      if(status == 'RUN'){
        return '중지';
      }
      else if(status == 'STOPPED'){
        return '실행';
      }
      else if(status == 'STOPPING'){
        return '중지 중';
      }
      else if(status == 'STARTING'){
        return '실행 중';
      }
      else {
        return '';
      }
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
        title: "파이프라인 상태 수정",
        text:
          name +
          " 파이프라인의 상태가" +
          "<br/> <b> " +
          this.showStatusBtn(status) + "</b> " +
          "로 변경됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
        id: id,
        url: "update",
        body: status,
        name: name
      };
      EventBus.$emit("show-confirm-popup", alertPayload);
    },
    deletePipeline(item) {
      let alertPayload = {
        title: "파이프라인 삭제",
        text:
          item.name +
          " 파이프라인을 삭제하기 위해" +
          "<br/>프로세서를 중지하고 <br/>" +
          "잔여 Queue가 모두 삭제됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
        url: "deleteComplete",
        id: item.id,
        name: item.name
      };
      EventBus.$emit("show-confirm-popup", alertPayload);
      
    },
    goPipelineDetailEdit(item) {
      this.$store.state.completedPlId = item.id;
      this.$router.push({
          path: "pipelineUpdate/" + item.id,
        });
    },
    goPipelineRegister() {
      this.$store.state.tableShowMode = "REGISTER";
      this.$router.push({
        path: "/pipelineRegister/new",
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
        // this.filteritems = JSON.parse(data);
        this.actionFilter()
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
      if(filter == "status"){
        this.selectedFilter = filter;
        this.searchValue = event.target.value;
      }
      else{
        if(event.target.value == ""){
          this.pipelineFilterInput = "";
        }
        this.pipelineFilter = event.target.value;
      }
    },
    actionFilter(){
      this.filteritems = this.$store.state.pipelineList;

      if(this.searchValue){
        if(this.pipelineFilter){
          this.filteritems = this.$store.state.pipelineList.filter((i) => {
          return (
            i[this.selectedFilter] === this.searchValue && i[this.pipelineFilter].includes(this.pipelineFilterInput) 
          );
        });
        }
        else{
          this.filteritems = this.$store.state.pipelineList.filter((i) => {
          return (
            i[this.selectedFilter] === this.searchValue 
          );
        });
        }
      }
      else{
        if(this.pipelineFilter){
          this.filteritems = this.$store.state.pipelineList.filter((i) => {
          return (
            i[this.pipelineFilter].includes(this.pipelineFilterInput) 
          );
        });
        }
        else{
          this.filteritems = this.$store.state.pipelineList;
        }
      }
    }
  },
};
</script>
