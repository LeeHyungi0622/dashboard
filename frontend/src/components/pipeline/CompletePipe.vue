<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 요약</div>
    </div>
    <div class="pipelineUpdateSubTitle fsb14">데이터 파이프라인 기본 정보
    </div>
    
    <custom-table :contents="infoContents"/>

    <div class="pipelineUpdateSubTitle fsb14">데이터 수집 정보
    </div>
    <custom-table :contents="collectorContents" />
    <div class="pipelineUpdateSubTitle fsb14">데이터 정제 정보
    </div>
    <custom-table :contents="filterContents" />
    <div class="pipelineUpdateSubTitle fsb14">데이터 변환 정보
    </div>
    <custom-table :contents="converterContents" />
    <div
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton mgL12" @click="beforeRoute()">이전</button>
      <button
        class="pipelineButton mgL12"
        @click="saveComplete()"
      >
        등록완료
      </button>
    </div>
  </div>
</template>

<script>
import CustomTable from "./CustomTable.vue";
import PipelineRegister from "../../js/api/pipelineRegister";
export default {
  components: {
    CustomTable
  },
  data: () => ({
    infoContents: [        
      {
          name: "파이프라인 이름",
          inputValue: "",
        },
        {
          name: "파이프라인 정의",
          inputValue: "",
        }
      ],
    collectorContents: [
      {
        name: "데이터 수집",
        inputValue: "",
      }
    ],
    filterContents: [
      {
          name: "Base64 Decoder",
          inputValue: "",
        },
        {
          name: "Message Root",
          inputValue: "",
        }
    ],
    converterContents: [
      {
          name: "DataSet",
          inputValue: "",
        },
        {
          name: "생성된 ID Key",
          inputValue: "",
        }
    ],
  }),
  created() {
    this.getDraftPl();
    this.$store.state.tableShowMode = 'UPDATE';
  },
  mounted() {
  },
  methods: {
    getDraftPl(){
      PipelineRegister.getPipelineDraft(this.$store.state.registerPipeline.id)
      .then((res) => {
        this.infoContents[0].inputValue = res.name;
        this.infoContents[1].inputValue = res.detail;
        this.collectorContents[0].inputValue = res.collector.name;
        this.filterContents[0].inputValue = this.$store.state.filterBase;
        this.filterContents[1].inputValue = this.$store.state.filterRootKey;
        this.converterContents[0].inputValue = this.$store.state.convertDataSet;
        this.converterContents[1].inputValue = this.$store.state.registerPipeId;

      })
      .catch((err) =>
      console.error("임시저장 Pipeline 조회에 실패했습니다.", err)
      );
    },
    beforeRoute(){
      this.$store.state.tableShowMode = 'REGISTER';
      this.$store.state.showRegisterMode = 'convertor';
    },
    saveComplete(){
      PipelineRegister
        .postPipelineCompleted(this.$store.state.registerPipeline.id, this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
        })
        .catch((err) => {
          console.error(err);
        });
        //TODO PopUP
      this.$router.push({
          name: "pipelineList"
        });
    }
  },
};
</script>

<style scoped></style>
