<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
    </div>
    <div style="margin: 3%">
      <default-info
        :convert-mode="devData.convertMode"
        :contents="devData.defaultInfo"
        :mode="devData.mode.defaultInfo"
      />
      <data-collect
        :convert-mode="devData.convertMode"
        :contents="devData.contents"
        :mode="devData.mode.collect"
      />
      <data-refine
        :convert-mode="devData.convertMode"
        :contents="devData.refine"
        :mode="devData.mode.refine"
      />
      <data-convert
        :mode="devData.mode.convert"
        :contents="devData.convert"
        :convert-mode="devData.convertMode"
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
import DataRefine from "../../components/pipeline/DataRefine.vue";
import DataConvert from "../../components/pipeline/DataConvert.vue";
import devData from "../../json/devData.json";
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataRefine,
  },
  data() {
    return {
      devData: devData,
    };
  },
  methods: {
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
