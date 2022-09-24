<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 수집</div>
      <button
        v-if="mode == 'UPDATE'"
        class="pipelineUpdateButton"
        @click="convertMode('collect')"
      >
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents.collector" :mode="mode" />
    <div class="pipelineUpdateSubTitle fsb14">필수 설정 값</div>
    <custom-table :contents="contents.requiredSettingValue" :mode="mode" />
    <div class="pipelineUpdateSubTitle fsb14">선택 설정 값</div>
    <custom-table :contents="contents.selectedSettingValue" :mode="mode" />
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
