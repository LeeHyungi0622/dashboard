<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 기본정보</div>
      <button
        v-if="$store.state.tableShowMode == `UPDATE`"
        class="pipelineUpdateButton"
        @click="changeUpdateFlag"
      >
        {{ $store.state.tableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="sendInfo" />
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton mgL12">임시 저장</button>
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
export default {
  components: {
    CustomTable,
  },
  // props: {
  //   contents: Array
  // },

  computed: {
    updatePipeline(){
      return this.$store.state.pipelineVo;
    }
  },
  data() {
    return {
      // contents: [
      //   {
      //     name: "파이프라인 이름",
      //     inputValue: this.updatePipeline.name,
      //   },
      //   {
      //     name: "파이프라인 정의",
      //     inputValue: this.updatePipeline.detail,
      //   },
      // ],
    };
  },
  methods: {
    nextRoute() {
      this.$store.state.pipelineVo.name = this.contents[0].inputValue;
      this.$store.state.pipelineVo.creator = this.$store.state.userInfo.userId;
      this.$store.state.pipelineVo.detail = this.contents[1].inputValue;
      pipelineRegisterService
        .craetePipelineDraft(this.$store.state.pipelineVo)
        .then((res) => {
          console.log(res);
          this.$store.state.pipelineVo = res;
          this.$router.push({
            name: "collector"
          });
        })
        .catch((err) => {
          console.error(err);
        });
    },
    changeUpdateFlag(){
      this.$store.state.tableUpdateFlag = !this.$store.state.tableUpdateFlag;
    },
    sendInfo(){
      return [
        {
          name: "파이프라인 이름",
          inputValue: this.updatePipeline.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.updatePipeline.detail,
        },
      ];
    },
  },
};
</script>
