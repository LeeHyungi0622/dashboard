<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="1400">
      <v-card>
        <v-card-title class="lighten-2 fsb14" style="color: #2b4f8c">
          {{ contents.title }}
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
          <div class="search" style="height: 30px">
            <select style="width: 10%" @change="settingFilter('pipelineFilter', $event)">
                <option
                  v-for="([title, val], index) in pipelineListFilterList"
                  :key="index"
                  :value="val"
                >
                  {{ title }}
                </option>
              </select>
            <input type="text" class="mgL12" v-model="pipelineFilterInput"/>
            <button class="mgL12" @click="actionFilter()">검색</button>
            <select name="" id="" class="mgL12" v-model="perPage">
              <option value="5">5개씩 표시</option>
              <option value="10">10개씩 표시</option>
            </select>
          </div>
          <v-data-table
            :headers="headers"
            :items="vuetifyData"
            :items-per-page="parseInt(perPage)"
            :page="currentPage"
            item-key="id"
            class="pipelineTable mgT12"
            :search="searchValue"
            :hide-default-footer="true"
            style="text-align: center; width: 100%"
          >
            <template v-slot:[`item.readAction`]="{ item }">
              <button @click="goPipelineRegister(item)">보기</button>
            </template>
            <template v-slot:[`item.deleteAction`]="{ item }">
              <button @click="deleteTempPipeline(item)">삭제</button>
            </template>
            <template v-slot:[`item.isCollector`]="{ item }">
              <img :src="imgSrc[getImg(item.isCollector)]" />
            </template>
            <template v-slot:[`item.isFilter`]="{ item }">
              <img :src="imgSrc[getImg(item.isFilter)]" />
            </template>
            <template v-slot:[`item.isConverter`]="{ item }">
              <img :src="imgSrc[getImg(item.isConverter)]" />
            </template>
          </v-data-table>
          <div>
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
          </div>
        </v-card-text>

        <v-card-actions style="display: flex; justify-content: center">
          <button
            style="width: 15%; padding: 3px"
            class="fs12"
            @click="goPipelineRegister(`default`)"
          >
            파이프라인 새로 만들기
          </button>
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
import tempPipelineListService from "../../js/api/tempPipelineList";
import EventBus from "@/eventBus/EventBus.js";
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
  created() {
    tempPipelineListService
      .getTempPipelineList()
      .then((res) => {
        this.$store.state.tempPipelineList = res;
        this.vuetifyData = res;
      })
      .catch((error) => error);
  },
  computed: {
    totalPage() {
      return Math.floor(
        (this.vuetifyData.length + parseInt(this.perPage)) / this.perPage
      );
    },
  },
  data() {
    return {
      headers: [
        { text: "NO", value: "id", sortable: false },
        { text: "파이프라인 이름", value: "name", sortable: false },
        { text: "최종 수정일시", value: "modifiedAt", sortable: false },
        { text: "데이터 수집 설정", value: "isCollector", sortable: false },
        { text: "데이터 정제 설정", value: "isFilter", sortable: false },
        { text: "데이터 변환 설정", value: "isConverter", sortable: false },
        { text: "불러오기", value: "readAction", sortable: false },
        { text: "삭제", value: "deleteAction", sortable: false },
      ],
      pipelineListFilterList: [["전체",""], ["파이프라인 이름","name"]],
      pipelineFilterInput: "",
      pipelineFilter: null,
      searchValue: "",
      selectedFilter: "",
      dialog: true,
      perPage: 5,
      currentPage: 1,
      vuetifyData: [],
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
      this.$emit("close-temp-pipeline-popup");
    },
    firstPage() {
      this.currentPage = 1;
    },
    lastPage() {
      this.currentPage = this.totalPage;
    },
    deleteTempPipeline(item) {
      let alertPayload = {
        title: "파이프라인 삭제",
        text:
          item.name +
          " 파이프라인이 삭제됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
        url: "deleteTemp",
        id: item.id
      };
      EventBus.$emit("show-confirm-popup", alertPayload);
    },
    goPipelineRegister(item) {
      if (item == `default`) {
        this.$store.state.pipelineVo = [],
        this.$router.push({
          name: "pipelineRegister",
        });
      } else if (this.$route.name != "pipelineRegister") {
        this.$store.state.pipelineVo.id = item.id;
        this.$router.push({
          name: "pipelineRegister",
        });
      }
      this.close();
    },
    settingFilter(filter, event) {
      if(filter == "status"){
        this.selectedFilter = filter;
        this.searchValue = event.target.value;
      }
      else{
        this.pipelineFilter = event.target.value;
      }
    },
    actionFilter(){
      this.vuetifyData = this.$store.state.tempPipelineList;
        if(this.pipelineFilter){
          this.vuetifyData = this.$store.state.tempPipelineList.filter((i) => {
          return (
            i[this.pipelineFilter].includes(this.pipelineFilterInput) 
          );
        });
        }
        else{
          this.vuetifyData = this.$store.state.tempPipelineList;
        }
    }
  },
};
</script>
