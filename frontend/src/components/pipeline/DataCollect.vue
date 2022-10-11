<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 수집</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      >
        {{ $store.state.tableUpdateFlag == "UPDATE" ? "수정완료" : "수정" }}
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
                {{ item }}
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
              <option
                v-for="(item, key) in collectorData.nifiComponents"
                :key="key"
                :value="item"
              >
                {{ item.name }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="pipelineUpdateSubTitle fsb14">필수 설정 값</div>
    <custom-table :contents="selectedSettingValue.requiredProps" />
    <div class="pipelineUpdateSubTitle fsb14">선택 설정 값</div>
    <custom-table :contents="selectedSettingValue.optionalProps" />
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton">이전</button>
      <button class="pipelineButton mgL12">임시 저장</button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
import collectorService from "../../js/api/collector";
export default {
  components: {
    CustomTable,
  },
  created() {
    this.getCollector();
  },
  
  watch: {
    selectedCollectValue() {
      if (this.$store.state.tableShowMode == "REGISTER") {
        collectorService
          .getPipelineDraft({
            adaptorName: this.selectedCollectValue,
            pipelineid: this.$store.state.registerPipeline.id,
            page: "COLLECTOR",
          })
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.collectorData =
              this.$store.state.registerPipeline.collector;
          })
          .catch((error) => {
            console.error(error);
          });
      } else {
        collectorService
          .getPipelineComplete({
            adaptorName: this.selectedCollectValue,
            pipelineid: this.$store.state.completedPipeline.id,
            page: 'COLLECTOR',
          })
          .then((res) => {
            console.log(res);
            this.$store.state.completedPipeline= res;
            this.collectorData =
              this.$store.state.completedPipeline.collector;
          })
          .catch((err) => {
            console.error(err);
          });
      }
    },
  },
  data() {
    return {
      collectorContents: {
        key: "데이터 수집기",
        value: {
          datas: [],
        },
      },
      selectedCollectValue: {},
      selectedSettingValue: {},
      collectorData: {},
    };
  },
  props: {
    contents: Array
  },
  methods: {
    getCollector() {
      collectorService
        .getCollectorList()
        .then((res) => {
          this.collectorContents.value.datas = res;
        })
        .catch((err) => {
          console.log("collector 목록의 조회에 실패했습니다.", err);
        });
    },
    changeUpdateFlag(){
      this.$store.state.tableUpdateFlag = !this.$store.state.tableUpdateFlag;
    },
    nextRoute(){
      this.$store.state.registerPipeline.collector = this.collectorData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'filter';
        })
        .catch((err) => {
          console.error(err);
        });
    }
  },
};
</script>
