<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정제</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      :disabled="!isCompleted[0]"
      >
        {{ $store.state.filterTableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="pipelineUpdateSubTitle fsb14">Base64 Decoder</div>
    <div v-for="(item, key) in filterData.nifiComponents" :key="key">
      <custom-table v-if="item.name == 'Base64Decoder'" :contents="item.requiredProps" :table-update-flag="$store.state.filterTableUpdateFlag"/>
    </div>
    <div class="pipelineUpdateSubTitle fsb14">Root Key Finder</div>
    <div v-for="(item, key) in filterData.nifiComponents" :key="key">
      <custom-table v-if="item.name == 'RootKeyFinder'" :contents="item.requiredProps" :table-update-flag="$store.state.filterTableUpdateFlag"/>
    </div>
    
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
    <button class="pipelineButton" @click="beforeRoute()" >이전</button>
    <button class="pipelineButton mgL12" @click="saveDraft()" >임시 저장</button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
        :disabled="!isCompleted[0]"
      >
        다음
      </button>
    </div>
  </div>
</template>
<script>
import CustomTable from "./CustomTable.vue";
import collectorService from "../../js/api/collector";
import EventBus from "@/eventBus/EventBus.js";
export default {
  components: {
    CustomTable,
  },
  created() {
    this.getFilter();
  },
  data() {
    return {
      filterData: [],
      getPipeline: {},
    };
  },
  computed:{
    isCompleted(){
      if(this.filterData){
        for(var nifi of this.filterData.nifiComponents){
          if(nifi.requiredProps){
            for(var prop of nifi.requiredProps){
              if(prop.inputValue == null || prop.inputValue == ""|| prop.inputValue.replace(/^\s+|\s+$/g, '')==""){
                return [false, prop, nifi.name];
              }
            }
          }
        }
        return [true, null, null];
      }
      return [false, "collecter not found", null];
    },
    isVaild(){
      if(this.filterData){
        for(let nifi of this.filterData.nifiComponents){
          if(nifi.requiredProps){
            for(let prop of nifi.requiredProps){
              if(prop.name == "root_key"){
                if(prop.inputValue == 'origin') {
                  return [true, null];
                }
                if(!prop.inputValue.includes("\"") || prop.inputValue.replace(/^\s+|\s+$/g, '')=="" || prop.inputValue.includes("\"\"")){
                  return [false, prop];
                }
                else{
                  if((prop.inputValue.split("\"").length - 1)%2 != 0){
                    return [false, prop];
                  }
                  else{
                    for(let e = 0; e < prop.inputValue.split("\"").length; e++){
                      if(e == 0 || e == prop.inputValue.split("\"").length-1){
                        if(prop.inputValue.split("\"")[e] != ""){
                          return [false, prop];
                        }
                      }
                      else if(e % 2 == 0){
                        if(prop.inputValue.split("\"")[e] != "."){
                          return [false, prop];
                        }
                      }
                    }
                  }
                  return [true, null];
                }
              }
            }
          }
        }
        return [false,"filter not found", null];
      }
      return [false,"filter not found", null];
    }
  },
  methods:{
    changeUpdateFlag(){
      if(this.isVaild[0]){
        this.$store.state.filterTableUpdateFlag = !this.$store.state.filterTableUpdateFlag;
        }else{
          let alertPayload = {
          title: "입력 값 오류",
          text:
            this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
            "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오." +
            "<br/>원본 데이터를 그대로 사용할 경우 [origin]을 입력해주십시오." ,
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }

    },
    getFilter(){
      if (this.$store.state.tableShowMode == "REGISTER") {
        if(this.$store.state.registerPipeline.filter != null){
          this.filterData = this.$store.state.registerPipeline.filter;
        }
        else{
          collectorService
            .getPipelineDraft({
              pipelineid: this.$store.state.registerPipeline.id,
              adaptorName: "filter",
              page: "filter",
            })
            .then((res) => {
              this.$store.state.registerPipeline = res;
              this.filterData =
              this.$store.state.registerPipeline.filter;
            })
            .catch((error) => {
              console.error(error);
            });
        }
      } else if (this.$store.state.tableShowMode == "UPDATE"){
        this.filterData =
              this.$store.state.completedPipeline.filter;
      }
    },
    nextRoute(){
      this.$store.state.overlay = true;
      if(this.isVaild[0]){
        this.checkCompletedPage();
        
        this.$store.state.registerPipeline.filter = this.filterData;
        collectorService
          .postPipelineDraft(this.$store.state.registerPipeline)
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.$store.state.showRegisterMode = 'convertor';
            this.$store.state.overlay = false;
          })
          .catch((err) => {
            console.error(err);
          });
      }
      else{
        let alertPayload = {
          title: "입력 값 오류",
          text:
            this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
            "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오."+
            "<br/>원본 데이터를 그대로 사용할 경우 [origin]을 입력해주십시오." ,
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    },
    beforeRoute(){
      this.$store.state.overlay = true;
      if(this.isVaild[0]){
      this.$store.state.registerPipeline.filter = this.filterData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'collector';
          this.$store.state.overlay = false;
        })
        .catch((err) => {
          this.$store.state.overlay = false;
          console.error(err);
        });
      }else{
        this.$store.state.overlay = false;
        let alertPayload = {
          title: "입력 값 오류",
          text:
            this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
            "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오."+
            "<br/>원본 데이터를 그대로 사용할 경우 [origin]을 입력해주십시오." ,
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    },
    saveDraft(){
      this.$store.state.overlay = true;
      if(this.isVaild[0]){
      this.$store.state.registerPipeline.filter = this.filterData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.overlay = false;
          this.showDraftCompleted();
        })
        .catch((err) => {
          this.$store.state.overlay = false;
          console.error(err);
        });
      }else{
        let alertPayload = {
          title: "입력 값 오류",
          text:
            this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
            "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오."+
            "<br/>원본 데이터를 그대로 사용할 경우 [origin]을 입력해주십시오." ,
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    },
    checkCompletedPage(){
      for(var nifi of this.filterData.nifiComponents){
          if(nifi.requiredProps){
            for(var prop of nifi.requiredProps){
              if(prop.name == "isBase64"){
                this.$store.state.filterBase = prop.inputValue;
              }
              else if(prop.name == "root_key"){
                this.$store.state.filterRootKey = prop.inputValue;
              }
            }
          }
        }
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
  }
};
</script>
