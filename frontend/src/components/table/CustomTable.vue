<template>
  <v-app>
    <div class="pipelineListBox">
      <div class="pipelineListTitle">
        <p class="fsb16">파이프라인 목록</p>
        <div class="searchBox">
          <div class="activationFilter">
            <p>동작</p>
            <select>
              <option
                v-for="(item, index) in activationStatusList"
                :key="index"
              >
                {{ item }}
              </option>
            </select>
          </div>
          <div class="search">
            <select style="width: 10%">
              <option
                v-for="(item, index) in activationStatusList"
                :key="index"
              >
                {{ item }}
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
          :headers="headers"
          :items="data"
          :items-per-page="perPage"
          :page="currentPage"
          item-key="name"
          class="elevation-1 mgT12"
          :hide-default-footer="true"
          style="text-align: center"
        >
          <template v-slot:[`item.activationStatus`]="{ item }">
            <div class="activationStatusBox">
              <div class="acsTextBox">
                {{ item.activationStatus }}
              </div>
              <div class="acsBtnBox">
                <button
                  @click="
                    pipelineStatusAlertShows(item.name, item.activationStatus)
                  "
                  :disabled="
                    item.activationStatus == 'STARTING' ||
                    item.activationStatus == 'STOPPING'
                  "
                >
                  {{ item.activationStatus }}
                </button>
              </div>
            </div>
          </template>
          <template v-slot:[`item.delete`]="{ item }">
            <button
              v-if="item.activationStatus == 'STOPPED'"
              @click="deletePipeline(item)"
            >
              삭제
            </button>
            <button v-else @click="deletePipeline(item)">중지 후 삭제</button>
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
          <button class="pipelineButton">임시저장 파이프라인</button>
          <button class="pipelineButton mgL12">파이프라인 등록</button>
        </div>
      </div>
    </div>
  </v-app>
</template>
<script>
import EventBus from "@/eventBus/EventBus.js";
export default {
  computed: {
    totalPage() {
      return Math.floor((this.total + parseInt(this.perPage)) / this.perPage);
    },
  },
  data() {
    return {
      perPage: 10,
      currentPage: 1,
      total: 15,
      activationStatusList: [
        "전체",
        "RUNNING",
        "STARTING",
        "STOPPING",
        "STOPPED",
      ],

      headers: [
        { text: "NO", value: "NO", sortable: false },
        { text: "파이프라인 이름", value: "name", sortable: false },
        { text: "파이프라인 정의", value: "desc", sortable: false },
        { text: "적재 DataSet", value: "dataSet", sortable: false },
        {
          text: "파이프라인 동작 상태",
          value: "activationStatus",
          sortable: false,
        },
        { text: "등록 일시", value: "createDateTime", sortable: false },
        { text: "수정 일시", value: "editDateTime", sortable: false },
        { text: "삭제", value: "delete", sortable: false },
      ],
      data: [
        {
          NO: "1",
          name: "Mobius Pipeline",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "2",
          name: "Mobius Pipeline2",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "STOPPING",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "3",
          name: "Mobius Pipeline3",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "STOPPED",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "4",
          name: "Mobius Pipeline4",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "6",
          name: "Mobius Pipeline6",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "7",
          name: "Mobius Pipeline7",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "8",
          name: "Mobius Pipeline8",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "9",
          name: "Mobius Pipeline9",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "10",
          name: "Mobius Pipeline10",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "11",
          name: "Mobius Pipeline11",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "12",
          name: "Mobius Pipeline12",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "13",
          name: "Mobius Pipeline13",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "14",
          name: "Mobius Pipeline14",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "15",
          name: "Mobius Pipeline15",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
        {
          NO: "16",
          name: "Mobius Pipeline16",
          desc: "Mobius 사업용",
          dataSet: "PakingSensorStatus mobius",
          activationStatus: "RUN",
          createDateTime: "2022-07-28T00:00:00",
          editDateTime: "2022-07-28T00:00:00",
          delete: "삭제",
        },
      ],
    };
  },
  methods: {
    firstPage() {
      this.currentPage = 1;
    },
    lastPage() {
      this.currentPage = this.totalPage;
    },
    pipelineStatusAlertShows(name, status) {
      let alertPayload = {
        title: "파이프라인 삭제",
        text:
          name +
          " 파이프라인이 " +
          status +
          "되어 <br/>실행 상태가 " +
          status +
          "로 변경됩니다." +
          "<br/> <br/> 계속 진행하시겠습니까?",
        url: "/test",
        param: "test=test",
        body: "default",
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
        url: "/test",
        param: "test=test",
        body: "default",
      };
      console.log(item);
      EventBus.$emit("show-confirm-popup", alertPayload);
    },
  },
};
</script>
