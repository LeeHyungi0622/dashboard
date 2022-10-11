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
    };
  },
  props: {
    itemId: String
  },
  mounted() {
    this.$store.state.tableShowMode = "REGISTER";
    this.$store.state.showRegisterMode = "info";
    this.$store.state.tableUpdateFlag = false;
    this.$store.state.infoTableUpdateFlag = true;
    this.$store.state.collectorTableUpdateFlag = true;
    this.$store.state.filterTableUpdateFlag = true;
    this.$store.state.convertorTableUpdateFlag = true;

    console.log(this.$route.params)
  },
  methods: {
    convertMode(val) {
      this.$store.state.showRegisterMode = val;
    },
  },
};
</script>
