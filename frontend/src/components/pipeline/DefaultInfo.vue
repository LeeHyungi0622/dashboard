<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex" class="fsb14">
      <div class="fsb16" style="color: #2b4f8c">데이터 파이프라인 기본정보</div>
      <button
        v-if="mode == `UPDATE`"
        class="pipelineUpdateButton"
        @click="convertMode('defaultInfo')"
      >
        {{ mode == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <custom-table :contents="contents" :mode="mode" />
    <div
      v-if="mode == `REGISTER`"
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

  props: {
    mode: String,
    convertMode: Function,
  },
  data() {
    return {
      contents: [
        {
          name: "파이프라인 이름",
          inputValue: this.$store.state.pipelineVo.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.$store.state.pipelineVo.detail,
        },
      ],
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
            name: "collector",
            params: {
              convertMode: this.convertMode,
              mode: "REGISTER",
            },
          });
        })
        .catch((err) => {
          console.error(err);
        });
    },
  },
};
</script>
