<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정제</div>
      <button class="pipelineUpdateButton" @click="convertMode('refine')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div v-for="(item, key) in contents" :key="key">
      <custom-table :contents="item.requiredProps" :mode="mode" />
    </div>
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
import CustomTable from "./CustomTable.vue";

export default {
  components: {
    CustomTable,
  },

  props: {
    convertMode: Function,
    contents: Array,
    mode: String,
  },
};
</script>
