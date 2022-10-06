<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
    </div>
    <div style="margin: 3%">
      <default-info :contents="defaultInfoContents()"/>
      <data-collect
        v-if="$store.state.pipelineVo['collector']"
        :contents="getContents('collector')"
      />
      <data-filters
        v-if="$store.state.pipelineVo['filter']"
        :contents="getContents('filter')"
      />
      <data-convert
        v-if="$store.state.pipelineVo['converter']"
        :contents="getContents('converter')"
        @selected-data-set="selectedDataSetFunction"
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
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataFilters,
  },
  computed: {
    updatePipeline(){
      return this.$store.state.pipelineVo;
    }
  },
  data() {
    return {
      // updatePipeline: {},
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

    goPipelineList() {
      this.$router.push({
        name: "pipelineList",
      });
    },

    defaultInfoContents() {
      return [
        {
          name: "파이프라인 이름",
          inputValue: this.updatePipeline.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.updatePipeline.detail,
        },
      ];
    },
  },
};
</script>
