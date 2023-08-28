<template>
  <v-app>
    <div class="pipelineListBox">
      <div class="pipelineListTitle">
        <p class="text-base font-bold float-left">파이프라인 목록</p>
        <div class="redirectBtn" @click="redirectNiFi()"><button class="float-right">NiFi</button></div>
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
            <select class="" @change="settingFilter('pipelineFilter', $event)">
              <option
                v-for="([title, val], index) in pipelineListFilterList"
                :key="index"
                :value="val"
              >
                {{ title }}
              </option>
            </select>
            <input v-if="pipelineFilter != 'all'" type="text" class="ml-3" v-model="pipelineFilterInput" maxlength="300"/>
            <div v-else class="ml-3" maxlength="300"/>
            <button class="ml-3" @click="actionFilter()">검색</button>
            <select class="ml-3 w-20" v-model="perPage" @change="resetPage($event)">
              <option value="10">10개씩 표시</option>
              <option value="20">20개씩 표시</option>
            </select>
          </div>
        </div>
        
        <v-data-table
          :headers="pipelineListData.headers"
          :items="filteritems"
          :item-key="id"
          :items-per-page="parseInt(perPage)"
          :page="currentPage"
          :expanded.sync="expanded"
          class="pipelineTable mt-3 text-center"
          show-expand
          :hide-default-footer="true"
          @item-expanded ="getcommandList"
        >
          <template v-slot:[`item.status`]="{ item }">
            <div class="activationStatusBox">
              <div class="acsTextBox">
                {{ item.status }}
              </div>
              <div class="acsBtnBox">
                <button v-if="item.status.toUpperCase() == 'FAILED'"
                  @click="
                    failedPipeline(
                      item.name,
                      item.status.toUpperCase(),
                      item.id
                    )
                  "
                  :disabled="item.status.toUpperCase().includes('ING')"
                  >
                  {{ showStatusBtn(item.status.toUpperCase()) }}
                </button>
                <button v-else
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
              v-if="item.status.toUpperCase() == 'STOPPED' ||  item.status.toUpperCase() == 'FAILED'"
              @click="deletePipeline(item)"
            >
              삭제
            </button>
            <button v-else @click="deletePipeline(item)">중지 후 삭제</button>
          </template>
          <template v-slot:[`item.pipelineUpdate`]="{ item }">
            <button @click="goPipelineDetailEdit(item)" :disabled="item.status.toUpperCase() == 'FAILED'">보기</button>
          </template>
          

          <!-- ## start dev DJ-->
          <template v-slot:expanded-item="{ headers }">
            <td :colspan="headers.length">
            <template>
              <v-app>
                  <div class="pipelineListBox">
                    <div class="pipelineListTitle">
                      <p class="text-base font-bold">Command History</p>
                      
                      <v-data-table
                        height="100%"
                        :headers="commandList.headers"
                        :items="commandDataList"
                        item-key="id"
                        :items-per-page="parseInt(commandperPage)"
                        :page="commandcurrentPage"
                        :expanded.sync="expanded"
                        class="pipelineTable mt-3 text-center"
                        :hide-default-footer="true"
                      >
                        <template v-slot:[`item.detail`]="{ item }">
                          <button @click="gettaskList(item.id)">
                            자세히
                          </button>
                        </template>                        
                      </v-data-table>
                      <div class="paginationBox">
                        <div class="firstPageBtnBox">
                          <button
                            class="v-pagination__navigation"
                            @click="commandfirstPage"
                            :disabled="commandcurrentPage == 1"
                          ></button>
                        </div>
                        <v-pagination
                          v-model="commandcurrentPage"
                          :length="commandtotalPage"
                          class="v-pagination__item"
                          color="#2B4F8C"
                        ></v-pagination>
                        <div class="lastPageBtnBox">
                          <button
                            class="v-pagination__navigation"
                            @click="commandlastPage"
                            :disabled="commandcurrentPage == commandtotalPage"
                          ></button>
                        </div>
                      </div>
                    </div>
                  </div>
                </v-app>
              </template>
              </td>
          </template>
          <!-- ## end dev DJ-->
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

        <div class="pipelineBtnBox mt-3">
          <button class="pipelineButton" @click="tempPipelineShows">
            임시저장 파이프라인
          </button>
          <button class="pipelineButton ml-3" @click="goPipelineRegister">
            파이프라인 등록
          </button>
        </div>
      </div>
    </div>
  </v-app>
</template>
<script>

import pipelineListService from "../../js/api/pipelineList";
import historyListService from "../../js/api/history";
import redirect from "../../js/api/redirect";
import EventBus from "@/eventBus/EventBus.js";
import pipelineListData from "../../json/pipelineList.json";
import tempPipeline from "../../json/tempPipeline.json";
import commandList from "../../json/command.json";

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
    this.pipelineFilter = "all";

    redirect.getNiFiURL().then((res) => {
        this.$store.state.redirectNiFiURL = res;
      })
      .catch((err) => {
        console.log("NiFi 주소를 받아오는데에 실패했습니다.", err);
      });
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
    commandtotalPage() { 
      return Math.floor(
        (this.commandDataList.length + parseInt(this.commandperPage) - 1) / this.commandperPage
      );
    },
  },
  data() {
    return {
      expanded: [],
      filteritems: [],
      commandDataList: [],
      taskDataList: [],
      commandList: commandList,
      pipelineFilterInput: "",
      pipelineFilter: null,
      perPage: 10,
      commandperPage: 5,
      currentPage: 1,
      commandcurrentPage: 1,
      total: 15,
      tempPipeline: tempPipeline,
      activationStatusList: [["전체",""], ["RUN","RUN"], ["STARTING","STARTING"], ["STOPPED","STOPPED"], ["STOPPING","STOPPING"]],
      pipelineListFilterList: [["전체","all"], ["파이프라인 이름","name"], ["적재Dataset","dataSet"]],
      searchValue: null,
      pipelineListData: pipelineListData,
      history: [],
      item: [],
    };
  },
  methods: {
    getcommandList({item}) {
      historyListService
      .getHistorytCmd(item.id)
      .then((res) => {
        this.$store.state.commandDataList = res;
        this.commandDataList = this.$store.state.commandDataList;
      })
      .catch((err) => err);
    },
    gettaskList(item) {
      historyListService
      .getHistorytask(item)
      .then((res) => {
        this.$store.state.taskDataList = res;
        EventBus.$emit("show-task-list-popup", res);
      })
      .catch((err) => err);

      
    },
    // API 사용법
    resetPage(){
      this.currentPage = 1;
    },
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
      else if(status == 'FAILED'){
        return '실패';
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
    commandfirstPage() {
      this.commandcurrentPage = 1;
    },
    commandlastPage() {
      this.commandcurrentPage = this.commandtotalPage;
    },    
    tempPipelineShows() {
      let alertPayload = {
        contents: this.tempPipeline,
      };
      EventBus.$emit("show-temp-pipeline-popup", alertPayload);
    },
    failedPipeline(name, status, id) {
      let alertPayload = {
        title: "파이프라인 동작 실패",
        text:
          name +
          " 파이프라인이 동작 실패하였습니다.<br/>" +
          "삭제 후 재등록 이후에도 실패시,<br/>" +
          "<b> " +
          "관리자에게 문의 바랍니다.",
        id: id,
        url: "update",
        body: status
      };
      EventBus.$emit("show-failed-confirm-popup", alertPayload);
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
        if(event.target.value == "all"){
          this.pipelineFilterInput = "";
        }
        this.pipelineFilter = event.target.value;
      }
    },
    actionFilter(){
      this.filteritems = this.$store.state.pipelineList;
      if(this.searchValue){
        if(this.pipelineFilter != "all"){
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
        if(this.pipelineFilter != "all"){
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
    },
    redirectNiFi() {
      window.open(this.$store.state.redirectNiFiURL,"_blank","")
    },
  },
};
</script>


<style scoped>
.paginationBox > .firstPageBtnBox button:disabled {
    width: 32px;
    height: 32px;
    margin-top: 9%;
    /* background-image: url(/img/keyboard_double_arrow_left_24dp.221c9647.svg); */
    /* background-image: url(../../img/keyboard_double_arrow_left_24dp.svg); */
    background-position: center;
    opacity: .6;
    pointer-events: none;
}

.paginationBox > .lastPageBtnBox button:disabled {
    width: 32px;
    height: 32px;
    margin-top: 9%;
    /* background-image: url(/img/keyboard_double_arrow_right_24dp.e2b0d809.svg); */
    /* background-image: url(../../img/keyboard_double_arrow_right_24dp.svg); */
    background-position: center;
    opacity: .6;
    pointer-events: none;
}
.paginationBox > .firstPageBtnBox button {
  width: 32px;
  height: 32px;
  margin-top: 9%;
  background-color: #ffffff;
  /* background-image: url(../../img/keyboard_double_arrow_left_24dp.svg); */
  background-position: center;
}
.paginationBox > .lastPageBtnBox button{
  width: 32px;
  height: 32px;
  margin-top: 9%;
  background-color: #ffffff;
  /* background-image: url(../../img/keyboard_double_arrow_right_24dp.svg); */
  background-position: center; 
}
.v-pagination__item {
    background: transparent;
    border-radius: 4px;
    font-size: 1rem;
    height: 34px !important;
    margin: 0.3rem;
    min-width: 34px;
    padding: 0 5px;
    text-decoration: none;
    transition: .3s cubic-bezier(0,0,.2,1);
    width: auto !important;
    box-shadow: 0 3px 1px -2px rgb(0 0 0 / 20%), 0 2px 2px 0 rgb(0 0 0 / 14%), 0 1px 5px 0 rgb(0 0 0 / 12%);
}
</style>