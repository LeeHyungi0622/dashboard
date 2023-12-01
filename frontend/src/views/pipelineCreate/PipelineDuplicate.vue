<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button :class="$store.state.showRegisterMode == 'info'? 'activationBtn':'navBoxBtn'" 
      disabled>
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button :class="$store.state.showRegisterMode == 'collector'? 'activationBtn':'navBoxBtn'" 
      disabled>
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button :class="$store.state.showRegisterMode == 'filter'? 'activationBtn':'navBoxBtn'" 
      disabled>
        <div class="numberBox">3</div>
        데이터 정제
      </button>
      <button :class="$store.state.showRegisterMode == 'convertor'? 'activationBtn':'navBoxBtn'" 
      disabled>
        <div class="numberBox">4</div>
        데이터 변환
      </button>
      <button :class="$store.state.showRegisterMode == 'complete'? 'activationBtn':'lastbtn'" 
      disabled>
        <div class="numberBox">5</div>
        요약 및 등록완료
      </button>
    </div>

    <div style="margin: 3%">
      <div class="pipelineBtnBox mgT12">
        <button class="pipelineButton" @click="tempPipelineShows">
          임시저장 파이프라인 목록
        </button>
        <button class="pipelineButton mgL12" @click="goPipelineList">
          파이프라인 목록
        </button>
      </div>
        <default-info :item-id="this.itemId"
        v-if="$store.state.showRegisterMode == 'info'"/>
        <data-collect
        v-if="$store.state.showRegisterMode == 'collector'"/>
        <data-filters
        v-if="$store.state.showRegisterMode == 'filter'"/>
        <data-convert
        v-if="$store.state.showRegisterMode == 'convertor'"/>
        <complete-pipe
        v-if="$store.state.showRegisterMode == 'complete'"/>
    </div>

  </div>
</template>

<script>
import DefaultInfo from "../../components/pipeline/DefaultInfo.vue";
import DataCollect from "../../components/pipeline/DataCollect.vue";
import DataFilters from "../../components/pipeline/DataFilters.vue";
import DataConvert from "../../components/pipeline/DataConvert.vue";
import CompletePipe from "../../components/pipeline/CompletePipe.vue";
import EventBus from "@/eventBus/EventBus.js";
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataFilters,
    CompletePipe,
  },
  data() {
    return {
      overlay: false,
    };
  },
  props: {
    itemId: String
  },
  mounted() {
    
    this.$store.state.showRegisterMode = "info";
    this.$store.state.tableUpdateFlag = false;
    this.$store.state.infoTableUpdateFlag = true;
    this.$store.state.collectorTableUpdateFlag = true;
    this.$store.state.filterTableUpdateFlag = true;
    this.$store.state.convertorTableUpdateFlag = true;

  },
  methods: {
    convertMode(val) {
      this.$store.state.showRegisterMode = val;
    },
    goPipelineList(){
      this.$router.push({
          name: "pipelineList"
        });
    },
    tempPipelineShows() {
      let alertPayload = {
        contents: this.$store.state.tempPipeline,
      };
      EventBus.$emit("show-temp-pipeline-popup", alertPayload);
    },
    showOverlay(val){
      this.overlay = val;
    }
  },
};
</script>
