<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">
        데이터 파이프라인 기본 정보
      </div>
      <button class="pipelineUpdateButton" @click="convertMode('defaultInfo')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents" :mode="mode" />
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
export default {
  components: {
    CustomTable,
  },
  props: {
    mode: String,
    contents: Array,
    convertMode: Function,
  },
};
</script>
