<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
    </div>
    <div style="margin: 3%">
      <default-info
        :contents="defaultInfoContents()"
        :convert-mode="convertMode"
        :mode="mode.defaultInfo"
      />
      <data-collect
        v-if="$store.state.pipelineVo['collector']"
        :contents="getContents('collector')"
        :convert-mode="convertMode"
        :mode="mode.collect"
      />
      <data-filters
        v-if="$store.state.pipelineVo['filter']"
        :contents="getContents('filter')"
        :convert-mode="convertMode"
        :mode="mode.refine"
      />
      <data-convert
        v-if="$store.state.pipelineVo['converter']"
        :contents="getContents('converter')"
        :convert-mode="convertMode"
        @selected-data-set="selectedDataSetFunction"
        :mode="mode.convert"
      />
      <div class="pipelineUpdateBtnBox mgT12">
        <button class="pipelineUpdateButton">목록으로</button>
        <button class="pipelineUpdateButton mgL12">저장</button>
      </div>
    </div>
  </div>
</template>
<script>
import DefaultInfo from "../../components/pipeline/DefaultInfo.vue";
import DataCollect from "../../components/pipeline/DataCollect.vue";
import DataFilters from "../../components/pipeline/DataFilters.vue";
import DataConvert from "../../components/pipeline/DataConvert.vue";
import pipelineUpdateService from "../../js/api/pipelineUpdate";
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataFilters,
  },
  created() {
    pipelineUpdateService
      .getPipelineListById(this.$route.query.id)
      .then((res) => {
        this.$store.state.pipelineVo = res;
      })
      .catch((err) => {
        console.log("PipelinListById 조회에 실패했습니다.", err);
      });
  },
  data() {
    return {
      selectedDataSet: "",
      mode: {
        defaultInfo: "",
        collect: "",
        refine: "",
        convert: "",
      },
    };
  },
  methods: {
    selectedDataSetFunction(value) {
      this.selectedDataSet = value;
    },
    getContents(contentsName) {
      return this.$store.state.pipelineVo[contentsName].NifiComponents;
    },

    defaultInfoContents() {
      return [
        {
          name: "파이프라인 이름",
          inputValue: this.$store.state.pipelineVo.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.$store.state.pipelineVo.detail,
        },
      ];
    },

    convertMode(item) {
      switch (item) {
        case "defaultInfo":
          if (this.mode.defaultInfo == "UPDATE") {
            this.mode.defaultInfo = "";
          } else {
            this.mode.defaultInfo = "UPDATE";
          }
          break;
        case "collect":
          if (this.mode.collect == "UPDATE") {
            this.mode.collect = "";
          } else {
            this.mode.collect = "UPDATE";
          }
          break;
        case "refine":
          if (this.mode.refine == "UPDATE") {
            this.mode.refine = "";
          } else {
            this.mode.refine = "UPDATE";
          }
          break;
        case "convert":
          if (this.mode.convert == "UPDATE") {
            this.mode.convert = "";
          } else {
            this.mode.convert = "UPDATE";
          }
          break;
        default:
          break;
      }
    },
  },
};
</script>
