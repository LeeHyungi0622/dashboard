<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button :class="$store.state.showRegisterMode == 'info'? 'activationBtn':'navBoxBtn'" 
      @click="convertMode('info')"
      :disabled="!($store.state.showRegisterMode == 'info')">
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button :class="$store.state.showRegisterMode == 'collector'? 'activationBtn':'navBoxBtn'" 
      @click="convertMode('collector')"
      :disabled="!$store.state.showRegisterMode == 'collector'">
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button :class="$store.state.showRegisterMode == 'filter'? 'activationBtn':'navBoxBtn'" @click="convertMode('filter')"
      :disabled="$store.state.showRegisterMode == 'filter'">
        <div class="numberBox">3</div>
        데이터 정제
      </button>
      <button :class="$store.state.showRegisterMode == 'convertor'? 'activationBtn':'navBoxBtn'" @click="convertMode('convertor')"
      :disabled="$store.state.showRegisterMode == 'convertor'">
        <div class="numberBox">4</div>
        데이터 변환
      </button>
      <button :class="$store.state.showRegisterMode == 'complete'? 'activationBtn':'lastbtn'" @click="convertMode('complete')"
      :disabled="$store.state.showRegisterMode == 'complete'">
        <div class="numberBox">5</div>
        요약 및 등록완료
      </button>
    </div>
    <div style="margin: 3%">
        <default-info 
        v-if="$store.state.showRegisterMode == 'info'"/>
        <data-collect
        v-if="$store.state.showRegisterMode == 'collector'"
        :contents="contents"/>
        <data-filters
        v-if="$store.state.showRegisterMode == 'filter'"
        :contents="contents"/>
        <data-convert
        v-if="$store.state.showRegisterMode == 'convertor'"
        />
        <complete-pipe
        v-if="$store.state.showRegisterMode == 'complete'"
        />
    </div>
  </div>
</template>

<script>
import pipelineRegisterService from "../../js/api/pipelineRegister";
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
      title: "데이터 파이프라인 기본정보",
      showModule: "",
    };
  },
  created() {
    this.$store.state.showRegisterMode = "info";
    if (this.$store.state.registerPipeline.id) {
      pipelineRegisterService
      .getPipelineDraft(this.$store.state.registerPipeline.id)
      .then((res) => {
        this.$store.state.registerPipeline = res;
      })
      .catch((err) =>
      console.error("임시저장 Pipeline 조회에 실패했습니다.", err)
      );
    } else {
      pipelineRegisterService
      .getPipelineVo()
      .then((res) => {
        this.$store.state.registerPipeline = res;
      })
      .catch((err) => {
        console.error("PipelinVo 조회에 실패했습니다.", err);
      });
    }
  },
  methods: {
    convertMode(val) {
      this.$store.state.showRegisterMode = val;
    },
  },
};
</script>
