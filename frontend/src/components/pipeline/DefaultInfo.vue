<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 기본정보</div>
      <button
        v-if="mode == `UPDATE`"
        class="pipelineUpdateButton"
        @click="convertMode('defaultInfo')"
      >
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents" :mode="mode" />
    <div
      v-if="mode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton">이전</button>
      <button class="pipelineButton mgL12">임시 저장</button>
      <button class="pipelineButton mgL12" @click="nextRoute()">다음</button>
    </div>
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
export default {
  components: {
    CustomTable,
  },
  watch: {
    contents: {},
  },
  props: {
    mode: String,
    contents: Array,
    convertMode: Function,
  },
};
</script>
