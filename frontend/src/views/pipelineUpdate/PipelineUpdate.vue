<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
      <default-info
        :convert-mode="convertMode"
        :contents="defaultInfoContents"
        :mode="mode.defaultInfo"
      />
      <data-collect
        :convert-mode="convertMode"
        :data-collect="dataCollect.collector"
        :required-setting-value="dataCollect.requiredSettingValue"
        :selected-setting-value="dataCollect.selectedSettingValue"
        :mode="mode.collect"
      />
      <data-refine
        :convert-mode="convertMode"
        :refine="refine"
        :mode="mode.refine"
      />
      <data-convert
        :mode="mode.convert"
        :contents="ConvertContents"
        :convert-headers="convertHeaders"
        :convert-data="convertData"
        :convert-mode="convertMode"
        :id-headers="IdHeaders"
        :id-data="IdData"
      />
      <div class="pipelineUpdateBtnBox mgT12">
        <button class="pipelineUpdateButton">목록으로</button>
        <button class="pipelineUpdateButton mgL12">저장</button>
      </div>
    </div>
  </div>
</template>
<script>
import DefaultInfo from "../../components/pipeline/DefaultInfo.vue";
import DataCollect from "../../components/pipeline/DataCollect.vue";
import DataRefine from "../../components/pipeline/DataRefine.vue";
import DataConvert from "../../components/pipeline/DataConvert.vue";
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataRefine,
  },
  data() {
    return {
      convertHeaders: [
        {
          text: "DataSet 속성 (속성 유형)",
          sortable: false,
          value: "name",
        },
        { text: "Raw Data 속성", value: "value", sortable: false },
      ],
      convertData: [
        {
          name: "location (GeoProperty)",
          value: "Position, Site, Place",
        },
        {
          name: "ParkingAreaId (Property)",
        },
        {
          name: "ParkingLotId (Property)",
        },
        {
          name: "ParkingSensorId (Property)",
        },
      ],
      IdHeaders: [
        {
          text: "Depth",
          sortable: false,
          value: "name",
        },
        { text: "ID Key", value: "value", sortable: false },
      ],
      IdData: [
        {
          name: "Level 1",
          value: "Location",
        },
        {
          name: "Level 2",
          value: "coordinates",
        },
        {
          name: "Level 3",
        },
      ],
      mode: {
        defaultInfo: null,
        collect: null,
        refine: null,
        convert: null,
      },
      defaultInfoContents: [
        { key: "파이프라인 이름", value: "Parking Lot Adaptor" },
        {
          key: "파이프라인 정의",
          value: "산탄데르 주차장 센서 데이터용",
        },
      ],
      dataCollect: {
        collector: [{ key: "데이터 수집기", value: "HTTP Listener" }],
        requiredSettingValue: [
          {
            key: "Base Path",
            desc: "a",
            value: "ContentListener",
          },
          {
            key: "Listerning Port",
            desc: "a",
            value: "",
          },
          {
            key: "Base Path",
            desc: "a",
            value: "",
          },
          {
            key: "Authorized Subject DN Pattern",
            desc: "a",
            value: ".*",
          },
          {
            key: "Max Uncomfirmed Profile Time",
            desc: "a",
            value: "60 Secs",
          },
          {
            key: "Multipart Request Max Size",
            desc: "a",
            value: "1 MB",
          },
        ],
        selectedSettingValue: [
          {
            key: "Listening Port for Health Check Requests",
            desc: "a",
            value: "",
          },
          {
            key: "Max Data to Receive Per Second",
            desc: "a",
            value: "",
          },
          {
            key: "SSL Context Service",
            desc: "a",
            value: "",
          },
          {
            key: "Autorized Issure DN Pattern",
            desc: "a",
            value: ".*",
          },
          {
            key: "HTTP Headers to receive as Attributes (Regex)",
            desc: "a",
            value: "",
          },
        ],
      },
      refine: [
        {
          key: "Base64 Decoder",
          value: "On",
        },
        {
          key: "Root Key",
          value: " ParkingLot1 : Location ",
        },
      ],
      ConvertContents: [
        {
          key: "DataSet",
          value: "ParkingLot1 : Location",
        },
      ],
    };
  },
  methods: {
    convertMode(item) {
      switch (item) {
        case "defaultInfo":
          if (this.mode.defaultInfo == "UPDATE") {
            this.mode.defaultInfo = "";
          } else {
            this.mode.defaultInfo = "UPDATE";
          }
          break;
        case "collect":
          if (this.mode.collect == "UPDATE") {
            this.mode.collect = "";
          } else {
            this.mode.collect = "UPDATE";
          }
          break;
        case "refine":
          if (this.mode.refine == "UPDATE") {
            this.mode.refine = "";
          } else {
            this.mode.refine = "UPDATE";
          }
          break;
        case "convert":
          if (this.mode.convert == "UPDATE") {
            this.mode.convert = "";
          } else {
            this.mode.convert = "UPDATE";
          }
          break;
        default:
          break;
      }
    },
  },
};
</script>
