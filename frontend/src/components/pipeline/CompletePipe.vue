<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 요약</div>
    </div>
    <div class="pipelineUpdateSubTitle fsb14">데이터 파이프라인 기본 정보
    </div>
    <custom-table :contents="infoContents" />
    <div class="pipelineUpdateSubTitle fsb14">데이터 수집 정보
    </div>
    <custom-table :contents="collectorContents" />
    <div class="pipelineUpdateSubTitle fsb14">데이터 정제 정보
    </div>
    <custom-table :contents="filterContents" />
    <div class="pipelineUpdateSubTitle fsb14">데이터 변환 정보
    </div>
    <custom-table :contents="converterContents" />
    <div
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton mgL12">이전</button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
      >
        등록완료
      </button>
    </div>
  </div>
</template>

<script>
import CustomTable from "./CustomTable.vue";
export default {
  components: {
    CustomTable
  },
  props: {},
  data: () => ({
    infoContents: [
        {
          name: "파이프라인 이름",
          inputValue: this.$store.state.registerPipeline.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.$store.state.registerPipeline.detail,
        },
      ],
      collectorContents: [
        {
          name: "데이터 수집",
          inputValue: this.$store.state.registerPipeline.collector.name,
        }
      ],
      filterContents: [
        {
          name: "Base64 Decoder",
          inputValue: this.$store.state.registerPipeline.name,
        },
        {
          name: "Message Root",
          inputValue: this.$store.state.registerPipeline.detail,
        },
      ],
      converterContents: [
        {
          name: "DataSet",
          inputValue: this.$store.state.registerPipeline.dataSet,
        },
        {
          name: "생성된 ID Key",
          inputValue: this.$store.state.registerPipeId,
        },
      ],
  }),
  watch: {},
  created() {},
  mounted() {},
  methods: {},
};
</script>

<style scoped></style>
