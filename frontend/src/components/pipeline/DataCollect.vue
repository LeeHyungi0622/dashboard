<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 수집</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      >
        {{ $store.state.collectorTableUpdateFlag ? "수정완료" : "수정" }}
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
              @change="callCollectorProps($event)"
              :disabled="!$store.state.collectorTableUpdateFlag"
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
          <div v-if="getPipeline.collector !== null">
            <select
              style="padding: 0px 20px 0px 20px"
              v-model="selectedSettingValue"
              :disabled="!$store.state.collectorTableUpdateFlag"
            >
              <option
                v-for="(item, key) in getPipeline.collector.nifiComponents"
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
    <custom-table 
    :contents="selectedSettingValue.requiredProps" 
    :table-update-flag="$store.state.collectorTableUpdateFlag"/>
    <div class="pipelineUpdateSubTitle fsb14">선택 설정 값</div>
    <custom-table 
    :contents="selectedSettingValue.optionalProps" 
    :table-update-flag="$store.state.collectorTableUpdateFlag"/>
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton" @click="beforeRoute()">이전</button>
      <button class="pipelineButton mgL12"  @click="saveDraft()">임시 저장</button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
        :disabled="!isCompleted"
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
    if(this.$store.state.tableShowMode == `UPDATE`){
      this.getPipeline = this.$store.state.completedPipeline;
      this.selectedCollectValue = this.getPipeline.collector.name;
      this.selectedSettingValue = this.getPipeline.collector.nifiComponents[0];
    }
    else{
      this.getPipeline = this.$store.state.registerPipeline;
      if (this.getPipeline.collector != null) {
        this.selectedCollectValue = this.getPipeline.collector.name;
        this.selectedSettingValue = this.getPipeline.collector.nifiComponents[0];
      }
      else{
        this.selectedCollectValue = {};
        this.selectedSettingValue = {};
      }
    }
  },
  computed:{
    isCompleted(){
      console.log(this.getPipeline.collector);
      if(this.getPipeline.collector){
        for(var nifi of this.getPipeline.collector.nifiComponents){
          if(nifi.requiredProps){
            for(var prop of nifi.requiredProps){
              if(prop.inputValue == null || prop.inputValue == ""){
                return false;
              }
            }
          }
        }
        return true;
      }
      return false;
    }
  },
  data() {
    return {
      collectorContents: {
        key: "데이터 수집기",
        value: {
          datas: [],
        },
      },
      getPipeline: {},
      selectedCollectValue: null,
      selectedSettingValue: {},
    };
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
    callCollectorProps(event){
      if(this.$store.state.tableShowMode == `UPDATE`){
        this.completedContents(event.target.value);
      }
      else{
        this.registerContents(event.target.value);
      }
    },
    completedContents(val){
      collectorService
          .getPipelineComplete({
            adaptorName: val,
            pipelineid: this.getPipeline.id,
            page: 'collector',
          })
          .then((res) => {
            this.getPipeline = res;
          })
          .catch((err) => {
            console.error(err);
          });
    },
    registerContents(val){
      collectorService
          .getPipelineDraft({
            adaptorName: val,
            pipelineid: this.getPipeline.id,
            page: "collector",
          })
          .then((res) => {
            this.getPipeline = res;
          })
          .catch((error) => {
            console.error(error);
          });
    },
    changeUpdateFlag(){
      this.$store.state.collectorTableUpdateFlag = !this.$store.state.collectorTableUpdateFlag;
    },
    nextRoute(){
      this.$store.state.registerPipeline= this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'filter';
        })
        .catch((err) => {
          console.error(err);
        });
    },
    beforeRoute(){
      this.$store.state.registerPipeline = this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'info';
        })
        .catch((err) => {
          console.error(err);
        });
    },
    saveDraft(){
      this.$store.state.registerPipeline = this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
        })
        .catch((err) => {
          console.error(err);
        });
    }
  },
};
</script>
