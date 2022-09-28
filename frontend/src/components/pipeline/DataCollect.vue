<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 수집</div>
      <button class="pipelineUpdateButton" @click="convertMode('collect')">
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="customTableMainArea">
      <div class="customTable">
        <div class="header fsb12">
          <p>데이터 수집기</p>
        </div>
        <div class="value">
          <div>
            <select
              style="padding: 0px 20px 0px 20px"
              v-model="selectedCollectValue"
            >
              <option
                v-for="(item, key) in collectorContents.value.datas"
                :key="key"
                :value="item"
              >
                {{ item.nifi_name }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="customTableMainArea">
      <div class="customTable">
        <div class="header fsb12">
          <p>수집 설정 목록</p>
        </div>
        <div class="value">
          <div>
            <select
              style="padding: 0px 20px 0px 20px"
              v-model="selectedSettingValue"
            >
              <option v-for="(item, key) in contents" :key="key" :value="item">
                {{ item.name }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="pipelineUpdateSubTitle fsb14">필수 설정 값</div>
    <custom-table :contents="selectedSettingValue.requiredProps" :mode="mode" />
    <div class="pipelineUpdateSubTitle fsb14">선택 설정 값</div>
    <custom-table :contents="selectedSettingValue.optionalProps" :mode="mode" />
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
import collector from "../../json/collector.json";
export default {
  components: {
    CustomTable,
  },
  data() {
    return {
      collectorContents: {
        key: "데이터 수집기",
        value: {
          selectedValue: "",
          datas: collector,
        },
      },
      selectedCollectValue: {},
      selectedSettingValue: {},
    };
  },
  props: {
    mode: String,
    contents: Array,
    convertMode: Function,
  },
  methods: {},
};
</script>
