<template>
  <div class="pipelineUpdateContentBox">
    <div class="flex justify-between">
      <div class="pipelineUpdateMainTitle text-base font-bold">데이터 수집</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      :disabled="!isCompleted[0]"
      >
        {{ $store.state.collectorTableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="customTableMainArea">
      <div class="customTable">
        <div class="header text-xs font-bold">
          <p>데이터 수집기</p>
        </div>
        <div class="value">
          <div>
            <select
              class="px-5"
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
        <div class="header text-xs font-bold">
          <p>수집 설정 목록</p>
        </div>
        <div class="value">
          <div v-if="getPipeline.collector !== null">
            <select
              class="px-5"
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
    <div class="pipelineUpdateMainTitle text-base font-bold" style="padding: 20px 20px 0px 0px" v-if="selectedCollectValue !='REST Server'">수집 주기 설정</div>
    <div class="customTableMainArea" v-if="selectedCollectValue !='REST Server'">
      <div class="customTable">
        <div class="header text-xs font-bold">
          <p>모드 선택</p>
        </div>
        <div class="value">
          <div>
            <select
              class="px-5"
              v-model="schedulingMode"
              :disabled="!$store.state.collectorTableUpdateFlag"
            >
            <option value="TIMER_DRIVEN">Timer driven</option>
            <option value="CRON_DRIVEN">CRON driven</option>
            </select>
          </div>
        </div>
      </div>
      <div class="customTable"  v-if="schedulingMode">
        <div class="header text-xs font-bold">
          <p>상세 설정</p>
        </div>
        <div class="value">
          <div>
            <input v-if="$store.state.collectorTableUpdateFlag" type="text" v-model="schedulingDetail" maxlength="300"/>
            <div class="pl-5" v-else>{{ schedulingDetail }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="pipelineUpdateSubTitle text-sm font-bold">필수 설정 값</div>
    <custom-table 
    :contents="selectedSettingValue.requiredProps" 
    :table-update-flag="$store.state.collectorTableUpdateFlag"/>
    
    <div class="pipelineUpdateSubTitle text-sm font-bold">선택 설정 값</div>
    <custom-table 
    :contents="selectedSettingValue.optionalProps" 
    :table-update-flag="$store.state.collectorTableUpdateFlag"/>
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mt-3 flex justify-end"
    >
      <button class="pipelineButton" @click="beforeRoute()">이전</button>
      <button class="pipelineButton ml-3"  @click="saveDraft()">임시 저장</button>
      <button
        class="pipelineButton ml-3"
        @click="nextRoute()"
        :disabled="!isCompleted[0]"
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
import CronVaildator from 'cron-expression-validator';
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
      for(let nifi of this.getPipeline.collector.nifiComponents){
        if(nifi.requiredProps){
          for(let prop of nifi.requiredProps){
            if(prop.name == "Scheduling"){
                this.schedulingMode = prop.detail;
                this.schedulingDetail = prop.inputValue;
            }
          }
        }
      }
    }
    else{
      this.getPipeline = this.$store.state.registerPipeline;
      if (this.getPipeline.collector != null) {
        this.selectedCollectValue = this.getPipeline.collector.name;
        this.selectedSettingValue = this.getPipeline.collector.nifiComponents[0];
        for(let nifi of this.getPipeline.collector.nifiComponents){
        if(nifi.requiredProps){
          for(let prop of nifi.requiredProps){
            if(prop.name == "Scheduling"){
                this.schedulingMode = prop.detail;
                this.schedulingDetail = prop.inputValue;
            }
          }
        }
      }
      }
      else{
        this.selectedCollectValue = {};
        this.selectedSettingValue = {};
      }
    }
  },
  watch:{
    schedulingDetail(){
        if(this.getPipeline.collector!= null){
          for(var nifi of this.getPipeline.collector.nifiComponents){
            if(nifi.requiredProps){
              for(var prop of nifi.requiredProps){
                if(prop.name == "Scheduling"){                  
                  prop.inputValue = this.schedulingDetail;
                }
              }
            }
          }
      }
    },
    schedulingMode(){
        if(this.getPipeline.collector!= null){
          for(var nifi of this.getPipeline.collector.nifiComponents){
            if(nifi.requiredProps){
              for(var prop of nifi.requiredProps){
                if(prop.name == "Scheduling"){                  
                  prop.detail = this.schedulingMode;
                }
              }
            }
          }
      }
    }
  },
  computed:{
    isCompleted(){
      if(this.getPipeline.collector!= null){
        for(let nifi of this.getPipeline.collector.nifiComponents){
          if(nifi.requiredProps){
            for(let prop of nifi.requiredProps){
              if(prop.inputValue == null || prop.inputValue == "" || this.$removeBlank(prop.inputValue)==""){
                return [false, prop, nifi.name];
              }
            }
          }
          if(nifi.optionalProps){
            for(let prop of nifi.optionalProps){
              if(prop.inputValue == ""){
                prop.inputValue = null;
              } else if(prop.inputValue != null && this.$removeBlank(prop.inputValue)==""){
                return [false, prop, nifi.name];
              }
            }
          }
        }
        return [true, null, null];
      }
      else{
        return [false, "collector not found", null];
      }
    },
    isSchedulingVaild(){
      if(this.selectedCollectValue !='REST Server'){
        if(this.schedulingMode == "TIMER_DRIVEN"){
          return this.$checkTimerPattern(this.schedulingDetail);
        }
        else if(this.schedulingMode == "CRON_DRIVEN"){
          return CronVaildator.isValidCronExpression(this.schedulingDetail);
        }
        else return false;
      }
      else {
        return true;
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
      schedulingMode: "",
      schedulingDetail: "",
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
            this.$store.state.overlay = false;
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
      if(this.isSchedulingVaild){
        if(this.isCompleted[0]){
          this.$store.state.collectorTableUpdateFlag = !this.$store.state.collectorTableUpdateFlag;
          this.$store.state.completedPipeline = this.getPipeline;
        } else if(this.isCompleted[1]=="collector not found"){
          let alertPayload = {
          title: "입력 값 오류",
          text:
            " 수집기 선택이 필요합니다. " +
            "<br/> 수집기 목록 중 하나를 선택해주십시오.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
        }
        else{
          let alertPayload = {
          title: "입력 값 오류",
          text:
            " 입력 값에 오류가 있습니다. " +
            "<br/> 수집 설정 목록 중 " + this.isCompleted[2] + " 의" +
            "<br/>" + this.isCompleted[1].name + " 항목을 확인해주세요.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    }else {
        let alertPayload = {
          title: "입력 값 오류",
          text:
            " 입력 값에 오류가 있습니다. " +
            "<br/>수집 주기 설정 입력 값을 확인해 주십시오." +
            "<br/>Timer Input Example : OOO sec" +
            "<br/>CRON Input Example : * * * * * ? *",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
    }
  },
    nextRoute(){
      this.$store.state.overlay = true;
      if(this.isSchedulingVaild){
        if(this.isCompleted[0]){
          this.$store.state.registerPipeline= this.getPipeline;
          collectorService
            .postPipelineDraft(this.getPipeline)
            .then((res) => {
              this.$store.state.registerPipeline = res;
              this.$store.state.showRegisterMode = 'filter';
              this.$store.state.overlay = false;
            })
            .catch((err) => {
              console.error(err);
            });
        }else if(this.isCompleted[1]=="collector not found"){
          let alertPayload = {
          title: "입력 값 오류",
          text:
            " 수집기 선택이 필요합니다. " +
            "<br/> 수집기 목록 중 하나를 선택해주십시오.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
        }
        else{
          let alertPayload = {
          title: "입력 값 오류",
          text:
            " 입력 값에 오류가 있습니다. " +
            "<br/> 수집 설정 목록 중 " + this.isCompleted[2] + " 의" +
            "<br/>" + this.isCompleted[1].name + " 항목을 확인해주세요.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
        }
      }
      else {
        let alertPayload = {
          title: "입력 값 오류",
          text:
            " 입력 값에 오류가 있습니다. " +
            "<br/>수집 주기 설정 입력 값을 확인해 주십시오." +
            "<br/>Timer Input Example : OOO sec" +
            "<br/>CRON Input Example : * * * * * ? *",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
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
    },
  },
};
</script>
