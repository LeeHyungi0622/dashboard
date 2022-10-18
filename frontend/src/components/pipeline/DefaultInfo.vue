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
import EventBus from "@/eventBus/EventBus.js";
export default {
  props: {
    itemId: String
  }, 
  components: {
    CustomTable,
  },
  mounted() {
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
      this.$store.state.registerPipeline.name = this.contents[0].inputValue;
      this.$store.state.registerPipeline.creator = this.$store.state.userInfo.userId;
      this.$store.state.registerPipeline.detail = this.contents[1].inputValue;
      pipelineRegisterService
        .craetePipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res.status);
          if(res.status != 400){
            this.$store.state.registerPipeline = res.data;
            this.$store.state.showRegisterMode = 'collector';
            this.$store.state.overlay = false;
          }
          else{
            this.showInvaildPipelineName();
          }
        })
        .catch((err) => {
          console.error(err);
        });
    },
    changeUpdateFlag(){
      this.$store.state.infoTableUpdateFlag = !this.$store.state.infoTableUpdateFlag;
      this.$store.state.completedPipeline.name = this.contents[0].inputValue;
      this.$store.state.completedPipeline.detail = this.contents[1].inputValue;
    },
    saveDraft(){
      this.$store.state.overlay = true;
      this.$store.state.registerPipeline.name = this.contents[0].inputValue;
      this.$store.state.registerPipeline.creator = this.$store.state.userInfo.userId;
      this.$store.state.registerPipeline.detail = this.contents[1].inputValue;
      pipelineRegisterService
        .craetePipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          this.$store.state.registerPipeline = res;
          this.$store.state.overlay = false;
          this.showDraftCompleted();
        })
        .catch((err) => {
          console.error(err);
        });
    },
    showInvaildPipelineName(){
      let alertPayload = {
            title: "입력 값 오류",
            text:
              " 임시저장 파이프라인 목록 중  " +
              "<br/>같은 이름의 파이프라인이 존재합니다.",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
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
