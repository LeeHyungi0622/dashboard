<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 수집</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      :disabled="!isCompleted"
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
import EventBus from "@/eventBus/EventBus.js";
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
      if(this.getPipeline.collector!= null){
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
      else{
        return false;
      }
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
      getPipeline: {
        collector: {
          nifiComponents: null,
        }
      },
      selectedCollectValue: null,
      selectedSettingValue: {},
    };
  },
  methods: {
    getCollector() {
      this.$store.state.overlay = true;
      collectorService
        .getCollectorList()
        .then((res) => {
          this.collectorContents.value.datas = res;
          this.$store.state.overlay = false;
        })
        .catch((err) => {
          console.log("collector 목록의 조회에 실패했습니다.", err);
        });
    },
    callCollectorProps(event){
      if(this.$store.state.tableShowMode == `UPDATE`){
        this.completedContents(event.target.value);
      } else{
        this.registerContents(event.target.value);
      }
    },
    completedContents(val){
      this.$store.state.overlay = true;
      collectorService
          .getPipelineComplete({
            adaptorName: val,
            id: this.getPipeline.id,
            page: 'collector',
          })
          .then((res) => {
            this.getPipeline = res;
            this.$store.state.overlay = false;
          })
          .catch((err) => {
            console.error(err);
          });
    },
    registerContents(val){
      this.$store.state.overlay = true;
      collectorService
          .getPipelineDraft({
            adaptorName: val,
            pipelineid: this.getPipeline.id,
            page: "collector",
          })
          .then((res) => {
            this.getPipeline = res;
            this.$store.state.overlay = false;
          })
          .catch((error) => {
            console.error(error);
          });
    },
    changeUpdateFlag(){
      this.$store.state.collectorTableUpdateFlag = !this.$store.state.collectorTableUpdateFlag;
      this.$store.state.completedPipeline = this.getPipeline;
    },
    nextRoute(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline= this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'filter';
          this.$store.state.overlay = false;
        })
        .catch((err) => {
          console.error(err);
        });
    },
    beforeRoute(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline = this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'info';
          this.$store.state.overlay = false;
        })
        .catch((err) => {
          console.error(err);
        });
    },
    saveDraft(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline = this.getPipeline;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.overlay = false;
          this.showDraftCompleted();
        })
        .catch((err) => {
          console.error(err);
        });
    },
    showDraftCompleted(){
      let alertPayload = {
            title: "임시저장",
            text:
              "임시저장 성공",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    }
  },
};
</script>
