<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정제</div>
      <button
        v-if="!mode == `REGISTER`"
        class="pipelineUpdateButton"
        @click="convertMode('refine')"
      >
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div v-for="(item, key) in contents" :key="key">
      <custom-table :contents="item.requiredProps" :mode="mode" />
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
