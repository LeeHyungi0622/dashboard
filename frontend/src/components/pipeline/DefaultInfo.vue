<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 기본정보</div>
      <button
        v-if="$store.state.tableShowMode == `UPDATE`"
        class="pipelineUpdateButton"
        @click="changeUpdateFlag"
        :disabled="!this.contents[0].inputValue || !this.contents[1].inputValue"
      >
        {{ $store.state.infoTableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents" :table-update-flag="$store.state.infoTableUpdateFlag"/>
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button 
        class="pipelineButton mgL12" 
        @click="saveDraft()" 
        :disabled="!this.contents[0].inputValue"
      >
        임시 저장
      </button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
        :disabled="!this.contents[0].inputValue || !this.contents[1].inputValue"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script>
import CustomTable from "../../components/pipeline/CustomTable.vue";
import pipelineRegisterService from "../../js/api/pipelineRegister";
import tempPipelineListService from "../../js/api/tempPipelineList";

import EventBus from "@/eventBus/EventBus.js";
export default {
  props: {
    itemId: String
  }, 
  components: {
    CustomTable,
  },
  mounted() {
    tempPipelineListService
      .getTempPipelineList()
      .then((res) => {
        this.$store.state.tempPipelineList = res;
      })
      .catch((error) => error);
    if (this.itemId) { // 임시저장 call
      if(this.itemId != "new"){
        pipelineRegisterService
        .getPipelineDraft(this.itemId)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          if(this.$store.state.tableShowMode == `UPDATE`){
            this.contents[0].inputValue = this.$store.state.completedPipeline.name;
            this.contents[1].inputValue = this.$store.state.completedPipeline.detail;
          }
          else{
            if(this.$store.state.registerPipeline.name){
              this.contents[0].inputValue = this.$store.state.registerPipeline.name;
            }
            if(this.$store.state.registerPipeline.detail){
              this.contents[1].inputValue = this.$store.state.registerPipeline.detail;
            }
          }
        })
        .catch((err) =>
        console.error("임시저장 Pipeline 조회에 실패했습니다.", err)
        );
      }
    }
    else if(this.$store.state.registerPipeline.id){ // 최초 등록시 collector에서 이전버튼 에러 해결
      pipelineRegisterService
      .getPipelineDraft(this.$store.state.registerPipeline.id)
      .then((res) => {
        this.$store.state.registerPipeline = res;
        if(this.$store.state.tableShowMode == `UPDATE`){
        this.contents[0].inputValue = this.$store.state.completedPipeline.name;
        this.contents[1].inputValue = this.$store.state.completedPipeline.detail;
      }
      else{
        if(this.$store.state.registerPipeline.name){
          this.contents[0].inputValue = this.$store.state.registerPipeline.name;
        }
        if(this.$store.state.registerPipeline.detail){
          this.contents[1].inputValue = this.$store.state.registerPipeline.detail;
        }
      }
      })
      .catch((err) =>
      console.error("임시저장 Pipeline 조회에 실패했습니다.", err)
      );
    }
    else {
      pipelineRegisterService
      .getPipelineVo()
      .then((res) => {
        this.$store.state.registerPipeline = res;
      })
      .catch((err) => {
        console.error("PipelinVo 조회에 실패했습니다.", err);
      });
    }
    this.getInfo();
  },
 
  data() {
    return {
      title: "데이터 파이프라인 기본정보",
      contents:[
        {
          name: "파이프라인 이름",
          inputValue: "",
        },
        {
          name: "파이프라인 정의",
          inputValue: "",
        }
      ]
    };
  },
  methods: {
    checkLength(){
      if(this.contents[0].inputValue.length > 0 && this.contents[0].inputValue.length <= 20){
        if(this.contents[1].inputValue.length > 0){
          if(this.contents[1].inputValue.length <= 40){
            return true;
          }
          else return false;
        }
        else{
          return true;
        }
      } else{
        return false;
      }
    },
    checkSpaceInput(contents){
      for(let e of contents){
        if(e.inputValue != ""){
          if(e.inputValue.replace(/^\s+|\s+$/g, '')==""){
            return false;
          }
        }
      }
      return true;
    },
    checkTmpPipelineName(){
      //현재 등록 중인 파이프라인과 이름이 동일할 경우 그냥 Pass
      if(this.$store.state.registerPipeline.name == this.contents[0].inputValue){
        return true;
      }
      for(let tmpPl of this.$store.state.tempPipelineList){
        if(this.contents[0].inputValue == tmpPl.name){
          return false;
        }
      }
      return true;
    },
    checkComPipelineName(){
      //현재 등록 중인 파이프라인과 이름이 동일할 경우 그냥 Pass
      if(this.$store.state.completedPipeline.name == this.contents[0].inputValue){
        return true;
      }
      for(let comPl of this.$store.state.pipelineList){
        if(this.contents[0].inputValue == comPl.name){
          return false;
        }
      }
      return true;
    },
    getInfo(){
      if(this.$store.state.tableShowMode == `UPDATE`){
        this.contents[0].inputValue = this.$store.state.completedPipeline.name;
        this.contents[1].inputValue = this.$store.state.completedPipeline.detail;
      }
      else{
        if(this.$store.state.registerPipeline.name){
          this.contents[0].inputValue = this.$store.state.registerPipeline.name;
        }
        if(this.$store.state.registerPipeline.detail){
          this.contents[1].inputValue = this.$store.state.registerPipeline.detail;
        }
      }
    },
    nextRoute() {
      this.$store.state.overlay = true;
      if(this.checkTmpPipelineName()){
        if(this.checkSpaceInput(this.contents)){
          if(this.checkLength()){
            this.$store.state.registerPipeline.name = this.contents[0].inputValue;
            this.$store.state.registerPipeline.creator = this.$store.state.userInfo.userId;
            this.$store.state.registerPipeline.detail = this.contents[1].inputValue;
            pipelineRegisterService
              .craetePipelineDraft(this.$store.state.registerPipeline)
              .then((res) => {
                if(res.status != 400){
                  this.$store.state.registerPipeline = res.data;
                  this.$store.state.showRegisterMode = 'collector';
                  this.$store.state.overlay = false;
                }
                else{
                  this.$store.state.overlay = false;
                  this.showInvaildPipelineName();
                }
              })
              .catch((err) => {
                console.error(err);
              });
          }
          else{
            this.$store.state.overlay = false;
            this.showInputLengthPipeline();
          }
        }
        else{
          this.$store.state.overlay = false;
          this.showInputErrorPipeline();
        }
      }else{
        this.$store.state.overlay = false;
          this.showInvaildPipelineName();
      }
    },
    changeUpdateFlag(){
      if(this.checkComPipelineName()){
        if(this.checkSpaceInput(this.contents)){
          if(this.checkLength()){
          this.$store.state.infoTableUpdateFlag = !this.$store.state.infoTableUpdateFlag;
          this.$store.state.completedPipeline.name = this.contents[0].inputValue;
          this.$store.state.completedPipeline.detail = this.contents[1].inputValue;
          }
          else{
            this.showInputLengthPipeline();
          }
        }
        else{
          this.showInputErrorPipeline();
        }
      }
      else{
        this.showInvaildComPipelineName();
      }
    },
    saveDraft(){
      this.$store.state.overlay = true;
      if(this.checkTmpPipelineName()){
        if(this.checkComPipelineName()){
          if(this.checkSpaceInput(this.contents)){
            if(this.checkLength()){
              this.$store.state.registerPipeline.name = this.contents[0].inputValue;
              this.$store.state.registerPipeline.creator = this.$store.state.userInfo.userId;
              this.$store.state.registerPipeline.detail = this.contents[1].inputValue;
              pipelineRegisterService
                .craetePipelineDraft(this.$store.state.registerPipeline)
                .then((res) => {
                  this.$store.state.registerPipeline = res.data;
                  this.$store.state.overlay = false;
                  this.showDraftCompleted();
                })
                .catch((err) => {
                  console.error(err);
                });
            }
            else{
              this.$store.state.overlay = false;
              this.showInputLengthPipeline();
            }
          }
          else{
            this.$store.state.overlay = false;
            this.showInputErrorPipeline();
          }
        }
        else{
          this.$store.state.overlay = false;
          this.showInvaildComPipelineName();
        }
      }
      else{
        this.$store.state.overlay = false;
          this.showInvaildPipelineName();
      }
    },
    showInvaildPipelineName(){
      let alertPayload = {
            title: "중복 값 오류",
            text:
              " 임시 저장 파이프라인 목록 중  " +
              "<br/>같은 이름의 파이프라인이 존재합니다.",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    },
    showInvaildComPipelineName(){
      let alertPayload = {
            title: "중복 값 오류",
            text:
              " 등록 완료 파이프라인 목록 중  " +
              "<br/>같은 이름의 파이프라인이 존재합니다.",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    },
    showInputErrorPipeline(){
      let alertPayload = {
            title: "입력 값 공백 오류",
            text:
              "입력 값의 앞/뒤에 공백 설정된 값이 존재합니다." +
              "<br/> 또는 공백 만으로 이름 또는 정의를 설정할 수 없습니다.",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    },
    showInputLengthPipeline(){
      let alertPayload = {
            title: "입력 값 길이 오류",
            text:
              "파이프라인 이름은 최대 20자,"+
              "<br/>파이프라인 정의는 최대 40자까지 등록 가능합니다.",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    },
    showDraftCompleted(){
      let alertPayload = {
            title: "임시 저장",
            text:
              "임시저장 성공",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    }
  },
};
</script>
