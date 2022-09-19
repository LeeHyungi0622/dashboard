<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정재</div>
      <button class="pipelineUpdateButton" @click="convertMode('refine')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="refine" :mode="mode" />
  </div>
</template>
<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
export default {
  components: {
    CustomTable,
  },
  props: {
    convertMode: Function,
    refine: Array,
    mode: String,
  },
};
</script>
