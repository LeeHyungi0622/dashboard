<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
    </div>
    <div style="margin: 3%">
      <default-info/>
      <data-collect
        v-if="$store.state.completedPipeline['collector']"
        :contents="getContents('collector')"
      />
      <data-filters
        :contents="getContents('filter')"
      />
      <data-convert
        v-if="$store.state.completedPipeline['converter']"
        :contents="getContents('converter')"
      />
      <div class="pipelineUpdateBtnBox mgT12">
        <button class="pipelineUpdateButton" @click="goPipelineList()">
          목록으로
        </button>
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
  mounted(){
    pipelineUpdateService
      .getPipelineListById(this.$store.state.completedPlId)
      .then((res) => {
        this.$store.state.completedPipeline = res;
        this.$store.state.tableShowMode = "UPDATE";
      })
      .catch((err) => {
        console.log("PipelinListById 조회에 실패했습니다.", err);
      });
  },
  data() {
    return {
      selectedDataSet: "",
    };
  },
  methods: {
    selectedDataSetFunction(value) {
      this.selectedDataSet = value;
    },
    getContents(contentsName) {
      return this.$store.state.completedPipeline[contentsName].NifiComponents;
    },

    goPipelineList() {
      this.$router.push({
        name: "pipelineList",
      });
    },
  },
};
</script>
