<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 변환</div>
      <button class="pipelineUpdateButton" @click="convertMode('convert')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="customTableMainArea">
      <div class="customTable">
        <div class="header fsb12">
          <p>dataSet</p>
        </div>
        <div class="value">
          <div>
            <select
              style="padding: 0px 20px 0px 20px"
              @change="selectedDataSet($event)"
            >
              <option
                v-for="(item, key) in dataSetList.dataSetId"
                :key="key"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <v-data-table
      :headers="convertHeaders"
      :items="getVuetifyContents('DataSetProperties')[0].requiredProps"
      class="pipelineUpdateConvertVFT"
      :hide-default-footer="true"
      style="text-align: center"
      ><template v-slot:[`item.inputValue`]="{ item }">
        <input
          v-if="mode == 'UPDATE' || mode == 'REGISTER'"
          v-model="item.inputValue"
        />
        <div style="padding-left: 10px" v-else>{{ item.inputValue }}</div>
      </template>
    </v-data-table>
    <div class="pipelineUpdateSubTitle fsb14">ID 생성 규칙 설정</div>
    <div class="customTableBox fsb12">{{ generationKey }}</div>

    <v-data-table
      :headers="IdHeaders"
      :items="getVuetifyContents('IDGenerater')[0].requiredProps"
      class="pipelineUpdateIdVFT"
      :hide-default-footer="true"
      style="text-align: center"
    >
      <template v-slot:[`item.inputValue`]="{ item }">
        <input
          v-if="mode == 'UPDATE' || mode == 'REGISTER'"
          v-model="item.inputValue"
        />
        <div style="padding-left: 10px" v-else>{{ item.inputValue }}</div>
      </template>
    </v-data-table>
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
import dataSetList from "../../json/dataSetList.json";
export default {
  computed: {
    generationKey() {
      let last = "";
      this.getVuetifyContents("IDGenerater")[0].requiredProps.forEach(
        (item) => (last += "${" + item.inputValue + "}")
      );

      return "Generated key : urn:datahub:${datamodel ID}" + last;
    },
  },
  data() {
    return {
      convertHeaders: [
        {
          text: "DataSet 속성 (속성 유형)",
          sortable: false,
          value: "name",
        },
        {
          text: "Raw Data 속성",
          value: "inputValue",
          sortable: false,
        },
      ],
      IdHeaders: [
        {
          text: "Depth",
          sortable: false,
          value: "name",
        },
        { text: "ID Key", value: "inputValue", sortable: false },
      ],
      dataSetList: dataSetList,
    };
  },
  props: {
    mode: String,
    contents: Array,
    convertMode: Function,
  },
  methods: {
    selectedDataSet(event) {
      this.$emit("selected-data-set", event.target.value);
    },
    getVuetifyContents(propertyName) {
      return this.contents.filter((item) => item.name == propertyName);
    },
  },
};
</script>
