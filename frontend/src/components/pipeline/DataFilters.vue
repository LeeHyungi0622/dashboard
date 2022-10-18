<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정제</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      :disabled="!isCompleted"
      >
        {{ $store.state.filterTableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    
    <div v-for="(item, key) in filterData.nifiComponents" :key="key">
      <custom-table :contents="item.requiredProps" :table-update-flag="$store.state.filterTableUpdateFlag"/>
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
        :disabled="!isCompleted"
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
              if(prop.inputValue == null || prop.inputValue == ""){
                return false;
              }
            }
          }
        }
        return true;
      }
      return false;
    },
    isVaild(){
      if(this.filterData){
        for(let nifi of this.filterData.nifiComponents){
          if(nifi.requiredProps){
            for(let prop of nifi.requiredProps){
              if(prop.name == "root_key"){
                if(!prop.inputValue.includes("\"") || prop.inputValue.includes(" ") || prop.inputValue.includes("\"\"")){
                  return false;
                }
                else{
                  if((prop.inputValue.split("\"").length - 1)%2 != 0){
                    return false;
                  }
                  else{
                    for(let e = 0; e < prop.inputValue.split("\"").length; e++){
                      if(e == 0 || e == prop.inputValue.split("\"").length-1){
                        if(prop.inputValue.split("\"")[e] != ""){
                          return false;
                        }
                      }
                      else if(e % 2 == 0){
                        if(prop.inputValue.split("\"")[e] != "."){
                          return false;
                        }
                      }
                    }
                  }
                  return true;
                }
              }
            }
          }
        }
        return false;
      }
      return false;
    }
  },
  methods:{
    changeUpdateFlag(){
      this.$store.state.filterTableUpdateFlag = !this.$store.state.filterTableUpdateFlag;
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
      if(this.isVaild){
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
            " 입력 값에 오류가 있습니다. " +
            "<br/>구분자 혹은 입력 값을 확인해 주십시오.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    },
    beforeRoute(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline.filter = this.filterData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'collector';
          this.$store.state.overlay = false;
        })
        .catch((err) => {
          console.error(err);
        });
    },
    saveDraft(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline.filter = this.filterData;
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
