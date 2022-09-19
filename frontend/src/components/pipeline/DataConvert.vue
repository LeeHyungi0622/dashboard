<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 변환</div>
      <button class="pipelineUpdateButton" @click="convertMode('convert')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents" :mode="mode" />
    <v-data-table
      :headers="convertHeaders"
      :items="convertData"
      class="pipelineUpdateConvertVFT"
      :hide-default-footer="true"
      style="text-align: center"
      ><template v-slot:[`item.value`]="{ item }">
        <input v-if="mode == 'UPDATE'" v-model="item.value" />
        <div style="padding-left: 10px" v-else>{{ item.value }}</div>
      </template>
    </v-data-table>
    <div class="pipelineUpdateSubTitle fsb14">ID 생성 규칙 설정</div>
    <div class="customTableBox fsb12">{{ generationKey }}</div>

    <v-data-table
      :headers="IdHeaders"
      :items="IdData"
      class="pipelineUpdateIdVFT"
      :hide-default-footer="true"
      style="text-align: center"
    >
      <template v-slot:[`item.value`]="{ item }">
        <input v-if="mode == 'UPDATE'" v-model="item.value" />
        <div style="padding-left: 10px" v-else>{{ item.value }}</div>
      </template>
    </v-data-table>
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
export default {
  components: {
    CustomTable,
  },
  computed: {
    generationKey() {
      return "Generated key : urn:datahub:${datamodel ID}:${Location}:${coordinates}";
    },
  },
  props: {
    mode: String,
    contents: Array,
    convertHeaders: Array,
    convertData: Array,
    IdHeaders: Array,
    IdData: Array,
    convertMode: Function,
  },
};
</script>
