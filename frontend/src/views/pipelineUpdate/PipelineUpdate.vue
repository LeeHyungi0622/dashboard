<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
      <div style="width: 95%; margin-top: 5%">
        <div
          style="justify-content: space-between; display: flex"
          class="fsb14"
        >
          <div style="color: #2b4f8c">데이터 파이프라인 기본 정보</div>
          <button class="pipelineUpdateButton" @click="editMode('defaultInfo')">
            {{ mode.defaultInfo == "UPDATE" ? "수정완료" : "수정" }}
          </button>
        </div>
        <custom-table
          :contents="defaultInfoContents"
          :mode="mode.defaultInfo"
        />
      </div>
      <div style="width: 95%; margin-top: 5%">
        <div style="justify-content: space-between; display: flex">
          <div class="pipelineUpdateMainTitle fsb14">수정</div>
          <button class="pipelineUpdateButton" @click="editMode('collect')">
            {{ mode.collect == "UPDATE" ? "수정완료" : "수정" }}
          </button>
        </div>
        <custom-table :contents="dataCollect" :mode="mode.collect" />
        <div class="pipelineUpdateSubTitle fsb14">필수 설정 값</div>
        <custom-table :contents="requiredSettingValue" :mode="mode.collect" />
        <div class="pipelineUpdateSubTitle fsb14">선택 설정 값</div>
        <custom-table :contents="selectedSettingValue" :mode="mode.collect" />
      </div>
      <div style="width: 95%; margin-top: 5%">
        <div style="justify-content: space-between; display: flex">
          <div class="pipelineUpdateSubTitle fsb14">데이터 정재</div>
          <button class="pipelineUpdateButton" @click="editMode('refine')">
            {{ mode.refine == "UPDATE" ? "수정완료" : "수정" }}
          </button>
        </div>
        <custom-table :contents="refine" :mode="mode.refine" />
      </div>
      <div style="width: 95%; margin-top: 5%">
        <div style="justify-content: space-between; display: flex">
          <div class="pipelineUpdateMainTitle fsb14">데이터 변환</div>
          <button class="pipelineUpdateButton" @click="editMode('convert')">
            {{ mode.convert == "UPDATE" ? "수정완료" : "수정" }}
          </button>
        </div>
        <custom-table :contents="dataConvert" :mode="mode.convert" />
        <v-data-table
          :headers="convertHeaders"
          :items="convertData"
          class="pipelineUpdateConvertVFT"
          :hide-default-footer="true"
          style="text-align: center"
          ><template v-slot:[`item.value`]="{ item }">
            <input v-if="mode.convert == 'UPDATE'" v-model="item.value" />
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
            <input v-if="mode.convert == 'UPDATE'" v-model="item.value" />
            <div style="padding-left: 10px" v-else>{{ item.value }}</div>
          </template>
        </v-data-table>
        <div class="mgT12" style="display: flex; justify-content: flex-end">
          <button class="pipelineUpdateButton">목록으로</button>
          <button class="pipelineUpdateButton mgL12">저장</button>
        </div>
      </div>
    </div>
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
      dataCollect: [{ key: "데이터 수집기", value: "HTTP Listener" }],
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
      dataConvert: [
        {
          key: "DataSet",
          value: "ParkingLot1 : Location",
        },
      ],
    };
  },
  methods: {
    editMode(item) {
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
